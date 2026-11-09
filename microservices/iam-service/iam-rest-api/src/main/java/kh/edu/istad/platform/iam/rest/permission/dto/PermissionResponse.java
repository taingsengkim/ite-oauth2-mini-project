package kh.edu.istad.platform.iam.rest.permission.dto;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(UUID uuid, String name, String resource, String action,
                                 String description, boolean system, boolean active,
                                 Instant createdAt, Instant updatedAt) {}
