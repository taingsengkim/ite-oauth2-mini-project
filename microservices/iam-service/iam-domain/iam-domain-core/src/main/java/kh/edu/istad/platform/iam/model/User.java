package kh.edu.istad.platform.iam.model;

import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.valueobject.Address;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
public final class User {

    private UUID uuid;
    private String username;
    private String email;
    private boolean emailVerified;
    private String passwordHash;

    private String givenName;
    private String familyName;
    private String middleName;
    private String nickname;
    private String gender;
    private LocalDate birthdate;

    private String profileUrl;
    private String pictureUrl;
    private String websiteUrl;

    private String zoneinfo;
    private String locale;

    private String phoneNumber;
    private boolean phoneNumberVerified;
    private Address address;

    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    private int failedLoginAttempts;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;

    private boolean deleted;
    private Instant deletedAt;
    private String deletedBy;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getZoneinfo() {
        return zoneinfo;
    }

    public String getLocale() {
        return locale;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
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

    public static User createLocal(String username, String email,
                                   String passwordHash, String givenName,
                                   String familyName, String locale,
                                   String actor, Instant now) {

        User user = new User();

        user.uuid = UUID.randomUUID();
        user.username = required(username, "username", 50);
        user.email = required(email, "email", 255)
                .toLowerCase(java.util.Locale.ROOT);
        user.passwordHash = required(passwordHash, "passwordHash", 255);
        user.givenName = required(givenName, "givenName", 100);
        user.familyName = required(familyName, "familyName", 100);

        if (!"en".equals(locale) && !"km".equals(locale)) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Locale must be en or km"
            );
        }

        user.locale = locale;

        String validActor = required(actor, "actor", 100);
        Instant validTime = Objects.requireNonNull(now, "now");

        user.enabled = true;
        user.accountNonExpired = true;
        user.accountNonLocked = true;
        user.credentialsNonExpired = true;

        user.passwordChangedAt = validTime;
        user.createdAt = validTime;
        user.updatedAt = validTime;
        user.createdBy = validActor;
        user.updatedBy = validActor;

