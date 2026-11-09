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
        } else {
            user.recordFailedLogin("system", now);
        }

        repository.updateLoginMetadata(user);
    }
}