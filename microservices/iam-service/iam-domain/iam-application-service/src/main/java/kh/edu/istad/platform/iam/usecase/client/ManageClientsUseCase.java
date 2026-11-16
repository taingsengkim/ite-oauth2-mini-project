package kh.edu.istad.platform.iam.usecase.client;

import kh.edu.istad.platform.iam.audit.Audited;
import kh.edu.istad.platform.iam.audit.AuditAction;

import kh.edu.istad.platform.iam.application.exception.ClientNotFoundException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.client.*;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.client.ClientManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManageClientsUseCase {
    public static final Set<String> AUTHENTICATION_METHODS = Set.of("none", "client_secret_basic", "client_secret_post");
    public static final Set<String> GRANT_TYPES = Set.of("authorization_code", "refresh_token", "client_credentials");
    public static final Set<String> PROTECTED_IDS = Set.of("oidc-client", "client-registrar");
    private final ClientManagementRepository repository;
    private final kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository configuration;

    public PageResult<ManagedClientResult> list(String search, int page, int size) {
        require(page >= 0 && page <= 100000 && size >= 1 && size <= 100, "Invalid client pagination");
        require(search == null || search.length() <= 255, "Client search must be at most 255 characters");
        return repository.list(search == null ? "" : search.strip(), page, size);
    }

    public ManagedClientResult get(String id) {
        validateId(id);
        return repository.findById(id).orElseThrow(ClientNotFoundException::new);
    }

    @Transactional
    @Audited(action = AuditAction.CLIENT_CREATED, target = "#result.client().id()")
    public IssuedClientResult create(ClientDefinition definition) {
        definition = withDefaults(definition);
        validate(definition);
        if (PROTECTED_IDS.contains(definition.clientId())) throw new ResourceConflictException("This client ID is reserved for a built-in client");
        return repository.create(definition);
    }

    @Transactional
    @Audited(action = AuditAction.CLIENT_UPDATED, target = "#p0", fields = {"name", "grantTypes", "redirectUris", "postLogoutRedirectUris", "scopes", "requireProofKey", "requireConsent", "accessTokenTtlSeconds", "refreshTokenTtlSeconds"}, fieldArgument = 1)
    public ManagedClientResult update(String id, ClientDefinition definition) {
        validateId(id);
        var current = repository.findByIdForUpdate(id).orElseThrow(ClientNotFoundException::new);
        if (!current.editable()) throw new ResourceConflictException("This client's configuration cannot be edited from the dashboard");
        validate(definition);
        if (!current.clientId().equals(definition.clientId())
                || !current.authenticationMethods().equals(Set.of(definition.authenticationMethod())))
            throw new ResourceConflictException("Client ID and authentication method cannot be changed; register a new client instead");
        return repository.update(id, definition);
    }

    @Transactional
    @Audited(action = AuditAction.CLIENT_SECRET_ROTATED, target = "#p0")
    public IssuedClientResult rotateSecret(String id) {
        validateId(id);
        var current = repository.findByIdForUpdate(id).orElseThrow(ClientNotFoundException::new);
        if (current.authenticationMethods().contains("none") || !current.authenticationMethods().stream()
                .anyMatch(method -> method.equals("client_secret_basic") || method.equals("client_secret_post")))
            throw new ResourceConflictException("Only clients that authenticate with a secret can rotate a secret");
        return repository.rotateSecret(id);
    }

    @Transactional
    @Audited(action = AuditAction.CLIENT_DELETED, target = "#p0")
    public void delete(String id) {
        validateId(id);
        var current = repository.findByIdForUpdate(id).orElseThrow(ClientNotFoundException::new);
        if (current.protectedClient()) throw new ResourceConflictException("Built-in clients cannot be deleted");
        repository.delete(id);
    }

    private ClientDefinition withDefaults(ClientDefinition value) {
        if (value == null || (value.accessTokenTtlSeconds() != 0 && value.refreshTokenTtlSeconds() != 0)) return value;
        var policy = configuration.current().policy();
        return new ClientDefinition(value.clientId(), value.name(), value.authenticationMethod(), value.grantTypes(), value.redirectUris(), value.postLogoutRedirectUris(), value.scopes(), value.requireProofKey(), value.requireConsent(),
                value.accessTokenTtlSeconds() == 0 ? policy.defaultAccessTokenTtlSeconds() : value.accessTokenTtlSeconds(),
                value.refreshTokenTtlSeconds() == 0 ? policy.defaultRefreshTokenTtlSeconds() : value.refreshTokenTtlSeconds());
    }

    private void validate(ClientDefinition value) {
        require(value != null, "Client configuration is required");
        require(value.clientId() != null && value.clientId().matches("[A-Za-z0-9._~-]{1,100}"), "Client ID must contain 1–100 letters, digits, dots, underscores, tildes, or hyphens");
        require(value.name() != null && !value.name().isBlank() && value.name().length() <= 200
                && value.name().chars().noneMatch(Character::isISOControl), "Client name must contain 1–200 printable characters");
        require(value.authenticationMethod() != null && AUTHENTICATION_METHODS.contains(value.authenticationMethod()), "Unsupported client authentication method");
        require(value.grantTypes() != null && !value.grantTypes().isEmpty() && value.grantTypes().stream().allMatch(grant -> grant != null && GRANT_TYPES.contains(grant)), "Choose supported authorization grant types");
        require(value.scopes() != null && !value.scopes().isEmpty() && value.scopes().size() <= 30
                && value.scopes().stream().allMatch(scope -> scope != null && scope.matches("[A-Za-z0-9._:/-]{1,100}")), "Scopes must use letters, digits, dots, underscores, colons, slashes, or hyphens");
        require(String.join(",", value.scopes()).length() <= 1000, "Combined scopes must be at most 1000 characters");
        validateUris(value.redirectUris()); validateUris(value.postLogoutRedirectUris());
        boolean code = value.grantTypes().contains("authorization_code");
        boolean publicClient = "none".equals(value.authenticationMethod());
        require(!code || !value.redirectUris().isEmpty(), "Authorization code clients need at least one redirect URI");
        require(code || (value.redirectUris().isEmpty() && value.postLogoutRedirectUris().isEmpty()), "Redirect URIs require the authorization code grant");
        require(code || !value.grantTypes().contains("refresh_token"), "Refresh token grants require authorization code");
        require(code || !value.scopes().contains("openid"), "OpenID Connect requires the authorization code grant");
        require(!publicClient || (code && value.requireProofKey() && !value.grantTypes().contains("client_credentials")), "Public clients must use authorization code with PKCE and cannot use client credentials");
        require(value.accessTokenTtlSeconds() >= 60 && value.accessTokenTtlSeconds() <= 86400, "Access token lifetime must be 60–86400 seconds");
        require(value.refreshTokenTtlSeconds() >= 300 && value.refreshTokenTtlSeconds() <= 2592000, "Refresh token lifetime must be 300–2592000 seconds");
        require(!value.grantTypes().contains("refresh_token") || value.refreshTokenTtlSeconds() >= value.accessTokenTtlSeconds(), "Refresh tokens must last at least as long as access tokens");
    }

    private void validateUris(Set<String> values) {
        require(values != null && values.size() <= 10, "Provide at most ten URIs in each redirect list");
        require(values.stream().allMatch(value -> value != null && !value.isBlank()), "Redirect URIs cannot be blank");
        require(String.join(",", values).length() <= 1000, "Each combined redirect list must be at most 1000 characters");
        for (String value : values) {
            try {
                var uri = URI.create(value);
                String host = uri.getHost();
                boolean loopback = host != null && Set.of("localhost", "127.0.0.1", "::1", "[::1]").contains(host.toLowerCase(java.util.Locale.ROOT));
                require(host != null && uri.getRawUserInfo() == null && uri.getRawFragment() == null
                        && !value.contains("*") && !value.contains(",") && (uri.getPort() == -1 || (uri.getPort() > 0 && uri.getPort() <= 65535))
                        && ("https".equalsIgnoreCase(uri.getScheme()) || ("http".equalsIgnoreCase(uri.getScheme()) && loopback)),
                        "Redirect URIs must use HTTPS, or HTTP on localhost/loopback, without fragments, credentials, commas, or wildcards");
            } catch (IllegalArgumentException exception) {
                throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Invalid redirect URI");
            }
        }
    }

    private void validateId(String id) { require(id != null && !id.isBlank() && id.length() <= 100, "Invalid client identifier"); }
    private void require(boolean condition, String message) {
        if (!condition) throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message);
    }
}