        return user;
    }
    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank()
                || value.length() > max
                || !value.equals(value.strip())) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must contain 1 to " + max + " characters without surrounding whitespace");
        }

        return value;
    }


    private void touch(String actor, Instant now) {
        if (deleted) {
            throw new IamDomainException(IamDomainException.Code.RULE_VIOLATION, "Deleted users cannot be changed");
        }

        String validActor = required(actor, "actor", 100);
        Objects.requireNonNull(now, "now");

        if (now.isBefore(updatedAt)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Change time must not precede the last update");
        }

        updatedBy = validActor;
        updatedAt = now;
    }


    public void enable(String actor, Instant now) {
        touch(actor, now);
        enabled = true;
    }

    public void disable(String actor, Instant now) {
        touch(actor, now);
        enabled = false;
    }

    public void lock(String actor, Instant now) {
        touch(actor, now);
        accountNonLocked = false;
    }

    public void unlock(String actor, Instant now) {
        touch(actor, now);
        accountNonLocked = true;
        failedLoginAttempts = 0;
    }
    public void delete(String actor, Instant now) {
        touch(actor, now);
        deleted = true;
        enabled = false;
        deletedAt = now;
        deletedBy = actor;
    }

    public void changePasswordHash(
            String passwordHash,
            String actor,
            Instant now) {

        String validatedHash = required(
                passwordHash, "passwordHash", 255
        );

        touch(actor, now);

        this.passwordHash = validatedHash;
        passwordChangedAt = now;
        credentialsNonExpired = true;
    }

    public void recordSuccessfulLogin(String actor, Instant now) {
        touch(actor, now);
        failedLoginAttempts = 0;
        lastLoginAt = now;
    }

    public void recordFailedLogin(String actor, Instant now) {
        touch(actor, now);

        if (failedLoginAttempts < Integer.MAX_VALUE) {
            failedLoginAttempts++;
        }
    }

    private static String optional(String value, String field, int max) {

        if (value.length() > max) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must not exceed " + max + " characters");
        }

        return value.isBlank() ? null : value.strip();
    }

    public void updateProfile(String givenName, String familyName, String nickname, String locale, String phoneNumber, String actor, Instant now) {

        String nextGivenName = givenName == null
                ? this.givenName
                : required(givenName, "givenName", 100);

        String nextFamilyName = familyName == null
                ? this.familyName
                : required(familyName, "familyName", 100);

        String nextNickname = nickname == null
                ? this.nickname
                : optional(nickname, "nickname", 100);

        String nextLocale = locale == null ? this.locale : locale;

        if (!"en".equals(nextLocale) && !"km".equals(nextLocale)) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR, "Locale must be en or km"
            );
        }

        String nextPhoneNumber = phoneNumber == null
                ? this.phoneNumber
                : optional(phoneNumber, "phoneNumber", 20);

        if (nextPhoneNumber != null
                && !nextPhoneNumber.matches("\\+[1-9]\\d{1,14}")) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR, "Phone number must use international format"
            );
        }

        boolean phoneChanged = !Objects.equals(this.phoneNumber, nextPhoneNumber);

        touch(actor, now);

        this.givenName = nextGivenName;
        this.familyName = nextFamilyName;
        this.nickname = nextNickname;
        this.locale = nextLocale;
        this.phoneNumber = nextPhoneNumber;

        if (phoneChanged) {
            phoneNumberVerified = false;
        }
    }

    public void updateAddress(
            Address address,
            String actor,
            Instant now) {

        touch(actor, now);
        this.address = address;
    }


    public void updatePersonalDetails(
            String middleName,
            String gender,
            LocalDate birthdate,
            String zoneinfo,
            String actor,
            Instant now) {

        Objects.requireNonNull(now, "now");

        String nextMiddleName = middleName == null
                ? this.middleName
                : optional(middleName, "middleName", 100);

        String nextGender = gender == null
                ? this.gender
                : optional(gender, "gender", 20);

        if (nextGender != null
                && !Set.of("female", "male", "other").contains(nextGender)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Gender must be female, male or other");
        }

        LocalDate nextBirthdate = birthdate == null
                ? this.birthdate
                : birthdate;

        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);

        if (nextBirthdate != null && nextBirthdate.isAfter(today)) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Birthdate must not be in the future"
            );
        }

        String nextZoneinfo = zoneinfo == null
                ? this.zoneinfo
                : optional(zoneinfo, "zoneinfo", 50);

        if (nextZoneinfo != null
                && !ZoneId.getAvailableZoneIds().contains(nextZoneinfo)) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Timezone must be a recognized timezone ID"
            );
        }

        touch(actor, now);

        this.middleName = nextMiddleName;
        this.gender = nextGender;
        this.birthdate = nextBirthdate;
        this.zoneinfo = nextZoneinfo;
    }


    public void updateUrls(
            String profileUrl,
            String pictureUrl,
            String websiteUrl,
            String actor,
            Instant now) {

        String nextProfileUrl = profileUrl == null
                ? this.profileUrl
                : validUrl(profileUrl, "profileUrl");

        String nextPictureUrl = pictureUrl == null
                ? this.pictureUrl
                : validUrl(pictureUrl, "pictureUrl");

        String nextWebsiteUrl = websiteUrl == null
                ? this.websiteUrl
                : validUrl(websiteUrl, "websiteUrl");

        touch(actor, now);

        this.profileUrl = nextProfileUrl;
        this.pictureUrl = nextPictureUrl;
        this.websiteUrl = nextWebsiteUrl;
    }


    private static String validUrl(String value, String field) {
        String normalized = optional(value, field, 500);

        if (normalized == null) {
            return null;
        }

        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if ((!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must be an absolute HTTP or HTTPS URL");
            }
            return normalized;
        } catch (URISyntaxException exception) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must be a valid URL");
        }
    }


    public static User restore(UserSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");

        User user = new User();

        user.uuid = Objects.requireNonNull(snapshot.uuid(), "uuid");
        user.username = required(snapshot.username(), "username", 50);
        user.email = required(snapshot.email(), "email", 255).toLowerCase(java.util.Locale.ROOT);
        user.emailVerified = snapshot.emailVerified();
        user.passwordHash = snapshot.passwordHash() == null ? null : required(snapshot.passwordHash(), "passwordHash", 255);

        user.givenName = required(snapshot.givenName(), "givenName", 100);
        user.familyName = required(snapshot.familyName(), "familyName", 100);
        user.middleName = nullableText(snapshot.middleName(), "middleName", 100);
        user.nickname = nullableText(snapshot.nickname(), "nickname", 100);
        user.gender = nullableText(snapshot.gender(), "gender", 20);
        user.birthdate = snapshot.birthdate();

        if (user.gender != null && !Set.of("female", "male", "other").contains(user.gender)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Gender must be female, male or other");
        }

        user.profileUrl = snapshot.profileUrl() == null ? null : validUrl(snapshot.profileUrl(), "profileUrl");
        user.pictureUrl = snapshot.pictureUrl() == null ? null : validUrl(snapshot.pictureUrl(), "pictureUrl");
        user.websiteUrl = snapshot.websiteUrl() == null ? null : validUrl(snapshot.websiteUrl(), "websiteUrl");

        user.zoneinfo = nullableText(snapshot.zoneinfo(), "zoneinfo", 50);

        if (user.zoneinfo != null && !ZoneId.getAvailableZoneIds().contains(user.zoneinfo)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Timezone must be a recognized timezone ID");
        }

        user.locale = snapshot.locale();

        if (!"en".equals(user.locale) && !"km".equals(user.locale)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Locale must be en or km");
        }

        user.phoneNumber = nullableText(snapshot.phoneNumber(), "phoneNumber", 20);

        if (user.phoneNumber != null && !user.phoneNumber.matches("\\+[1-9]\\d{1,14}")) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Phone number must use international format");
        }
        user.phoneNumberVerified = snapshot.phoneNumberVerified();
        user.address = snapshot.address();

        user.enabled = snapshot.enabled();
        user.accountNonExpired = snapshot.accountNonExpired();
        user.accountNonLocked = snapshot.accountNonLocked();
        user.credentialsNonExpired = snapshot.credentialsNonExpired();

        if (snapshot.failedLoginAttempts() < 0) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Failed login attempts must not be negative");
        }

        user.failedLoginAttempts = snapshot.failedLoginAttempts();
        user.lastLoginAt = snapshot.lastLoginAt();
        user.passwordChangedAt = snapshot.passwordChangedAt();

        user.createdAt = Objects.requireNonNull(snapshot.createdAt(), "createdAt");
        user.createdBy = required(snapshot.createdBy(), "createdBy", 100);
        user.updatedAt = Objects.requireNonNull(snapshot.updatedAt(), "updatedAt");
        user.updatedBy = required(snapshot.updatedBy(), "updatedBy", 100);

        if (user.updatedAt.isBefore(user.createdAt)) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Update time must not precede creation time");
        }

        if (user.birthdate != null && user.birthdate.isAfter(LocalDate.ofInstant(user.updatedAt, ZoneOffset.UTC))) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Birthdate must not be after the saved update date");
        }

        user.deleted = snapshot.deleted();

        if (user.deleted) {
            if (user.enabled) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "A deleted user cannot be enabled");
            }

            user.deletedAt = Objects.requireNonNull(snapshot.deletedAt(), "deletedAt");
            user.deletedBy = required(snapshot.deletedBy(), "deletedBy", 100);

            if (user.deletedAt.isBefore(user.createdAt) || user.deletedAt.isAfter(user.updatedAt)) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Invalid deletion time");
            }
        } else if (snapshot.deletedAt() != null || snapshot.deletedBy() != null) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "A non-deleted user cannot have deletion metadata");
        }

        return user;
    }

    private static String nullableText(String value, String field, int max) {
        return value == null ? null : optional(value, field, max);
    }

    public static User createFederated(
            String username,
            String email,
            boolean emailVerified,
            String givenName,
            String familyName,
            String locale,
            String actor,
            Instant now
    ) {
        User user = new User();

        user.uuid = UUID.randomUUID();
        user.username = required(username, "username", 50);
        user.email = required(email, "email", 255)
                .toLowerCase(java.util.Locale.ROOT);
        user.emailVerified = emailVerified;

        user.givenName = required(givenName, "givenName", 100);
        user.familyName = required(familyName, "familyName", 100);

        if (!"en".equals(locale) && !"km".equals(locale)) {
            throw new IamDomainException(
                    IamDomainException.Code.VALIDATION_ERROR,
                    "Locale must be en or km"
            );
        }

        user.locale = locale;

        String validActor = required(actor, "actor", 100);
        Instant validTime = Objects.requireNonNull(now, "now");

        user.enabled = true;
        user.accountNonExpired = true;
        user.accountNonLocked = true;
        user.credentialsNonExpired = true;

        user.createdAt = validTime;
        user.updatedAt = validTime;
        user.createdBy = validActor;
        user.updatedBy = validActor;

        return user;
    }


}