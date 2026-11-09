package kh.edu.istad.platform.iam.infrastructure.security;

import kh.edu.istad.platform.iam.dto.identity.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.dto.identity.LinkIdentityIntent;
import kh.edu.istad.platform.iam.dto.user.AuthUserResult;
import kh.edu.istad.platform.iam.usecase.user.LoadUserForTokenUseCase;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.usecase.identity.FindOrProvisionFederatedUserUseCase;
import kh.edu.istad.platform.iam.usecase.identity.LinkIdentityUseCase;
import kh.edu.istad.platform.iam.usecase.user.GetUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class IamFederatedUserResolver {

    private final FindOrProvisionFederatedUserUseCase findOrProvision;
    private final LinkIdentityUseCase linkIdentity;
    private final GetUserUseCase getUser;
    private final LoadUserForTokenUseCase loadUser;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public AuthUserResult resolve(FederatedIdentityCommand command) {
        var requestAttributes = RequestContextHolder
                .currentRequestAttributes();

        if (!(requestAttributes instanceof ServletRequestAttributes servlet)) {
            throw invalidLink();
        }

        var request = servlet.getRequest();

        Object value = request.getAttribute("iam.identityLinkIntent");
        request.removeAttribute("iam.identityLinkIntent");

        if (value == null) {
            return findOrProvision.execute(command);
        }

        if (!(value instanceof LinkIdentityIntent intent)) {
            throw invalidLink();
        }

        String registrationId = command.provider()
                .name()
                .toLowerCase(Locale.ROOT);

        if (!intent.expiresAt().isAfter(clock.instant())
                || !intent.registrationId().equals(registrationId)
                || !intent.userUuid().equals(
                currentUserProvider.currentUserUuid()
        )) {
            throw invalidLink();
        }

        linkIdentity.execute(intent.userUuid(), command);
        request.setAttribute("iam.identityLinked", Boolean.TRUE);

        return loadUser.execute(getUser.execute(intent.userUuid()).username());
    }

    private static OAuth2AuthenticationException invalidLink() {
        return new OAuth2AuthenticationException(
                new OAuth2Error(
                        "invalid_request",
                        "Identity linking request is invalid or expired",
                        null
                )
        );
    }
}
