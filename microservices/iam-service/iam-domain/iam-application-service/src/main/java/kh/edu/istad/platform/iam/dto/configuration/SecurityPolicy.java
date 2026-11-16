package kh.edu.istad.platform.iam.dto.configuration;

public record SecurityPolicy(int maxFailedLoginAttempts, int lockoutDurationSeconds,
        int passwordMinLength, int passwordMaxLength, boolean passwordRequireUppercase,
        boolean passwordRequireLowercase, boolean passwordRequireDigit, boolean passwordRequireSpecial,
        int sessionIdleTimeoutSeconds, long defaultAccessTokenTtlSeconds, long defaultRefreshTokenTtlSeconds) {}
