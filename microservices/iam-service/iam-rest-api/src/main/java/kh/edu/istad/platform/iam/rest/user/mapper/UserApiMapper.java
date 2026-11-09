package kh.edu.istad.platform.iam.rest.user.mapper;

import kh.edu.istad.platform.iam.dto.user.*;
import kh.edu.istad.platform.iam.rest.user.dto.*;
import kh.edu.istad.platform.iam.valueobject.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserApiMapper {

    RegisterUserCommand toCommand(RegisterUserRequest request);

    UserResponse toResponse(UserResult result);

    UserSummaryResponse toResponse(UserSummary result);

    RoleReferenceResponse toResponse(RoleReference role);


    @Mapping(target = "uuid", source = "uuid")
    UpdateUserProfileCommand toUpdateCommand(UUID uuid, UpdateUserProfileRequest request);


    @Mapping(target = "userUuid", source = "userUuid")
    ChangePasswordCommand toChangePasswordCommand(UUID userUuid, ChangePasswordRequest request);

    @Mapping(target = "userUuid", source = "userUuid")
    ResetUserPasswordCommand toResetPasswordCommand(UUID userUuid, ResetUserPasswordRequest request);

    @Mapping(target = "userUuid", source = "userUuid")
    @Mapping(target = "roleUuids", source = "request.roleUuids")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    @Mapping(target = "reason", source = "request.reason")
    AssignRolesToUserCommand toAssignRolesCommand(
            UUID userUuid,
            AssignRolesToUserRequest request
    );

    RoleAssignmentResponse toResponse(RoleAssignmentResult result);

    @Mapping(target = "userUuid", source = "userUuid")
    @Mapping(target = "permissionUuids", source = "request.permissionUuids")
    @Mapping(target = "effect", source = "request.effect")
    @Mapping(target = "expiresAt", source = "request.expiresAt")
    @Mapping(target = "reason", source = "request.reason")
    GrantPermissionsToUserCommand toGrantPermissionsCommand(
            UUID userUuid,
            GrantPermissionsToUserRequest request
    );

    PermissionGrantResponse toResponse(PermissionGrantResult result);

    UserAuthoritiesResponse toResponse(UserAuthoritiesResult result);

    CreateUserCommand toCreateCommand(CreateUserRequest request);

    Address toAddress(AddressRequest request);

}