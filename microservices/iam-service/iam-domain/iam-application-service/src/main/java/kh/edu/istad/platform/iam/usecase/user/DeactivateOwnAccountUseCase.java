package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.audit.*;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.*;
import kh.edu.istad.platform.iam.port.out.user.*;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeactivateOwnAccountUseCase {
    private final CurrentUserProvider currentUser;
    private final CurrentActorProvider actor;
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final ClientConsentRepository consents;
    private final UserSessions sessions;
    private final Clock clock;
    public record Status(String username, boolean hasPassword) {}
    public record Command(String confirmation, boolean acknowledged, String currentPassword) {
        @Override public String toString() { return "DeactivateOwnAccountCommand[REDACTED]"; }
    }
    @Transactional(readOnly = true)
    public Status get() {
        var uuid = currentUser.currentUserUuid();
        var user = users.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid));
        return new Status(user.getUsername(), user.getPasswordHash() != null && !user.getPasswordHash().isBlank());
    }
    @Transactional
    @Audited(action = AuditAction.ACCOUNT_DEACTIVATED, target = "#result")
    public UUID execute(Command command) {
        var uuid = currentUser.currentUserUuid();
        var user = users.findByUuidForUpdate(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid));
        if (!command.acknowledged()) invalid("Confirm that you understand account deactivation");
        if (!user.getUsername().equals(command.confirmation())) invalid("Username confirmation does not match");
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()
                && (command.currentPassword() == null || command.currentPassword().isBlank()
                || !passwords.matches(command.currentPassword(), user.getPasswordHash()))) invalid("Current password is incorrect");
        user.disable(actor.currentActor(), clock.instant());
        users.updateAccountState(user);
        consents.revokeAllForPrincipal(user.getUsername());
        sessions.terminateAll(uuid);
        return uuid;
    }
    private void invalid(String message) { throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message); }
}
