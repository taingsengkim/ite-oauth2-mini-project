package kh.edu.istad.platform.iam.persistence.repository.role;

import jakarta.persistence.LockModeType;
import kh.edu.istad.platform.iam.persistence.entity.role.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity> {
    Optional<RoleEntity> findByUuidAndDeletedFalse(UUID uuid);
    Page<RoleEntity> findByDeletedFalse(Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoleEntity r where r.uuid = :uuid and r.deleted = false")
    Optional<RoleEntity> findForUpdate(@Param("uuid") UUID uuid);
    Optional<RoleEntity> findByNameAndDeletedFalse(String name);

}
