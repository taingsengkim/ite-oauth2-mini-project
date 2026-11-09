package kh.edu.istad.platform.iam.dto.client;

import java.util.Set;

public record ConsentClientResult(String clientId, String name, Set<String> scopes, Set<String> grantedScopes) {
    public ConsentClientResult {
        scopes = Set.copyOf(scopes);
        grantedScopes = Set.copyOf(grantedScopes);
    }
}
