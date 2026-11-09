package kh.edu.istad.platform.iam.persistence.adapter.permission;

import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.permission.PermissionFilterQuery;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.model.Permission;
import kh.edu.istad.platform.iam.persistence.entity.permission.PermissionEntity;
import kh.edu.istad.platform.iam.persistence.mapper.permission.PermissionPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.query.PersistencePageQueries;
import kh.edu.istad.platform.iam.persistence.repository.permission.PermissionJpaRepository;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionPersistenceAdapter implements PermissionRepository {
    private final PermissionJpaRepository repository;
    private final PermissionPersistenceMapper mapper;

    @Override
    @Transactional
    public Permission create(Permission permission) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toNewEntity(permission)));
        } catch (DataIntegrityViolationException exception) {
            throw conflict(exception);
        }
    }

    @Override
    public Optional<Permission> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid).map(mapper::toDomain);
    }

    @Override
    public PageResult<Permission> findAll(PermissionFilterQuery query) {
        Specification<PermissionEntity> specification = (entity, criteria, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.isFalse(entity.get("deleted")));
            if (query.active() != null) predicates.add(builder.equal(entity.get("active"), query.active()));
            if (query.resource() != null) predicates.add(builder.equal(entity.get("resource"), query.resource()));
            if (query.action() != null) predicates.add(builder.equal(entity.get("action"), query.action()));
            if (query.search() != null) {
                String pattern = PersistencePageQueries.contains(query.search());
                predicates.add(builder.or(
                        builder.like(builder.lower(entity.get("name")), pattern, '\\'),
                        builder.like(builder.lower(entity.get("description")), pattern, '\\')));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        var page = repository.findAll(specification, PersistencePageQueries.page(query.pageQuery(), "name",
                Set.of("name", "resource", "action", "uuid", "active", "createdAt", "updatedAt")));
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public Optional<Permission> update(Permission permission) {
        var entity = findEntity(permission.getUuid());
        mapper.updateEntity(permission, entity);
        return Optional.of(save(entity));
    }

    @Override
    @Transactional
    public void softDelete(Permission permission) {
        var entity = findEntity(permission.getUuid());
        mapper.softDeleteEntity(permission, entity);
        save(entity);
    }

    @Override
    @Transactional
    public void updateActiveState(Permission permission) {
        var entity = findEntity(permission.getUuid());
        mapper.updateActiveState(permission, entity);
        save(entity);
    }

    private PermissionEntity findEntity(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", uuid));
    }

    private Permission save(PermissionEntity entity) {
        try {
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException exception) {
            throw new ResourceConflictException("Permission changed concurrently; reload and try again");
        } catch (DataIntegrityViolationException exception) {
            throw conflict(exception);
        }
    }

    private RuntimeException conflict(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains("uk_permissions_name")) {
                return new ResourceConflictException("A permission with this name already exists");
            }
        }
        return exception;
    }
}
