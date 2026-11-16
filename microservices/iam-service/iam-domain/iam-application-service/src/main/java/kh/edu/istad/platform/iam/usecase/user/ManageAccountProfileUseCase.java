package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.audit.*;
import kh.edu.istad.platform.iam.application.exception.*;
import kh.edu.istad.platform.iam.dto.user.UpdateAccountProfileCommand;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.*;
import kh.edu.istad.platform.iam.port.out.user.*;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import kh.edu.istad.platform.iam.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageAccountProfileUseCase {
    private final UserRepository users;
    private final CurrentUserProvider currentUser;
    private final CurrentActorProvider actors;
    private final UserSessions sessions;
    private final ClientConsentRepository consents;
    private final Clock clock;
    public record Profile(UUID uuid, String username, String email, boolean emailVerified,
            String givenName, String familyName, String nickname, String phoneNumber, boolean phoneNumberVerified,
            String pictureUrl, String locale, Instant createdAt, Instant updatedAt) {}
    public record Saved(Profile profile, boolean signInRequired) {}

    @Transactional(readOnly = true)
    public Profile get() {
        UUID uuid = currentUser.currentUserUuid();
        return profile(users.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid)));
    }

    @Transactional
    @Audited(action = AuditAction.ACCOUNT_PROFILE_UPDATED, target = "#result.profile().uuid()",
        fields = {"username", "email", "givenName", "familyName", "nickname", "phoneNumber", "pictureUrl", "locale"})
    public Saved update(UpdateAccountProfileCommand command) {
        UUID uuid = currentUser.currentUserUuid();
        var user = users.findByUuidForUpdate(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid));
        if (!Objects.equals(user.getUpdatedAt(), command.expectedUpdatedAt()))
            throw new ResourceConflictException("Profile changed in another session. Reload before saving.");
        String username = command.username().strip();
        String email = command.email().strip().toLowerCase(java.util.Locale.ROOT);
        users.findByUsername(username).filter(other -> !other.getUuid().equals(uuid)).ifPresent(other -> conflict("Username is already registered"));
        users.findByEmail(email).filter(other -> !other.getUuid().equals(uuid)).ifPresent(other -> conflict("Email is already registered"));
        // Prevent an email from colliding with another account's email-shaped legacy username.
        users.findByUsername(email).filter(other -> !other.getUuid().equals(uuid)).ifPresent(other -> conflict("Email is already registered"));
        boolean identifiersChanged = !user.getUsername().equals(username) || !Objects.equals(user.getEmail(), email);
        String previousUsername = user.getUsername();
        String picture = command.pictureUrl().strip();
        if (!picture.isEmpty()) {
            try {
                var uri = URI.create(picture);
                if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null)
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException exception) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Profile picture must be an HTTPS URL without credentials or fragments");
            }
        }
        String actor = actors.currentActor(); var now = clock.instant();
        user.updateIdentifiers(username, email, actor, now);
        user.updateProfile(command.givenName().strip(), command.familyName().strip(), command.nickname().strip(), command.locale(), command.phoneNumber().strip(), actor, now);
        if (!Objects.equals(user.getPictureUrl(), picture.isEmpty() ? null : picture))
            user.manageProfilePicture(picture, actor, now);
        var saved = users.updateAccountProfile(user);
        if (identifiersChanged) {
            consents.revokeAllForPrincipal(previousUsername);
            sessions.terminateAll(uuid);
        }
        return new Saved(profile(saved), identifiersChanged);
    }
    private Profile profile(User user) {
        return new Profile(user.getUuid(), user.getUsername(), user.getEmail(), user.isEmailVerified(), user.getGivenName(), user.getFamilyName(),
                user.getNickname(), user.getPhoneNumber(), user.isPhoneNumberVerified(), user.getPictureUrl(), user.getLocale(), user.getCreatedAt(), user.getUpdatedAt());
    }
    private void conflict(String message) { throw new ResourceConflictException(message); }
}
