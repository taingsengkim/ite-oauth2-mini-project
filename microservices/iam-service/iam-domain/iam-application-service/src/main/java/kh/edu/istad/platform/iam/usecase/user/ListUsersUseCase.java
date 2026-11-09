package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.dto.user.UserFilterQuery;
import kh.edu.istad.platform.iam.dto.user.UserSummary;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.mapper.user.UserResultMapper;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {
    private final UserRepository repository;
    private final UserResultMapper mapper;

    @Transactional(readOnly = true)
    public PageResult<UserSummary> execute(UserFilterQuery query) {
        var result = repository.findAll(query);
        return new PageResult<>(result.content().stream().map(mapper::toSummary).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
