package kh.edu.istad.platform.iam.persistence.mapper.permission;

import kh.edu.istad.platform.iam.model.Permission;
import kh.edu.istad.platform.iam.persistence.entity.permission.PermissionEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PermissionPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    PermissionEntity toNewEntity(Permission permission);

    default Permission toDomain(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Permission.restore(
                entity.getUuid(),
                entity.getResource(),
                entity.getAction(),
                entity.getDescription(),
                entity.isSystem(),
                entity.isActive(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy());
    }


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "resource", source = "resource")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "description", source = "description")
    void updateEntity(
            Permission permission,
            @MappingTarget PermissionEntity entity
    );


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "active",source = "active")
    @Mapping(target = "deleted",source = "deleted")
    @Mapping(target = "deletedAt",source = "deletedAt")
    @Mapping(target = "deletedBy",source = "deletedBy")
    void softDeleteEntity(Permission permission, @MappingTarget PermissionEntity entity);



    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "active", source = "active")
    void updateActiveState(Permission permission, @MappingTarget PermissionEntity entity);
}
