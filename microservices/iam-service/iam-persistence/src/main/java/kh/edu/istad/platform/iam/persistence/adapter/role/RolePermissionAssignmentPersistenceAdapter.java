package kh.edu.istad.platform.iam.persistence.adapter.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.model.RolePermissionAssignment;
import kh.edu.istad.platform.iam.persistence.mapper.role.RolePermissionAssignmentPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.permission.PermissionJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.role.RoleJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.role.RolePermissionAssignmentJpaRepository;
import kh.edu.istad.platform.iam.port.out.role.RolePermissionAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class RolePermissionAssignmentPersistenceAdapter implements RolePermissionAssignmentRepository {
    private final RolePermissionAssignmentJpaRepository repository;
    private final RoleJpaRepository roles;
    private final PermissionJpaRepository permissions;
    private final RolePermissionAssignmentPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RolePermissionAssignment> findByRoleUuid(UUID roleUuid) {
        return repository.findForRole(roleUuid).stream().map(mapper::toDomain).toList();
    }

    @Override
    public RolePermissionAssignment create(RolePermissionAssignment assignment) {
        var role = roles.findByUuidAndDeletedFalse(assignment.getRoleUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Role", assignment.getRoleUuid()));
        var permission = permissions.findByUuidAndDeletedFalse(assignment.getPermissionUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", assignment.getPermissionUuid()));
        return mapper.toDomain(repository.saveAndFlush(mapper.toNewEntity(assignment, role, permission)));
    }

    @Override
    public void update(RolePermissionAssignment assignment) {
        var entity = repository.findById(assignment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Role permission assignment", assignment.getPermissionUuid()));
        mapper.updateEntity(assignment, entity);
        try {
            repository.saveAndFlush(entity);
        } catch (OptimisticLockingFailureException exception) {
            throw new ResourceConflictException("Assignment changed concurrently; reload it and try again");
        }
    }
}
