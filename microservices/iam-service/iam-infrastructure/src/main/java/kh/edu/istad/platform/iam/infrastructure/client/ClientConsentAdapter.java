package kh.edu.istad.platform.iam.infrastructure.client;

import kh.edu.istad.platform.iam.dto.client.ConnectedAppResult;
import kh.edu.istad.platform.iam.dto.client.ConsentClientResult;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientConsentAdapter implements ClientConsentRepository {
    private final JdbcOperations jdbc;
    private final RegisteredClientRepository clients;
    private final OAuth2AuthorizationConsentService consents;

    @Override
    public List<ConnectedAppResult> findByPrincipalName(String principalName) {
        var ids = jdbc.queryForList(
                "SELECT registered_client_id FROM oauth2_authorization_consent WHERE principal_name = ?",
                String.class, principalName);
        var result = new ArrayList<ConnectedAppResult>();
        for (String id : ids) {
            var client = clients.findById(id);
            var consent = consents.findById(id, principalName);
            if (client != null && consent != null) {
                result.add(new ConnectedAppResult(id, client.getClientId(), client.getClientName(), consent.getScopes()));
            }
        }
        result.sort(Comparator.comparing(ConnectedAppResult::name));
        return List.copyOf(result);
    }

    @Override
    public Optional<ConsentClientResult> findClient(String clientId, String principalName) {
        var client = clients.findByClientId(clientId);
        if (client == null) return Optional.empty();
        var consent = consents.findById(client.getId(), principalName);
        return Optional.of(new ConsentClientResult(clientId, client.getClientName(), client.getScopes(),
                consent == null ? Set.of() : consent.getScopes()));
    }

    @Override
    @Transactional
    public void revoke(String registeredClientId, String principalName) {
        var consent = consents.findById(registeredClientId, principalName);
        if (consent != null) consents.remove(consent);
        jdbc.update("DELETE FROM oauth2_authorization WHERE registered_client_id = ? AND principal_name = ?",
                registeredClientId, principalName);
    }
}
