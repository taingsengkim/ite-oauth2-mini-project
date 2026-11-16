package kh.edu.istad.platform.iam.port.out.client;

import kh.edu.istad.platform.iam.dto.client.*;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import java.util.Optional;

public interface ClientManagementRepository {
    PageResult<ManagedClientResult> list(String search, int page, int size);
    Optional<ManagedClientResult> findById(String id);
    Optional<ManagedClientResult> findByIdForUpdate(String id);
    IssuedClientResult create(ClientDefinition definition);
    ManagedClientResult update(String id, ClientDefinition definition);
    IssuedClientResult rotateSecret(String id);
    void delete(String id);
}
