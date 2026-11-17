package kh.edu.istad.platform.iam.port.out.user;

public interface RegistrationCodeMail {
    record Delivery(String email, String locale, String code, String tokenHash, String codeHash) {
        @Override public String toString() { return "RegistrationCodeDelivery[REDACTED]"; }
    }
    boolean enabled();
    void send(Delivery delivery);
}
