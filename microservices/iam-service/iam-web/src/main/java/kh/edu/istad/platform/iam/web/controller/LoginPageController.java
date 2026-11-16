package kh.edu.istad.platform.iam.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model, Authentication authentication) {
        var session = request.getSession(false);
        boolean loggedOut = session != null && Boolean.TRUE.equals(session.getAttribute("iam.logoutNotice"));
        if (session != null) session.removeAttribute("iam.logoutNotice");
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken))
            return "redirect:/dashboard/index.html";
        model.addAttribute("loginError", request.getParameterMap().containsKey("error"));
        String error = request.getParameter("error");
        model.addAttribute("loginErrorKey", "account_link_required".equals(error) ? "login.linkRequired"
                : "profile_expired".equals(error) ? "completion.expired" : "login.error");
        model.addAttribute("loggedOut", loggedOut);
        model.addAttribute("registered", request.getParameterMap().containsKey("registered"));
        return "login";
    }
}
