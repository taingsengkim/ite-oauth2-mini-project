package kh.edu.istad.platform.iam.infrastructure.security;

import com.maxmind.db.Reader;
import jakarta.annotation.PreDestroy;
import kh.edu.istad.platform.iam.port.out.user.IpLocationResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/** Local lookups only: never sends session IPs to a remote service or performs DNS lookups. */
@Component
public class OfflineIpLocationResolver implements IpLocationResolver {
    private final Reader reader;
    public OfflineIpLocationResolver(@Value("${iam.geoip.database:}") String configured) throws IOException {
        List<Path> candidates = configured.isBlank() ? List.of(Path.of("data/geoip/dbip-city-lite.mmdb"),
                Path.of("../data/geoip/dbip-city-lite.mmdb"), Path.of("microservices/iam-service/data/geoip/dbip-city-lite.mmdb")) : List.of(Path.of(configured));
        var file = candidates.stream().filter(Files::isRegularFile).findFirst();
        if (!configured.isBlank() && file.isEmpty()) throw new IllegalArgumentException("IAM_GEOIP_DATABASE does not point to an existing MMDB file");
        reader = file.isPresent() ? new Reader(file.get().toFile()) : null;
    }
    @Override public Location resolve(String input) {
        if (input == null || input.isBlank()) return empty(Status.UNAVAILABLE);
        InetAddress address;
        try { address = InetAddress.ofLiteral(input); } catch (IllegalArgumentException invalid) { return empty(Status.UNAVAILABLE); }
        if (address.isLoopbackAddress()) return empty(Status.LOCAL);
        byte[] bytes = address.getAddress();
        int first = bytes[0] & 255;
        boolean reserved = bytes.length == 4 ? first == 0 || first >= 224
                || (first == 100 && (bytes[1] & 255) >= 64 && (bytes[1] & 255) <= 127)
                || (first == 198 && ((bytes[1] & 255) == 18 || (bytes[1] & 255) == 19))
                || (first == 192 && bytes[1] == 0 && bytes[2] == 2)
                || (first == 198 && (bytes[1] & 255) == 51 && (bytes[2] & 255) == 100)
                || (first == 203 && bytes[1] == 0 && (bytes[2] & 255) == 113)
                : (first & 0xe0) != 0x20 || (first == 0x20 && (bytes[1] & 255) == 1 && (bytes[2] & 255) == 0x0d && (bytes[3] & 255) == 0xb8);
        if (reserved || address.isAnyLocalAddress() || address.isSiteLocalAddress() || address.isLinkLocalAddress() || address.isMulticastAddress()) return empty(Status.PRIVATE);
        if (reader == null) return empty(Status.UNAVAILABLE);
        try {
            Map<?, ?> value = reader.get(address, Map.class);
            if (value == null) return empty(Status.UNAVAILABLE);
            String country = value.get("country") instanceof Map<?, ?> data ? text(data.get("iso_code")) : null;
            String city = value.get("city") instanceof Map<?, ?> data && data.get("names") instanceof Map<?, ?> names ? text(names.get("en")) : null;
            if (country == null || !country.matches("[A-Z]{2}")) return empty(Status.UNAVAILABLE);
            return new Location(Status.RESOLVED, city, country);
        } catch (IOException | RuntimeException unavailable) { return empty(Status.UNAVAILABLE); }
    }
    private static String text(Object value) { return value instanceof String text && !text.isBlank() && text.length() <= 100 ? text : null; }
    private static Location empty(Status status) { return new Location(status, null, null); }
    @PreDestroy public void close() throws IOException { if (reader != null) reader.close(); }
}
