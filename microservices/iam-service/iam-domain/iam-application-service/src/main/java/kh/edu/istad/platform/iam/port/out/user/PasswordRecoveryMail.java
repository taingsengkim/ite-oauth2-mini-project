package kh.edu.istad.platform.iam.port.out.user;

public interface PasswordRecoveryMail {
    record Delivery(String email, String token, String locale) {
        @Override public String toString() { return "PasswordRecoveryDelivery[REDACTED]"; }
    }
    boolean enabled();
    void send(Delivery delivery);
}
