package kh.edu.istad.platform.iam.infrastructure.security;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;

/** Preserve protected pages and OAuth authorization requests, never login/logout utility pages. */
@Component
public class BrowserRequestCache extends HttpSessionRequestCache {
    public BrowserRequestCache() {
        setRequestMatcher(request -> "GET".equals(request.getMethod()) && !utilityPath(request.getServletPath()));
    }
    static boolean utilityPath(String path) {
        if (path == null || path.equals("/")) return true;
        for (String prefix : new String[]{"/login", "/logout", "/error", "/register", "/assets", "/dashboard/assets",
                "/api", "/dashboard-api", "/recovery-api", "/oauth2/authorization", "/profile"})
            if (path.equals(prefix) || path.startsWith(prefix + "/")) return true;
        return path.equals("/dashboard/recovery.html") || path.equals("/account/identities/result") || path.equals("/favicon.ico");
    }
}
