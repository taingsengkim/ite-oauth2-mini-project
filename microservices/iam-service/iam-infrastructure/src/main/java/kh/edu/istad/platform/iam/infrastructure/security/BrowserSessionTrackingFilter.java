package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

final class BrowserSessionTrackingFilter extends OncePerRequestFilter {
    private final BrowserUserSessions sessions;
    BrowserSessionTrackingFilter(BrowserUserSessions sessions) { this.sessions = sessions; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                             FilterChain chain) throws ServletException, IOException {
        sessions.enforceSessionPolicy(request);
        sessions.observe(request, SecurityContextHolder.getContext().getAuthentication());
        try { chain.doFilter(request, response); }
        finally { sessions.observe(request, SecurityContextHolder.getContext().getAuthentication()); }
    }
}
