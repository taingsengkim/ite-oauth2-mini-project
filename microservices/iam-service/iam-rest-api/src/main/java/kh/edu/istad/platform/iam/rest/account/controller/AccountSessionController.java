package kh.edu.istad.platform.iam.rest.account.controller;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.port.out.user.UserSessions;
import kh.edu.istad.platform.iam.usecase.user.TerminateUserSessionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/account/sessions")
public class AccountSessionController {
    private final CurrentUserProvider currentUser;
    private final UserSessions sessions;
    private final TerminateUserSessionsUseCase terminateSessions;
    @GetMapping public ResponseEntity<List<UserSessions.Session>> list(HttpServletRequest request) {
        var session = request.getSession(false);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(sessions.list(currentUser.currentUserUuid(), session == null ? null : session.getId()));
    }
    @DeleteMapping("/{sessionId}") public ResponseEntity<Void> terminate(@PathVariable("sessionId") UUID sessionId) {
        terminateSessions.terminate(currentUser.currentUserUuid(), sessionId);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
    @DeleteMapping public ResponseEntity<Void> terminateAll() {
        terminateSessions.terminateAll(currentUser.currentUserUuid());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
