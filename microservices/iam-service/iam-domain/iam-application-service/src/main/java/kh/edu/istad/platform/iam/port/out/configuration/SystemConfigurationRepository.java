package kh.edu.istad.platform.iam.port.out.configuration;

import kh.edu.istad.platform.iam.dto.configuration.SecurityPolicy;
import java.time.Instant;

public interface SystemConfigurationRepository {
    record Snapshot(String id, SecurityPolicy policy, long version, Instant updatedAt, String updatedBy) {}
    Snapshot current();
    Snapshot update(SecurityPolicy policy, long expectedVersion, String actor, Instant now);
}
