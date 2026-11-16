package kh.edu.istad.platform.iam.rest.account.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.ChangePasswordCommand;
import kh.edu.istad.platform.iam.rest.user.dto.ChangePasswordRequest;
import kh.edu.istad.platform.iam.usecase.user.ChangePasswordUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/security")
public class AccountSecurityController {
    private final CurrentUserProvider currentUser;
    private final UserRepository users;
    private final ChangePasswordUseCase changePassword;
    public record Status(boolean hasPassword, Instant passwordChangedAt) {}
    @GetMapping
    public ResponseEntity<Status> get() {
        var uuid = currentUser.currentUserUuid();
        var user = users.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid));
        boolean hasPassword = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Status(hasPassword, hasPassword ? user.getPasswordChangedAt() : null));
    }
    @PutMapping("/password")
    public ResponseEntity<Void> change(@Valid @RequestBody ChangePasswordRequest request) {
        changePassword.execute(new ChangePasswordCommand(currentUser.currentUserUuid(), request.currentPassword(), request.newPassword(), request.confirmPassword()));
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
