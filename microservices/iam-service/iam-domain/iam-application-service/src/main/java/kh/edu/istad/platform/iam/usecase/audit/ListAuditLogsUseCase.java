package kh.edu.istad.platform.iam.usecase.audit;

import kh.edu.istad.platform.iam.audit.AuditAction;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ListAuditLogsUseCase {
    private final AuditLogRepository repository;
    @Transactional(readOnly = true)
    public PageResult<AuditLogRepository.Event> execute(AuditLogRepository.Query query) {
        require(query.page() >= 0 && query.page() <= 100000 && query.size() >= 1 && query.size() <= 100, "Invalid audit log pagination");
        require(query.actor() == null || query.actor().length() <= 255, "Actor filter is too long");
        require(query.targetId() == null || query.targetId().length() <= 100, "Target filter is too long");
        require(blank(query.action()) || Arrays.stream(AuditAction.values()).anyMatch(action -> action.code().equals(query.action())), "Unknown audit action");
        require(blank(query.resource()) || Arrays.stream(AuditAction.values()).anyMatch(action -> action.resource().equals(query.resource())), "Unknown audit resource");
        require(query.from() == null || query.to() == null || !query.from().isAfter(query.to()), "From must be before To");
        return repository.findAll(query);
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private void require(boolean condition, String message) {
        if (!condition) throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, message);
    }
}
