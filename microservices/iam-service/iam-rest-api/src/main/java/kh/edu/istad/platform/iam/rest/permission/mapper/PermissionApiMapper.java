package kh.edu.istad.platform.iam.rest.permission.mapper;

import kh.edu.istad.platform.iam.dto.permission.CreatePermissionCommand;
import kh.edu.istad.platform.iam.dto.permission.PermissionResult;
import kh.edu.istad.platform.iam.dto.permission.UpdatePermissionCommand;
import kh.edu.istad.platform.iam.rest.permission.dto.CreatePermissionRequest;
import kh.edu.istad.platform.iam.rest.permission.dto.PermissionResponse;
import kh.edu.istad.platform.iam.rest.permission.dto.UpdatePermissionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PermissionApiMapper {
    CreatePermissionCommand toCommand(CreatePermissionRequest request);
    PermissionResponse toResponse(PermissionResult result);

    @Mapping(target = "uuid", source = "uuid")
    @Mapping(target = "resource", source = "request.resource")
    @Mapping(target = "action", source = "request.action")
    @Mapping(target = "description", source = "request.description")
    UpdatePermissionCommand toUpdateCommand(UUID uuid, UpdatePermissionRequest request);
}
