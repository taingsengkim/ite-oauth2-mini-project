package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.exception.IamDomainException;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class Identity {

    private Identity() {
    }

    private UUID uuid;
    private UUID userUuid;

    private IdentityProvider provider;
    private String providerSubject;

    private String email;
    private boolean emailVerified;
    private String displayName;
    private String username;
    private String pictureUrl;
    private Map<String, Object> attributes;

    private Instant linkedAt;
    private Instant lastLoginAt;

    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public static Identity create(
            UUID userUuid,
            IdentityProvider provider,
            String providerSubject,
            String email,
            boolean emailVerified,
            String displayName,
            String username,
            String pictureUrl,
            Map<String, Object> attributes,
            String actor,
            Instant now
    ) {
        var identity = new Identity();

        identity.uuid = UUID.randomUUID();
        identity.userUuid = Objects.requireNonNull(userUuid, "userUuid");
        identity.provider = Objects.requireNonNull(provider, "provider");
        identity.providerSubject = required(
                providerSubject, "providerSubject", 255
        );

        identity.email = optional(email, "email", 255);
        identity.emailVerified = emailVerified;

        if (emailVerified && (email == null || email.isBlank())) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "A verified email must not be empty"
            );
        }

        identity.displayName = optional(displayName, "displayName", 150);
        identity.username = optional(username, "username", 100);
        identity.pictureUrl = optional(pictureUrl, "pictureUrl", 500);
        identity.attributes = attributes == null
                ? Map.of()
                : java.util.Collections.unmodifiableMap(
                new java.util.LinkedHashMap<>(attributes)
        );

        identity.createdBy = required(actor, "actor", 100);
        identity.updatedBy = identity.createdBy;

        identity.linkedAt = Objects.requireNonNull(now, "now");
        identity.createdAt = now;
        identity.updatedAt = now;
        identity.deleted = false;

        return identity;
    }

    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must contain 1 to " + max + " characters"
            );
        }

        return value;
    }

    private static String optional(String value, String field, int max) {
        if (value != null && value.length() > max) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must not exceed " + max + " characters"
            );
        }

        return value;
    }


    public static Identity restore(IdentitySnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");

        var identity = new Identity();

        identity.uuid = Objects.requireNonNull(snapshot.uuid(), "uuid");
        identity.userUuid = Objects.requireNonNull(
                snapshot.userUuid(), "userUuid"
        );
        identity.provider = Objects.requireNonNull(
                snapshot.provider(), "provider"
        );
        identity.providerSubject = required(
                snapshot.providerSubject(), "providerSubject", 255
        );

        identity.email = optional(snapshot.email(), "email", 255);
        identity.emailVerified = snapshot.emailVerified();

        if (identity.emailVerified
                && (identity.email == null || identity.email.isBlank())) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "A verified email must not be empty"
            );
        }

        identity.displayName = optional(
                snapshot.displayName(), "displayName", 150
        );
        identity.username = optional(snapshot.username(), "username", 100);
        identity.pictureUrl = optional(
                snapshot.pictureUrl(), "pictureUrl", 500
        );
        identity.attributes = snapshot.attributes() == null
                ? Map.of()
                : java.util.Collections.unmodifiableMap(
                new java.util.LinkedHashMap<>(snapshot.attributes())
        );

        identity.linkedAt = Objects.requireNonNull(
                snapshot.linkedAt(), "linkedAt"
        );
        identity.lastLoginAt = snapshot.lastLoginAt();
        identity.createdAt = Objects.requireNonNull(
                snapshot.createdAt(), "createdAt"
        );
        identity.createdBy = required(snapshot.createdBy(), "createdBy", 100);
        identity.updatedAt = Objects.requireNonNull(
                snapshot.updatedAt(), "updatedAt"
        );
        identity.updatedBy = required(snapshot.updatedBy(), "updatedBy", 100);

        identity.deleted = snapshot.deleted();

        if (identity.deleted) {
            identity.deletedAt = Objects.requireNonNull(
                    snapshot.deletedAt(), "deletedAt"
            );
            identity.deletedBy = required(
                    snapshot.deletedBy(), "deletedBy", 100
            );
        } else if (snapshot.deletedAt() != null
                || snapshot.deletedBy() != null) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "A non-deleted identity cannot have deletion metadata"
            );
        }

        if (identity.updatedAt.isBefore(identity.createdAt)
                || identity.linkedAt.isAfter(identity.updatedAt)
                || (identity.lastLoginAt != null
                && (identity.lastLoginAt.isBefore(identity.linkedAt)
                || identity.lastLoginAt.isAfter(identity.updatedAt)))
                || (identity.deletedAt != null
                && (identity.deletedAt.isBefore(identity.linkedAt)
                || identity.deletedAt.isAfter(identity.updatedAt)))) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Identity audit timestamps are inconsistent"
            );
        }

        return identity;
    }

    private void prepareChange(String actor, Instant now) {
        if (deleted) {
            throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "A deleted identity cannot be changed"
            );
        }

        String validatedActor = required(actor, "actor", 100);
        Objects.requireNonNull(now, "now");

        if (now.isBefore(updatedAt)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Change time must not precede the last update"
            );
        }

        updatedBy = validatedActor;
        updatedAt = now;
    }
    public void recordSuccessfulLogin(String actor, Instant now) {
        prepareChange(actor, now);
        lastLoginAt = now;
    }

    public void delete(String actor, Instant now) {
        prepareChange(actor, now);

        deleted = true;
        deletedAt = now;
        deletedBy = updatedBy;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public IdentityProvider getProvider() {
        return provider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }
}
