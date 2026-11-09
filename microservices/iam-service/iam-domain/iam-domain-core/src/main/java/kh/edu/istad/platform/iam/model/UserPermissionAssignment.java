package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.exception.IamDomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class UserPermissionAssignment {

    private final Long id;
    private final UUID userUuid;
    private final UUID permissionUuid;
    private final PermissionEffect effect;
    private final Instant assignedAt;
    private final String assignedBy;
    private final Instant expiresAt;

    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    private UserPermissionAssignment(
            Long id,
            UUID userUuid,
            UUID permissionUuid,
            PermissionEffect effect,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason) {

        this.id = id;
        this.userUuid = Objects.requireNonNull(userUuid, "userUuid");
        this.permissionUuid = Objects.requireNonNull(
                permissionUuid, "permissionUuid"
        );
        this.effect = Objects.requireNonNull(effect, "effect");
        this.assignedAt = Objects.requireNonNull(
                assignedAt, "assignedAt"
        );
        this.assignedBy = validActor(assignedBy);

        if (expiresAt != null && !expiresAt.isAfter(assignedAt)) {
            invalid("Expiry must be after assignment time");
        }

        validateReason(reason);

        if ((revokedAt == null) != (revokedBy == null)) {
            invalid("Revocation requires both actor and timestamp");
        }

        if (revokedAt != null) {
            validActor(revokedBy);

            if (revokedAt.isBefore(assignedAt)) {
                invalid("Revocation must not precede assignment");
            }
        }

        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokedBy = revokedBy;
        this.reason = reason;
    }

    public static UserPermissionAssignment assign(
            User user,
            Permission permission,
            PermissionEffect effect,
            String actor,
            Instant now,
            Instant expiresAt,
            String reason) {

        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(permission, "permission");

        if (user.isDeleted()) {
            throw new IamDomainException(
                    IamDomainException.Code.RULE_VIOLATION,
                    "Cannot assign permissions to a deleted user"
            );
        }

        permission.requireAssignable();

        return new UserPermissionAssignment(
                null,
                user.getUuid(),
                permission.getUuid(),
                effect,
                now,
                actor,
                expiresAt,
                null,
                null,
                reason
        );
    }

    public static UserPermissionAssignment restore(
            Long id,
            UUID userUuid,
            UUID permissionUuid,
            PermissionEffect effect,
            Instant assignedAt,
            String assignedBy,
            Instant expiresAt,
            Instant revokedAt,
            String revokedBy,
            String reason) {

        return new UserPermissionAssignment(
                Objects.requireNonNull(id, "id"),
                userUuid,
                permissionUuid,
                effect,
                assignedAt,
                assignedBy,
                expiresAt,
                revokedAt,
                revokedBy,
                reason
        );
    }

    public boolean isActive(Instant now) {
        Objects.requireNonNull(now, "now");

        return revokedAt == null
                && !now.isBefore(assignedAt)
                && (expiresAt == null || expiresAt.isAfter(now));
    }

    public void revoke(String actor, Instant now, String reason) {
        if (revokedAt != null) {
            throw new IamDomainException(
                    IamDomainException.Code.RULE_VIOLATION,
                    "Assignment is already revoked"
            );
        }

        String validatedActor = validActor(actor);
        Objects.requireNonNull(now, "now");

        if (now.isBefore(assignedAt)) {
            invalid("Revocation must not precede assignment");
        }

        validateReason(reason);

        revokedAt = now;
        revokedBy = validatedActor;
        this.reason = reason;
    }

    private static String validActor(String value) {
        if (value == null || value.isBlank()
                || value.length() > 100
                || !value.equals(value.strip())) {
            invalid("Invalid actor");
        }

        return value;
    }

    private static void validateReason(String value) {
        if (value != null && value.length() > 255) {
            invalid("Reason must not exceed 255 characters");
        }
    }

    private static void invalid(String message) {
        throw new IamDomainException(
                IamDomainException.Code.VALIDATION_ERROR,
                message
        );
    }

    public Long getId() { return id; }
    public UUID getUserUuid() { return userUuid; }
    public UUID getPermissionUuid() { return permissionUuid; }
    public PermissionEffect getEffect() { return effect; }
    public Instant getAssignedAt() { return assignedAt; }
    public String getAssignedBy() { return assignedBy; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getRevokedBy() { return revokedBy; }
    public String getReason() { return reason; }
}
