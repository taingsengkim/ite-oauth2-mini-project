package kh.edu.istad.platform.iam.usecase.audit;

import kh.edu.istad.platform.iam.audit.AuditAction;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.port.out.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ListAccountActivityUseCase {
    private final CurrentUserProvider currentUser;
    private final ListAuditLogsUseCase logs;
    public record Activity(UUID uuid, Instant occurredAt, String action, boolean performedByYou,
                           String ipAddress, String userAgent) {}
    @Transactional(readOnly = true)
    public PageResult<Activity> execute(String action, Instant from, Instant to, int page, int size) {
        if (action != null && !action.isBlank() && Arrays.stream(AuditAction.values())
                .noneMatch(value -> value.resource().equals("user") && value.code().equals(action))) {
            throw new IamDomainException(IamDomainException.Code.VALIDATION_ERROR, "Unknown account activity action");
        }
        var uuid = currentUser.currentUserUuid();
        // Match the affected account, rather than everything this actor did to other users.
        var result = logs.execute(new AuditLogRepository.Query(null, action, "user", uuid.toString(), AuditLogRepository.Outcome.SUCCESS, from, to, page, size));
        var content = result.content().stream().map(event -> {
            boolean self = uuid.equals(event.actorUuid());
            return new Activity(event.uuid(), event.occurredAt(), event.action(), self,
                    self ? event.ipAddress() : null, self ? event.userAgent() : null);
        }).toList();
        return new PageResult<>(content, result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
