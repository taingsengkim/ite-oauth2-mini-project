package kh.edu.istad.platform.iam.persistence.repository.permission;

import kh.edu.istad.platform.iam.persistence.entity.permission.PermissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity,Long>, JpaSpecificationExecutor<PermissionEntity>{
    Optional<PermissionEntity> findByUuidAndDeletedFalse(UUID uuid);
    Page<PermissionEntity> findByDeletedFalse(Pageable pageable);
}
