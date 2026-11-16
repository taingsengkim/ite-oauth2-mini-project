package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.port.out.user.UserSessions;
import kh.edu.istad.platform.iam.usecase.user.GetUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard-api/users/{uuid}/sessions")
public class DashboardUserSessionController {
    private final UserSessions sessions;
    private final kh.edu.istad.platform.iam.usecase.user.TerminateUserSessionsUseCase terminateSessions;
    private final GetUserUseCase getUser;

    @GetMapping
    public ResponseEntity<List<UserSessions.Session>> list(@PathVariable("uuid") UUID uuid, HttpServletRequest request) {
        getUser.execute(uuid);
        var current = request.getSession(false);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(sessions.list(uuid, current == null ? null : current.getId()));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> terminate(@PathVariable("uuid") UUID uuid, @PathVariable("sessionId") UUID sessionId) {
        getUser.execute(uuid);
        terminateSessions.terminate(uuid, sessionId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> terminateAll(@PathVariable("uuid") UUID uuid) {
        getUser.execute(uuid);
        terminateSessions.terminateAll(uuid);
        return ResponseEntity.noContent().build();
    }
}
