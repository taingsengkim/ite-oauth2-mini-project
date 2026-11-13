package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.ResetUserPasswordCommand;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class ResetUserPasswordUseCase {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    @Transactional
    public void execute(ResetUserPasswordCommand command) {
        passwordPolicy.validate(
                command.newPassword(),
                command.confirmPassword()
        );

        var user = repository.findByUuidForUpdate(command.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", command.userUuid()));

        user.changePasswordHash(
                passwordHasher.hash(command.newPassword()),
                actorProvider.currentActor(),
                clock.instant()
        );

        repository.updatePassword(user);
    }
}
