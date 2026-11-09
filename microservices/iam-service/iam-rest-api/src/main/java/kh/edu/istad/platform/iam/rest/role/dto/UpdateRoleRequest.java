package kh.edu.istad.platform.iam.rest.role.dto;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

public record UpdateRoleRequest(
    @Size(max = 50) @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String name,
    @Size(max = 255) String description
) {}
