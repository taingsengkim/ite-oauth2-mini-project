package kh.edu.istad.platform.iam.port.out.user;

public interface PasswordRecoveryRequests {
    boolean enabled();
    void submit(String email, String ipAddress);
}
