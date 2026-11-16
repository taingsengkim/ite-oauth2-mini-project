package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListLoginHistoryUseCase {
    private final LoginHistoryRepository history;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public PageResult<LoginHistoryRepository.Event> execute(LoginHistoryRepository.Query query) {
        if (query.page() < 0 || query.page() > 100000 || query.size() < 1 || query.size() > 100
                || (query.login() != null && query.login().length() > 255)
                || (query.from() != null && query.to() != null && query.from().isAfter(query.to()))) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Invalid login history filters or date range");
        }
        if (query.userUuid() != null) users.findByUuid(query.userUuid())
                .orElseThrow(() -> new ResourceNotFoundException("User", query.userUuid()));
        return history.findAll(query);
    }
}
