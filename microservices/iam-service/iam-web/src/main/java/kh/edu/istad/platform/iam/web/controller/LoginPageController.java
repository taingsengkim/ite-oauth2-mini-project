package kh.edu.istad.platform.iam.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginPageController {
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        model.addAttribute("loginError", request.getParameterMap().containsKey("error"));
        model.addAttribute("loggedOut", request.getParameterMap().containsKey("logout"));
        model.addAttribute("registered", request.getParameterMap().containsKey("registered"));
        return "login";
    }
}
