package kh.edu.istad.platform.iam.usecase.identity;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.identity.IdentityResult;
import kh.edu.istad.platform.iam.mapper.identity.IdentityApplicationMapper;
import kh.edu.istad.platform.iam.port.out.identity.IdentityRepository;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListUserIdentitiesUseCase {

    private final UserRepository users;
    private final IdentityRepository identities;
    private final IdentityApplicationMapper mapper;

    @Transactional(readOnly = true)
    public List<IdentityResult> execute(UUID userUuid) {
        Objects.requireNonNull(userUuid, "userUuid");

        users.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException("User", userUuid));

        return mapper.toResults(
                identities.findAllByUserUuid(userUuid)
        );
    }
}
