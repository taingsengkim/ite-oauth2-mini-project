package kh.edu.istad.platform.iam.infrastructure.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InitRegisteredClient implements ApplicationRunner {

    private final RegisteredClientRepository clients;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        var existing = clients.findByClientId("oidc-client");

        RegisteredClient.Builder builder = existing == null
                ? RegisteredClient.withId(UUID.randomUUID().toString())
                : RegisteredClient.from(existing);

        var tokenSettings = TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.ofHours(5))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .authorizationCodeTimeToLive(Duration.ofMinutes(3))
                .reuseRefreshTokens(false)
                .build();

        var clientSettings = ClientSettings.builder()
                .requireProofKey(true)
                .requireAuthorizationConsent(true)
                .build();

        var client = builder
                .clientId("oidc-client")
                .clientName("OIDC Client")
                .clientSecret(null)
                .clientAuthenticationMethods(methods -> {
                    methods.clear();
                    methods.add(ClientAuthenticationMethod.NONE);
                })
                .authorizationGrantTypes(grants -> {
                    grants.clear();
                    grants.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                    grants.add(AuthorizationGrantType.REFRESH_TOKEN);
                })
                .redirectUri(
                        "http://127.0.0.1:8080/login/oauth2/code/oidc-client"
                )
                .redirectUri(
                        "https://oauth.pstmn.io/v1/browser-callback"
                )
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope(OidcScopes.PHONE)
                .scope(OidcScopes.ADDRESS)
                .clientSettings(clientSettings)
                .tokenSettings(tokenSettings)
                .build();

        clients.save(client);
        initializeRegistrationClient();
    }


    private void initializeRegistrationClient() {
        if (clients.findByClientId("client-registrar") != null) {
            return;
        }

        String secret = environment.getRequiredProperty(
                "IAM_REGISTRATION_CLIENT_SECRET"
        );

        if (secret.isBlank()) {
            throw new IllegalStateException(
                    "IAM_REGISTRATION_CLIENT_SECRET must not be blank"
            );
        }

        var client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("client-registrar")
                .clientName("Client Registrar")
                .clientSecret(passwordEncoder.encode(secret))
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .authorizationGrantType(
                        AuthorizationGrantType.CLIENT_CREDENTIALS
                )
                .scope("client.create")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .build())
                .build();

        clients.save(client);

    }


}
