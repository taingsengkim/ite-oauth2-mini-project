package kh.edu.istad.platform.iam.rest.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank @Size(max=50) String resource,
        @NotBlank @Size(max=50) String action,
        @Size(max=255) String description
) {
}
