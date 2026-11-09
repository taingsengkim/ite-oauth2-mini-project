package kh.edu.istad.platform.iam.persistence.mapper.role;

import kh.edu.istad.platform.iam.model.RolePermissionAssignment;
import kh.edu.istad.platform.iam.persistence.entity.permission.PermissionEntity;
import kh.edu.istad.platform.iam.persistence.entity.role.RoleEntity;
import kh.edu.istad.platform.iam.persistence.entity.role.RolePermissionAssignmentEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RolePermissionAssignmentPersistenceMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "role", source = "role")
    @Mapping(target = "permission", source = "permission")
    @Mapping(target = "assignedAt", source = "assignment.assignedAt")
    @Mapping(target = "assignedBy", source = "assignment.assignedBy")
    @Mapping(target = "expiresAt", source = "assignment.expiresAt")
    @Mapping(target = "reason", source = "assignment.reason")
    RolePermissionAssignmentEntity toNewEntity(RolePermissionAssignment assignment,
                                               RoleEntity role, PermissionEntity permission);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "revokedAt", source = "revokedAt")
    @Mapping(target = "revokedBy", source = "revokedBy")
    @Mapping(target = "reason", source = "reason")
    void updateEntity(RolePermissionAssignment assignment, @MappingTarget RolePermissionAssignmentEntity entity);

    default RolePermissionAssignment toDomain(RolePermissionAssignmentEntity entity) {
        return RolePermissionAssignment.restore(entity.getId(), entity.getRole().getUuid(),
                entity.getPermission().getUuid(), entity.getAssignedAt(), entity.getAssignedBy(),
                entity.getExpiresAt(), entity.getRevokedAt(), entity.getRevokedBy(), entity.getReason());
    }
}
