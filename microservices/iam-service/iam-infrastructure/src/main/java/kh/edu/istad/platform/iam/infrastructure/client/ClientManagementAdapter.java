package kh.edu.istad.platform.iam.infrastructure.client;

import kh.edu.istad.platform.iam.application.exception.ClientNotFoundException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.client.*;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.port.out.client.ClientManagementRepository;
import kh.edu.istad.platform.iam.usecase.client.ManageClientsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientManagementAdapter implements ClientManagementRepository {
    private final RegisteredClientRepository clients;
    private final JdbcOperations jdbc;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override public PageResult<ManagedClientResult> list(String search, int page, int size, String priorityId) {
        String pattern = "%" + search.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        String where = " WHERE lower(client_id) LIKE ? ESCAPE '!' OR lower(client_name) LIKE ? ESCAPE '!'";
        Long count = jdbc.queryForObject("SELECT count(*) FROM oauth2_registered_client" + where, Long.class, pattern, pattern);
        var ids = jdbc.queryForList("SELECT id FROM oauth2_registered_client" + where
                + " ORDER BY CASE WHEN id = ? THEN 0 ELSE 1 END, updated_at DESC, id LIMIT ? OFFSET ?", String.class, pattern, pattern, priorityId, size, (long) page * size);
        var content = ids.stream().map(clients::findById).filter(Objects::nonNull).map(this::view).toList();
        long total = count == null ? 0 : count;
        return new PageResult<>(content, page, size, total, (int) Math.ceil((double) total / size));
    }

    @Override public Optional<ManagedClientResult> findById(String id) { return Optional.ofNullable(clients.findById(id)).map(this::view); }

    @Override @Transactional(propagation = Propagation.MANDATORY)
    public Optional<ManagedClientResult> findByIdForUpdate(String id) {
        var ids = jdbc.queryForList("SELECT id FROM oauth2_registered_client WHERE id = ? FOR UPDATE", String.class, id);
        return ids.isEmpty() ? Optional.empty() : findById(id);
    }

    @Override @Transactional
    public IssuedClientResult create(ClientDefinition definition) {
        if (clients.findByClientId(definition.clientId()) != null) throw new ResourceConflictException("Client ID is already registered");
        boolean confidential = !"none".equals(definition.authenticationMethod());
        String secret = confidential ? newSecret() : null;
        var builder = RegisteredClient.withId(UUID.randomUUID().toString()).clientId(definition.clientId())
                .clientIdIssuedAt(clock.instant()).clientAuthenticationMethod(new ClientAuthenticationMethod(definition.authenticationMethod()));
        if (secret != null) builder.clientSecret(passwordEncoder.encode(secret));
        var client = configure(builder, definition, ClientSettings.builder().build(), TokenSettings.builder().build()).build();
        try { clients.save(client); }
        catch (DataIntegrityViolationException exception) { throw new ResourceConflictException("Client ID is already registered"); }
        return new IssuedClientResult(view(client), secret);
    }

    @Override @Transactional
    public ManagedClientResult update(String id, ClientDefinition definition) {
        var current = requireClient(id);
        var client = configure(RegisteredClient.from(current), definition, current.getClientSettings(), current.getTokenSettings()).build();
        clients.save(client);
        return view(client);
    }

    @Override @Transactional
    public IssuedClientResult rotateSecret(String id) {
        var current = requireClient(id);
        String secret = newSecret();
        var client = RegisteredClient.from(current).clientSecret(passwordEncoder.encode(secret)).clientSecretExpiresAt(null).build();
        clients.save(client);
        return new IssuedClientResult(view(client), secret);
    }

    @Override @Transactional
    public void delete(String id) {
        jdbc.update("DELETE FROM oauth2_authorization WHERE registered_client_id = ?", id);
        jdbc.update("DELETE FROM oauth2_authorization_consent WHERE registered_client_id = ?", id);
        jdbc.update("DELETE FROM oauth2_registered_client WHERE id = ?", id);
    }

    private RegisteredClient requireClient(String id) {
        var client = clients.findById(id);
        if (client == null) throw new ClientNotFoundException();
        return client;
    }

    private RegisteredClient.Builder configure(RegisteredClient.Builder builder, ClientDefinition definition, ClientSettings clientSettings, TokenSettings tokenSettings) {
        // Preserve settings not managed by this screen, including token format/signing configuration.
        return builder.clientName(definition.name().strip()).authorizationGrantTypes(values -> {
                    values.clear(); definition.grantTypes().forEach(value -> values.add(new AuthorizationGrantType(value)));
                }).redirectUris(values -> { values.clear(); values.addAll(definition.redirectUris()); })
                .postLogoutRedirectUris(values -> { values.clear(); values.addAll(definition.postLogoutRedirectUris()); })
                .scopes(values -> { values.clear(); values.addAll(definition.scopes()); })
                .clientSettings(ClientSettings.withSettings(clientSettings.getSettings())
                        .requireProofKey(definition.requireProofKey()).requireAuthorizationConsent(definition.requireConsent()).build())
                .tokenSettings(TokenSettings.withSettings(tokenSettings.getSettings())
                        .accessTokenTimeToLive(Duration.ofSeconds(definition.accessTokenTtlSeconds()))
                        .refreshTokenTimeToLive(Duration.ofSeconds(definition.refreshTokenTtlSeconds())).build());
    }

    private ManagedClientResult view(RegisteredClient client) {
        var methods = client.getClientAuthenticationMethods().stream().map(ClientAuthenticationMethod::getValue).collect(Collectors.toUnmodifiableSet());
        var grants = client.getAuthorizationGrantTypes().stream().map(AuthorizationGrantType::getValue).collect(Collectors.toUnmodifiableSet());
        boolean editable = !"client-registrar".equals(client.getClientId()) && methods.size() == 1
                && ManageClientsUseCase.AUTHENTICATION_METHODS.containsAll(methods) && ManageClientsUseCase.GRANT_TYPES.containsAll(grants);
        return new ManagedClientResult(client.getId(), client.getClientId(), client.getClientName(), methods, grants,
                Set.copyOf(client.getRedirectUris()), Set.copyOf(client.getPostLogoutRedirectUris()), Set.copyOf(client.getScopes()),
                client.getClientSettings().isRequireProofKey(), client.getClientSettings().isRequireAuthorizationConsent(),
                client.getTokenSettings().getAccessTokenTimeToLive().getSeconds(), client.getTokenSettings().getRefreshTokenTimeToLive().getSeconds(),
                client.getClientIdIssuedAt(), client.getClientSecretExpiresAt(), client.getClientSecret() != null && !client.getClientSecret().isBlank(),
                ManageClientsUseCase.PROTECTED_IDS.contains(client.getClientId()), editable);
    }

    private String newSecret() { byte[] bytes = new byte[32]; RANDOM.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
}
