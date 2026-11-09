package kh.edu.istad.platform.iam.port.out.client;

import kh.edu.istad.platform.iam.dto.client.ConnectedAppResult;
import kh.edu.istad.platform.iam.dto.client.ConsentClientResult;
import java.util.List;
import java.util.Optional;

public interface ClientConsentRepository {
    List<ConnectedAppResult> findByPrincipalName(String principalName);
    Optional<ConsentClientResult> findClient(String clientId, String principalName);
    void revoke(String registeredClientId, String principalName);
}
