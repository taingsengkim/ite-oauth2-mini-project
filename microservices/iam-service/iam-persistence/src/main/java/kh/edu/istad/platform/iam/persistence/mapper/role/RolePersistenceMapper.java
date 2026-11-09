package kh.edu.istad.platform.iam.persistence.mapper.role;

import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.persistence.entity.role.RoleEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RolePersistenceMapper {
    @Mapping(target = "id", ignore = true)
    RoleEntity toNewEntity(Role role);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    void updateEntity(Role role, @MappingTarget RoleEntity entity);

    default Role toDomain(RoleEntity entity) {
        if (entity == null) return null;
        return Role.restore(entity.getUuid(), entity.getName(), entity.getDescription(), entity.isSystem(),
                entity.isActive(), entity.isDeleted(), entity.getCreatedAt(), entity.getCreatedBy(),
                entity.getUpdatedAt(), entity.getUpdatedBy(), entity.getDeletedAt(), entity.getDeletedBy());
    }
}
