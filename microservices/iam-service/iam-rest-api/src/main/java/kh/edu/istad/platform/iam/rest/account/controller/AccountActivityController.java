package kh.edu.istad.platform.iam.rest.account.controller;

import kh.edu.istad.platform.iam.dto.pagination.PageResult;
import kh.edu.istad.platform.iam.usecase.audit.ListAccountActivityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/activity")
public class AccountActivityController {
    private final ListAccountActivityUseCase activity;
    @GetMapping public ResponseEntity<PageResult<ListAccountActivityUseCase.Activity>> list(
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(activity.execute(action, from, to, page, size));
    }
}
