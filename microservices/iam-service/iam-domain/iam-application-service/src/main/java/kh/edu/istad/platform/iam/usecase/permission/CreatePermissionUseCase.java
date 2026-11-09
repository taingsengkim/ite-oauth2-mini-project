package kh.edu.istad.platform.iam.usecase.permission;

import kh.edu.istad.platform.iam.dto.permission.CreatePermissionCommand;
import kh.edu.istad.platform.iam.dto.permission.PermissionResult;
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
public class CreatePermissionUseCase {
    private final PermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final PermissionApplicationMapper mapper;
    private final Clock clock;

    @Transactional
    public PermissionResult execute(CreatePermissionCommand command) {
        var permission = Permission.create(command.resource(), command.action(), command.description(),
                actorProvider.currentActor(), clock.instant());
        return mapper.toResult(repository.create(permission));
    }
}
