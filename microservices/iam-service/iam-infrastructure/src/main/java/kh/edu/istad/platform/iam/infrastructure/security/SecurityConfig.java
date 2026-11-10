package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerChain(
            HttpSecurity http) throws Exception {

        var authorizationServer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServer.getEndpointsMatcher())
                .with(authorizationServer, server ->
//                        server.oidc(Customizer.withDefaults())
                                server.authorizationEndpoint(endpoint -> endpoint.consentPage("/oauth2/consent"))
                                        .oidc(oidc -> oidc
                                        .clientRegistrationEndpoint(registration -> registration
                                                .authenticationProviders(providers -> providers.forEach(provider -> {
                                                    if (provider instanceof OidcClientRegistrationAuthenticationProvider registrar) {
                                                        registrar.setAuthenticationValidator(
                                                                OidcClientRegistrationAuthenticationValidator
                                                                        .DEFAULT_REDIRECT_URI_VALIDATOR
                                                                        .andThen(OidcClientRegistrationAuthenticationValidator
                                                                                .DEFAULT_POST_LOGOUT_REDIRECT_URI_VALIDATOR)
                                                                        .andThen(OidcClientRegistrationAuthenticationValidator
                                                                                .DEFAULT_JWK_SET_URI_VALIDATOR)
                                                                        .andThen(context -> {
                                                                            OidcClientRegistrationAuthenticationToken authentication =
                                                                                    context.getAuthentication();

                                                                            var scopes = authentication
                                                                                    .getClientRegistration()
                                                                                    .getScopes();

                                                                            var allowed = Set.of(
                                                                                    "openid", "profile", "email", "phone", "address"
                                                                            );

                                                                            if (scopes != null
                                                                                    && !allowed.containsAll(scopes)) {
                                                                                throw new OAuth2AuthenticationException(
                                                                                        new OAuth2Error(
                                                                                                "invalid_scope",
                                                                                                "Only openid, profile, email, phone and address are allowed",
                                                                                                null
                                                                                        )
                                                                                );
                                                                            }
                                                                        })
                                                        );
                                                    }
                                                }))
                                        )
                                 )
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer(resource ->
                        resource.jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiChain(HttpSecurity http, ApiSecurityErrorHandler errors) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST, "/api/auth/register"
                        ).permitAll()

                        .requestMatchers(
                                "/api/me", "/api/me/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/*/roles",
                                "/api/users/*/permissions"
                        ).hasAuthority("user:assign")

                        .requestMatchers(
                                "/error",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*/roles/*",
                                "/api/users/*/permissions/*"
                        ).hasAuthority("user:assign")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*/identities/*"
                        ).hasAuthority("user:update")

                        .requestMatchers(
                                HttpMethod.GET, "/api/users/**"
                        ).hasAuthority("user:read")
                        .requestMatchers(
                                HttpMethod.POST, "/api/users"
                        ).hasAuthority("user:create")
                        .requestMatchers(
                                HttpMethod.PATCH, "/api/users/*"
                        ).hasAuthority("user:update")
                        .requestMatchers(
                                HttpMethod.PUT, "/api/users/*/password"
                        ).hasAuthority("user:update")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/*/enable",
                                "/api/users/*/disable",
                                "/api/users/*/lock",
                                "/api/users/*/unlock"
                        ).hasAuthority("user:update")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/users/*"
                        ).hasAuthority("user:delete")

                        .requestMatchers(
                                HttpMethod.POST, "/api/roles/*/permissions"
                        ).hasAuthority("role:assign")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/roles/*/permissions/*"
                        ).hasAuthority("role:assign")
                        .requestMatchers(
                                HttpMethod.GET, "/api/roles/**"
                        ).hasAuthority("role:read")
                        .requestMatchers(
                                HttpMethod.POST, "/api/roles"
                        ).hasAuthority("role:create")
                        .requestMatchers(
                                HttpMethod.PATCH, "/api/roles/*"
                        ).hasAuthority("role:update")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/roles/*/activate",
                                "/api/roles/*/deactivate"
                        ).hasAuthority("role:update")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/roles/*"
                        ).hasAuthority("role:delete")

                        .requestMatchers(
                                HttpMethod.GET, "/api/permissions/**"
                        ).hasAuthority("permission:read")
                        .requestMatchers(
                                HttpMethod.POST, "/api/permissions"
                        ).hasAuthority("permission:create")
                        .requestMatchers(
                                HttpMethod.PATCH, "/api/permissions/*"
                        ).hasAuthority("permission:update")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/permissions/*/activate",
                                "/api/permissions/*/deactivate"
                        ).hasAuthority("permission:update")
                        .requestMatchers(
                                HttpMethod.DELETE, "/api/permissions/*"
                        ).hasAuthority("permission:delete")

                        .anyRequest().denyAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors)
                )
                .oauth2ResourceServer(resource -> resource
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(apiAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain browserChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            IamOidcUserService oidcUserService,
            IamOAuth2UserService oauth2UserService,
            IdentityLinkAuthorizationRequestResolver authorizationRequestResolver,
            IdentityLinkAuthorizationRequestRepository authorizationRequestRepository,
            SocialLoginFailureHandler socialLoginFailureHandler
    ) throws Exception {

        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error", "/register", "/register/complete", "/register/complete/cancel", "/assets/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .permitAll()
                        .failureHandler(socialLoginFailureHandler)
                        .successHandler((request, response, authentication) -> {
                            if (Boolean.TRUE.equals(request.getAttribute("iam.identityLinked"))) {
                                new org.springframework.security.web.savedrequest.HttpSessionRequestCache()
                                        .removeRequest(request, response);
                                response.sendRedirect("/profile/accounts?linked=1");
                            } else {
                                new org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler()
                                        .onAuthenticationSuccess(request, response, authentication);
                            }
                        })
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(
                                        authorizationRequestResolver
                                )
                                .authorizationRequestRepository(
                                        authorizationRequestRepository
                                )
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)
                                .userService(oauth2UserService)
                        )
                );

        return http.build();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(
            IamUserDetailsService users,
            PasswordEncoder passwordEncoder
    ) {
        var provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {

        return new DefaultAuthenticationEventPublisher(publisher);
    }


    private JwtAuthenticationConverter apiAuthenticationConverter() {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("authorities");
        authorities.setAuthorityPrefix("");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        return converter;
    }
}
