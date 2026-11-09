package kh.edu.istad.platform.iam.mapper.role;
import kh.edu.istad.platform.iam.dto.role.RoleResult;
import kh.edu.istad.platform.iam.dto.role.RoleSummary;
import kh.edu.istad.platform.iam.dto.role.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.model.RolePermissionAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleApplicationMapper {
    RoleResult toResult(Role role);
    RoleSummary toSummary(Role role);
    PermissionAssignmentResult toResult(RolePermissionAssignment assignment);
}
