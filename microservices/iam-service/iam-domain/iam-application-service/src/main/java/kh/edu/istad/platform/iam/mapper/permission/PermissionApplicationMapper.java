package kh.edu.istad.platform.iam.mapper.permission;


import kh.edu.istad.platform.iam.dto.permission.PermissionResult;
import kh.edu.istad.platform.iam.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PermissionApplicationMapper {
    PermissionResult toResult(Permission permission);
}
