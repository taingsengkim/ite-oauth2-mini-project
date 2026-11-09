package kh.edu.istad.platform.iam.infrastructure.audit;

import kh.edu.istad.platform.iam.application.exception.AuthenticationRequiredException;
import kh.edu.istad.platform.iam.infrastructure.security.IamOAuth2User;
import kh.edu.istad.platform.iam.infrastructure.security.IamOidcUser;
import kh.edu.istad.platform.iam.infrastructure.security.IamUserDetails;
import kh.edu.istad.platform.iam.port.out.CurrentActorProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityCurrentActorProvider
        implements CurrentActorProvider {

    @Override
    public String currentActor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "system";
        }

        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }

        var principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String subject = jwt.getSubject();

            if (subject == null || subject.isBlank()) {
                throw new AuthenticationRequiredException();
            }

            return subject;
        }

        if (principal instanceof IamUserDetails user) {
            return user.getUuid().toString();
        }

        if (principal instanceof IamOidcUser user) {
            return user.getUserUuid().toString();
        }
        if (principal instanceof IamOAuth2User user) {
            return user.getUserUuid().toString();
        }
        throw new AuthenticationRequiredException();
    }
}