package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.dto.user.AuthUserResult;
import kh.edu.istad.platform.iam.mapper.user.UserApplicationMapper;
import kh.edu.istad.platform.iam.port.out.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoadUserForTokenUseCase {

    private final UserRepository users;
    private final UserApplicationMapper mapper;
    private final UserAuthorityResolver authorityResolver;

    @Transactional(readOnly = true)
    public AuthUserResult execute(String username) {
        if (username == null || username.isBlank()) {
            throw new AuthenticationFailedException();
        }

        var user = users.findByUsername(username)
                .orElseThrow(AuthenticationFailedException::new);

        if (!user.isEnabled()
                || !user.isAccountNonExpired()
                || !user.isAccountNonLocked()
                || !user.isCredentialsNonExpired()) {
            throw new AuthenticationFailedException();
        }

        return mapper.toAuthResult(user, authorityResolver.resolve(user.getUuid())
        );
    }
}
