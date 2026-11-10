package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SocialLoginFailureHandler implements AuthenticationFailureHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SocialLoginFailureHandler.class);
    private final Clock clock;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.debug("Social authentication failed", exception);
        if (exception instanceof FederatedProfileRequiredException pending) {
            var session = request.getSession();
            request.changeSessionId();
            session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            session.setAttribute("iam.pendingProfile", new PendingFederatedProfile(
                    UUID.randomUUID().toString(), pending.getRegistrationId(), pending.getIdentity(),
                    clock.instant().plus(Duration.ofMinutes(10))));
            response.sendRedirect("/register/complete");
            return;
        }
        if (exception instanceof OAuth2AuthenticationException oauth
                && "account_link_required".equals(oauth.getError().getErrorCode())) {
            response.sendRedirect("/login?error=account_link_required");
            return;
        }
        response.sendRedirect("/login?error");
    }
}
