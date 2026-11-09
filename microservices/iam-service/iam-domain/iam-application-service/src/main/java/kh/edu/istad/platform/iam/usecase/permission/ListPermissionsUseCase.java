package kh.edu.istad.platform.iam.usecase.permission;

import kh.edu.istad.platform.iam.dto.permission.PermissionFilterQuery;
import kh.edu.istad.platform.iam.dto.permission.PermissionResult;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.mapper.permission.PermissionApplicationMapper;
import kh.edu.istad.platform.iam.port.out.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListPermissionsUseCase {
    private final PermissionRepository repository;
    private final PermissionApplicationMapper mapper;

    @Transactional(readOnly = true)
    public PageResult<PermissionResult> execute(PermissionFilterQuery query) {
        var result = repository.findAll(query);
        return new PageResult<>(result.content().stream().map(mapper::toResult).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
