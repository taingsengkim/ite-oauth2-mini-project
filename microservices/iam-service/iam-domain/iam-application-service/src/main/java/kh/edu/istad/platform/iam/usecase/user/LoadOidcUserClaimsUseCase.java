package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoadOidcUserClaimsUseCase {
    private final UserRepository users;

    @Transactional(readOnly = true)
    public Map<String, Object> execute(UUID userUuid, Set<String> scopes) {
        var user = users.findByUuid(userUuid).orElseThrow(AuthenticationFailedException::new);
        if (!user.isEnabled() || !user.isAccountNonExpired() || !user.isAccountNonLocked()
                || !user.isCredentialsNonExpired()) {
            throw new AuthenticationFailedException();
        }
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", user.getUuid().toString());
        if (scopes.contains("profile")) {
            String middle = user.getMiddleName() == null || user.getMiddleName().isBlank()
                    ? "" : user.getMiddleName() + " ";
            claims.put("name", user.getGivenName() + " " + middle + user.getFamilyName());
            put(claims, "given_name", user.getGivenName());
            put(claims, "family_name", user.getFamilyName());
            put(claims, "middle_name", user.getMiddleName());
            put(claims, "nickname", user.getNickname());
            put(claims, "preferred_username", user.getUsername());
            put(claims, "profile", user.getProfileUrl());
            put(claims, "picture", user.getPictureUrl());
            put(claims, "website", user.getWebsiteUrl());
            put(claims, "gender", user.getGender());
            put(claims, "birthdate", user.getBirthdate() == null ? null : user.getBirthdate().toString());
            put(claims, "zoneinfo", user.getZoneinfo());
            put(claims, "locale", user.getLocale());
            claims.put("updated_at", user.getUpdatedAt().getEpochSecond());
        }
        if (scopes.contains("email")) {
            put(claims, "email", user.getEmail());
            claims.put("email_verified", user.isEmailVerified());
        }
        if (scopes.contains("phone") && user.getPhoneNumber() != null) {
            put(claims, "phone_number", user.getPhoneNumber());
            claims.put("phone_number_verified", user.isPhoneNumberVerified());
        }
        if (scopes.contains("address") && user.getAddress() != null) {
            var address = user.getAddress();
            Map<String, Object> fields = new LinkedHashMap<>();
            put(fields, "street_address", address.streetAddress());
            put(fields, "locality", address.locality());
            put(fields, "region", address.region());
            put(fields, "postal_code", address.postalCode());
            put(fields, "country", address.country());
            if (!fields.isEmpty()) claims.put("address", new LinkedHashMap<>(fields));
        }
        return Map.copyOf(claims);
    }

    private static void put(Map<String, Object> values, String key, Object value) {
        if (value != null && (!(value instanceof String text) || !text.isBlank())) values.put(key, value);
    }
}
