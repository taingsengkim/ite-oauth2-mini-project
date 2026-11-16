package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.audit.Audited;
import kh.edu.istad.platform.iam.audit.AuditAction;

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
public class DeactivateRoleUseCase {
    private final RoleRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;
    @Transactional
    @Audited(action = AuditAction.ROLE_DEACTIVATED, target = "#p0")
    public void execute(UUID uuid) {
        Role role = repository.findByUuidForUpdate(uuid).orElseThrow(() -> new ResourceNotFoundException("Role", uuid));
        role.deactivate(actorProvider.currentActor(), clock.instant());
        repository.update(role);
    }
}
