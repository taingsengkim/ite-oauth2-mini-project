package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.PermissionEffect;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.port.out.user.UserPermissionAssignmentRepository;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
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
public class ListUserPermissionsUseCase {
    private final UserRepository users;
    private final UserPermissionAssignmentRepository assignments;
    private final PermissionRepository permissions;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<DirectPermission> execute(UUID userUuid) {
        users.findByUuid(userUuid).orElseThrow(() -> new ResourceNotFoundException("User", userUuid));
        var now = clock.instant();
        return assignments.findByUserUuid(userUuid).stream().filter(value -> value.getRevokedAt() == null)
                .flatMap(value -> permissions.findByUuid(value.getPermissionUuid()).map(permission -> new DirectPermission(
                        permission.getUuid(), permission.getName(), permission.getResource(), permission.getAction(),
                        permission.isActive(), value.getEffect(), value.getAssignedAt(), value.getAssignedBy(),
                        value.getExpiresAt(), value.getExpiresAt() != null && !value.getExpiresAt().isAfter(now))).stream())
                .sorted(Comparator.comparing(DirectPermission::name)).toList();
    }

    public record DirectPermission(UUID uuid, String name, String resource, String action, boolean active,
                                    PermissionEffect effect, Instant assignedAt, String assignedBy,
                                    Instant expiresAt, boolean expired) {}
}
