package kh.edu.istad.platform.iam.persistence.adapter.user;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.UserRoleAssignment;
import kh.edu.istad.platform.iam.persistence.mapper.user.UserRoleAssignmentPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.role.RoleJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.user.UserJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.user.UserRoleAssignmentJpaRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRoleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleAssignmentPersistenceAdapter
        implements UserRoleAssignmentRepository {

    private final UserRoleAssignmentJpaRepository repository;
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final UserRoleAssignmentPersistenceMapper mapper;

    @Override
    public List<UserRoleAssignment> findByUserUuid(UUID userUuid) {
        return repository.findForUser(userUuid).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public UserRoleAssignment create(UserRoleAssignment assignment) {
        var user = users
                .findByUuidAndDeletedFalse(assignment.getUserUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", assignment.getUserUuid()));

        var role = roles
                .findByUuidAndDeletedFalse(assignment.getRoleUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Role", assignment.getRoleUuid()));

        var entity = mapper.toNewEntity(assignment, user, role);

        try {
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception;
                 cause != null;
                 cause = cause.getCause()) {

                if (cause.getMessage() != null && cause.getMessage().contains("uk_user_roles_unrevoked")) {

                    throw new ResourceConflictException("Role is already assigned to this user");
                }
            }

            throw exception;
        }
    }

    @Override
    @Transactional
    public void update(UserRoleAssignment assignment) {
        var entity = repository.findById(assignment.getId())
                .filter(value -> !value.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User-role assignment", assignment.getRoleUuid()));

        mapper.updateEntity(assignment, entity);

        try {
            repository.saveAndFlush(entity);
        } catch (OptimisticLockingFailureException exception) {
            throw new ResourceConflictException("Assignment changed concurrently; reload and try again");
        }
    }
}