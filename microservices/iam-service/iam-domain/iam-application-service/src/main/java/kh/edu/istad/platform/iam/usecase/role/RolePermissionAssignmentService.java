package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.role.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.mapper.role.RoleApplicationMapper;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.model.RolePermissionAssignment;
import kh.edu.istad.platform.iam.port.out.*;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import kh.edu.istad.platform.iam.port.out.role.RolePermissionAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RolePermissionAssignmentService {
    private final PermissionRepository permissions;
    private final RolePermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    private final RoleApplicationMapper mapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public List<PermissionAssignmentResult> assign(Role role, List<UUID> permissionUuids, Instant expiresAt, String reason) {
        role.requireMutable();
        role.requireAssignable();
        if (permissionUuids == null || permissionUuids.isEmpty() || permissionUuids.size() > 100
                || permissionUuids.stream().anyMatch(Objects::isNull) || new HashSet<>(permissionUuids).size() != permissionUuids.size()) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Provide 1 to 100 distinct permission UUIDs");
        }
        Instant now = clock.instant();
        String actor = actorProvider.currentActor();
        List<RolePermissionAssignment> existing = assignments.findByRoleUuid(role.getUuid());
        List<PermissionAssignmentResult> results = new ArrayList<>();
        for (UUID uuid : permissionUuids) {
            var permission = permissions.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("Permission", uuid));
            var assignment = RolePermissionAssignment.assign(role, permission, actor, now, expiresAt, reason);
            for (var previous : existing) {
                if (!previous.getPermissionUuid().equals(uuid) || previous.getRevokedAt() != null) continue;
                if (previous.isActive(now)) {
                    throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "Permission is already assigned to this role");
                }
                previous.revoke(actor, now, "Expired assignment replaced");
                assignments.update(previous);
            }
            results.add(mapper.toResult(assignments.create(assignment)));
        }
        return List.copyOf(results);
    }
}
