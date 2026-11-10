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
        log.warn("Social authentication failed", exception);
        if (exception instanceof OAuth2AuthenticationException oauth) {
            String code = oauth.getError().getErrorCode();
            // Expose only known protocol codes, never provider messages or tokens.
            if (!java.util.Set.of("account_link_required", "authorization_request_not_found",
                    "invalid_state_parameter", "invalid_token_response", "invalid_id_token",
                    "invalid_user_info_response", "invalid_request", "invalid_client",
                    "invalid_grant", "access_denied", "server_error", "temporarily_unavailable",
                    "profile_completion_required").contains(code)) {
                code = "social_login_failed";
            }
            response.sendRedirect("/login?error=" + code);
            return;
        }
        response.sendRedirect("/login?error");
    }
}
