package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

/** Keeps the authorization request session on the same origin as its callback. */
final class TelegramLoginOriginFilter extends OncePerRequestFilter {
    private final URI callback;

    TelegramLoginOriginFilter(String redirectUri) {
        this.callback = redirectUri.contains("{") ? null : URI.create(redirectUri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (callback != null && "GET".equals(request.getMethod())
                && "/oauth2/authorization/telegram".equals(request.getServletPath())) {
            URI current = URI.create(request.getRequestURL().toString());
            if (!callback.getScheme().equalsIgnoreCase(current.getScheme())
                    || !callback.getRawAuthority().equalsIgnoreCase(current.getRawAuthority())) {
                response.sendRedirect(callback.resolve("/oauth2/authorization/telegram").toString());
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
