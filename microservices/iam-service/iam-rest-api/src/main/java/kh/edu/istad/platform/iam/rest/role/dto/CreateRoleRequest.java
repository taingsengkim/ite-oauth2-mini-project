package kh.edu.istad.platform.iam.rest.role.dto;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public record CreateRoleRequest(
    @NotBlank @Size(max = 50) String name,
    @Size(max = 255) String description,
    @Size(max = 100) List<@NotNull UUID> permissionUuids
) {}
