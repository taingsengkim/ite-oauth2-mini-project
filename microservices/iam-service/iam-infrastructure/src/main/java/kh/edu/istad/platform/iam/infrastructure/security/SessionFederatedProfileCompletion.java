package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kh.edu.istad.platform.iam.dto.identity.*;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.port.out.identity.FederatedProfileCompletion;
import kh.edu.istad.platform.iam.usecase.identity.FindOrProvisionFederatedUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionFederatedProfileCompletion implements FederatedProfileCompletion {
    private final FindOrProvisionFederatedUserUseCase findOrProvision;
    private final Clock clock;

    @Override
    public Optional<FederatedProfileDraft> pending() {
        var request = attributes().getRequest();
        var session = request.getSession(false);
        var pending = findPending(session);
        if (pending == null) return Optional.empty();
        var identity = pending.identity();
        return Optional.of(new FederatedProfileDraft(pending.proofToken(), identity.provider(),
                identity.email(), identity.givenName(), identity.familyName(),
                identity.locale() == null ? "en" : identity.locale()));
    }

    @Override
    public String complete(CompleteFederatedProfileCommand command) {
        var attributes = attributes();
        var request = attributes.getRequest();
        var response = attributes.getResponse();
        var session = request.getSession(false);
        if (session == null || response == null) throw invalidProof();
        synchronized (session) {
            var pending = requirePending(session, command.proofToken());
            var result = findOrProvision.completeProfile(pending.identity(), new FederatedProfileDetails(
                    command.email(), command.givenName(), command.familyName(), command.locale()));
            var authorities = result.authorities().stream().map(SimpleGrantedAuthority::new).toList();
            var principal = new IamOAuth2User(result.uuid(), result.username(), authorities,
                    Map.of("iam_username", result.username()), "iam_username");
            var authentication = new OAuth2AuthenticationToken(principal, authorities, pending.registrationId());
            new ChangeSessionIdAuthenticationStrategy().onAuthentication(authentication, request, response);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);
            session.removeAttribute("iam.pendingProfile");
            return redirect(request, response);
        }
    }

    @Override
    public void cancel(String proofToken) {
        var session = attributes().getRequest().getSession(false);
        if (session == null) return;
        synchronized (session) {
            requirePending(session, proofToken);
            session.removeAttribute("iam.pendingProfile");
        }
    }

    private PendingFederatedProfile findPending(HttpSession session) {
        if (session == null) return null;
        Object value = session.getAttribute("iam.pendingProfile");
        if (!(value instanceof PendingFederatedProfile pending)) return null;
        if (!pending.expiresAt().isAfter(clock.instant())) {
            session.removeAttribute("iam.pendingProfile");
            return null;
        }
        return pending;
    }

    private PendingFederatedProfile requirePending(HttpSession session, String token) {
        var pending = findPending(session);
        if (pending == null || token == null || !MessageDigest.isEqual(
                pending.proofToken().getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            throw invalidProof();
        }
        return pending;
    }

    private String redirect(HttpServletRequest request, HttpServletResponse response) {
        var cache = new HttpSessionRequestCache();
        var saved = cache.getRequest(request, response);
        if (saved != null) {
            var uri = URI.create(saved.getRedirectUrl());
            if ("/oauth2/authorize".equals(uri.getPath())) {
                return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
            }
        }
        cache.removeRequest(request, response);
        return "/";
    }

    private ServletRequestAttributes attributes() {
        return (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    }

    private IamDomainException invalidProof() {
        return new IamDomainException(IamDomainException.Code.VALIDATION_ERROR,
                "Profile completion request is invalid or expired");
    }
}
