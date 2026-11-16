package kh.edu.istad.platform.iam.infrastructure.security;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class BrowserLogoutSuccessHandler implements LogoutSuccessHandler {
    private final BrowserRequestCache cache;
    @Override public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        cache.removeRequest(request, response);
        // The authenticated session has already been invalidated by Spring Security.
        // Store a single-use notice in a new anonymous session, rather than a persistent query flag.
        request.getSession(true).setAttribute("iam.logoutNotice", Boolean.TRUE);
        String language = request.getParameter("lang");
        response.sendRedirect(request.getContextPath() + "/login" + ("km".equals(language) ? "?lang=km" : "en".equals(language) ? "?lang=en" : ""));
    }
}
