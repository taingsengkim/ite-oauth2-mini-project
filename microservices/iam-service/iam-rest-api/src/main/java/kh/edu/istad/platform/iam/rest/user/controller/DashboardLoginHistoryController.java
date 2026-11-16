package kh.edu.istad.platform.iam.rest.user.controller;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository.*;
import kh.edu.istad.platform.iam.usecase.user.ListLoginHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DashboardLoginHistoryController {
    private final ListLoginHistoryUseCase listHistory;

    @GetMapping({"/dashboard-api/login-history", "/dashboard-api/users/{uuid}/login-history"})
    public ResponseEntity<PageResult<Event>> list(
            @PathVariable(name = "uuid", required = false) UUID uuid,
            @RequestParam(name = "login", required = false) String login,
            @RequestParam(name = "outcome", required = false) Outcome outcome,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "suspiciousOnly", defaultValue = "false") boolean suspiciousOnly,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(listHistory.execute(
                new Query(uuid, login, outcome, from, to, suspiciousOnly, page, size)));
    }
}
