package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.ChangePasswordCommand;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import kh.edu.istad.platform.iam.audit.Audited;
import kh.edu.istad.platform.iam.audit.AuditAction;
import kh.edu.istad.platform.iam.port.out.user.UserSessions;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    private final UserSessions sessions;
    private final ClientConsentRepository consents;

    @Transactional
    @Audited(action = AuditAction.ACCOUNT_PASSWORD_CHANGED, target = "#p0.userUuid()")
    public void execute(ChangePasswordCommand command) {
        passwordPolicy.validate(
                command.newPassword(), command.confirmPassword()
        );

        var user = repository.findByUuidForUpdate(command.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userUuid()));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "This account signs in with a linked provider and has no local password");
        }
        if (command.currentPassword() == null || !passwordHasher.matches(command.currentPassword(), user.getPasswordHash())) {

            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Current password is incorrect");
        }

        if (passwordHasher.matches(command.newPassword(), user.getPasswordHash())) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "New password must differ from the current password");
        }

        user.changePasswordHash(
                passwordHasher.hash(command.newPassword()),
                actorProvider.currentActor(),
                clock.instant()
        );

        repository.updatePassword(user);
        consents.revokeAllForPrincipal(user.getUsername());
        sessions.terminateAll(user.getUuid());
    }
}
