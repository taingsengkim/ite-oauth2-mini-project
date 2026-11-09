package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.exception.IamDomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
public final class Permission {
    private final UUID uuid;
    private String resource;
    private String action;
    private final boolean system;
    private final Instant createdAt;
    private final String createdBy;
    private String description;
    private boolean active = true;
    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;
    private Instant updatedAt;
    private String updatedBy;

    private Permission(String resource, String action, String description,
                       boolean system, String actor, Instant now) {
        this(UUID.randomUUID(), resource, action, description, system, true, false,
                now, actor, now, actor, null, null);
    }

    private Permission(UUID uuid, String resource, String action, String description,
                       boolean system, boolean active, boolean deleted,
                       Instant createdAt, String createdBy, Instant updatedAt,
                       String updatedBy, Instant deletedAt, String deletedBy) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.resource = required(resource, "resource", 50);
        this.action = required(action, "action", 50);
        if (resource.contains(":")) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Resource must not contain ':'");
        }
        if (getName().length() > 100) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Permission name must not exceed 100 characters");
        }
        this.description = validDescription(description);
        this.createdBy = required(createdBy, "createdBy", 100);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedBy = required(updatedBy, "updatedBy", 100);
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        if (updatedAt.isBefore(createdAt)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Update time must not precede creation time");
        }
        this.system = system;
        this.active = active;
        this.deleted = deleted;
        if (deleted) {
            if (active) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "A deleted permission cannot be active");
            }
            this.deletedAt = Objects.requireNonNull(deletedAt, "deletedAt");
            this.deletedBy = required(deletedBy, "deletedBy", 100);
            if (deletedAt.isBefore(createdAt) || deletedAt.isAfter(updatedAt)) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Deletion time must be between creation and last update");
            }
        } else if (deletedAt != null || deletedBy != null) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "A non-deleted permission cannot have deletion metadata");
        }
    }

    /** Rebuilds stored state without generating a new identity or audit history. */
    public static Permission restore(UUID uuid, String resource, String action,
                                     String description, boolean system, boolean active,
                                     boolean deleted, Instant createdAt, String createdBy,
                                     Instant updatedAt, String updatedBy,
                                     Instant deletedAt, String deletedBy) {
        return new Permission(uuid, resource, action, description, system, active, deleted,
                createdAt, createdBy, updatedAt, updatedBy, deletedAt, deletedBy);
    }

    public static Permission create(String resource, String action, String description,
                                    String actor, Instant now) {
        return new Permission(resource, action, description, false, actor, now);
    }

    public static Permission createSystem(String resource, String action, String description,
                                          String actor, Instant now) {
        return new Permission(resource, action, description, true, actor, now);
    }

    public void update(
            String resource,
            String action,
            String description,
            String actor,
            Instant now) {

        String nextResource = resource == null
                ? this.resource
                : required(resource, "resource", 50);

        String nextAction = action == null
                ? this.action
                : required(action, "action", 50);

        String nextDescription = description == null
                ? this.description
                : validDescription(description);

        if (nextResource.contains(":")) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Resource must not contain ':'"
            );
        }

        if ((nextResource + ":" + nextAction).length() > 100) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Permission name must not exceed 100 characters"
            );
        }
        prepareChange(actor, now);

        this.resource = nextResource;
        this.action = nextAction;
        this.description = nextDescription;
    }


    public void updateDescription(String description, String actor, Instant now) {
        String validated = validDescription(description);
        prepareChange(actor, now);
        this.description = validated;
    }

    public void activate(String actor, Instant now) {
        prepareChange(actor, now);
        active = true;
    }

    public void deactivate(String actor, Instant now) {
        prepareChange(actor, now);
        active = false;
    }

    public void delete(String actor, Instant now) {
        prepareChange(actor, now);
        deleted = true;
        active = false;
        deletedAt = now;
        deletedBy = actor;
    }

    public void requireAssignable() {
        if (deleted || !active) {
            throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "Only active, non-deleted permissions can be assigned");
        }
    }

    private void prepareChange(String actor, Instant now) {
        if (system) {
            throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "System permissions cannot be changed or deleted");
        }
        if (deleted) {
            throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "Deleted permissions cannot be changed");
        }
        String validatedActor = required(actor, "actor", 100);
        Objects.requireNonNull(now, "now");
        if (now.isBefore(updatedAt)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Change time must not precede the last update");
        }
        updatedBy = validatedActor;
        updatedAt = now;
    }

    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must contain 1 to " + max + " characters");
        }
        if (!value.equals(value.strip())) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must not have surrounding whitespace");
        }
        return value;
    }

    private static String validDescription(String value) {
        if (value != null && value.length() > 255) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Description must not exceed 255 characters");
        }
        return value;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return resource + ":" + action; }
    public String getResource() { return resource; }
    public String getAction() { return action; }
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
