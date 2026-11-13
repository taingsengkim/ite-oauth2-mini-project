package kh.edu.istad.platform.iam.usecase.identity;

import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.valueobject.Address;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Fills missing local details without replacing user-edited profile fields. */
@Component
public class FederatedUserProfileSync {
    public void apply(User user, FederatedIdentityCommand command, String previousPicture,
                      String actor, Instant now) {
        var attributes = command.attributes();
        String nickname = missing(user.getNickname()) ? text(attributes, "nickname", 100) : null;
        if (missing(user.getNickname()) && nickname == null) nickname = text(attributes, "preferred_username", 100);
        String phone = missing(user.getPhoneNumber()) ? text(attributes, "phone_number", 20) : null;
        if (phone != null && !phone.matches("\\+[1-9]\\d{1,14}")) phone = null;
        user.updateProfile(null, null, nickname, null, phone, actor, now);

        String middle = missing(user.getMiddleName()) ? text(attributes, "middle_name", 100) : null;
        String gender = user.getGender() == null ? text(attributes, "gender", 20) : null;
        if (gender != null && !Set.of("male", "female", "other").contains(gender)) gender = null;
        String zone = user.getZoneinfo() == null ? text(attributes, "zoneinfo", 50) : null;
        if (zone != null && !ZoneId.getAvailableZoneIds().contains(zone)) zone = null;
        LocalDate birthdate = null;
        String date = text(attributes, "birthdate", 10);
        if (user.getBirthdate() == null && date != null) {
            try {
                var parsed = LocalDate.parse(date);
                if (parsed.getYear() > 0 && !parsed.isAfter(LocalDate.ofInstant(now, ZoneOffset.UTC))) birthdate = parsed;
            } catch (java.time.format.DateTimeParseException ignored) {
                // Partial OIDC birthdates cannot be stored in a SQL DATE column.
            }
        }
        user.updatePersonalDetails(middle, gender, birthdate, zone, actor, now);

        String picture = missing(user.getPictureUrl()) || Objects.equals(user.getPictureUrl(), previousPicture)
                ? url(command.pictureUrl()) : null;
        user.updateUrls(
                missing(user.getProfileUrl()) ? url(text(attributes, "profile", 500)) : null,
                picture,
                missing(user.getWebsiteUrl()) ? url(text(attributes, "website", 500)) : null,
                actor, now);

        if (attributes.get("address") instanceof Map<?, ?> address) {
            var old = user.getAddress();
            String country = prefer(old == null ? null : old.country(), text(address, "country", 2));
            if (country != null && !Set.of(Locale.getISOCountries()).contains(country.toUpperCase(Locale.ROOT))) {
                country = old == null ? null : old.country();
            }
            var merged = new Address(
                    prefer(old == null ? null : old.streetAddress(), text(address, "street_address", 255)),
                    prefer(old == null ? null : old.locality(), text(address, "locality", 100)),
                    prefer(old == null ? null : old.region(), text(address, "region", 100)),
                    prefer(old == null ? null : old.postalCode(), text(address, "postal_code", 20)), country);
            if (merged.streetAddress() != null || merged.locality() != null || merged.region() != null
                    || merged.postalCode() != null || merged.country() != null) {
                user.updateAddress(merged, actor, now);
            }
        }
    }

    public static String normalizeLocale(String locale) {
        if (locale == null) return "en";
        String language = Locale.forLanguageTag(locale.replace('_', '-')).getLanguage();
        return "km".equals(language) ? "km" : "en";
    }

    private static String text(Map<?, ?> attributes, String key, int max) {
        Object value = attributes.get(key);
        if (!(value instanceof String text) || text.isBlank() || text.strip().length() > max) return null;
        return text.strip();
    }

    private static String url(String value) {
        if (missing(value) || value.strip().length() > 500) return null;
        try {
            var uri = URI.create(value.strip());
            return ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null ? uri.toString() : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static boolean missing(String value) {
        return value == null || value.isBlank();
    }

    private static String prefer(String existing, String supplied) {
        return missing(existing) ? supplied : existing;
    }
}
