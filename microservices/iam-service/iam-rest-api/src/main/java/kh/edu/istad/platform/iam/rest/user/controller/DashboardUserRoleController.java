package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.rest.user.dto.*;
import kh.edu.istad.platform.iam.rest.user.mapper.UserApiMapper;
import kh.edu.istad.platform.iam.usecase.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard-api/users/{userUuid}/roles")
@RequiredArgsConstructor
public class DashboardUserRoleController {
    private final ListUserRolesUseCase listRoles;
    private final AssignRolesToUserUseCase assignRoles;
    private final RevokeRoleFromUserUseCase revokeRole;
    private final UpdateUserRoleAssignmentUseCase updateAssignment;
    private final CurrentUserProvider currentUser;
    private final UserApiMapper mapper;

    @GetMapping
    public List<ListUserRolesUseCase.AssignedRole> list(@PathVariable("userUuid") UUID userUuid) {
        return listRoles.execute(userUuid);
    }

    @PostMapping
    public List<RoleAssignmentResponse> assign(@PathVariable("userUuid") UUID userUuid,
                                               @Valid @RequestBody AssignRolesToUserRequest request) {
        return assignRoles.execute(mapper.toAssignRolesCommand(userUuid, request)).stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{roleUuid}")
    public RoleAssignmentResponse update(@PathVariable("userUuid") UUID userUuid, @PathVariable("roleUuid") UUID roleUuid,
                                          @Valid @RequestBody UpdateUserRoleRequest request) {
        requireAnotherAccount(userUuid);
        return mapper.toResponse(updateAssignment.execute(userUuid, roleUuid, request.expiresAt(), request.reason()));
    }

    @DeleteMapping("/{roleUuid}")
    public ResponseEntity<Void> revoke(@PathVariable("userUuid") UUID userUuid, @PathVariable("roleUuid") UUID roleUuid) {
        requireAnotherAccount(userUuid);
        revokeRole.execute(userUuid, roleUuid, "Revoked from user role management");
        return ResponseEntity.noContent().build();
    }

    private void requireAnotherAccount(UUID userUuid) {
        if (userUuid.equals(currentUser.currentUserUuid())) {
            throw new ResourceConflictException("Ask another administrator to change or revoke your own role assignments.");
        }
    }
}
