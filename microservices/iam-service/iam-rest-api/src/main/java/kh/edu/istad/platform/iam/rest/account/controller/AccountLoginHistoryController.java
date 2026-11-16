package kh.edu.istad.platform.iam.rest.account.controller;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository.*;
import kh.edu.istad.platform.iam.usecase.user.ListLoginHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/login-history")
public class AccountLoginHistoryController {
    private final CurrentUserProvider currentUser;
    private final ListLoginHistoryUseCase history;

    @GetMapping public ResponseEntity<PageResult<Event>> list(
            @RequestParam(name = "outcome", required = false) Outcome outcome,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "suspiciousOnly", defaultValue = "false") boolean suspiciousOnly,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(history.execute(
                new Query(currentUser.currentUserUuid(), null, outcome, from, to, suspiciousOnly, page, size)));
    }
}
