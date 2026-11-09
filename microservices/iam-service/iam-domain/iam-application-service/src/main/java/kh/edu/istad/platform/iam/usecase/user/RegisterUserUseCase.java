package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.user.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.dto.user.RegisterUserCommand;
import kh.edu.istad.platform.iam.dto.user.UserResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.mapper.user.UserResultMapper;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider actorProvider;
    private final UserResultMapper mapper;
    private final Clock clock;

    private final RoleRepository roles;
    private final AssignRolesToUserUseCase assignRoles;
    private final PasswordPolicy passwordPolicy;

    @Transactional
    public UserResult execute(RegisterUserCommand command) {
        Objects.requireNonNull(command, "command");

        String username = required(command.username(), "Username");
        String email = required(command.email(), "Email").toLowerCase(Locale.ROOT);

        String password = command.password();
        passwordPolicy.validate(password, command.confirmPassword());

        if (password == null || password.isBlank()) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Password is required");
        }

        if (!password.equals(command.confirmPassword())) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Passwords do not match");
        }

        if (repository.findByUsername(username).isPresent()) {
            throw new ResourceConflictException(
                    "Username is already registered"
            );
        }

        if (repository.findByEmail(email).isPresent()) {
            throw new ResourceConflictException("Email is already registered");
        }

        var defaultRole = roles.findByName("USER")
                .orElseThrow(() -> new IllegalStateException(
                        "Default USER role has not been seeded"
                ));

        defaultRole.requireAssignable();

        String passwordHash = passwordHasher.hash(password);

        User user = User.createLocal(
                username,
                email,
                passwordHash,
                command.givenName(),
                command.familyName(),
                command.locale(),
                actorProvider.currentActor(),
                clock.instant()
        );

        User saved = repository.create(user);

        assignRoles.execute(new AssignRolesToUserCommand(
                saved.getUuid(),
                List.of(defaultRole.getUuid()),
                null,
                "Default role assigned during registration"
        ));

        return mapper.toResult(saved);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank() || !value.equals(value.strip())) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " is required without surrounding whitespace");
        }
        return value;
    }
}
