package kh.edu.istad.platform.iam.web.config;

import jakarta.servlet.http.HttpServletRequest;
import kh.edu.istad.platform.iam.web.view.WebUi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@ControllerAdvice(basePackages = "kh.edu.istad.platform.iam.web.controller")
@RequiredArgsConstructor
public class WebPageAdvice {
    private final MessageSource messages;

    @ModelAttribute("ui")
    public WebUi ui(HttpServletRequest request) {
        var csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        return new WebUi(messages, LocaleContextHolder.getLocale(),
                csrf == null ? "_csrf" : csrf.getParameterName(), csrf == null ? "" : csrf.getToken(),
                authenticated, request.getRequestURI(), languageUrl(request, "en"), languageUrl(request, "km"));
    }

    private String languageUrl(HttpServletRequest request, String language) {
        var builder = UriComponentsBuilder.fromPath(request.getRequestURI());
        request.getParameterMap().forEach((name, values) -> {
            if (!"lang".equals(name)) {
                for (String value : values) {
                    builder.queryParam(UriUtils.encode(name, StandardCharsets.UTF_8),
                            UriUtils.encode(value, StandardCharsets.UTF_8));
                }
            }
        });
        return builder.queryParam("lang", language).build(true).toUriString();
    }
}
