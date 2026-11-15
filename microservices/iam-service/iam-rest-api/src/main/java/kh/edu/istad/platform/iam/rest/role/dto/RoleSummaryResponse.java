package kh.edu.istad.platform.iam.rest.role.dto;

import java.util.UUID;

public record RoleSummaryResponse(UUID uuid, String name, String description, boolean system, boolean active) {}
