package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.role.RoleResult;
import kh.edu.istad.platform.iam.dto.role.UpdateRoleCommand;
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
public class UpdateRoleUseCase {
    private final RoleRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    private final RoleApplicationMapper mapper;
    @Transactional
    public RoleResult execute(UpdateRoleCommand command) {
        Role role = repository.findByUuid(command.uuid())
                .orElseThrow(() -> new ResourceNotFoundException("Role", command.uuid()));
        role.update(command.name(), command.description(), actorProvider.currentActor(), clock.instant());
        return mapper.toResult(repository.update(role));
    }
}
