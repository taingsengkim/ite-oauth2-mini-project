package kh.edu.istad.platform.iam.port.out.user;


import kh.edu.istad.platform.iam.dto.user.UserFilterQuery;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User create(User user);

    Optional<User> findByUuid(UUID uuid);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    PageResult<User> findAll(UserFilterQuery query);

    User updateProfile(User user);

    void updateAccountState(User user);

    void softDelete(User user);

    void updatePassword(User user);

    Optional<User> findByUuidForUpdate(UUID uuid);

    void updateLoginMetadata(User user);

    Optional<User> findByLoginForUpdate(String usernameOrEmail);

}