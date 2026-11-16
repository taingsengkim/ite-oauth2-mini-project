package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.*;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.port.out.user.UserSessions;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import kh.edu.istad.platform.iam.port.out.configuration.SystemConfigurationRepository;

/** Tracks local servlet sessions. Multiple IAM instances require a shared Spring Session repository. */
@Component
public class BrowserUserSessions implements UserSessions, HttpSessionListener {
    private final Clock clock;
    private final kh.edu.istad.platform.iam.port.out.user.IpLocationResolver locations;
    private final SystemConfigurationRepository configuration;
    private final Map<UUID, Entry> entries = new HashMap<>();
    private static final String KEY = BrowserUserSessions.class.getName() + ".id";
    private static final String LAST_ACTIVITY = BrowserUserSessions.class.getName() + ".lastActivity";
    private static final String TIMEOUT = BrowserUserSessions.class.getName() + ".timeout";
    private record Entry(UUID id, UUID user, HttpSession session, Instant signedInAt,
                         Instant lastActiveAt, String ip, String agent) {}

    public BrowserUserSessions(Clock clock, SystemConfigurationRepository configuration, kh.edu.istad.platform.iam.port.out.user.IpLocationResolver locations) { this.clock = clock; this.configuration = configuration; this.locations = locations; }

    void enforceSessionPolicy(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) return;
        int timeout = configuration.current().policy().sessionIdleTimeoutSeconds();
        request.setAttribute(TIMEOUT, timeout);
        try {
            Instant lastActivity = (Instant) session.getAttribute(LAST_ACTIVITY);
            if (lastActivity != null && !lastActivity.plusSeconds(timeout).isAfter(clock.instant())) {
                session.invalidate();
                var context = org.springframework.security.core.context.SecurityContextHolder.getContext();
                var authentication = context.getAuthentication();
                if (authentication != null && (authentication.getPrincipal() instanceof IamUserDetails
                        || authentication.getPrincipal() instanceof IamOidcUser || authentication.getPrincipal() instanceof IamOAuth2User))
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                return;
            }
            session.setMaxInactiveInterval(timeout);
            session.setAttribute(LAST_ACTIVITY, clock.instant());
        } catch (IllegalStateException ignored) { /* A concurrent request invalidated this session. */ }
    }

    synchronized void observe(HttpServletRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return;
        UUID user;
        Object principal = authentication.getPrincipal();
        if (principal instanceof IamUserDetails local) user = local.getUuid();
        else if (principal instanceof IamOidcUser oidc) user = oidc.getUserUuid();
        else if (principal instanceof IamOAuth2User oauth) user = oauth.getUserUuid();
        else return;
        HttpSession session = request.getSession(false);
        if (session == null) return;
        try {
            Integer timeout = (Integer) request.getAttribute(TIMEOUT);
            session.setMaxInactiveInterval(timeout != null ? timeout : configuration.current().policy().sessionIdleTimeoutSeconds());
            session.setAttribute(LAST_ACTIVITY, clock.instant());
            UUID id = (UUID) session.getAttribute(KEY);
            Entry previous = id == null ? null : entries.get(id);
            if (previous == null || !previous.user().equals(user)) {
                if (id != null) entries.remove(id);
                id = UUID.randomUUID();
                session.setAttribute(KEY, id);
            }
            String agent = Objects.toString(request.getHeader("User-Agent"), "Unknown device");
            if (agent.length() > 512) agent = agent.substring(0, 512);
            Instant now = clock.instant();
            entries.put(id, new Entry(id, user, session,
                    previous != null && previous.user().equals(user) ? previous.signedInAt() : now,
                    now, request.getRemoteAddr(), agent));
        } catch (IllegalStateException ignored) { /* Concurrent session termination. */ }
    }

    @Override public synchronized List<Session> list(UUID userUuid, String currentSessionId) {
        List<Session> result = new ArrayList<>();
        var iterator = entries.values().iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            try {
                int timeout = entry.session().getMaxInactiveInterval();
                Instant expires = timeout <= 0 ? null : Instant.ofEpochMilli(entry.session().getLastAccessedTime()).plusSeconds(timeout);
                if (expires != null && !expires.isAfter(clock.instant())) { iterator.remove(); continue; }
                if (entry.user().equals(userUuid)) result.add(new Session(entry.id(), entry.signedInAt(),
                        entry.lastActiveAt(), expires, entry.ip(), entry.agent(), entry.session().getId().equals(currentSessionId), locations.resolve(entry.ip())));
            } catch (IllegalStateException ignored) { iterator.remove(); }
        }
        result.sort(Comparator.comparing(Session::lastActiveAt).reversed());
        return List.copyOf(result);
    }

    @Override public void terminate(UUID userUuid, UUID sessionId) {
        synchronized (this) {
            Entry entry = entries.get(sessionId);
            if (entry == null || !entry.user().equals(userUuid)) throw new ResourceNotFoundException("Session", sessionId);
        }
        afterCommit(() -> invalidate(userUuid, sessionId));
    }

    @Override public void terminateAll(UUID userUuid) { afterCommit(() -> invalidate(userUuid, null)); }

    private synchronized void invalidate(UUID userUuid, UUID sessionId) {
        var targets = entries.values().stream().filter(entry -> entry.user().equals(userUuid)
                && (sessionId == null || entry.id().equals(sessionId))).toList();
        for (Entry entry : targets) {
            entries.remove(entry.id());
            try { entry.session().invalidate(); } catch (IllegalStateException ignored) { }
        }
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { action.run(); }
            });
        } else action.run();
    }

    @Override public synchronized void sessionDestroyed(HttpSessionEvent event) {
        entries.values().removeIf(entry -> entry.session().getId().equals(event.getSession().getId()));
    }
}
