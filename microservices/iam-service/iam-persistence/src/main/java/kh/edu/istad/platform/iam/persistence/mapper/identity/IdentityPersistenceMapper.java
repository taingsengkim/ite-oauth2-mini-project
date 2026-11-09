package kh.edu.istad.platform.iam.persistence.mapper.identity;

import kh.edu.istad.platform.iam.model.Identity;
import kh.edu.istad.platform.iam.model.IdentitySnapshot;
import kh.edu.istad.platform.iam.persistence.entity.identity.IdentityEntity;
import kh.edu.istad.platform.iam.persistence.entity.user.UserEntity;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface IdentityPersistenceMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "uuid", source = "identity.uuid")
    @Mapping(target = "user", source = "owner")
    @Mapping(target = "provider", source = "identity.provider")
    @Mapping(target = "providerSubject", source = "identity.providerSubject")
    @Mapping(target = "email", source = "identity.email")
    @Mapping(target = "emailVerified", source = "identity.emailVerified")
    @Mapping(target = "displayName", source = "identity.displayName")
    @Mapping(target = "username", source = "identity.username")
    @Mapping(target = "pictureUrl", source = "identity.pictureUrl")
    @Mapping(target = "attributes", source = "identity.attributes")
    @Mapping(target = "linkedAt", source = "identity.linkedAt")
    @Mapping(target = "lastLoginAt", source = "identity.lastLoginAt")
    @Mapping(target = "deleted", source = "identity.deleted")
    @Mapping(target = "deletedAt", source = "identity.deletedAt")
    @Mapping(target = "deletedBy", source = "identity.deletedBy")
    IdentityEntity toNewEntity(
            Identity identity,
            UserEntity owner
    );

    @Mapping(target = "userUuid", source = "user.uuid")
    IdentitySnapshot toSnapshot(IdentityEntity entity);

    default Identity toDomain(IdentityEntity entity) {
        if (entity == null) {
            return null;
        }

        return Identity.restore(toSnapshot(entity));
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "lastLoginAt", source = "lastLoginAt")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    void updateEntity(
            Identity identity,
            @MappingTarget IdentityEntity entity
    );


}