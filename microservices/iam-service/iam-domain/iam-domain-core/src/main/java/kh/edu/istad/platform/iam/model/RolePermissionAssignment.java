package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.exception.IamDomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RolePermissionAssignment {
    private final Long id;
    private final UUID roleUuid;
    private final UUID permissionUuid;
    private final Instant assignedAt;
    private final String assignedBy;
    private final Instant expiresAt;
    private Instant revokedAt;
    private String revokedBy;
    private String reason;

    private RolePermissionAssignment(Long id, UUID roleUuid, UUID permissionUuid,
                                     Instant assignedAt, String assignedBy, Instant expiresAt,
                                     Instant revokedAt, String revokedBy, String reason) {
        this.id = id;
        this.roleUuid = Objects.requireNonNull(roleUuid, "roleUuid");
        this.permissionUuid = Objects.requireNonNull(permissionUuid, "permissionUuid");
        this.assignedAt = Objects.requireNonNull(assignedAt, "assignedAt");
        this.assignedBy = actor(assignedBy);
        if (expiresAt != null && !expiresAt.isAfter(assignedAt)) invalid("Expiry must be after assignment time");
        if (reason != null && reason.length() > 255) invalid("Reason must not exceed 255 characters");
        if ((revokedAt == null) != (revokedBy == null)) invalid("Revocation requires both actor and timestamp");
        if (revokedAt != null) {
            actor(revokedBy);
            if (revokedAt.isBefore(assignedAt)) invalid("Revocation must not precede assignment");
        }
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.revokedBy = revokedBy;
        this.reason = reason;
    }

    public static RolePermissionAssignment assign(Role role, Permission permission,
                                                   String actor, Instant now, Instant expiresAt, String reason) {
        role.requireMutable();
        role.requireAssignable();
        permission.requireAssignable();
        return new RolePermissionAssignment(null, role.getUuid(), permission.getUuid(),
                now, actor, expiresAt, null, null, reason);
    }

    public static RolePermissionAssignment restore(Long id, UUID roleUuid, UUID permissionUuid,
                                                    Instant assignedAt, String assignedBy, Instant expiresAt,
                                                    Instant revokedAt, String revokedBy, String reason) {
        return new RolePermissionAssignment(Objects.requireNonNull(id, "id"), roleUuid, permissionUuid,
                assignedAt, assignedBy, expiresAt, revokedAt, revokedBy, reason);
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && !now.isBefore(assignedAt) && (expiresAt == null || expiresAt.isAfter(now));
    }

    public void revoke(String actor, Instant now, String reason) {
        if (revokedAt != null) throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION,
                "Assignment is already revoked");
        String validActor = actor(actor);
        Objects.requireNonNull(now, "now");
        if (now.isBefore(assignedAt)) invalid("Revocation must not precede assignment");
        if (reason != null && reason.length() > 255) invalid("Reason must not exceed 255 characters");
        this.revokedAt = now;
        this.revokedBy = validActor;
        this.reason = reason;
    }

    private static String actor(String value) {
        if (value == null || value.isBlank() || value.length() > 100 || !value.equals(value.strip())) invalid("Invalid actor");
        return value;
    }
    private static void invalid(String message) {
        throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message);
    }
    public Long getId() { return id; }
    public UUID getRoleUuid() { return roleUuid; }
    public UUID getPermissionUuid() { return permissionUuid; }
    public Instant getAssignedAt() { return assignedAt; }
    public String getAssignedBy() { return assignedBy; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getRevokedBy() { return revokedBy; }
    public String getReason() { return reason; }
}
