package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.audit.*;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.port.out.user.*;
import kh.edu.istad.platform.iam.port.out.user.PasswordRecoveryMail.Delivery;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import kh.edu.istad.platform.iam.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecoverPasswordUseCase {
    private final UserRepository users;
    private final PasswordResetRepository tokens;
    private final PasswordHasher passwords;
    private final PasswordPolicy policy;
    private final LoginLockoutRepository lockouts;
    private final ClientConsentRepository consents;
    private final UserSessions sessions;
    private final Clock clock;
    private static final SecureRandom RANDOM = new SecureRandom();
    public record Reset(String token, String newPassword, String confirmPassword) {
        @Override public String toString() { return "PasswordRecoveryReset[REDACTED]"; }
    }
    @Transactional
    public Delivery request(String email, String ip) {
        var now = clock.instant();
        String normalized = email.strip().toLowerCase(Locale.ROOT);
        // Keys are hashed so the rate-limit table stores neither email nor IP address.
        boolean ipAllowed = tokens.allow(digest("ip:" + ip), 10, Duration.ofMinutes(15), now);
        boolean emailAllowed = tokens.allow(digest("email:" + normalized), 3, Duration.ofMinutes(15), now);
        if (!ipAllowed || !emailAllowed) return null;
        var candidate = users.findByEmail(normalized);
        if (candidate.isEmpty()) return null;
        var locked = users.findByUuidForUpdate(candidate.get().getUuid());
        if (locked.isEmpty()) return null;
        var user = locked.get();
        if (!eligible(user) || !normalized.equals(user.getEmail()) || tokens.recentlyIssued(user.getUuid(), now.minusSeconds(60))) return null;
        byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.issue(new PasswordResetRepository.Token(digest(raw), user.getUuid(), now, now.plus(Duration.ofMinutes(15))));
        return new Delivery(user.getEmail(), raw, user.getLocale());
    }
    @Transactional
    @Audited(action = AuditAction.ACCOUNT_PASSWORD_RECOVERED, target = "#result")
    public UUID reset(Reset command) {
        if (command.token() == null || !command.token().matches("[A-Za-z0-9_-]{43}")) throw invalid();
        policy.validate(command.newPassword(), command.confirmPassword());
        String hash = digest(command.token());
        var initial = tokens.find(hash, false).orElseThrow(RecoverPasswordUseCase::invalid);
        // All credential writers lock the user first, then invalidate its reset rows.
        var user = users.findByUuidForUpdate(initial.userUuid()).orElseThrow(RecoverPasswordUseCase::invalid);
        var token = tokens.find(hash, true).orElseThrow(RecoverPasswordUseCase::invalid);
        if (!token.userUuid().equals(user.getUuid()) || !token.expiresAt().isAfter(clock.instant()) || !eligible(user)) throw invalid();
        if (passwords.matches(command.newPassword(), user.getPasswordHash()))
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "New password must differ from the current password");
        user.changePasswordHash(passwords.hash(command.newPassword()), "password-recovery", clock.instant());
        tokens.revokeUser(user.getUuid());
        users.updatePassword(user);
        lockouts.clear(user.getUuid());
        consents.revokeAllForPrincipal(user.getUsername());
        sessions.terminateAll(user.getUuid());
        return user.getUuid();
    }
    private static boolean eligible(User user) {
        return user.isEnabled() && user.isAccountNonExpired() && !user.isDeleted()
                && user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
    }
    public static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    private static IamDomainException invalid() {
        return new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Reset link is invalid or expired");
    }
}
