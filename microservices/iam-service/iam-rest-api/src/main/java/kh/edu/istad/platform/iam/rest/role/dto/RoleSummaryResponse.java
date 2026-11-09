package kh.edu.istad.platform.iam.rest.role.dto;

import java.util.UUID;

public record RoleSummaryResponse(UUID uuid, String name, boolean active) {}
