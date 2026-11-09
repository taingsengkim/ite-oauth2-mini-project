package kh.edu.istad.platform.iam.persistence.adapter;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.UserFilterQuery;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.persistence.mapper.user.UserPersistenceMapper;
import kh.edu.istad.platform.iam.persistence.repository.user.UserJpaRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import kh.edu.istad.platform.iam.persistence.entity.user.UserEntity;
import kh.edu.istad.platform.iam.persistence.query.PersistencePageQueries;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    @Transactional
    public User create(User user) {
        var entity = mapper.toNewEntity(user);
        var saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return repository.findByUuidAndDeletedFalse(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsernameAndDeletedFalse(username).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        return repository.findByEmailAndDeletedFalse(normalizedEmail)
                .map(mapper::toDomain);
    }


    @Override
    public PageResult<User> findAll(UserFilterQuery query) {
        Specification<UserEntity> specification = (entity, criteria, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.isFalse(entity.get("deleted")));
            if (query.enabled() != null) predicates.add(builder.equal(entity.get("enabled"), query.enabled()));
            if (query.search() != null) {
                String pattern = PersistencePageQueries.contains(query.search());
                predicates.add(builder.or(
                    builder.like(builder.lower(entity.get("username")), pattern, '\\'),
                    builder.like(builder.lower(entity.get("email")), pattern, '\\'),
                    builder.like(builder.lower(entity.get("givenName")), pattern, '\\'),
                    builder.like(builder.lower(entity.get("familyName")), pattern, '\\')
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        var result = repository.findAll(specification, PersistencePageQueries.page(query.pageQuery(),
                "username", Set.of("username", "email", "createdAt", "updatedAt", "uuid", "enabled")));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    @Transactional
    public User updateProfile(User user) {
        var entity = repository.findByUuidAndDeletedFalse(user.getUuid()).orElseThrow(() ->
                new ResourceNotFoundException("User", user.getUuid()
        ));
        mapper.updateProfileEntity(user, entity);
        var saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void updateAccountState(User user) {
        var entity = repository.findByUuidAndDeletedFalse(user.getUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", user.getUuid()));
        mapper.updateAccountStateEntity(user, entity);
        repository.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public void softDelete(User user) {
        var entity = repository.findByUuidAndDeletedFalse(user.getUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", user.getUuid()));

        mapper.softDeleteEntity(user, entity);

        repository.saveAndFlush(entity);
    }

    @Override
    @Transactional
    public void updatePassword(User user) {
        var entity = repository.findByUuidAndDeletedFalse(user.getUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", user.getUuid()));

        mapper.updatePasswordEntity(user, entity);

        repository.saveAndFlush(entity);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<User> findByUuidForUpdate(UUID uuid) {
        return repository.findForUpdate(uuid)
                .map(mapper::toDomain);
    }
    @Override
    @Transactional
    public void updateLoginMetadata(User user) {
        var entity = repository
                .findByUuidAndDeletedFalse(user.getUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", user.getUuid()
                ));

        mapper.updateLoginMetadataEntity(user, entity);

        repository.saveAndFlush(entity);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<User> findByLoginForUpdate(String usernameOrEmail) {
        String login = usernameOrEmail.strip();

        return repository.findByUsernameForUpdate(login)
                .or(() -> repository.findByEmailForUpdate(login.toLowerCase(Locale.ROOT)))
                .map(mapper::toDomain);
    }


}