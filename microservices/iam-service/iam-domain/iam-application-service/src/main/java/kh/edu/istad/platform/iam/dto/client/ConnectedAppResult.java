package kh.edu.istad.platform.iam.dto.client;

import java.util.Set;

public record ConnectedAppResult(String id, String clientId, String name, Set<String> scopes) {
    public ConnectedAppResult {
        scopes = Set.copyOf(scopes);
    }
}
