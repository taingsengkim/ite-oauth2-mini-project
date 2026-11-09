package kh.edu.istad.platform.iam.mapper.user;

import kh.edu.istad.platform.iam.dto.user.AuthUserResult;
import kh.edu.istad.platform.iam.dto.user.PermissionGrantResult;
import kh.edu.istad.platform.iam.dto.user.RoleAssignmentResult;
import kh.edu.istad.platform.iam.dto.user.UserResult;
import kh.edu.istad.platform.iam.dto.user.UserSummary;
import kh.edu.istad.platform.iam.dto.user.RoleReference;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.model.UserPermissionAssignment;
import kh.edu.istad.platform.iam.model.UserRoleAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserApplicationMapper {
    @Mapping(target = "name",
            expression = "java(user.getGivenName() + \" \" + user.getFamilyName())"
    )
    UserResult toResult(User user, List<RoleReference> roles);

    UserSummary toSummary(UserResult user);

    @Mapping(target = "authorities", source = "authorities")
    AuthUserResult toAuthResult(
            User user,
            List<String> authorities
    );

    RoleAssignmentResult toResult(UserRoleAssignment assignment);

    PermissionGrantResult toResult(UserPermissionAssignment assignment);

}
