package kh.edu.istad.platform.iam.rest.permission.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.dto.permission.PermissionFilterQuery;
import kh.edu.istad.platform.iam.rest.permission.dto.*;
import kh.edu.istad.platform.iam.rest.permission.mapper.PermissionApiMapper;
import kh.edu.istad.platform.iam.usecase.permission.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final CreatePermissionUseCase createPermissionUseCase;
    private final GetPermissionUseCase getPermissionUseCase;
    private final ListPermissionsUseCase listPermissionsUseCase;
    private final UpdatePermissionUseCase updatePermissionUseCase;
    private final DeletePermissionUseCase deletePermissionUseCase;
    private final ActivatePermissionUseCase activePermissionUseCase;
    private final DeactivatePermissionUseCase deactivePermissionUseCase;
    private final PermissionApiMapper mapper;

    @PostMapping
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody CreatePermissionRequest request) {
        var result = createPermissionUseCase.execute(mapper.toCommand(request));
        return ResponseEntity.created(URI.create("/api/permissions/" + result.uuid()))
                .body(mapper.toResponse(result));
    }

    @GetMapping("/{uuid}")
    public PermissionResponse getByUuid(@PathVariable("uuid") UUID uuid) {
        return mapper.toResponse(getPermissionUseCase.execute(uuid));
    }

    @GetMapping
    public PermissionPageResponse list(
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(name = "sort", defaultValue = "name,asc") String sort,
            @RequestParam(name = "search", required = false) @Size(max = 255) String search,
            @RequestParam(name = "resource", required = false) @Size(max = 50) String resource,
            @RequestParam(name = "action", required = false) @Size(max = 50) String action,
            @RequestParam(name = "active", required = false) Boolean active
    ) {
        var result = listPermissionsUseCase.execute(
                new PermissionFilterQuery(page, size, sort, search, resource, action, active));
        return new PermissionPageResponse(result.content().stream().map(mapper::toResponse).toList(),
                new PermissionPageResponse.PageMetadata(
                        result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @PatchMapping("/{uuid}")
    public PermissionResponse update(@PathVariable("uuid") UUID uuid,
                                     @Valid @RequestBody UpdatePermissionRequest request) {
        return mapper.toResponse(updatePermissionUseCase.execute(mapper.toUpdateCommand(uuid, request)));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> softDelete(@PathVariable("uuid") UUID uuid) {
        deletePermissionUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/activate")
    public ResponseEntity<Void> activate(@PathVariable("uuid") UUID uuid) {
        activePermissionUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable("uuid") UUID uuid) {
        deactivePermissionUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }
}
