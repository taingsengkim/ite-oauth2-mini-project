package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.dto.role.CreateRoleCommand;
import kh.edu.istad.platform.iam.dto.role.RoleResult;
import kh.edu.istad.platform.iam.mapper.role.RoleApplicationMapper;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.port.out.*;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CreateRoleUseCase {
    private final RoleRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    private final RoleApplicationMapper mapper;
    private final RolePermissionAssignmentService assignmentService;
    @Transactional
    public RoleResult execute(CreateRoleCommand command) {
        Role role = repository.create(Role.create(command.name(), command.description(),
                actorProvider.currentActor(), clock.instant()));
        if (command.permissionUuids() != null && !command.permissionUuids().isEmpty()) {
            assignmentService.assign(role, command.permissionUuids(), null, "Assigned during role creation");
        }
        return mapper.toResult(role);
    }
}
