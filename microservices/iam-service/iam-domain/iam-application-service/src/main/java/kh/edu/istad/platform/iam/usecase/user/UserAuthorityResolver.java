package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.dto.user.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.model.PermissionEffect;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import kh.edu.istad.platform.iam.port.out.role.RolePermissionAssignmentRepository;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import kh.edu.istad.platform.iam.port.out.user.UserPermissionAssignmentRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAuthorityResolver {

    private final UserRoleAssignmentRepository userRoles;
    private final RoleRepository roles;
    private final RolePermissionAssignmentRepository rolePermissions;
    private final UserPermissionAssignmentRepository userPermissions;
    private final PermissionRepository permissions;
    private final Clock clock;

    public List<String> resolve(UUID userUuid) {
        var result = resolveDetails(userUuid);
        var authorities = new TreeSet<String>();

        for (var role : result.roles()) {
            authorities.add("ROLE_" + role.name());
        }

        authorities.addAll(result.effectivePermissions());

        return List.copyOf(authorities);
    }

    public UserAuthoritiesResult resolveDetails(UUID userUuid) {
        var now = clock.instant();

        var roleDetails =
                new ArrayList<UserAuthoritiesResult.RoleAuthority>();

        var directPermissions =
                new ArrayList<UserAuthoritiesResult.DirectPermission>();

        var effectivePermissions = new TreeSet<String>();
        var deniedPermissions = new TreeSet<String>();

        for (var assignment : userRoles.findByUserUuid(userUuid)) {
            if (!assignment.isActive(now)) {
                continue;
            }

            var role = roles.findByUuid(assignment.getRoleUuid())
                    .orElse(null);

            if (role == null || !role.isActive() || role.isDeleted()) {
                continue;
            }

            roleDetails.add(
                    new UserAuthoritiesResult.RoleAuthority(
                            role.getUuid(),
                            role.getName(),
                            assignment.getExpiresAt()
                    )
            );

            for (var permissionAssignment :
                    rolePermissions.findByRoleUuid(role.getUuid())) {

                if (!permissionAssignment.isActive(now)) {
                    continue;
                }

                var permission = permissions
                        .findByUuid(permissionAssignment.getPermissionUuid())
                        .orElse(null);

                if (permission != null
                        && permission.isActive()
                        && !permission.isDeleted()) {

                    effectivePermissions.add(permission.getName());
                }
            }
        }

        for (var assignment :
                userPermissions.findByUserUuid(userUuid)) {

            if (!assignment.isActive(now)) {
                continue;
            }

            var permission = permissions
                    .findByUuid(assignment.getPermissionUuid())
                    .orElse(null);

            if (permission == null
                    || !permission.isActive()
                    || permission.isDeleted()) {
                continue;
            }

            directPermissions.add(
                    new UserAuthoritiesResult.DirectPermission(
                            permission.getName(),
                            assignment.getEffect(),
                            assignment.getExpiresAt()
                    )
            );

            if (assignment.getEffect() == PermissionEffect.GRANT) {
                effectivePermissions.add(permission.getName());
            } else if (assignment.getEffect() == PermissionEffect.DENY) {
                deniedPermissions.add(permission.getName());
            }
        }

        effectivePermissions.removeAll(deniedPermissions);

        roleDetails.sort(
                Comparator.comparing(UserAuthoritiesResult.RoleAuthority::name)
                        .thenComparing(UserAuthoritiesResult.RoleAuthority::uuid)
        );

        directPermissions.sort(
                Comparator.comparing(UserAuthoritiesResult.DirectPermission::name)
        );

        return new UserAuthoritiesResult(
                roleDetails,
                directPermissions,
                List.copyOf(effectivePermissions)
        );
    }
}
