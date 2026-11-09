package kh.edu.istad.platform.iam.persistence.adapter.role;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.pagination.*;
import kh.edu.istad.platform.iam.dto.role.RoleFilterQuery;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.persistence.mapper.role.RolePersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.role.RoleJpaRepository;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import kh.edu.istad.platform.iam.persistence.entity.role.RoleEntity;
import kh.edu.istad.platform.iam.persistence.query.PersistencePageQueries;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Transactional
public class RolePersistenceAdapter implements RoleRepository {
    private final RoleJpaRepository repository;
    private final RolePersistenceMapper mapper;

    @Override
    public Role create(Role role) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toNewEntity(role)));
        } catch (DataIntegrityViolationException exception) {
            throw conflict(exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByUuidForUpdate(UUID uuid) {
        return repository.findForUpdate(uuid).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Role> findAll(RoleFilterQuery query) {
        Specification<RoleEntity> specification = (entity, criteria, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.isFalse(entity.get("deleted")));
            if (query.active() != null) predicates.add(builder.equal(entity.get("active"), query.active()));
            if (query.search() != null) {
                String pattern = PersistencePageQueries.contains(query.search());
                predicates.add(builder.or(
                    builder.like(builder.lower(entity.get("name")), pattern, '\\'),
                    builder.like(builder.lower(entity.get("description")), pattern, '\\')
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        var result = repository.findAll(specification, PersistencePageQueries.page(query.pageQuery(),
                "name", Set.of("name", "createdAt", "updatedAt", "uuid", "active")));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Role update(Role role) {
        var entity = repository.findByUuidAndDeletedFalse(role.getUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Role", role.getUuid()));
        mapper.updateEntity(role, entity);
        try {
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException exception) {
            throw new ResourceConflictException("Role changed concurrently; reload it and try again");
        } catch (DataIntegrityViolationException exception) {
            throw conflict(exception);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return repository.findByNameAndDeletedFalse(name).map(mapper::toDomain);
    }

    private RuntimeException conflict(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains("uk_roles_name")) {
                return new IamDomainException(IamDomainException.Code.RULE_VIOLATION,
                        "A role with this name already exists");
            }
        }
        return exception;
    }
}
