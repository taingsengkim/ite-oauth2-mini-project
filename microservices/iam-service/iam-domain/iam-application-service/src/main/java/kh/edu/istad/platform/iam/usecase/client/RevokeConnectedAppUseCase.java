package kh.edu.istad.platform.iam.usecase.client;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.client.ClientConsentRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RevokeConnectedAppUseCase {
    private final UserRepository users;
    private final ClientConsentRepository consents;

    @Transactional
    public void execute(UUID userUuid, String registeredClientId) {
        var user = users.findByUuidForUpdate(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));
        consents.revoke(registeredClientId, user.getUsername());
    }
}
