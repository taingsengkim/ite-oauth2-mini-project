package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kh.edu.istad.platform.iam.dto.identity.LinkIdentityIntent;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class IdentityLinkAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    private final HttpSessionOAuth2AuthorizationRequestRepository delegate =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(
            HttpServletRequest request
    ) {
        return delegate.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        delegate.saveAuthorizationRequest(
                authorizationRequest,
                request,
                response
        );
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        request.removeAttribute("iam.identityLinkIntent");

        var authorizationRequest =
                delegate.removeAuthorizationRequest(request, response);

        if (authorizationRequest == null) {
            return null;
        }

        Object value = authorizationRequest.getAttribute(
                "iam.identityLinkIntent"
        );

        if (value instanceof LinkIdentityIntent intent) {
            String registrationId =
                    authorizationRequest.getAttribute("registration_id");

            if (!Objects.equals(
                    authorizationRequest.getState(),
                    request.getParameter("state")
            )
                    || !intent.expiresAt().isAfter(clock.instant())
                    || !intent.registrationId().equals(registrationId)
                    || !intent.userUuid().equals(
                    currentUserProvider.currentUserUuid()
            )) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                "invalid_request",
                                "Identity linking request is invalid or expired",
                                null
                        )
                );
            }

            request.setAttribute("iam.identityLinkIntent", intent);
        }

        return authorizationRequest;
    }
}
