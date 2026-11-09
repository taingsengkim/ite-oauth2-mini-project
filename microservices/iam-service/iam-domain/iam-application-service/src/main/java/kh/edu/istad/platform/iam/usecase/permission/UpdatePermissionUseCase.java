package kh.edu.istad.platform.iam.usecase.permission;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.permission.PermissionResult;
import kh.edu.istad.platform.iam.dto.permission.UpdatePermissionCommand;
import kh.edu.istad.platform.iam.mapper.permission.PermissionApplicationMapper;
import kh.edu.istad.platform.iam.model.Permission;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class UpdatePermissionUseCase {
    private final PermissionRepository permissionRepository;
    private final CurrentActorProvider actorProvider;
    private final PermissionApplicationMapper mapper;
    private final Clock clock;



    @Transactional
    public PermissionResult execute(UpdatePermissionCommand command) {

        Permission permission = permissionRepository.findByUuid(command.uuid())
                .orElseThrow(()->new ResourceNotFoundException("Permission",command.uuid()));
        permission.update(command.resource(),command.action(),command.description(),actorProvider.currentActor(),clock.instant());
        Permission saved = permissionRepository.update(permission).orElseThrow(()->new ResourceNotFoundException("Permission",command.uuid()));
        return mapper.toResult(saved);
    }
}
