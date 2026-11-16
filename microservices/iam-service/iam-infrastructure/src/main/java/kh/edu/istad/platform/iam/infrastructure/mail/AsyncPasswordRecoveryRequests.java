package kh.edu.istad.platform.iam.infrastructure.mail;

import kh.edu.istad.platform.iam.port.out.user.*;
import kh.edu.istad.platform.iam.usecase.user.RecoverPasswordUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.util.concurrent.RejectedExecutionException;

@Service
public class AsyncPasswordRecoveryRequests implements PasswordRecoveryRequests {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsyncPasswordRecoveryRequests.class);
    private final RecoverPasswordUseCase recovery;
    private final PasswordRecoveryMail mail;
    private final PasswordResetRepository tokens;
    private final TaskExecutor executor;
    private final Clock clock;
    public AsyncPasswordRecoveryRequests(RecoverPasswordUseCase recovery, PasswordRecoveryMail mail, PasswordResetRepository tokens,
            @Qualifier("passwordRecoveryExecutor") TaskExecutor executor, Clock clock) {
        this.recovery = recovery; this.mail = mail; this.tokens = tokens; this.executor = executor; this.clock = clock;
    }
    @Override public boolean enabled() { return mail.enabled(); }
    @Override public void submit(String email, String ip) {
        try { executor.execute(() -> deliver(email, ip)); }
        catch (RejectedExecutionException busy) { log.warn("Password recovery queue is full"); }
    }
    private void deliver(String email, String ip) {
        try {
            var delivery = recovery.request(email, ip); // Transaction commits before SMTP delivery.
            if (delivery == null) return;
            try { mail.send(delivery); }
            catch (RuntimeException failed) {
                tokens.revoke(RecoverPasswordUseCase.digest(delivery.token()));
                // Do not log SMTP exceptions, recipient addresses, or message bodies containing reset links.
                log.warn("Password recovery delivery failed; reset link revoked. Check SMTP configuration.");
            }
        } catch (RuntimeException failed) { log.warn("Password recovery request could not be processed"); }
    }
    @Scheduled(fixedDelay = 3600000, initialDelay = 3600000)
    public void cleanup() { tokens.cleanup(clock.instant()); }
}
