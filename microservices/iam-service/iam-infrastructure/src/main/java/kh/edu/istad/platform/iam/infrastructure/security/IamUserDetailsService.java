package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.usecase.user.LoadUserForAuthenticationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IamUserDetailsService implements UserDetailsService {
    private final LoadUserForAuthenticationUseCase loadUser;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        try {
            return new IamUserDetails(loadUser.execute(usernameOrEmail));
        } catch (AuthenticationFailedException exception) {
            throw new UsernameNotFoundException("Invalid username or password", exception);
        }
    }
}