package kh.edu.istad.platform.iam.dto.role;

import java.util.UUID;

public record UpdateRoleCommand(UUID uuid, String name, String description) {}
