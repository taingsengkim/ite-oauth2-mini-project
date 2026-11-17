package kh.edu.istad.platform.iam.web.form;

import jakarta.validation.constraints.*;

public record RegistrationForm(
        @NotBlank @Size(max = 50) @Pattern(regexp="[A-Za-z0-9_.-]{3,50}",message="Use 3–50 letters, digits, dots, underscores, or hyphens") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @NotBlank @Size(max = 72) String password,
        @NotBlank String confirmPassword,
        @NotBlank @Pattern(regexp = "en|km") String locale
) {
    @Override public String toString() { return "RegistrationForm[username=" + username + "]"; }
    public static RegistrationForm empty(String locale) {
        return new RegistrationForm("", "", "", "", "", "", locale);
    }
}
