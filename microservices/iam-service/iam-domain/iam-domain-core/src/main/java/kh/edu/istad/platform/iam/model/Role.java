package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.exception.IamDomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Role {
    private final UUID uuid;
    private String name;
    private String description;
    private final boolean system;
    private boolean active;
    private boolean deleted;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;
    private Instant deletedAt;
    private String deletedBy;

    private Role(UUID uuid, String name, String description, boolean system,
                 boolean active, boolean deleted, Instant createdAt, String createdBy,
                 Instant updatedAt, String updatedBy, Instant deletedAt, String deletedBy) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = required(name, "name", 50);
        this.description = description(description);
        this.system = system;
        this.active = active;
        this.deleted = deleted;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.createdBy = required(createdBy, "createdBy", 100);
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.updatedBy = required(updatedBy, "updatedBy", 100);
        if (updatedAt.isBefore(createdAt)) invalid("Update time must not precede creation time");
        if (deleted) {
            if (active) invalid("A deleted role cannot be active");
            this.deletedAt = Objects.requireNonNull(deletedAt, "deletedAt");
            this.deletedBy = required(deletedBy, "deletedBy", 100);
            if (deletedAt.isBefore(createdAt) || deletedAt.isAfter(updatedAt)) invalid("Invalid deletion time");
        } else if (deletedAt != null || deletedBy != null) {
            invalid("A non-deleted role cannot have deletion metadata");
        }
    }

    public static Role create(String name, String description, String actor, Instant now) {
        return new Role(UUID.randomUUID(), name, description, false, true, false,
                now, actor, now, actor, null, null);
    }

    public static Role createSystem(String name, String description, String actor, Instant now) {
        return new Role(UUID.randomUUID(), name, description, true, true, false,
                now, actor, now, actor, null, null);
    }

    public static Role restore(UUID uuid, String name, String description, boolean system,
                               boolean active, boolean deleted, Instant createdAt, String createdBy,
                               Instant updatedAt, String updatedBy, Instant deletedAt, String deletedBy) {
        return new Role(uuid, name, description, system, active, deleted, createdAt,
                createdBy, updatedAt, updatedBy, deletedAt, deletedBy);
    }

    public void update(String name, String description, String actor, Instant now) {
        String nextName = name == null ? this.name : required(name, "name", 50);
        String nextDescription = description == null ? this.description : description(description);
        touch(actor, now);
        this.name = nextName;
        this.description = nextDescription;
    }

    public void activate(String actor, Instant now) { touch(actor, now); active = true; }
    public void deactivate(String actor, Instant now) { touch(actor, now); active = false; }
    public void delete(String actor, Instant now) {
        touch(actor, now);
        deleted = true;
        active = false;
        deletedAt = now;
        deletedBy = actor;
    }

    public void requireMutable() {
        if (system) rule("System roles cannot be changed or deleted");
        if (deleted) rule("Deleted roles cannot be changed");
    }

    public void requireAssignable() {
        if (!active || deleted) rule("Only active, non-deleted roles can be assigned");
    }

    private void touch(String actor, Instant now) {
        requireMutable();
        String validActor = required(actor, "actor", 100);
        Objects.requireNonNull(now, "now");
        if (now.isBefore(updatedAt)) invalid("Change time must not precede the last update");
        updatedAt = now;
        updatedBy = validActor;
    }

    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank() || value.length() > max || !value.equals(value.strip()))
            invalid(field + " must contain 1 to " + max + " characters without surrounding whitespace");
        return value;
    }

    private static String description(String value) {
        if (value != null && value.length() > 255) invalid("Description must not exceed 255 characters");
        return value;
    }
    private static void invalid(String message) {
        throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message);
    }
    private static void rule(String message) {
        throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, message);
    }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSystem() { return system; }
    public boolean isActive() { return active; }
    public boolean isDeleted() { return deleted; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
}
