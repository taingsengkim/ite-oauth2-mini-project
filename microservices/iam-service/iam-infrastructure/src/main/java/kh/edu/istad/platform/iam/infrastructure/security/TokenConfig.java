package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.dto.user.AuthUserResult;
import kh.edu.istad.platform.iam.usecase.user.LoadUserForTokenUseCase;
import kh.edu.istad.platform.iam.usecase.user.LoadOidcUserClaimsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.ArrayList;

@Configuration
@RequiredArgsConstructor
public class TokenConfig {
    private final LoadUserForTokenUseCase loadUser;
    private final LoadOidcUserClaimsUseCase loadClaims;


    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            boolean accessToken = OAuth2TokenType.ACCESS_TOKEN
                    .equals(context.getTokenType());

            boolean idToken = OidcParameterNames.ID_TOKEN
                    .equals(context.getTokenType().getValue());

            if (!accessToken && !idToken) {
                return;
            }

            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(
                    context.getAuthorizationGrantType())) {
                return;
            }

            var authentication = context.getPrincipal();

            if (authentication == null
                    || !authentication.isAuthenticated()) {
                throw invalidGrant();
            }

            AuthUserResult user;

            try {
                user = loadUser.execute(authentication.getName());
            } catch (AuthenticationFailedException exception) {
                throw invalidGrant();
            }

            if (!user.enabled()
                    || !user.accountNonExpired()
                    || !user.accountNonLocked()
                    || !user.credentialsNonExpired()) {
                throw invalidGrant();
            }

            try {
                var claims = loadClaims.execute(user.uuid(), context.getAuthorizedScopes());
                context.getClaims().claims(values -> values.putAll(claims));
            } catch (AuthenticationFailedException exception) {
                throw invalidGrant();
            }

            if (accessToken) {
                var authorities = user.authorities().stream()
                        .distinct()
                        .sorted()
                        .toList();

                var roles = authorities.stream()
                        .filter(value -> value.startsWith("ROLE_"))
                        .map(value -> value.substring(5))
                        .toList();

                var permissions = authorities.stream()
                        .filter(value -> !value.startsWith("ROLE_"))
                        .toList();

                context.getClaims()
                        .claim("authorities", new ArrayList<>(authorities))
                        .claim("roles", new ArrayList<>(roles))
                        .claim("permissions", new ArrayList<>(permissions));
            }
        };
    }

    private static OAuth2AuthenticationException invalidGrant() {
        return new OAuth2AuthenticationException(
                new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_GRANT,
                        "User authentication is no longer valid",
                        null
                )
        );
    }
}
