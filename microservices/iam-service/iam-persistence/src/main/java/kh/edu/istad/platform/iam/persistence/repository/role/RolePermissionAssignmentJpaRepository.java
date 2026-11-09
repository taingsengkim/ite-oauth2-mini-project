package kh.edu.istad.platform.iam.persistence.repository.role;

import kh.edu.istad.platform.iam.persistence.entity.role.RolePermissionAssignmentEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface RolePermissionAssignmentJpaRepository extends JpaRepository<RolePermissionAssignmentEntity, Long> {
    @Query("select a from RolePermissionAssignmentEntity a join fetch a.role r join fetch a.permission "
            + "where r.uuid = :uuid and a.deleted = false order by a.id")
    List<RolePermissionAssignmentEntity> findForRole(@Param("uuid") UUID uuid);
}
