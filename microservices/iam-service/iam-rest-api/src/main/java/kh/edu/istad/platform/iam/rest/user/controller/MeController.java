package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.rest.identity.dto.IdentityResponse;
import kh.edu.istad.platform.iam.rest.identity.mapper.IdentityApiMapper;
import kh.edu.istad.platform.iam.rest.user.dto.ChangePasswordRequest;
import kh.edu.istad.platform.iam.rest.user.dto.UpdateUserProfileRequest;
import kh.edu.istad.platform.iam.rest.user.dto.UserResponse;
import kh.edu.istad.platform.iam.rest.user.mapper.UserApiMapper;
import kh.edu.istad.platform.iam.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.usecase.user.ChangePasswordUseCase;
import kh.edu.istad.platform.iam.usecase.user.GetUserUseCase;
import kh.edu.istad.platform.iam.usecase.user.UpdateUserProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final CurrentUserProvider currentUserProvider;
    private final GetUserUseCase getUserUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;


    private final ListUserIdentitiesUseCase listUserIdentitiesUseCase;
    private final UnlinkIdentityUseCase unlinkIdentityUseCase;
    private final IdentityApiMapper identityMapper;

    private final UserApiMapper mapper;

    @GetMapping
    public UserResponse getProfile() {
        var uuid = currentUserProvider.currentUserUuid();
        return mapper.toResponse(getUserUseCase.execute(uuid));
    }

    @PatchMapping
    public UserResponse updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {

        var uuid = currentUserProvider.currentUserUuid();
        var command = mapper.toUpdateCommand(uuid, request);

        return mapper.toResponse(
                updateUserProfileUseCase.execute(command)
        );
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        var uuid = currentUserProvider.currentUserUuid();
        var command = mapper.toChangePasswordCommand(uuid, request);
        changePasswordUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/identities")
    public List<IdentityResponse> listIdentities() {
        var userUuid = currentUserProvider.currentUserUuid();

        return identityMapper.toResponses(
                listUserIdentitiesUseCase.execute(userUuid)
        );
    }

    @DeleteMapping("/identities/{identityUuid}")
    public ResponseEntity<Void> unlinkIdentity(
            @PathVariable("identityUuid") UUID identityUuid
    ) {
        var userUuid = currentUserProvider.currentUserUuid();

        unlinkIdentityUseCase.execute(userUuid, identityUuid);

        return ResponseEntity.noContent().build();
    }

}
