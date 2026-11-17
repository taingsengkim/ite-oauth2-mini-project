package kh.edu.istad.platform.iam.infrastructure.mail;

import kh.edu.istad.platform.iam.port.out.user.RegistrationCodeMail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class RegistrationMailSender implements RegistrationCodeMail {
    private final JavaMailSender sender;
    private final boolean enabled;
    private final String from;
    public RegistrationMailSender(JavaMailSender sender,
            @Value("${iam.registration.verification-enabled:${iam.recovery.enabled:false}}") boolean enabled,
            @Value("${iam.recovery.from:no-reply@localhost}") String from) {
        this.sender=sender;this.enabled=enabled;this.from=from;
    }
    @Override public boolean enabled() { return enabled; }
    @Override public void send(Delivery delivery) {
        if(!enabled)throw new IllegalStateException("Email registration is unavailable");
        try {
            var helper=new MimeMessageHelper(sender.createMimeMessage(),false,StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);helper.setFrom(from);helper.setTo(delivery.email());
            boolean km="km".equals(delivery.locale());
            helper.setSubject(km?"ផ្ទៀងផ្ទាត់អ៊ីមែល ISTAD IAM របស់អ្នក":"Verify your ISTAD IAM email");
            helper.setText(km
                    ? "លេខកូដផ្ទៀងផ្ទាត់អ៊ីមែលរបស់អ្នក៖ " + delivery.code() + "\n\nលេខកូដនេះប្រើបានតែម្តង ហើយផុតកំណត់ក្នុងរយៈពេល ១០ នាទី បន្ទាប់ពីស្នើចុះឈ្មោះ។ ការផ្ញើឡើងវិញមិនពន្យារពេលផុតកំណត់ទេ។ កុំចែករំលែកលេខកូដនេះ។ បើអ្នកមិនបានស្នើចុះឈ្មោះ សូមមិនអើពើនឹងអ៊ីមែលនេះ។"
                    : "Your email verification code is: " + delivery.code() + "\n\nThis code is single-use and expires 10 minutes after registration was requested. Resending does not extend that window. Do not share this code. If you did not request an account, ignore this email.", false);
            sender.send(helper.getMimeMessage());
        } catch(jakarta.mail.MessagingException exception) { throw new IllegalStateException("Unable to construct registration email"); }
    }
}
