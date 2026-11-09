package kh.edu.istad.platform.iam.persistence.adapter.user;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.model.UserPermissionAssignment;
import kh.edu.istad.platform.iam.persistence.mapper.user.UserPermissionAssignmentPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.permission.PermissionJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.user.UserJpaRepository;
import kh.edu.istad.platform.iam.persistence.repository.user.UserPermissionAssignmentJpaRepository;
import kh.edu.istad.platform.iam.port.out.user.UserPermissionAssignmentRepository;
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
public class UserPermissionAssignmentPersistenceAdapter
        implements UserPermissionAssignmentRepository {

    private final UserPermissionAssignmentJpaRepository repository;
    private final UserJpaRepository users;
    private final PermissionJpaRepository permissions;
    private final UserPermissionAssignmentPersistenceMapper mapper;

    @Override
    public List<UserPermissionAssignment> findByUserUuid(UUID userUuid) {
        return repository.findForUser(userUuid).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public UserPermissionAssignment create(
            UserPermissionAssignment assignment) {

        var user = users
                .findByUuidAndDeletedFalse(assignment.getUserUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", assignment.getUserUuid()));

        var permission = permissions
                .findByUuidAndDeletedFalse(assignment.getPermissionUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", assignment.getPermissionUuid()));

        var entity = mapper.toNewEntity(
                assignment, user, permission
        );

        try {
            return mapper.toDomain(
                    repository.saveAndFlush(entity)
            );
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception;
                 cause != null;
                 cause = cause.getCause()) {

                if (cause.getMessage() != null
                        && cause.getMessage().contains("uk_user_permissions_unrevoked")) {

                    throw new ResourceConflictException("Permission already has a current assignment " + "for this user");
                }
            }

            throw exception;
        }
    }

    @Override
    @Transactional
    public void update(UserPermissionAssignment assignment) {
        var entity = repository.findById(assignment.getId())
                .filter(value -> !value.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User-permission assignment", assignment.getPermissionUuid()
                ));

        mapper.updateEntity(assignment, entity);

        try {
            repository.saveAndFlush(entity);
        } catch (OptimisticLockingFailureException exception) {
            throw new ResourceConflictException("Assignment changed concurrently; reload and try again");
        }
    }
}
