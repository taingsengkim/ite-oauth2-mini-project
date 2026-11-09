package kh.edu.istad.platform.iam.port.out.user;

import kh.edu.istad.platform.iam.model.UserRoleAssignment;

import java.util.List;
import java.util.UUID;

public interface UserRoleAssignmentRepository {

    List<UserRoleAssignment> findByUserUuid(UUID userUuid);

    UserRoleAssignment create(UserRoleAssignment assignment);

    void update(UserRoleAssignment assignment);
}