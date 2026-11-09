package kh.edu.istad.platform.iam.rest.role.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.istad.platform.iam.dto.role.RoleFilterQuery;
import kh.edu.istad.platform.iam.rest.role.dto.*;
import kh.edu.istad.platform.iam.rest.role.mapper.RoleApiMapper;
import kh.edu.istad.platform.iam.usecase.role.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final CreateRoleUseCase createRole;
    private final GetRoleUseCase getRole;
    private final ListRolesUseCase listRoles;
    private final UpdateRoleUseCase updateRole;
    private final ActivateRoleUseCase activateRole;
    private final DeactivateRoleUseCase deactivateRole;
    private final DeleteRoleUseCase deleteRole;
    private final AssignPermissionsToRoleUseCase assignPermissions;
    private final RevokePermissionFromRoleUseCase revokePermission;
    private final RoleApiMapper mapper;

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        var result = createRole.execute(mapper.toCommand(request));
        return ResponseEntity.created(URI.create("/api/roles/" + result.uuid())).body(mapper.toResponse(result));
    }

    @GetMapping("/{uuid}")
    public RoleResponse get(@PathVariable("uuid") UUID uuid) {
        return mapper.toResponse(getRole.execute(uuid));
    }

    @GetMapping
    public RolePageResponse list(@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                                 @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
                                 @RequestParam(name = "sort", defaultValue = "name,asc") String sort,
                                 @RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "active", required = false) Boolean active) {
        var result = listRoles.execute(new RoleFilterQuery(page, size, sort, search, active));
        return new RolePageResponse(result.content().stream().map(mapper::toResponse).toList(),
                new RolePageResponse.PageMetadata(result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @PatchMapping("/{uuid}")
    public RoleResponse update(@PathVariable("uuid") UUID uuid, @Valid @RequestBody UpdateRoleRequest request) {
        return mapper.toResponse(updateRole.execute(mapper.toCommand(uuid, request)));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable("uuid") UUID uuid) {
        deleteRole.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(@PathVariable("uuid") UUID uuid) {
        activateRole.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable("uuid") UUID uuid) {
        deactivateRole.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/permissions")
    public List<PermissionAssignmentResponse> assign(@PathVariable("uuid") UUID uuid,
            @Valid @RequestBody AssignPermissionsToRoleRequest request) {
        return assignPermissions.execute(mapper.toCommand(uuid, request)).stream().map(mapper::toResponse).toList();
    }

    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    public ResponseEntity<Void> revoke(@PathVariable("uuid") UUID uuid,
            @PathVariable("permissionUuid") UUID permissionUuid,
            @RequestParam(name = "reason", required = false) @Size(max = 255) String reason) {
        revokePermission.execute(uuid, permissionUuid, reason);
        return ResponseEntity.noContent().build();
    }
}
