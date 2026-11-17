package kh.edu.istad.platform.iam.port.out.user;

import kh.edu.istad.platform.iam.dto.user.RegisterUserCommand;
import java.time.Instant;
import java.util.UUID;

public interface RegistrationRequests {
    record Challenge(String token, String maskedEmail, Instant expiresAt) implements java.io.Serializable {
        @Override public String toString() { return "RegistrationChallenge[REDACTED]"; }
    }
    enum Outcome { VERIFIED, INVALID, RATE_LIMITED, CONFLICT }
    record Verification(Outcome outcome, UUID userUuid) {}
    class RateLimited extends RuntimeException {
        public RateLimited() { super("Too many registration requests. Try again later."); }
    }
    boolean enabled();
    Challenge start(RegisterUserCommand command, String ip);
    Verification verify(String token, String code, String ip);
    boolean resend(String token, String ip);
    void cancel(String token);
}
