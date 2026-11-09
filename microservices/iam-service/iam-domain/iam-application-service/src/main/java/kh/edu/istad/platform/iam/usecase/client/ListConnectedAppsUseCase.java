package kh.edu.istad.platform.iam.usecase.client;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.client.ConnectedAppResult;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListConnectedAppsUseCase {
    private final UserRepository users;
    private final ClientConsentRepository consents;

    @Transactional(readOnly = true)
    public List<ConnectedAppResult> execute(UUID userUuid) {
        var user = users.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));
        return consents.findByPrincipalName(user.getUsername());
    }
}
