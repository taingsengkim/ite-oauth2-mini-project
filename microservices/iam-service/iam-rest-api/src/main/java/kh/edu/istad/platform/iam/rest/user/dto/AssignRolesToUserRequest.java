package kh.edu.istad.platform.iam.rest.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssignRolesToUserRequest(
        @NotEmpty @Size(max = 100)
        List<@NotNull UUID> roleUuids,

        Instant expiresAt,
        @Size(max = 255)
        String reason
) {}
