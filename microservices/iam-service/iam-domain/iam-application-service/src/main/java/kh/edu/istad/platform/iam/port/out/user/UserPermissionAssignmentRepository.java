package kh.edu.istad.platform.iam.port.out.user;

import kh.edu.istad.platform.iam.model.UserPermissionAssignment;

import java.util.List;
import java.util.UUID;

public interface UserPermissionAssignmentRepository {

    List<UserPermissionAssignment> findByUserUuid(UUID userUuid);

    UserPermissionAssignment create(UserPermissionAssignment assignment);

    void update(UserPermissionAssignment assignment);
}
