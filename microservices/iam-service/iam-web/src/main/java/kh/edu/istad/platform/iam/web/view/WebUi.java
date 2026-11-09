package kh.edu.istad.platform.iam.web.view;

import org.springframework.context.MessageSource;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record WebUi(MessageSource messages, Locale locale, String csrfName, String csrfToken,
                    boolean authenticated, String path, String englishUrl, String khmerUrl) {
    public String t(String key, Object... arguments) {
        return messages.getMessage(key, arguments, locale);
    }
    public String language() { return locale.getLanguage(); }
    public String active(String route) { return path.equals(route) ? "nav-link active" : "nav-link"; }
    public String text(String value) { return value == null || value.isBlank() ? t("common.notProvided") : value; }
    public String date(Instant value) {
        return value == null ? t("common.notProvided") : DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", locale)
                .withZone(ZoneId.of("Asia/Phnom_Penh")).format(value);
    }
    public String initials(String name) {
        return name == null || name.isBlank() ? "?" : name.strip().substring(0, 1).toUpperCase(locale);
    }
}
