package kh.edu.istad.platform.iam.rest.account.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.istad.platform.iam.usecase.user.DeactivateOwnAccountUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/deactivation")
public class AccountDeactivationController {
    private final DeactivateOwnAccountUseCase deactivate;
    public record Request(@NotBlank @Size(max = 50) String confirmation, @AssertTrue boolean acknowledged,
                          @Size(max = 1024) String currentPassword) {
        @Override public String toString() { return "AccountDeactivationRequest[REDACTED]"; }
    }
    @GetMapping public ResponseEntity<DeactivateOwnAccountUseCase.Status> get() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(deactivate.get());
    }
    @PostMapping public ResponseEntity<Void> deactivate(@Valid @RequestBody Request request) {
        deactivate.execute(new DeactivateOwnAccountUseCase.Command(request.confirmation(), request.acknowledged(), request.currentPassword()));
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
