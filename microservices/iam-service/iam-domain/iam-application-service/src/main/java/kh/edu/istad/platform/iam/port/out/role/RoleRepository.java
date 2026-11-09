package kh.edu.istad.platform.iam.port.out.role;
import kh.edu.istad.platform.iam.model.Role;
import kh.edu.istad.platform.iam.dto.role.RoleFilterQuery;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Role create(Role role);
    Optional<Role> findByUuid(UUID uuid);
    Optional<Role> findByUuidForUpdate(UUID uuid);
    PageResult<Role> findAll(RoleFilterQuery query);
    Role update(Role role);
    Optional<Role> findByName(String name);
}
