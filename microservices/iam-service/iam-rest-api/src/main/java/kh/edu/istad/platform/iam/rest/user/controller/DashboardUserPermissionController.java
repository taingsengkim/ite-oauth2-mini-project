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
@RequiredArgsConstructor
public class DashboardUserPermissionController {
    private final ListUserPermissionsUseCase listPermissions;
    private final GrantPermissionsToUserUseCase grantPermissions;
    private final RevokePermissionFromUserUseCase revokePermission;
    private final GetUserAuthoritiesUseCase getAuthorities;
    private final CurrentUserProvider currentUser;
    private final UserApiMapper mapper;

    @GetMapping("/dashboard-api/users/{uuid}/permissions")
    public List<ListUserPermissionsUseCase.DirectPermission> list(@PathVariable("uuid") UUID uuid) { return listPermissions.execute(uuid); }

    @GetMapping("/dashboard-api/users/{uuid}/authorities")
    public UserAuthoritiesResponse authorities(@PathVariable("uuid") UUID uuid) { return mapper.toResponse(getAuthorities.execute(uuid)); }

    @PostMapping("/dashboard-api/users/{uuid}/permissions")
    public List<PermissionGrantResponse> grant(@PathVariable("uuid") UUID uuid, @Valid @RequestBody GrantPermissionsToUserRequest request) {
        requireAnotherAccount(uuid);
        return grantPermissions.execute(mapper.toGrantPermissionsCommand(uuid, request)).stream().map(mapper::toResponse).toList();
    }

    @DeleteMapping("/dashboard-api/users/{uuid}/permissions/{permissionUuid}")
    public ResponseEntity<Void> revoke(@PathVariable("uuid") UUID uuid, @PathVariable("permissionUuid") UUID permissionUuid) {
        requireAnotherAccount(uuid);
        revokePermission.execute(uuid, permissionUuid, "Revoked from direct permission management");
        return ResponseEntity.noContent().build();
    }

    private void requireAnotherAccount(UUID uuid) {
        if (uuid.equals(currentUser.currentUserUuid())) throw new ResourceConflictException("Ask another administrator to change your own direct permissions.");
    }
}
