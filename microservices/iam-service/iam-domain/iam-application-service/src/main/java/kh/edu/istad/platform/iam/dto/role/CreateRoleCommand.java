package kh.edu.istad.platform.iam.dto.role;

import java.util.List;
import java.util.UUID;

public record CreateRoleCommand(String name, String description, List<UUID> permissionUuids) {}
