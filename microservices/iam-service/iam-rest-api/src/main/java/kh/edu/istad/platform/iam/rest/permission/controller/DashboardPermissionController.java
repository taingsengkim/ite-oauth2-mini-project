package kh.edu.istad.platform.iam.rest.permission.controller;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.dto.permission.PermissionFilterQuery;
import kh.edu.istad.platform.iam.rest.permission.dto.PermissionPageResponse;
import kh.edu.istad.platform.iam.rest.permission.dto.*;
import kh.edu.istad.platform.iam.rest.permission.mapper.PermissionApiMapper;
import kh.edu.istad.platform.iam.usecase.permission.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DashboardPermissionController {
    private final ListPermissionsUseCase listPermissions;
    private final CreatePermissionUseCase createPermission;
    private final GetPermissionUseCase getPermission;
    private final UpdatePermissionUseCase updatePermission;
    private final DeletePermissionUseCase deletePermission;
    private final ActivatePermissionUseCase activatePermission;
    private final DeactivatePermissionUseCase deactivatePermission;
    private final PermissionApiMapper mapper;

    @GetMapping("/dashboard-api/permissions")
    public PermissionPageResponse list(@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                                       @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
                                       @RequestParam(name = "search", required = false) @Size(max = 255) String search,
                                       @RequestParam(name = "resource", required = false) @Size(max = 50) String resource,
                                       @RequestParam(name = "action", required = false) @Size(max = 50) String action,
                                       @RequestParam(name = "active", required = false) Boolean active) {
        var result = listPermissions.execute(new PermissionFilterQuery(page, size, "name,asc", search, resource, action, active));
        return new PermissionPageResponse(result.content().stream().map(mapper::toResponse).toList(),
                new PermissionPageResponse.PageMetadata(result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @GetMapping("/dashboard-api/permissions/{uuid}")
    public PermissionResponse get(@PathVariable("uuid") UUID uuid) { return mapper.toResponse(getPermission.execute(uuid)); }

    @PostMapping("/dashboard-api/permissions")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody CreatePermissionRequest request) {
        var result = createPermission.execute(mapper.toCommand(request));
        return ResponseEntity.created(URI.create("/dashboard-api/permissions/" + result.uuid())).body(mapper.toResponse(result));
    }

    @PatchMapping("/dashboard-api/permissions/{uuid}")
    public PermissionResponse update(@PathVariable("uuid") UUID uuid, @Valid @RequestBody UpdatePermissionRequest request) {
        return mapper.toResponse(updatePermission.execute(mapper.toUpdateCommand(uuid, request)));
    }

    @DeleteMapping("/dashboard-api/permissions/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable("uuid") UUID uuid) {
        deletePermission.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dashboard-api/permissions/{uuid}/activate")
    public ResponseEntity<Void> activate(@PathVariable("uuid") UUID uuid) {
        activatePermission.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/dashboard-api/permissions/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable("uuid") UUID uuid) {
        deactivatePermission.execute(uuid);
        return ResponseEntity.noContent().build();
    }
}
