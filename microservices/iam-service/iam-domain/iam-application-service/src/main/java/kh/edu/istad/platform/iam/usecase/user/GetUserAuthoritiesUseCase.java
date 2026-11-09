package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.dto.user.UserAuthoritiesResult;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserAuthoritiesUseCase {

    private final UserRepository users;
    private final UserAuthorityResolver authorityResolver;

    @Transactional(readOnly = true)
    public UserAuthoritiesResult execute(UUID userUuid) {
        users.findByUuid(userUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", userUuid
                ));

        return authorityResolver.resolveDetails(userUuid);
    }
}