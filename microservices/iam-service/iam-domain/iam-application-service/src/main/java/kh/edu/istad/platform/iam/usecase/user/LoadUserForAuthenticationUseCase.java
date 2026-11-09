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
public class LoadUserForAuthenticationUseCase {
    private final UserRepository repository;
    private final UserApplicationMapper mapper;
    private final UserAuthorityResolver authorityResolver;


    @Transactional(readOnly = true)
    public AuthUserResult execute(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new AuthenticationFailedException();
        }

        String login = usernameOrEmail.strip();

        var user = repository.findByUsername(login)
                .or(() -> repository.findByEmail(login))
                .orElseThrow(AuthenticationFailedException::new);

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new AuthenticationFailedException();
        }

        return mapper.toAuthResult(user, authorityResolver.resolve(user.getUuid()));
    }
}