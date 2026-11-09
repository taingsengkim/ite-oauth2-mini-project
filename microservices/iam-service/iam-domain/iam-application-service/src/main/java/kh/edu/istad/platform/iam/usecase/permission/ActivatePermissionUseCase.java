package kh.edu.istad.platform.iam.usecase.permission;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.Permission;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivatePermissionUseCase {
    private final PermissionRepository repository;
    private final CurrentActorProvider currentActorProvider;
    private final Clock clock;

    @Transactional
    public void execute(UUID uuid) {
        Permission permission= repository.findByUuid(uuid)
                .orElseThrow(()->new ResourceNotFoundException("Permission",uuid));
        permission.activate(currentActorProvider.currentActor(),clock.instant());
        repository.updateActiveState(permission);
    }
}
