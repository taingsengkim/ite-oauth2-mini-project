package kh.edu.istad.platform.iam.dto.user;

public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String confirmPassword,
        String givenName,
        String familyName,
        String locale
) {

    @Override
    public String toString() {
        return "RegisterUserCommand[username=" + username + "]";
    }
}
