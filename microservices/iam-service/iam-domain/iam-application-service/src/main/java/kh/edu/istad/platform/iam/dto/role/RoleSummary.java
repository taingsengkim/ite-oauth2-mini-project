package kh.edu.istad.platform.iam.dto.role;

import java.util.UUID;

public record RoleSummary(UUID uuid, String name, boolean active) {}
