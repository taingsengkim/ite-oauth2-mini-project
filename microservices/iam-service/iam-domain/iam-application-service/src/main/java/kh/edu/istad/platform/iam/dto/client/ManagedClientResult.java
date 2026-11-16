package kh.edu.istad.platform.iam.dto.client;

import java.time.Instant;
import java.util.Set;

public record ManagedClientResult(String id, String clientId, String name,
        Set<String> authenticationMethods, Set<String> grantTypes,
        Set<String> redirectUris, Set<String> postLogoutRedirectUris, Set<String> scopes,
        boolean requireProofKey, boolean requireConsent, long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds, Instant issuedAt, Instant secretExpiresAt,
        boolean hasSecret, boolean protectedClient, boolean editable) {}
