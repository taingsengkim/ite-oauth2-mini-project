package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 100)
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String givenName,

        @Size(max = 100)
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String familyName,

        @Size(max = 100)
        String nickname,

        @Pattern(regexp = "en|km")
        String locale,

        @Size(max = 20)
        @Pattern(regexp = "(?:|\\+[1-9]\\d{1,14})", message = "must be empty or use international format")
        String phoneNumber
) {}
