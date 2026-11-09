package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import kh.edu.istad.platform.iam.dto.user.UserFilterQuery;
import kh.edu.istad.platform.iam.rest.identity.dto.IdentityResponse;
import kh.edu.istad.platform.iam.rest.identity.mapper.IdentityApiMapper;
import kh.edu.istad.platform.iam.rest.user.dto.*;
import kh.edu.istad.platform.iam.rest.user.mapper.UserApiMapper;
import kh.edu.istad.platform.iam.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.usecase.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetUserUseCase getUserUseCase;
    private final UserApiMapper mapper;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final EnableUserUseCase enableUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final LockUserUseCase lockUserUseCase;
    private final UnlockUserUseCase unlockUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ResetUserPasswordUseCase resetUserPasswordUseCase;
    private final AssignRolesToUserUseCase assignRolesToUserUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;
    private final GetUserAuthoritiesUseCase getUserAuthoritiesUseCase;
    private final CreateUserUseCase createUserUseCase;

    private final GrantPermissionsToUserUseCase grantPermissionsToUserUseCase;
    private final RevokePermissionFromUserUseCase revokePermissionFromUserUseCase;



    private final ListUserIdentitiesUseCase listUserIdentitiesUseCase;
    private final UnlinkIdentityUseCase unlinkIdentityUseCase;
    private final IdentityApiMapper identityMapper;


    @GetMapping("/{uuid}")
    public UserResponse getByUuid(@PathVariable("uuid") UUID uuid) {
        return mapper.toResponse(getUserUseCase.execute(uuid));
    }

    @GetMapping
    public UserPageResponse list(@RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page, @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size, @RequestParam(name = "sort", defaultValue = "username,asc")
            String sort,
            @RequestParam(name = "search", required = false) @Size(max = 255) String search,
            @RequestParam(name = "enabled", required = false) Boolean enabled) {
        var result = listUsersUseCase.execute(new UserFilterQuery(page, size, sort, search, enabled));
        return new UserPageResponse(
                result.content().stream()
                        .map(mapper::toResponse)
                        .toList(),

                new UserPageResponse.PageMetadata(
                        result.page(),
                        result.size(),
                        result.totalElements(),
                        result.totalPages()
                )
        );
    }


    @PatchMapping("/{uuid}")
    public UserResponse updateProfile(
            @PathVariable("uuid") UUID uuid,
            @Valid @RequestBody UpdateUserProfileRequest request) {

        var command = mapper.toUpdateCommand(uuid, request);
        var result = updateUserProfileUseCase.execute(command);

        return mapper.toResponse(result);
    }

    @PostMapping("/{uuid}/enable")
    public ResponseEntity<Void> enable(
            @PathVariable("uuid") UUID uuid) {

        enableUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/disable")
    public ResponseEntity<Void> disable(
            @PathVariable("uuid") UUID uuid) {

        disableUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/lock")
    public ResponseEntity<Void> lock(
            @PathVariable("uuid") UUID uuid) {

        lockUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/unlock")
    public ResponseEntity<Void> unlock(
            @PathVariable("uuid") UUID uuid) {

        unlockUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable("uuid") UUID uuid) {
        deleteUserUseCase.execute(uuid);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}/password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable("uuid") UUID uuid,
            @Valid @RequestBody ResetUserPasswordRequest request) {

        var command = mapper.toResetPasswordCommand(uuid, request);

        resetUserPasswordUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/roles")
    public List<RoleAssignmentResponse> assignRoles(
            @PathVariable("uuid") UUID uuid,
            @Valid @RequestBody AssignRolesToUserRequest request) {

        var command = mapper.toAssignRolesCommand(uuid, request);

        return assignRolesToUserUseCase.execute(command)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{uuid}/roles/{roleUuid}")
    public ResponseEntity<Void> revokeRole(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("roleUuid") UUID roleUuid,
            @RequestParam(name = "reason", required = false)
            @Size(max = 255) String reason) {

        revokeRoleFromUserUseCase.execute(uuid, roleUuid, reason);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/permissions")
    public List<PermissionGrantResponse> grantPermissions(
            @PathVariable("uuid") UUID uuid,
            @Valid @RequestBody GrantPermissionsToUserRequest request) {

        var command = mapper.toGrantPermissionsCommand(uuid, request);

        return grantPermissionsToUserUseCase.execute(command)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{uuid}/permissions/{permissionUuid}")
    public ResponseEntity<Void> revokePermission(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("permissionUuid") UUID permissionUuid,
            @RequestParam(name = "reason", required = false)
            @Size(max = 255) String reason) {

        revokePermissionFromUserUseCase.execute(
                uuid, permissionUuid, reason
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{uuid}/authorities")
    public UserAuthoritiesResponse getAuthorities(@PathVariable("uuid") UUID uuid) {

        return mapper.toResponse(getUserAuthoritiesUseCase.execute(uuid));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request) {

        var result = createUserUseCase.execute(mapper.toCreateCommand(request));

        return ResponseEntity.created(URI.create("/api/users/" + result.uuid()))
                .body(mapper.toResponse(result));
    }

    @GetMapping("/{uuid}/identities")
    public List<IdentityResponse> listIdentities(
            @PathVariable("uuid") UUID uuid
    ) {
        return identityMapper.toResponses(
                listUserIdentitiesUseCase.execute(uuid)
        );
    }

    @DeleteMapping("/{uuid}/identities/{identityUuid}")
    public ResponseEntity<Void> unlinkIdentity(
            @PathVariable("uuid") UUID uuid,
            @PathVariable("identityUuid") UUID identityUuid
    ) {
        unlinkIdentityUseCase.execute(uuid, identityUuid);

        return ResponseEntity.noContent().build();
    }

}
