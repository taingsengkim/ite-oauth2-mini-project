package kh.edu.istad.platform.iam.persistence.repository.user;

import kh.edu.istad.platform.iam.persistence.entity.user.UserPermissionAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserPermissionAssignmentJpaRepository
        extends JpaRepository<UserPermissionAssignmentEntity, Long> {

    @Query("""
        select assignment
        from UserPermissionAssignmentEntity assignment
        join fetch assignment.user u
        join fetch assignment.permission
        where u.uuid = :uuid
          and assignment.deleted = false
        order by assignment.id
        """)
    List<UserPermissionAssignmentEntity> findForUser(
            @Param("uuid") UUID uuid
    );
}