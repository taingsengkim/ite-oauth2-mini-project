package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.port.out.user.LoginHistoryRepository.Outcome;
import kh.edu.istad.platform.iam.usecase.user.RecordLoginHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginHistoryRecorder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginHistoryRecorder.class);
    private final RecordLoginHistoryUseCase record;
    private static final Set<String> SOCIAL_REASONS = Set.of("account_link_required", "access_denied",
            "invalid_state_parameter", "authorization_request_not_found", "invalid_id_token",
            "invalid_token_response", "invalid_user_info_response", "invalid_grant", "invalid_client",
            "invalid_request", "server_error", "temporarily_unavailable");

    public void success(HttpServletRequest request, Authentication authentication) {
        if (Boolean.TRUE.equals(request.getAttribute("iam.identityLinked"))) return;
        UUID user;
        Object principal = authentication.getPrincipal();
        if (principal instanceof IamUserDetails local) user = local.getUuid();
        else if (principal instanceof IamOidcUser oidc) user = oidc.getUserUuid();
        else if (principal instanceof IamOAuth2User oauth) user = oauth.getUserUuid();
        else return;
        String provider = authentication instanceof OAuth2AuthenticationToken oauth
                ? oauth.getAuthorizedClientRegistrationId().toUpperCase(java.util.Locale.ROOT) : "LOCAL";
        String login = "LOCAL".equals(provider) ? request.getParameter("username") : authentication.getName();
        persist(request, user, login, provider, Outcome.SUCCESS, "authenticated");
    }

    public void passwordFailure(HttpServletRequest request, AuthenticationException exception) {
        String reason = exception instanceof LockedException ? "account_locked"
                : exception instanceof DisabledException ? "account_disabled"
                : exception instanceof AccountExpiredException ? "account_expired"
                : exception instanceof CredentialsExpiredException ? "credentials_expired"
                : exception instanceof BadCredentialsException ? "invalid_credentials" : "authentication_failed";
        persist(request, null, request.getParameter("username"), "LOCAL", Outcome.FAILURE, reason);
    }

    public void socialFailure(HttpServletRequest request, AuthenticationException exception) {
        if (exception instanceof FederatedProfileRequiredException) return; // Authentication is still in progress.
        String reason = "social_login_failed";
        if (exception instanceof OAuth2AuthenticationException oauth && SOCIAL_REASONS.contains(oauth.getError().getErrorCode()))
            reason = oauth.getError().getErrorCode();
        String prefix = request.getContextPath() + "/login/oauth2/code/";
        String provider = request.getRequestURI().startsWith(prefix)
                ? request.getRequestURI().substring(prefix.length()).toUpperCase(java.util.Locale.ROOT) : "SOCIAL";
        if (!Set.of("GOOGLE", "GITHUB", "FACEBOOK", "TELEGRAM").contains(provider)) provider = "SOCIAL";
        persist(request, null, null, provider, Outcome.FAILURE, reason);
    }

    private void persist(HttpServletRequest request, UUID user, String login, String provider, Outcome outcome, String reason) {
        try {
            // Use the servlet's resolved address; do not trust arbitrary forwarding headers here.
            record.execute(user, login, provider, outcome, reason, request.getRemoteAddr(), request.getHeader("User-Agent"));
        } catch (RuntimeException exception) {
            // An unavailable audit store must not turn a completed authentication into an ambiguous login error.
            log.error("Unable to persist login history", exception);
        }
    }
}
