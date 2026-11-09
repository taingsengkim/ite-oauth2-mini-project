package kh.edu.istad.platform.iam.persistence.repository.user;

import kh.edu.istad.platform.iam.persistence.entity.user.UserRoleAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleAssignmentJpaRepository
        extends JpaRepository<UserRoleAssignmentEntity, Long> {

    @Query("""
        select assignment
        from UserRoleAssignmentEntity assignment
        join fetch assignment.user user
        join fetch assignment.role
        where user.uuid = :uuid
          and assignment.deleted = false
        order by assignment.id
        """)
    List<UserRoleAssignmentEntity> findForUser(
            @Param("uuid") UUID uuid
    );
}