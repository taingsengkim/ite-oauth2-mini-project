package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.application.exception.AuthenticationRequiredException;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringSecurityCurrentUserProvider
        implements CurrentUserProvider {

    @Override
    public UUID currentUserUuid() {
        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationRequiredException();
        }

        var principal = authentication.getPrincipal();

        if (principal instanceof IamUserDetails user) {
            return user.getUuid();
        }

        if (principal instanceof IamOidcUser user) {
            return user.getUserUuid();
        }

        if (principal instanceof IamOAuth2User user) {
            return user.getUserUuid();
        }

        if (principal instanceof Jwt jwt) {
            String subject = jwt.getSubject();

            if (subject == null || subject.isBlank()) {
                throw new AuthenticationRequiredException();
            }

            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException exception) {
                throw new AuthenticationRequiredException();
            }
        }

        throw new AuthenticationRequiredException();
    }
}
