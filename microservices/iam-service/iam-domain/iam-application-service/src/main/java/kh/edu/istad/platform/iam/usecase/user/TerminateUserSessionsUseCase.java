package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.audit.Audited;
import kh.edu.istad.platform.iam.audit.AuditAction;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import kh.edu.istad.platform.iam.port.out.user.UserSessions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TerminateUserSessionsUseCase {
    private final UserRepository users;
    private final UserSessions sessions;
    @Transactional
    @Audited(action = AuditAction.SESSION_TERMINATED, target = "#p0", related = "#p1")
    public void terminate(UUID userUuid, UUID sessionId) {
        requireUser(userUuid);
        sessions.terminate(userUuid, sessionId);
    }
    @Transactional
    @Audited(action = AuditAction.SESSIONS_TERMINATED, target = "#p0")
    public void terminateAll(UUID userUuid) {
        requireUser(userUuid);
        sessions.terminateAll(userUuid);
    }
    private void requireUser(UUID uuid) { users.findByUuid(uuid).orElseThrow(() -> new ResourceNotFoundException("User", uuid)); }
}
