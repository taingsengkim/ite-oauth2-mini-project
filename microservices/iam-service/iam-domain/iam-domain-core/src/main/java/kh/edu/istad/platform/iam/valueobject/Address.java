package kh.edu.istad.platform.iam.valueobject;

import kh.edu.istad.platform.iam.exception.IamDomainException;

import java.util.Locale;
import java.util.Set;

public record Address(
        String streetAddress,
        String locality,
        String region,
        String postalCode,
        String country
) {
    public Address {
        streetAddress = optional(streetAddress, "streetAddress", 255);
        locality = optional(locality, "locality", 100);
        region = optional(region, "region", 100);
        postalCode = optional(postalCode, "postalCode", 20);
        country = optional(country, "country", 2);

        if (country != null) {
            country = country.toUpperCase(Locale.ROOT);

            if (!Set.of(Locale.getISOCountries()).contains(country)) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Country must be a valid two-letter country code"
                );
            }
        }
    }

    private static String optional(
            String value, String field, int max) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.strip();

        if (normalized.length() > max) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, field + " must not exceed " + max + " characters");
        }

        return normalized;
    }

}