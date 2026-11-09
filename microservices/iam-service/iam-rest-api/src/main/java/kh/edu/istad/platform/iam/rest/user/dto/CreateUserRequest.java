package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 72) String password,
        @NotBlank @Size(max = 100) String givenName,
        @NotBlank @Size(max = 100) String familyName,

        @Pattern(regexp = "female|male|other")
        String gender,

        @PastOrPresent
        LocalDate birthdate,

        @Size(max = 20)
        @Pattern(regexp = "\\+[1-9]\\d{1,14}")
        String phoneNumber,

        @Size(max = 50)
        String zoneinfo,

        @NotBlank @Pattern(regexp = "en|km")
        String locale,

        @Valid
        AddressRequest address,

        @Size(max = 100)
        List<@NotNull UUID> roleUuids
) {

    @Override
    public String toString() {
        return "CreateUserRequest[username=" + username + "]";
    }
}