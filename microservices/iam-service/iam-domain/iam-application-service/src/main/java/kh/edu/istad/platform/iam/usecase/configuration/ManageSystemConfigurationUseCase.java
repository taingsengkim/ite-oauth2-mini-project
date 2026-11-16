package kh.edu.istad.platform.iam.usecase.configuration;

import kh.edu.istad.platform.iam.audit.*;
import kh.edu.istad.platform.iam.dto.configuration.SecurityPolicy;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
@RequiredArgsConstructor
public class ManageSystemConfigurationUseCase {
    private final SystemConfigurationRepository repository;
    private final CurrentActorProvider actors;
    private final Clock clock;
    public record Update(long version, SecurityPolicy policy) {}
    @Transactional(readOnly = true)
    public SystemConfigurationRepository.Snapshot get() { return repository.current(); }

    @Transactional
    @Audited(action = AuditAction.CONFIGURATION_UPDATED, target = "#result.id()", fieldSource = "#p0.policy()",
        fields = {"maxFailedLoginAttempts", "lockoutDurationSeconds", "passwordMinLength", "passwordMaxLength",
            "passwordRequireUppercase", "passwordRequireLowercase", "passwordRequireDigit", "passwordRequireSpecial",
            "sessionIdleTimeoutSeconds", "defaultAccessTokenTtlSeconds", "defaultRefreshTokenTtlSeconds"})
    public SystemConfigurationRepository.Snapshot update(Update command) {
        require(command != null && command.version() >= 0 && command.policy() != null, "A configuration and version are required");
        var policy = command.policy();
        require(policy.maxFailedLoginAttempts() >= 3 && policy.maxFailedLoginAttempts() <= 20, "Failed sign-in limit must be 3–20");
        require(policy.lockoutDurationSeconds() >= 60 && policy.lockoutDurationSeconds() <= 86400, "Lockout duration must be 60–86400 seconds");
        require(policy.passwordMinLength() >= 8 && policy.passwordMinLength() <= 64, "Minimum password length must be 8–64 characters");
        require(policy.passwordMaxLength() >= policy.passwordMinLength() && policy.passwordMaxLength() <= 72, "Maximum password length must be between the minimum and 72 characters");
        require(policy.sessionIdleTimeoutSeconds() >= 60 && policy.sessionIdleTimeoutSeconds() <= 86400, "Session idle timeout must be 60–86400 seconds");
        require(policy.defaultAccessTokenTtlSeconds() >= 60 && policy.defaultAccessTokenTtlSeconds() <= 86400, "Access token default must be 60–86400 seconds");
        require(policy.defaultRefreshTokenTtlSeconds() >= 300 && policy.defaultRefreshTokenTtlSeconds() <= 2592000
                && policy.defaultRefreshTokenTtlSeconds() >= policy.defaultAccessTokenTtlSeconds(), "Refresh token default must be 300–2592000 seconds and at least the access token lifetime");
        return repository.update(policy, command.version(), actors.currentActor(), clock.instant());
    }
    private void require(boolean condition, String message) {
        if (!condition) throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message);
    }
}
