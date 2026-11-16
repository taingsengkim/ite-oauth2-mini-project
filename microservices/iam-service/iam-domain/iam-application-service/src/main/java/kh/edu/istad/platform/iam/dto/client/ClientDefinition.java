package kh.edu.istad.platform.iam.dto.client;

import java.util.Set;

public record ClientDefinition(String clientId, String name, String authenticationMethod,
        Set<String> grantTypes, Set<String> redirectUris, Set<String> postLogoutRedirectUris,
        Set<String> scopes, boolean requireProofKey, boolean requireConsent,
        long accessTokenTtlSeconds, long refreshTokenTtlSeconds) {}
