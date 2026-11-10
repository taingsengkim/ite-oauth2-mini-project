package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.dto.identity.LinkIdentityIntent;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;

@Component
public class IdentityLinkAuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public IdentityLinkAuthorizationRequestResolver(
            ClientRegistrationRepository registrations,
            CurrentUserProvider currentUserProvider,
            Clock clock
    ) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                registrations,
                "/oauth2/authorization"
        );
        this.delegate.setAuthorizationRequestCustomizer(
                org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers.withPkce());
        this.currentUserProvider = currentUserProvider;
        this.clock = clock;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request
    ) {
        return attachIntent(request, delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request,
            String registrationId
    ) {
        return attachIntent(
                request,
                delegate.resolve(request, registrationId)
        );
    }

    private OAuth2AuthorizationRequest attachIntent(
            HttpServletRequest request,
            OAuth2AuthorizationRequest authorizationRequest
    ) {
        if (authorizationRequest == null) {
            return null;
        }

        var session = request.getSession(false);

        if (session == null) {
            return authorizationRequest;
        }

        session.removeAttribute("iam.pendingProfile");
        Object value = session.getAttribute("iam.pendingIdentityLink");

        if (!(value instanceof LinkIdentityIntent intent)) {
            return authorizationRequest;
        }

        session.removeAttribute("iam.pendingIdentityLink");

        String registrationId =
                authorizationRequest.getAttribute("registration_id");

        if (!intent.expiresAt().isAfter(clock.instant())
                || !intent.registrationId().equals(registrationId)
                || !intent.userUuid().equals(
                currentUserProvider.currentUserUuid()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identity linking request is invalid or expired"
            );
        }

        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .attributes(attributes -> attributes.put(
                        "iam.identityLinkIntent",
                        intent
                ))
                .build();
    }
}