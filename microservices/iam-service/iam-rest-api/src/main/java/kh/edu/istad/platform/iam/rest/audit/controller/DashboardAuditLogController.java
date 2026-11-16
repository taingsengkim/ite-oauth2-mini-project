package kh.edu.istad.platform.iam.rest.audit.controller;

import kh.edu.istad.platform.iam.audit.AuditAction;
import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.port.out.audit.AuditLogRepository.*;
import kh.edu.istad.platform.iam.usecase.audit.ListAuditLogsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/audit-logs")
public class DashboardAuditLogController {
    private final ListAuditLogsUseCase listLogs;
    public record ActionOption(String code, String label, String resource) {}
    @GetMapping("/options") public ResponseEntity<List<ActionOption>> options() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Arrays.stream(AuditAction.values())
                .map(action -> new ActionOption(action.code(), action.label(), action.resource())).toList());
    }
    @GetMapping public ResponseEntity<PageResult<Event>> list(
            @RequestParam(name = "actor", required = false) String actor,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "resource", required = false) String resource,
            @RequestParam(name = "targetId", required = false) String targetId,
            @RequestParam(name = "outcome", required = false) Outcome outcome,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(listLogs.execute(new Query(actor, action, resource, targetId, outcome, from, to, page, size)));
    }
}
