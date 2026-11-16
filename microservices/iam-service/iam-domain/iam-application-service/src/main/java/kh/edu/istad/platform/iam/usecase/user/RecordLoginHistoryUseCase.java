package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository.Event;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository.Outcome;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordLoginHistoryUseCase {
    private final LoginHistoryRepository history;
    private final UserRepository users;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(UUID authenticatedUser, String login, String provider, Outcome outcome,
                        String reason, String ip, String userAgent) {
        // Only password attempts can be associated using the supplied username/email.
        // A failed social callback has no authenticated identity to trust.
        String normalizedLogin = bounded(login, 255);
        UUID userUuid = authenticatedUser;
        if (userUuid == null && "LOCAL".equals(provider) && normalizedLogin != null) {
            var account = users.findByUsername(normalizedLogin).or(() -> users.findByEmail(normalizedLogin));
            userUuid = account.map(user -> user.getUuid()).orElse(null);
        }
        history.append(new Event(UUID.randomUUID(), userUuid, normalizedLogin, bounded(provider, 100),
                outcome, bounded(reason, 64), clock.instant(), bounded(ip, 64), bounded(userAgent, 512), false));
    }

    private String bounded(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String clean = value.replaceAll("[\\p{Cntrl}]", " ").strip();
        return clean.length() > max ? clean.substring(0, max) : clean;
    }
}
