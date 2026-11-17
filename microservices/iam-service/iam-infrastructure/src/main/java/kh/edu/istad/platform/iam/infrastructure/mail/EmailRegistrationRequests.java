package kh.edu.istad.platform.iam.infrastructure.mail;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.user.RegisterUserCommand;
import kh.edu.istad.platform.iam.port.out.user.*;
import kh.edu.istad.platform.iam.usecase.user.VerifyRegistrationUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.util.concurrent.RejectedExecutionException;

@Service
public class EmailRegistrationRequests implements RegistrationRequests {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(EmailRegistrationRequests.class);
    private final VerifyRegistrationUseCase verification;
    private final RegistrationCodeMail mail;
    private final PendingRegistrationRepository pending;
    private final TaskExecutor executor;
    private final Clock clock;
    public EmailRegistrationRequests(VerifyRegistrationUseCase verification,RegistrationCodeMail mail,
            PendingRegistrationRepository pending,@Qualifier("passwordRecoveryExecutor") TaskExecutor executor,Clock clock) {
        this.verification=verification;this.mail=mail;this.pending=pending;this.executor=executor;this.clock=clock;
    }
    @Override public boolean enabled() { return mail.enabled(); }
    @Override public Challenge start(RegisterUserCommand command,String ip) {
        requireEnabled();var started=verification.start(command,ip); // Commit before queuing SMTP.
        if(started.limited())throw new RateLimited();
        deliver(started.delivery());return started.challenge();
    }
    @Override public Verification verify(String token,String code,String ip) {
        requireEnabled();
        try { return verification.verify(token,code,ip); }
        catch(ResourceConflictException conflict) {
            // Final uniqueness checks roll back account creation; invalidate the pending request separately.
            verification.cancel(token);return new Verification(Outcome.CONFLICT,null);
        }
    }
    @Override public boolean resend(String token,String ip) {
        requireEnabled();var result=verification.resend(token,ip);deliver(result.delivery());return !result.limited();
    }
    @Override public void cancel(String token) { verification.cancel(token); }
    private void deliver(RegistrationCodeMail.Delivery delivery) {
        if(delivery==null)return;
        try { executor.execute(() -> {
            try { mail.send(delivery); }
            catch(RuntimeException failure) { revoke(delivery);log.warn("Registration email delivery failed. Check SMTP configuration."); }
        }); }
        catch(RejectedExecutionException busy) { revoke(delivery);log.warn("Registration email queue is full"); }
    }
    private void revoke(RegistrationCodeMail.Delivery delivery) {
        try { pending.revokeDelivery(delivery.tokenHash(),delivery.codeHash()); }
        catch(RuntimeException failure) { log.warn("Registration email challenge could not be revoked; it will expire automatically"); }
    }
    private void requireEnabled() { if(!enabled())throw new IllegalStateException("Email registration is not configured"); }
    @Scheduled(fixedDelay=3600000,initialDelay=3600000)
    public void cleanup() { pending.cleanup(clock.instant()); }
}
