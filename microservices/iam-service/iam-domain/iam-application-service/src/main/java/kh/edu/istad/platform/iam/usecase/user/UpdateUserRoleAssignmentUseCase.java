package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.dto.user.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.dto.user.RoleAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserRoleAssignmentUseCase {
    private final RevokeRoleFromUserUseCase revoke;
    private final AssignRolesToUserUseCase assign;

    /** Keeps assignment history and rolls back the revocation if reassignment fails. */
    @Transactional
    public RoleAssignmentResult execute(UUID userUuid, UUID roleUuid, Instant expiresAt, String reason) {
        revoke.execute(userUuid, roleUuid, reason);
        return assign.execute(new AssignRolesToUserCommand(userUuid, List.of(roleUuid), expiresAt, reason)).getFirst();
    }
}
