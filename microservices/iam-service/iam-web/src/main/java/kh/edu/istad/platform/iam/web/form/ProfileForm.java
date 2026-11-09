package kh.edu.istad.platform.iam.web.form;

import jakarta.validation.constraints.*;

public record ProfileForm(
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @Size(max = 100) String nickname,
        @Size(max = 20) String phoneNumber,
        @NotBlank @Pattern(regexp = "en|km") String locale
) {}
