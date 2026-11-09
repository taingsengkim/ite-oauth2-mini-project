package kh.edu.istad.platform.iam.application.exception;

import java.util.Objects;
import java.util.UUID;

public final class ResourceNotFoundException extends RuntimeException {
    private final String resource;
    private final UUID resourceUuid;

    public ResourceNotFoundException(String resource, UUID resourceUuid) {
        super(Objects.requireNonNull(resource, "resource") + " with UUID "
                + Objects.requireNonNull(resourceUuid, "resourceUuid") + " was not found");
        this.resource = resource;
        this.resourceUuid = resourceUuid;
    }

    public String getResource() { return resource; }
    public UUID getResourceUuid() { return resourceUuid; }
}
