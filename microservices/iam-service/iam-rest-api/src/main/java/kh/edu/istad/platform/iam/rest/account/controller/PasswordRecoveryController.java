package kh.edu.istad.platform.iam.rest.account.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.istad.platform.iam.port.out.user.PasswordRecoveryRequests;
import kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.usecase.user.RecoverPasswordUseCase;
import kh.edu.istad.platform.iam.rest.configuration.controller.DashboardConfigurationController.PasswordRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recovery-api")
public class PasswordRecoveryController {
    private final PasswordRecoveryRequests requests;
    private final RecoverPasswordUseCase recovery;
    private final SystemConfigurationRepository configuration;
    public record Context(boolean enabled, PasswordRequirements policy, String csrfHeaderName, String csrfToken) {}
    public record EmailRequest(@NotBlank @Email @Size(max=255) String email) {
        @Override public String toString() { return "PasswordRecoveryEmailRequest[REDACTED]"; }
    }
    public record ResetRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{43}") String token,
                              @NotBlank @Size(max=72) String newPassword, @NotBlank @Size(max=72) String confirmPassword) {
        @Override public String toString() { return "PasswordRecoveryResetRequest[REDACTED]"; }
    }
    public record Accepted(String message) {}
    @GetMapping("/context") public ResponseEntity<Context> context(CsrfToken csrf) {
        var policy = configuration.current().policy();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Context(requests.enabled(),
                new PasswordRequirements(policy.passwordMinLength(), policy.passwordMaxLength(), policy.passwordRequireUppercase(),
                        policy.passwordRequireLowercase(), policy.passwordRequireDigit(), policy.passwordRequireSpecial()), csrf.getHeaderName(), csrf.getToken()));
    }
    @PostMapping("/request") public ResponseEntity<Accepted> request(@Valid @RequestBody EmailRequest body, HttpServletRequest request) {
        requireEnabled(); requests.submit(body.email(), request.getRemoteAddr());
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore()).body(new Accepted("If an eligible account exists, a reset link will be emailed."));
    }
    @PostMapping("/reset") public ResponseEntity<Void> reset(@Valid @RequestBody ResetRequest body) {
        requireEnabled(); recovery.reset(new RecoverPasswordUseCase.Reset(body.token(), body.newPassword(), body.confirmPassword()));
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
    private void requireEnabled() {
        if (!requests.enabled()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Email recovery is not configured");
    }
}
