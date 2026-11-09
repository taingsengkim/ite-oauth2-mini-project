package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.port.out.*;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivateRoleUseCase {
    private final RoleRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    @Transactional
    public void execute(UUID uuid) {
        Role role = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Role", uuid));
        role.activate(actorProvider.currentActor(), clock.instant());
        repository.update(role);
    }
}
