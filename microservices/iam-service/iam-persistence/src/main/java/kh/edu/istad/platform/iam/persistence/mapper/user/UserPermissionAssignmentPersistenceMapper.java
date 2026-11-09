package kh.edu.istad.platform.iam.persistence.mapper.user;

import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.model.UserPermissionAssignment;
import kh.edu.istad.platform.iam.persistence.entity.permission.PermissionEntity;
import kh.edu.istad.platform.iam.persistence.entity.user.UserEntity;
import kh.edu.istad.platform.iam.persistence.entity.user.UserPermissionAssignmentEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserPermissionAssignmentPersistenceMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "permission", source = "permission")
    @Mapping(target = "effect", source = "assignment.effect")
    @Mapping(target = "assignedAt", source = "assignment.assignedAt")
    @Mapping(target = "assignedBy", source = "assignment.assignedBy")
    @Mapping(target = "expiresAt", source = "assignment.expiresAt")
    @Mapping(target = "reason", source = "assignment.reason")
    UserPermissionAssignmentEntity toNewEntity(
            UserPermissionAssignment assignment,
            UserEntity user,
            PermissionEntity permission
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "revokedAt", source = "revokedAt")
    @Mapping(target = "revokedBy", source = "revokedBy")
    @Mapping(target = "reason", source = "reason")
    void updateEntity(
            UserPermissionAssignment assignment,
            @MappingTarget UserPermissionAssignmentEntity entity
    );

    default UserPermissionAssignment toDomain(
            UserPermissionAssignmentEntity entity) {

        if (entity == null) {
            return null;
        }

        return UserPermissionAssignment.restore(
                entity.getId(),
                entity.getUser().getUuid(),
                entity.getPermission().getUuid(),
                entity.getEffect(),
                entity.getAssignedAt(),
                entity.getAssignedBy(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getRevokedBy(),
                entity.getReason()
        );
    }



}
