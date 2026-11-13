package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.model.IdentityProvider;
import kh.edu.istad.platform.iam.application.exception.ProfileCompletionRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IamOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final IamFederatedUserResolver federatedUsers;

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    private final GithubEmailClient githubEmails;

    private OAuth2User loadFacebookUser(OAuth2UserRequest request) {
        OAuth2User providerUser = delegate.loadUser(request);

        Object providerId = providerUser.getAttribute("id");

        if (providerId == null) {
            throw failure(
                    "invalid_user_info",
                    "Facebook account ID is missing"
            );
        }

        String email = providerUser.getAttribute("email");
        String displayName = providerUser.getAttribute("name");
        String givenName = providerUser.getAttribute("first_name");
        String familyName = providerUser.getAttribute("last_name");

        String pictureUrl = null;

        Object picture = providerUser.getAttribute("picture");

        if (picture instanceof Map<?, ?> pictureMap
                && pictureMap.get("data") instanceof Map<?, ?> data
                && data.get("url") instanceof String url) {
            pictureUrl = url;
        }

        Map<String, Object> attributes = new LinkedHashMap<>();

        for (String key : List.of(
                "id", "name", "first_name", "last_name", "email"
        )) {
            Object value = providerUser.getAttributes().get(key);

            if (value != null) {
                attributes.put(key, value);
            }
        }

        if (pictureUrl != null) {
            attributes.put("picture_url", pictureUrl);
        }

        var command = new FederatedIdentityCommand(
                IdentityProvider.FACEBOOK,
                providerId.toString(),
                email,
                false,
                displayName,
                null,
                pictureUrl,
                givenName,
                familyName,
                "en",
                attributes
        );

        try {
            var user = federatedUsers.resolve(command);

            var authorities = user.authorities()
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new IamOAuth2User(
                    user.uuid(),
                    user.username(),
                    authorities,
                    attributes,
                    "id"
            );
        } catch (ProfileCompletionRequiredException exception) {
            throw new FederatedProfileRequiredException("facebook", command);
        } catch (AuthenticationFailedException exception) {
            throw failure(
                    "access_denied",
                    "Local account cannot sign in"
            );
        } catch (ResourceConflictException exception) {
            throw failure(
                    "account_link_required",
                    "Sign in to your existing account to link Facebook"
            );
        } catch (IamDomainException exception) {
            throw failure(
                    "profile_completion_required",
                    "Required profile information is missing or invalid"
            );
        }
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        String registrationId = request.getClientRegistration()
                .getRegistrationId();

        if ("facebook".equals(registrationId)) {
            return loadFacebookUser(request);
        }

        if (!"github".equals(registrationId)) {
            throw failure(
                    "unsupported_provider",
                    "This OAuth2 provider is not configured"
            );
        }



        OAuth2User providerUser = delegate.loadUser(request);

        Object providerId = providerUser.getAttribute("id");

        if (providerId == null) {
            throw failure("invalid_user_info", "GitHub account ID is missing");
        }

        String email = githubEmails.findVerifiedEmail(
                request.getAccessToken().getTokenValue());
        String displayName = providerUser.getAttribute("name");
        String username = providerUser.getAttribute("login");
        String pictureUrl = providerUser.getAttribute("avatar_url");

        String givenName = null;
        String familyName = null;

        if (displayName != null && !displayName.isBlank()) {
            String[] parts = displayName.strip().split("\\s+", 2);
            givenName = parts[0];
            familyName = parts.length == 2 ? parts[1] : null;
        }

        Map<String, Object> attributes = new LinkedHashMap<>();

        for (String key : List.of("id", "login", "name", "avatar_url", "html_url", "blog")) {
            Object value = providerUser.getAttributes().get(key);

            if (value != null) {
                attributes.put(key, value);
            }
        }

        if (attributes.get("html_url") instanceof String profile) attributes.put("profile", profile);
        if (attributes.get("blog") instanceof String website) attributes.put("website", website);
        if (username != null) attributes.put("nickname", username);

        var command = new FederatedIdentityCommand(
                IdentityProvider.GITHUB,
                providerId.toString(),
                email,
                email != null,
                displayName,
                username,
                pictureUrl,
                givenName,
                familyName,
                "en",
                attributes
        );

        try {
            var user = federatedUsers.resolve(command);

            var authorities = user.authorities()
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new IamOAuth2User(
                    user.uuid(),
                    user.username(),
                    authorities,
                    attributes,
                    "id"
            );
        } catch (ProfileCompletionRequiredException exception) {
            throw new FederatedProfileRequiredException(registrationId, command);
        } catch (AuthenticationFailedException exception) {
            throw failure("access_denied", "Local account cannot sign in");
        } catch (ResourceConflictException exception) {
            throw failure(
                    "account_link_required",
                    "Sign in to your existing account to link GitHub"
            );
        } catch (IamDomainException exception) {
            throw failure(
                    "profile_completion_required",
                    "Required profile information is missing or invalid"
            );
        }
    }

    private static OAuth2AuthenticationException failure(
            String code,
            String description
    ) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(code, description, null)
        );
    }
}
