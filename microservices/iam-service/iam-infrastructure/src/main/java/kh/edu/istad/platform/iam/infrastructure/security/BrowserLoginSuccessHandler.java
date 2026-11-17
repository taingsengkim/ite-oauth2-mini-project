package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;

@Component
public class BrowserLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final BrowserRequestCache cache;
    public BrowserLoginSuccessHandler(BrowserRequestCache cache) {
        this.cache = cache; setRequestCache(cache); setDefaultTargetUrl("/dashboard/");
    }
    @Override public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        var session = request.getSession(false);
        if (session != null) session.removeAttribute("iam.logoutNotice");
        var saved = cache.getRequest(request, response);
        if (saved != null) {
            boolean discard;
            try {
                String path = URI.create(saved.getRedirectUrl()).getPath();
                String context = request.getContextPath();
                if (!context.isEmpty() && path.startsWith(context + "/")) path = path.substring(context.length());
                discard = BrowserRequestCache.utilityPath(path);
            } catch (IllegalArgumentException invalid) { discard = true; }
            if (discard) cache.removeRequest(request, response);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
