package kh.edu.istad.platform.iam.rest.role.dto;
import java.time.Instant;
import java.util.UUID;

public record RoleResponse(UUID uuid, String name, String description, boolean system,
                           boolean active, Instant createdAt, Instant updatedAt) {}
