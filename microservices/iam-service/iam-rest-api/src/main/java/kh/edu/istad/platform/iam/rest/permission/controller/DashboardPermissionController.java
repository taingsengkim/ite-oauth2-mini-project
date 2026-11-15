package kh.edu.istad.platform.iam.rest.permission.controller;

import jakarta.validation.constraints.*;
import kh.edu.istad.platform.iam.dto.permission.PermissionFilterQuery;
import kh.edu.istad.platform.iam.rest.permission.dto.PermissionPageResponse;
import kh.edu.istad.platform.iam.rest.permission.mapper.PermissionApiMapper;
import kh.edu.istad.platform.iam.usecase.permission.ListPermissionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DashboardPermissionController {
    private final ListPermissionsUseCase listPermissions;
    private final PermissionApiMapper mapper;

    @GetMapping("/dashboard-api/permissions")
    public PermissionPageResponse list(@RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
                                       @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size,
                                       @RequestParam(name = "search", required = false) @Size(max = 255) String search,
                                       @RequestParam(name = "active", required = false) Boolean active) {
        var result = listPermissions.execute(new PermissionFilterQuery(page, size, "name,asc", search, null, null, active));
        return new PermissionPageResponse(result.content().stream().map(mapper::toResponse).toList(),
                new PermissionPageResponse.PageMetadata(result.page(), result.size(), result.totalElements(), result.totalPages()));
    }
}
