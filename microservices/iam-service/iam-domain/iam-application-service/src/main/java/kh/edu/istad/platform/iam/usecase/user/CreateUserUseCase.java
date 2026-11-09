package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.user.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.dto.user.CreateUserCommand;
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
public class CreateUserUseCase {

    private final UserRepository repository;
    private final RoleRepository roles;
    private final AssignRolesToUserUseCase assignRoles;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicy passwordPolicy;
    private final CurrentActorProvider actorProvider;
    private final UserResultMapper mapper;
    private final Clock clock;

    @Transactional
    public UserResult execute(CreateUserCommand command) {
        Objects.requireNonNull(command, "command");

        String username = required(command.username(), "Username");
        String email = required(command.email(), "Email").toLowerCase(Locale.ROOT);

        passwordPolicy.validate(command.password());

        if (repository.findByUsername(username).isPresent()) {
            throw new ResourceConflictException("Username is already registered");
        }

        if (repository.findByEmail(email).isPresent()) {
            throw new ResourceConflictException("Email is already registered");
        }

        var roleUuids = command.roleUuids();

        if (roleUuids == null || roleUuids.isEmpty()) {
            var defaultRole = roles.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("Default USER role has not been seeded"));

            roleUuids = List.of(defaultRole.getUuid());
        }

        var now = clock.instant();
        var actor = actorProvider.currentActor();

        User user = User.createLocal(
                username,
                email,
                passwordHasher.hash(command.password()),
                command.givenName(),
                command.familyName(),
                command.locale(),
                actor,
                now
        );

        user.updateProfile(
                null,
                null,
                null,
                null,
                command.phoneNumber(),
                actor,
                now
        );

        user.updatePersonalDetails(
                null,
                command.gender(),
                command.birthdate(),
                command.zoneinfo(),
                actor,
                now
        );

        if (command.address() != null) {
            user.updateAddress(command.address(), actor, now);
        }

        User saved = repository.create(user);

        assignRoles.execute(new AssignRolesToUserCommand(
                saved.getUuid(),
                roleUuids,
                null,
                "Roles assigned during user creation"
        ));

        return mapper.toResult(saved);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()
                || !value.equals(value.strip())) {

            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " is required without surrounding whitespace");
        }

        return value;
    }
}