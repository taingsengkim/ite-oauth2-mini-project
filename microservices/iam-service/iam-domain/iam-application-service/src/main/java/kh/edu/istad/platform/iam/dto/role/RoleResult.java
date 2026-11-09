package kh.edu.istad.platform.iam.dto.role;

import java.util.UUID;
import java.time.Instant;

public record RoleResult(UUID uuid, String name, String description, boolean system, boolean active, Instant createdAt, Instant updatedAt) {}
