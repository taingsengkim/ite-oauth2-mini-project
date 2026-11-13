package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.dto.user.CreateUserCommand;
import kh.edu.istad.platform.iam.rest.user.dto.DashboardCreateUserRequest;
import kh.edu.istad.platform.iam.usecase.user.CreateUserUseCase;
import org.springframework.http.ResponseEntity;
import kh.edu.istad.platform.iam.dto.user.UserFilterQuery;
import kh.edu.istad.platform.iam.rest.user.dto.UserPageResponse;
import kh.edu.istad.platform.iam.rest.user.dto.UserResponse;
import kh.edu.istad.platform.iam.rest.user.dto.UpdateUserProfileRequest;
import kh.edu.istad.platform.iam.usecase.user.UpdateUserProfileUseCase;
import kh.edu.istad.platform.iam.rest.user.mapper.UserApiMapper;
import kh.edu.istad.platform.iam.usecase.user.GetUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.ListUsersUseCase;
import kh.edu.istad.platform.iam.usecase.user.EnableUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.DisableUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.LockUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.UnlockUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.DeleteUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.ResetUserPasswordUseCase;
import kh.edu.istad.platform.iam.dto.user.ResetUserPasswordCommand;
import kh.edu.istad.platform.iam.rest.user.dto.ResetUserPasswordRequest;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/dashboard-api/users")
@RequiredArgsConstructor
public class DashboardUserController {

    private final ListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final EnableUserUseCase enableUserUseCase;
    private final DisableUserUseCase disableUserUseCase;
    private final LockUserUseCase lockUserUseCase;
    private final UnlockUserUseCase unlockUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ResetUserPasswordUseCase resetUserPasswordUseCase;
    private final CurrentUserProvider currentUser;

    private final UserApiMapper mapper;

    @PutMapping("/{uuid}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable("uuid") UUID uuid,
                                             @Valid @RequestBody ResetUserPasswordRequest request) {
        resetUserPasswordUseCase.execute(new ResetUserPasswordCommand(
                uuid, request.newPassword(), request.confirmPassword()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable("uuid") UUID uuid) {
        if (uuid.equals(currentUser.currentUserUuid())) {
            throw new ResourceConflictException("You cannot delete your own account.");
        }
        deleteUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/enable")
    public ResponseEntity<Void> enable(@PathVariable("uuid") UUID uuid) {
        enableUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/disable")
    public ResponseEntity<Void> disable(@PathVariable("uuid") UUID uuid) {
        preventSelfRestriction(uuid);
        disableUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/lock")
    public ResponseEntity<Void> lock(@PathVariable("uuid") UUID uuid) {
        preventSelfRestriction(uuid);
        lockUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable("uuid") UUID uuid) {
        unlockUserUseCase.execute(uuid);
        return ResponseEntity.noContent().build();
    }

    private void preventSelfRestriction(UUID uuid) {
        if (uuid.equals(currentUser.currentUserUuid())) {
            throw new ResourceConflictException("You cannot deactivate or lock your own account.");
        }
    }

    @PatchMapping("/{uuid}")
    public UserResponse update(@PathVariable("uuid") UUID uuid,
                               @Valid @RequestBody UpdateUserProfileRequest request) {
        return mapper.toResponse(updateUserProfileUseCase.execute(mapper.toUpdateCommand(uuid, request)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody DashboardCreateUserRequest request) {
        var user = createUserUseCase.execute(new CreateUserCommand(
                request.username(), request.email(), request.password(), request.givenName(), request.familyName(),
                null, null, request.phoneNumber(), null, request.locale(), null, List.of()));
        return ResponseEntity.created(URI.create("/dashboard-api/users/" + user.uuid()))
                .body(mapper.toResponse(user));
    }

    @GetMapping
    public UserPageResponse list(
            @RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page,

            @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(name = "sort", defaultValue = "username,asc")
            String sort,

            @RequestParam(name = "search", required = false)
            @Size(max = 255) String search,

            @RequestParam(name = "enabled", required = false)
            Boolean enabled
    ) {
        var result = listUsersUseCase.execute(
                new UserFilterQuery(page, size, sort, search, enabled)
        );

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

    @GetMapping("/{uuid}")
    public UserResponse getByUuid(@PathVariable("uuid")UUID uuid) {
        return mapper.toResponse(getUserUseCase.execute(uuid));
    }
}
