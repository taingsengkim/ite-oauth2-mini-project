package kh.edu.istad.platform.iam.infrastructure.mail;

import kh.edu.istad.platform.iam.port.out.user.PasswordRecoveryMail;
import kh.edu.istad.platform.iam.port.out.user.PasswordRecoveryMail.Delivery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class RecoveryMailSender implements PasswordRecoveryMail {
    private final JavaMailSender sender;
    private final boolean enabled;
    private final String from;
    private final String baseUrl;
    public RecoveryMailSender(JavaMailSender sender, @Value("${iam.recovery.enabled:false}") boolean enabled,
                              @Value("${iam.recovery.from:no-reply@localhost}") String from,
                              @Value("${iam.recovery.public-base-url:http://localhost:9000}") String baseUrl) {
        this.sender = sender; this.enabled = enabled; this.from = from;
        var uri = URI.create(baseUrl);
        boolean localHttp = "http".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && Set.of("localhost", "127.0.0.1", "[::1]", "::1").contains(uri.getHost());
        if (uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null
                || !(localHttp || "https".equalsIgnoreCase(uri.getScheme())) || !(uri.getPath().isEmpty() || "/".equals(uri.getPath())))
            throw new IllegalArgumentException("IAM_PUBLIC_BASE_URL must be an HTTPS origin (HTTP is allowed only on loopback)");
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }
    @Override public boolean enabled() { return enabled; }
    @Override public void send(Delivery delivery) {
        if (!enabled) throw new IllegalStateException("Email recovery is disabled");
        try {
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true); helper.setFrom(from); helper.setTo(delivery.email());
            boolean khmer = "km".equals(delivery.locale());
            helper.setSubject(khmer ? "កំណត់ពាក្យសម្ងាត់ IAM ឡើងវិញ" : "Reset your IAM password");
            String link = baseUrl + "/dashboard/recovery.html?lang=" + (khmer ? "km" : "en") + "#token=" + delivery.token();
            helper.setText(khmer
                    ? "អ្នកបានស្នើសុំកំណត់ពាក្យសម្ងាត់ IAM ឡើងវិញ។ តំណនេះអាចប្រើម្ដង និងផុតកំណត់ក្នុង ១៥ នាទី។\n\n" + link + "\n\nបើអ្នកមិនបានស្នើសុំ សូមមិនអើពើនឹងអ៊ីមែលនេះ។ កុំផ្ដល់តំណនេះឱ្យអ្នកផ្សេង។"
                    : "A password reset was requested for your IAM account. This single-use link expires in 15 minutes.\n\n" + link + "\n\nIf you did not request this, ignore this email. Your password has not changed. Do not share this link.", false);
            sender.send(message);
        } catch (jakarta.mail.MessagingException exception) {
            throw new IllegalStateException("Unable to construct password recovery email", exception);
        }
    }
}
