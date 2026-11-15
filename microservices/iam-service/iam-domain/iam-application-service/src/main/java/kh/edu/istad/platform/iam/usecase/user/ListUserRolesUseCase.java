package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRoleAssignmentRepository;
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
public class ListUserRolesUseCase {
    private final UserRepository users;
    private final UserRoleAssignmentRepository assignments;
    private final RoleRepository roles;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<AssignedRole> execute(UUID userUuid) {
        users.findByUuid(userUuid).orElseThrow(() -> new ResourceNotFoundException("User", userUuid));
        var now = clock.instant();
        return assignments.findByUserUuid(userUuid).stream().filter(value -> value.getRevokedAt() == null)
                .flatMap(value -> roles.findByUuid(value.getRoleUuid()).map(role -> new AssignedRole(role.getUuid(),
                        role.getName(), role.isActive(), role.isSystem(), value.getAssignedAt(), value.getAssignedBy(),
                        value.getExpiresAt(), value.getExpiresAt() != null && !value.getExpiresAt().isAfter(now))).stream())
                .sorted(Comparator.comparing(AssignedRole::name)).toList();
    }

    public record AssignedRole(UUID uuid, String name, boolean active, boolean system, Instant assignedAt,
                               String assignedBy, Instant expiresAt, boolean expired) {}
}
