package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class RecordLoginAttemptUseCase {

    private final UserRepository repository;
    private final Clock clock;
    private final kh.edu.istad.platform.iam.port.out.user.LoginLockoutRepository lockouts;
    private final kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository configuration;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(String usernameOrEmail, boolean success) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return;
        }

        var user = repository.findByLoginForUpdate(usernameOrEmail)
                .orElse(null);

        if (user == null) {
            return;
        }

        var now = clock.instant();

        if (success) {
            user.recordSuccessfulLogin("system", now);
            lockouts.clear(user.getUuid());
        } else {
            user.recordFailedLogin("system", now);
            var policy = configuration.current().policy();
            lockouts.recordFailure(user.getUuid(), policy.maxFailedLoginAttempts(), policy.lockoutDurationSeconds(), now);
        }

        repository.updateLoginMetadata(user);
    }
}