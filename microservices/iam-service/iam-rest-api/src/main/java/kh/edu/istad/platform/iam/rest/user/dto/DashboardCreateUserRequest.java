package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dashboard creation always assigns the default USER role. */
public record DashboardCreateUserRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 72) String password,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,
        @NotBlank @Pattern(regexp = "en|km") String locale,
        @Size(max = 20) @Pattern(regexp = "\\+[1-9]\\d{1,14}") String phoneNumber
) {
    @Override
    public String toString() {
        return "DashboardCreateUserRequest[username=" + username + "]";
    }
}
