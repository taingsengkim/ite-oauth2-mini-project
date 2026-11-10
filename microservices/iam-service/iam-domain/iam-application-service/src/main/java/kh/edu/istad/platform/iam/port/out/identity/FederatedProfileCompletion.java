package kh.edu.istad.platform.iam.port.out.identity;

import kh.edu.istad.platform.iam.dto.identity.CompleteFederatedProfileCommand;
import kh.edu.istad.platform.iam.dto.identity.FederatedProfileDraft;
import java.util.Optional;

public interface FederatedProfileCompletion {
    Optional<FederatedProfileDraft> pending();
    String complete(CompleteFederatedProfileCommand command);
    void cancel(String proofToken);
}
