package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.role.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.dto.role.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignPermissionsToRoleUseCase {
    private final RoleRepository repository;
    private final RolePermissionAssignmentService assignmentService;
    @Transactional
    public java.util.List<PermissionAssignmentResult> execute(AssignPermissionsToRoleCommand command) {
        Role role = repository.findByUuidForUpdate(command.roleUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Role", command.roleUuid()));
        return assignmentService.assign(role, command.permissionUuids(), command.expiresAt(), command.reason());
    }
}
