package kh.edu.istad.platform.iam.persistence.mapper.user;

import kh.edu.istad.platform.iam.model.UserRoleAssignment;
import kh.edu.istad.platform.iam.persistence.entity.role.RoleEntity;
import kh.edu.istad.platform.iam.persistence.entity.user.UserEntity;
import kh.edu.istad.platform.iam.persistence.entity.user.UserRoleAssignmentEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserRoleAssignmentPersistenceMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "assignedAt", source = "assignment.assignedAt")
    @Mapping(target = "assignedBy", source = "assignment.assignedBy")
    @Mapping(target = "expiresAt", source = "assignment.expiresAt")
    @Mapping(target = "reason", source = "assignment.reason")
    UserRoleAssignmentEntity toNewEntity(
            UserRoleAssignment assignment,
            UserEntity user,
            RoleEntity role
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "revokedAt", source = "revokedAt")
    @Mapping(target = "revokedBy", source = "revokedBy")
    @Mapping(target = "reason", source = "reason")
    void updateEntity(
            UserRoleAssignment assignment,
            @MappingTarget UserRoleAssignmentEntity entity
    );

    default UserRoleAssignment toDomain(
            UserRoleAssignmentEntity entity) {

        if (entity == null) {
            return null;
        }

        return UserRoleAssignment.restore(
                entity.getId(),
                entity.getUser().getUuid(),
                entity.getRole().getUuid(),
                entity.getAssignedAt(),
                entity.getAssignedBy(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokedBy(),
                entity.getReason()
        );
    }
}