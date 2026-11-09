package kh.edu.istad.platform.iam.persistence.repository.user;

import jakarta.persistence.LockModeType;
import kh.edu.istad.platform.iam.persistence.entity.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity,Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByUuidAndDeletedFalse(UUID uuid);

    Optional<UserEntity> findByUsernameAndDeletedFalse(String username);

    Optional<UserEntity> findByEmailAndDeletedFalse(String email);

    Page<UserEntity> findByDeletedFalse(Pageable pageable);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select u
    from UserEntity u
    where u.uuid = :uuid
      and u.deleted = false
    """)
    Optional<UserEntity> findForUpdate(@Param("uuid") UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select u from UserEntity u
    where u.username = :username
      and u.deleted = false
    """)
    Optional<UserEntity> findByUsernameForUpdate(
            @Param("username") String username
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select u from UserEntity u
    where u.email = :email
      and u.deleted = false
    """)
    Optional<UserEntity> findByEmailForUpdate(
            @Param("email") String email
    );
}
