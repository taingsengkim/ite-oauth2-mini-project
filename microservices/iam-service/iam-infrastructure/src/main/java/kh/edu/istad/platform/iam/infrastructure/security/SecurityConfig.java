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
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(0)
    SecurityFilterChain recoveryApiChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/recovery-api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/recovery-api/context").permitAll()
                        .requestMatchers(HttpMethod.POST, "/recovery-api/request", "/recovery-api/reset").permitAll()
                        .anyRequest().denyAll())
                .requestCache(org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> response.sendError(401))
                        .accessDeniedHandler((request, response, exception) -> response.sendError(403)));
        return http.build();
    }

    @Bean
    org.springframework.boot.web.servlet.ServletListenerRegistrationBean<BrowserUserSessions> sessionListener(BrowserUserSessions sessions) {
        return new org.springframework.boot.web.servlet.ServletListenerRegistrationBean<>(sessions);
    }

    @Bean
    @Order(1)
    SecurityFilterChain authorizationServerChain(
            HttpSecurity http, BrowserUserSessions sessions, BrowserRequestCache browserRequestCache) throws Exception {

        var authorizationServer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServer.getEndpointsMatcher())
                .requestCache(cache -> cache.requestCache(browserRequestCache))
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

        http.addFilterBefore(new BrowserSessionTrackingFilter(sessions),
                org.springframework.security.web.authentication.logout.LogoutFilter.class);
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
    SecurityFilterChain dashboardApiChain(HttpSecurity http, BrowserUserSessions sessions) throws Exception {
        http
                .securityMatcher("/dashboard-api/**")
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/security").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/identities").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/sessions").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/login-history").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/activity").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/account/deactivation").authenticated()
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/account/deactivation").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/account/sessions", "/dashboard-api/account/sessions/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/account/identities/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/dashboard-api/account/security/password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/dashboard-api/account/profile").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/session", "/dashboard-api/password-policy", "/dashboard-api/client-defaults").authenticated()
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/configuration").hasAuthority("configuration:read")
                        .requestMatchers(HttpMethod.PUT, "/dashboard-api/configuration").hasAuthority("configuration:update")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/overview").hasAuthority("overview:read")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/audit-logs", "/dashboard-api/audit-logs/options").hasAuthority("audit:read")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/clients", "/dashboard-api/clients/*").hasAuthority("client:read")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/clients").hasAuthority("client:create")
                        .requestMatchers(HttpMethod.PUT, "/dashboard-api/clients/*").hasAuthority("client:update")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/clients/*/secret").hasAuthority("client:update")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/clients/*").hasAuthority("client:delete")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/login-history", "/dashboard-api/users/*/login-history").hasAuthority("user:read")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/users/*/sessions").hasAuthority("user:read")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/users/*/sessions", "/dashboard-api/users/*/sessions/*").hasAuthority("user:update")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/users/*/roles").hasAuthority("user:read")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/users/*/roles").hasAuthority("user:assign")
                        .requestMatchers(HttpMethod.PUT, "/dashboard-api/users/*/roles/*").hasAuthority("user:assign")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/users/*/roles/*").hasAuthority("user:assign")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/roles", "/dashboard-api/roles/*",
                                "/dashboard-api/roles/*/permissions").hasAuthority("role:read")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/roles").hasAuthority("role:create")
                        .requestMatchers(HttpMethod.PATCH, "/dashboard-api/roles/*").hasAuthority("role:update")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/roles/*/activate",
                                "/dashboard-api/roles/*/deactivate").hasAuthority("role:update")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/roles/*").hasAuthority("role:delete")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/roles/*/permissions").hasAuthority("role:assign")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/roles/*/permissions/*").hasAuthority("role:assign")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/permissions", "/dashboard-api/permissions/*").hasAuthority("permission:read")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/permissions").hasAuthority("permission:create")
                        .requestMatchers(HttpMethod.PATCH, "/dashboard-api/permissions/*").hasAuthority("permission:update")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/permissions/*").hasAuthority("permission:delete")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/permissions/*/activate", "/dashboard-api/permissions/*/deactivate").hasAuthority("permission:update")
                        .requestMatchers(HttpMethod.GET, "/dashboard-api/users/*/permissions", "/dashboard-api/users/*/authorities").hasAuthority("user:read")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/users/*/permissions").hasAuthority("user:assign")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/users/*/permissions/*").hasAuthority("user:assign")
                        .requestMatchers(HttpMethod.POST, "/dashboard-api/users").hasAuthority("user:create")
                        .requestMatchers(HttpMethod.PATCH, "/dashboard-api/users/*").hasAuthority("user:update")
                        .requestMatchers(HttpMethod.DELETE, "/dashboard-api/users/*").hasAuthority("user:delete")
                        .requestMatchers(HttpMethod.PUT, "/dashboard-api/users/*/password").hasAuthority("user:update")
                        .requestMatchers(HttpMethod.POST,
                                "/dashboard-api/users/*/enable", "/dashboard-api/users/*/disable",
                                "/dashboard-api/users/*/lock", "/dashboard-api/users/*/unlock"
                        ).hasAuthority("user:update")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/dashboard-api/users",
                                "/dashboard-api/users/*"
                        ).hasAuthority("user:read")

                        .anyRequest().denyAll()
                )
                .requestCache(RequestCacheConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(401))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(403))
                );

        http.addFilterBefore(new BrowserSessionTrackingFilter(sessions),
                org.springframework.security.web.authentication.logout.LogoutFilter.class);
        return http.build();
    }

    @Bean
    @Order(4)
    SecurityFilterChain browserChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            IamOidcUserService oidcUserService,
            IamOAuth2UserService oauth2UserService,
            org.springframework.security.oauth2.client.registration.ClientRegistrationRepository registrations,
            IdentityLinkAuthorizationRequestResolver authorizationRequestResolver,
            IdentityLinkAuthorizationRequestRepository authorizationRequestRepository,
            LoginHistoryRecorder loginHistory,
            BrowserUserSessions sessions,
            SocialLoginFailureHandler socialLoginFailureHandler,
            BrowserRequestCache browserRequestCache,
            BrowserLoginSuccessHandler loginSuccess,
            BrowserLogoutSuccessHandler logoutSuccess
    ) throws Exception {

        http
                .authenticationProvider(authenticationProvider)
                .requestCache(cache -> cache.requestCache(browserRequestCache))
                .logout(logout -> logout.invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(logoutSuccess).permitAll())
                .addFilterBefore(new TelegramLoginOriginFilter(
                                registrations.findByRegistrationId("telegram").getRedirectUri()),
                        org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/error", "/register", "/register/complete", "/register/complete/cancel", "/assets/**", "/account/identities/result",
                                "/dashboard/recovery.html", "/dashboard/assets/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler((request, response, authentication) -> {
                            loginHistory.success(request, authentication);
                            loginSuccess.onAuthenticationSuccess(request, response, authentication);
                        })
                        .failureHandler((request, response, exception) -> {
                            loginHistory.passwordFailure(request, exception);
                            new org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler("/login?error")
                                    .onAuthenticationFailure(request, response, exception);
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .permitAll()
                        .failureHandler(socialLoginFailureHandler)
                        .successHandler((request, response, authentication) -> {
                            loginHistory.success(request, authentication);
                            if (Boolean.TRUE.equals(request.getAttribute("iam.identityLinked"))) {
                                browserRequestCache.removeRequest(request, response);
                                response.sendRedirect(Boolean.TRUE.equals(request.getAttribute("iam.identityLinkPopup"))
                                        ? "/account/identities/result" : "/profile/accounts?linked=1");
                            } else {
                                loginSuccess.onAuthenticationSuccess(request, response, authentication);
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

        http.addFilterBefore(new BrowserSessionTrackingFilter(sessions),
                org.springframework.security.web.authentication.logout.LogoutFilter.class);
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

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );
        configuration.setAllowedMethods(
                List.of("GET", "POST", "OPTIONS")
        );
        configuration.setAllowedHeaders(
                List.of("Content-Type", "Authorization", "Accept")
        );

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
