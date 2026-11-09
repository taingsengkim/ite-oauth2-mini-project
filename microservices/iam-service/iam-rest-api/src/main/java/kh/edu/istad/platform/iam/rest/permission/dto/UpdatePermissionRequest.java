package kh.edu.istad.platform.iam.rest.permission.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(
        @Size(max = 50)
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String resource,

        @Size(max = 50)
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
        String action,

        @Size(max = 255)
        String description
) {}