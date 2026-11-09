package kh.edu.istad.platform.iam.rest.role.mapper;

import kh.edu.istad.platform.iam.dto.role.*;
import kh.edu.istad.platform.iam.rest.role.dto.*;
import org.mapstruct.*;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleApiMapper {
    CreateRoleCommand toCommand(CreateRoleRequest request);
    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    UpdateRoleCommand toCommand(UUID uuid, UpdateRoleRequest request);
    @Mapping(target = "roleUuid", source = "uuid")
    @Mapping(target = "permissionUuids", source = "request.permissionUuids")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    @Mapping(target = "reason", source = "request.reason")
    AssignPermissionsToRoleCommand toCommand(UUID uuid, AssignPermissionsToRoleRequest request);
    RoleResponse toResponse(RoleResult result);
    RoleSummaryResponse toResponse(RoleSummary result);
    PermissionAssignmentResponse toResponse(PermissionAssignmentResult result);
}
