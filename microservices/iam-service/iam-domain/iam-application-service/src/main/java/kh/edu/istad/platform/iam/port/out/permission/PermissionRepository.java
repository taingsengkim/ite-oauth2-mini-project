package kh.edu.istad.platform.iam.port.out.permission;

import kh.edu.istad.platform.iam.dto.permission.PermissionFilterQuery;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.model.Permission;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    Permission create(Permission permission);

    Optional<Permission> findByUuid(UUID uuid);
    PageResult<Permission> findAll(PermissionFilterQuery query);
    Optional<Permission> update(Permission permission);
    void softDelete(Permission permission);
    void updateActiveState(Permission permission);
}


