package kh.edu.istad.platform.iam.usecase.role;

import kh.edu.istad.platform.iam.dto.role.RoleFilterQuery;
import kh.edu.istad.platform.iam.dto.role.RoleSummary;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.mapper.role.RoleApplicationMapper;
import kh.edu.istad.platform.iam.port.out.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListRolesUseCase {
    private final RoleRepository repository;
    private final RoleApplicationMapper mapper;

    @Transactional(readOnly = true)
    public PageResult<RoleSummary> execute(RoleFilterQuery query) {
        var result = repository.findAll(query);
        return new PageResult<>(result.content().stream().map(mapper::toSummary).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
