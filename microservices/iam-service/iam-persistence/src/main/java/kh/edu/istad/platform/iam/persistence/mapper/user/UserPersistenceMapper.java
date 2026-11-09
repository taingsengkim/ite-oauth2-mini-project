package kh.edu.istad.platform.iam.persistence.mapper.user;

import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.model.UserSnapshot;
import kh.edu.istad.platform.iam.persistence.entity.user.AddressEmbeddable;
import kh.edu.istad.platform.iam.persistence.entity.user.UserEntity;
import kh.edu.istad.platform.iam.valueobject.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    UserEntity toNewEntity(User user);

    UserSnapshot toSnapshot(UserEntity entity);

    AddressEmbeddable toEmbeddable(Address address);

    Address toAddress(AddressEmbeddable address);

    default User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.restore(toSnapshot(entity));
    }


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "givenName", source = "givenName")
    @Mapping(target = "familyName", source = "familyName")
    @Mapping(target = "nickname", source = "nickname")
    @Mapping(target = "locale", source = "locale")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "phoneNumberVerified", source = "phoneNumberVerified")
    void updateProfileEntity(User user, @MappingTarget UserEntity entity);


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "accountNonLocked", source = "accountNonLocked")
    @Mapping(target = "failedLoginAttempts", source = "failedLoginAttempts")
    void updateAccountStateEntity(User user, @MappingTarget UserEntity entity);



    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "deletedAt", source = "deletedAt")
    @Mapping(target = "deletedBy", source = "deletedBy")
    void softDeleteEntity(
            User user,
            @MappingTarget UserEntity entity
    );



    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "passwordChangedAt", source = "passwordChangedAt")
    @Mapping(target = "credentialsNonExpired", source = "credentialsNonExpired")
    void updatePasswordEntity(
            User user,
            @MappingTarget UserEntity entity
    );


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "failedLoginAttempts", source = "failedLoginAttempts")
    @Mapping(target = "lastLoginAt", source = "lastLoginAt")
    void updateLoginMetadataEntity(
            User user,
            @MappingTarget UserEntity entity
    );


}