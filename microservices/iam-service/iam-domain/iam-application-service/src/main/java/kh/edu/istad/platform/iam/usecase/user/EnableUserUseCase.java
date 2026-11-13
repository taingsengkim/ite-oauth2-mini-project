package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnableUserUseCase {

    private final UserRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    @Transactional
    public void execute(UUID uuid) {
        var user = repository.findByUuidForUpdate(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", uuid));

        user.enable(actorProvider.currentActor(), clock.instant());
        repository.updateAccountState(user);
    }
}
