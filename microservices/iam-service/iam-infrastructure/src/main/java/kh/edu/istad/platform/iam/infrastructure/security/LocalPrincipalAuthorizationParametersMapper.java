package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

public final class LocalPrincipalAuthorizationParametersMapper
        extends JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper {

    public LocalPrincipalAuthorizationParametersMapper(JsonMapper mapper) {
        super(mapper);
    }

    @Override
    public List<SqlParameterValue> apply(OAuth2Authorization authorization) {
        Authentication principal = authorization.getAttribute(Principal.class.getName());
        if (principal != null && principal.isAuthenticated()) {
            var local = UsernamePasswordAuthenticationToken.authenticated(
                    principal.getName(), null, new ArrayList<>(principal.getAuthorities()));
            authorization = OAuth2Authorization.from(authorization)
                    .attribute(Principal.class.getName(), local).build();
        }
        return super.apply(authorization);
    }
}
