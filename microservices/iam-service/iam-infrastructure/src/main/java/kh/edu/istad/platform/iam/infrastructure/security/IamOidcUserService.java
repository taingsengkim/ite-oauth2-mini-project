package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.application.exception.AuthenticationFailedException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.model.IdentityProvider;
import kh.edu.istad.platform.iam.application.exception.ProfileCompletionRequiredException;
import kh.edu.istad.platform.iam.usecase.identity.FederatedUserProfileSync;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IamOidcUserService
        implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final IamFederatedUserResolver federatedUsers;

    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        String registrationId = request.getClientRegistration()
                .getRegistrationId();

        IdentityProvider provider = switch (registrationId) {
            case "google" -> IdentityProvider.GOOGLE;
            case "telegram" -> IdentityProvider.TELEGRAM;
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider")
            );
        };

        OidcUser providerUser = delegate.loadUser(request);

        Map<String, Object> attributes = new LinkedHashMap<>();

        for (String claim : new String[]{
                "sub", "email", "email_verified",
                "name", "given_name", "family_name",
                "preferred_username", "picture", "locale", "middle_name", "nickname",
                "profile", "website", "gender", "birthdate", "zoneinfo",
                "phone_number", "phone_number_verified", "address"
        }) {
            Object value = providerUser.getClaims().get(claim);

            if (value != null) {
                attributes.put(claim, value);
            }
        }



        boolean telegram = provider == IdentityProvider.TELEGRAM;

        var command = new FederatedIdentityCommand(
                provider,
                providerUser.getSubject(),
                telegram ? null : providerUser.getEmail(),
                !telegram && Boolean.TRUE.equals(
                        providerUser.getEmailVerified()
                ),
                providerUser.getFullName(),
                providerUser.getPreferredUsername(),
                providerUser.getPicture(),
                providerUser.getGivenName(),
                providerUser.getFamilyName(),
                FederatedUserProfileSync.normalizeLocale(providerUser.getLocale()),
                attributes
        );

        try {
            var user = federatedUsers.resolve(command);

            var authorities = user.authorities()
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            return new IamOidcUser(
                    user.uuid(),
                    user.username(),
                    authorities,
                    providerUser
            );
        } catch (ProfileCompletionRequiredException exception) {
            throw new FederatedProfileRequiredException(registrationId, command);
        } catch (AuthenticationFailedException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("access_denied"),
                    "Local account cannot sign in",
                    exception
            );
        } catch (ResourceConflictException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_link_required"),
                    "Sign in to your existing account to link this provider",
                    exception
            );
        } catch (IamDomainException exception) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("profile_completion_required"), "Required profile information is missing or invalid", exception
            );
        }
    }
}
