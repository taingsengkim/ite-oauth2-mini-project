package kh.edu.istad.platform.iam.rest.identity.controller;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.dto.identity.LinkIdentityIntent;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class IdentityLinkController {

    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    @PostMapping("/account/identities/link/{registrationId}")
    public String startLink(
            @PathVariable("registrationId") String registrationId,
            HttpServletRequest request
    ) {
        if (!Set.of("google", "github", "facebook", "telegram").contains(registrationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported identity provider");
        }

        var intent = new LinkIdentityIntent(
                currentUserProvider.currentUserUuid(),
                registrationId,
                clock.instant().plus(Duration.ofMinutes(5))
        );

        request.getSession().setAttribute(
                "iam.pendingIdentityLink",
                intent
        );

        return "redirect:/oauth2/authorization/" + registrationId;
    }
}