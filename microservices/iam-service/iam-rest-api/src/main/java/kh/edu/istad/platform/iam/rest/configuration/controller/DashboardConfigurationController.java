package kh.edu.istad.platform.iam.rest.configuration.controller;

import kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository;
import kh.edu.istad.platform.iam.usecase.configuration.ManageSystemConfigurationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DashboardConfigurationController {
    private final ManageSystemConfigurationUseCase configuration;
    @GetMapping("/dashboard-api/configuration")
    public ResponseEntity<SystemConfigurationRepository.Snapshot> get() { return response(configuration.get()); }
    @PutMapping("/dashboard-api/configuration")
    public ResponseEntity<SystemConfigurationRepository.Snapshot> update(@RequestBody ManageSystemConfigurationUseCase.Update request) { return response(configuration.update(request)); }

    public record PasswordRequirements(int minLength, int maxLength, boolean requireUppercase, boolean requireLowercase, boolean requireDigit, boolean requireSpecial) {}
    public record ClientDefaults(long accessTokenTtlSeconds, long refreshTokenTtlSeconds) {}
    @GetMapping("/dashboard-api/password-policy") public ResponseEntity<PasswordRequirements> passwordPolicy() {
        var policy = configuration.get().policy();
        return response(new PasswordRequirements(policy.passwordMinLength(), policy.passwordMaxLength(), policy.passwordRequireUppercase(), policy.passwordRequireLowercase(), policy.passwordRequireDigit(), policy.passwordRequireSpecial()));
    }
    @GetMapping("/dashboard-api/client-defaults") public ResponseEntity<ClientDefaults> clientDefaults() {
        var policy = configuration.get().policy(); return response(new ClientDefaults(policy.defaultAccessTokenTtlSeconds(), policy.defaultRefreshTokenTtlSeconds()));
    }
    private <T> ResponseEntity<T> response(T body) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body); }
}
