package kh.edu.istad.platform.iam.usecase.identity;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.dto.user.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.dto.user.AuthUserResult;
import kh.edu.istad.platform.iam.usecase.user.UserAuthorityResolver;
import kh.edu.istad.platform.iam.application.exception.ProfileCompletionRequiredException;
import kh.edu.istad.platform.iam.dto.identity.FederatedProfileDetails;
import kh.edu.istad.platform.iam.mapper.user.UserApplicationMapper;
import kh.edu.istad.platform.iam.model.Identity;
import kh.edu.istad.platform.iam.model.IdentityProvider;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.port.out.identity.IdentityRepository;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.usecase.user.AssignRolesToUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindOrProvisionFederatedUserUseCase {

    private final UserRepository users;
    private final IdentityRepository identities;
    private final RoleRepository roles;
    private final AssignRolesToUserUseCase assignRoles;
    private final UserApplicationMapper mapper;
    private final UserAuthorityResolver authorityResolver;
    private final Clock clock;
    private final FederatedUserProfileSync profileSync;

    @Transactional
    public AuthUserResult execute(FederatedIdentityCommand command) {
        Objects.requireNonNull(command, "command");
        return resolve(command, new FederatedProfileDetails(
                command.email(), command.givenName(), command.familyName(), command.locale()));
    }

    @Transactional
    public AuthUserResult completeProfile(FederatedIdentityCommand command, FederatedProfileDetails profile) {
        return resolve(Objects.requireNonNull(command, "command"), Objects.requireNonNull(profile, "profile"));
    }

    private AuthUserResult resolve(FederatedIdentityCommand command, FederatedProfileDetails profile) {
        Objects.requireNonNull(command.provider(), "provider");

        if (command.providerSubject() == null
                || command.providerSubject().isBlank()) {
            throw new AuthenticationFailedException();
        }

        var existing = identities.findByProviderAndSubject(
                command.provider(),
                command.providerSubject()
        );

        if (existing.isPresent()) {
            return signIn(existing.get(), command);
        }

        // Only provider-verified email may select an existing local account.
        // Never use email entered on the profile-completion form for automatic linking.
        if (canLinkByEmail(command)) {
            var matchingUser = users.findByEmail(normalizedEmail(command.email()));
            if (matchingUser.isPresent()) {
                return linkVerifiedEmail(matchingUser.get().getUuid(), command);
            }
        }

        return provision(command, profile);
    }

    private static boolean canLinkByEmail(FederatedIdentityCommand command) {
        return (command.provider() == IdentityProvider.GOOGLE || command.provider() == IdentityProvider.GITHUB)
                && command.emailVerified() && command.email() != null && !command.email().isBlank();
    }

    private static String normalizedEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private AuthUserResult linkVerifiedEmail(UUID userUuid, FederatedIdentityCommand command) {
        var user = users.findByUuidForUpdate(userUuid).orElseThrow(AuthenticationFailedException::new);
        requireActiveAccount(user);
        if (!normalizedEmail(command.email()).equals(normalizedEmail(user.getEmail()))) {
            throw new AuthenticationFailedException();
        }

        // Recheck after locking the owner to serialize concurrent logins/link requests.
        var linked = identities.findByProviderAndSubject(command.provider(), command.providerSubject());
        if (linked.isPresent()) {
            if (!linked.get().getUserUuid().equals(userUuid)) {
                throw new ResourceConflictException("This external account is already linked");
            }
            return signIn(user, linked.get(), command);
        }
        if (identities.findByUserUuidAndProvider(userUuid, command.provider()).isPresent()) {
            throw new ResourceConflictException("A different account for this provider is already linked; sign in and manage Linked accounts");
        }

        var identity = identities.create(newIdentity(userUuid, command, clock.instant()));
        return signIn(user, identity, command);
    }

    private AuthUserResult signIn(Identity identity, FederatedIdentityCommand command) {
        var user = users.findByUuidForUpdate(identity.getUserUuid()).orElseThrow(AuthenticationFailedException::new);

        var currentIdentity = identities.findByUuid(identity.getUuid())
                .filter(value -> value.getUserUuid().equals(user.getUuid()))
                .orElseThrow(AuthenticationFailedException::new);

        return signIn(user, currentIdentity, command);
    }

    private static void requireActiveAccount(User user) {
        if (!user.isEnabled()
                || !user.isAccountNonLocked()
                || !user.isAccountNonExpired()) {
            throw new AuthenticationFailedException();
        }
    }

    private AuthUserResult signIn(User user, Identity currentIdentity, FederatedIdentityCommand command) {
        requireActiveAccount(user);
        var now = clock.instant();

        profileSync.apply(user, command, currentIdentity.getPictureUrl(), "system", now);
        users.updateProfile(user);
        currentIdentity.refreshProfile(command.email(), command.emailVerified(), command.displayName(),
                command.username(), command.pictureUrl(), command.attributes(), "system", now);
        currentIdentity.recordSuccessfulLogin("system", now);
        identities.update(currentIdentity);

        user.recordSuccessfulLogin("system", now);
        users.updateLoginMetadata(user);

        return mapper.toAuthResult(user, authorityResolver.resolve(user.getUuid()));
    }

    private AuthUserResult provision(FederatedIdentityCommand command, FederatedProfileDetails profile) {
        if (profile.email() == null || profile.email().isBlank()
                || profile.givenName() == null || profile.givenName().isBlank()
                || profile.familyName() == null || profile.familyName().isBlank()) {
            throw new ProfileCompletionRequiredException();
        }

        String email = normalizedEmail(profile.email());
        boolean emailVerified = command.emailVerified() && command.email() != null
                && email.equalsIgnoreCase(command.email().strip());

        if (users.findByEmail(email).isPresent()) {
            throw new ResourceConflictException("An account already uses this email; sign in and link the provider");
        }

        var defaultRole = roles.findByName("USER").orElseThrow(() -> new IllegalStateException("Default USER role has not been seeded"));

        defaultRole.requireAssignable();

        var now = clock.instant();

        String username = "user_" + UUID.randomUUID().toString().replace("-", "");

        var user = User.createFederated(
                username,
                email,
                emailVerified,
                profile.givenName().strip(),
                profile.familyName().strip(),
                FederatedUserProfileSync.normalizeLocale(profile.locale()),
                "system",
                now
        );

        profileSync.apply(user, command, null, "system", now);
        var saved = users.create(user);

        var identity = newIdentity(saved.getUuid(), command, now);

        identity.recordSuccessfulLogin("system", now);
        identities.create(identity);

        assignRoles.execute(new AssignRolesToUserCommand(
                saved.getUuid(),
                List.of(defaultRole.getUuid()),
                null,
                "Default role assigned during federated registration"
        ));

        saved.recordSuccessfulLogin("system", clock.instant());
        users.updateLoginMetadata(saved);

        return mapper.toAuthResult(saved, authorityResolver.resolve(saved.getUuid()));
    }

    private static Identity newIdentity(UUID userUuid, FederatedIdentityCommand command, java.time.Instant now) {
        return Identity.create(userUuid, command.provider(), command.providerSubject(), command.email(),
                command.emailVerified(), command.displayName(), command.username(), command.pictureUrl(),
                command.attributes(), "system", now);
    }
}
