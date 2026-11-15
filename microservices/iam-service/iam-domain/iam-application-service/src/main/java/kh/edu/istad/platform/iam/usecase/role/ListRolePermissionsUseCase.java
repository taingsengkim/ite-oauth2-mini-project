package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import kh.edu.istad.platform.iam.port.out.role.RolePermissionAssignmentRepository;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListRolePermissionsUseCase {
    private final RoleRepository roles;
    private final RolePermissionAssignmentRepository assignments;
    private final PermissionRepository permissions;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<AssignedPermission> execute(UUID roleUuid) {
        roles.findByUuid(roleUuid).orElseThrow(() -> new ResourceNotFoundException("Role", roleUuid));
        var now = clock.instant();
        return assignments.findByRoleUuid(roleUuid).stream().filter(assignment -> assignment.isActive(now))
                .flatMap(assignment -> permissions.findByUuid(assignment.getPermissionUuid())
                        .map(permission -> new AssignedPermission(permission.getUuid(), permission.getName(),
                                permission.getResource(), permission.getAction(), permission.isActive(),
                                assignment.getAssignedAt(), assignment.getAssignedBy(), assignment.getExpiresAt()))
                        .stream())
                .sorted(Comparator.comparing(AssignedPermission::name)).toList();
    }

    public record AssignedPermission(UUID uuid, String name, String resource, String action, boolean active,
                                     Instant assignedAt, String assignedBy, Instant expiresAt) {}
}
