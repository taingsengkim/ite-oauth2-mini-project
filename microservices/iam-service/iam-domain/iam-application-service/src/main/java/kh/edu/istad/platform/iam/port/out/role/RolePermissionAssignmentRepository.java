package kh.edu.istad.platform.iam.port.out.role;
import kh.edu.istad.platform.iam.model.RolePermissionAssignment;
import java.util.List;
import java.util.UUID;

public interface RolePermissionAssignmentRepository {
    List<RolePermissionAssignment> findByRoleUuid(UUID roleUuid);
    RolePermissionAssignment create(RolePermissionAssignment assignment);
    void update(RolePermissionAssignment assignment);
}
