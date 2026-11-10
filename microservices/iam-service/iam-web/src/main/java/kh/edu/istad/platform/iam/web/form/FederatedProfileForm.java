package kh.edu.istad.platform.iam.web.form;

import jakarta.validation.constraints.*;

public record FederatedProfileForm(
        @NotBlank String proofToken,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @NotBlank @Pattern(regexp = "en|km") String locale
) {
    @Override public String toString() { return "FederatedProfileForm[redacted]"; }
}
