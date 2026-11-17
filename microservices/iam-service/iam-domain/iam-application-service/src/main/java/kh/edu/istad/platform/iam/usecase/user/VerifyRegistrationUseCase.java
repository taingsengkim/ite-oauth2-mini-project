package kh.edu.istad.platform.iam.usecase.user;

import kh.edu.istad.platform.iam.dto.user.RegisterUserCommand;
import kh.edu.istad.platform.iam.exception.IamDomainException;
import kh.edu.istad.platform.iam.model.User;
import kh.edu.istad.platform.iam.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.port.out.user.*;
import kh.edu.istad.platform.iam.port.out.user.PendingRegistrationRepository.Pending;
import kh.edu.istad.platform.iam.port.out.user.RegistrationRequests.*;
import kh.edu.istad.platform.iam.port.out.user.RegistrationCodeMail.Delivery;
import kh.edu.istad.platform.iam.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
public class VerifyRegistrationUseCase {
    private final PendingRegistrationRepository pending;
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final PasswordPolicy policy;
    private final RegisterUserUseCase register;
    private final Clock clock;
    private static final SecureRandom RANDOM = new SecureRandom();
    public record Started(Challenge challenge, Delivery delivery, boolean limited, String conflict) {
        public Started(Challenge challenge, Delivery delivery, boolean limited) { this(challenge,delivery,limited,null); }
        @Override public String toString() { return "StartedRegistration[REDACTED]"; }
    }
    public record Resent(Delivery delivery, boolean limited) {
        @Override public String toString() { return "ResentRegistration[REDACTED]"; }
    }
    @Transactional
    public Started start(RegisterUserCommand command, String ip) {
        String username = required(command.username(),50);
        String email = required(command.email(),255).toLowerCase(Locale.ROOT);
        if (!username.matches("[A-Za-z0-9_.-]{3,50}")) throw invalid("Username must use 3–50 letters, digits, dots, underscores, or hyphens");
        policy.validate(command.password(),command.confirmPassword());
        var now=clock.instant();
        boolean allowed=pending.allow(digest("start-ip:"+ip),10,Duration.ofMinutes(15),now);
        allowed &= pending.allow(digest("mail-ip:"+ip),20,Duration.ofHours(1),now);
        if (!allowed) return new Started(null,null,true);
        allowed &= pending.allow(digest("mail-email:"+email),5,Duration.ofHours(1),now);
        allowed &= pending.allow(digest("mail-cooldown:"+email),1,Duration.ofSeconds(60),now);
        if (!allowed) return new Started(null,null,true); // Return normally so throttle counters commit.
        String hash=passwords.hash(command.password());
        // Domain validation also runs before storing any pending identity data.
        User.createLocal(username,email,hash,command.givenName(),command.familyName(),command.locale(),"registration",now);
        String token=newToken();String code=newCode();String tokenHash=digest(token);String codeHash=codeHash(token,code);
        var challenge=new Challenge(token,mask(email),now.plus(Duration.ofMinutes(10)));
        pending.cleanup(now);
        // Return conflicts normally so rate-limit counters commit before the facade reports them.
        if (users.isEmailRegistered(email)) return new Started(null,null,false,"Email is already registered");
        if (users.isUsernameRegistered(username)) return new Started(null,null,false,"Username is already registered");
        boolean inserted=pending.insert(new Pending(tokenHash,email,username,hash,command.givenName(),command.familyName(),
                command.locale(),codeHash,now,challenge.expiresAt(),now,0));
        return new Started(challenge,inserted?new Delivery(email,command.locale(),code,tokenHash,codeHash):null,false);
    }
    @Transactional
    public Verification verify(String token,String code,String ip) {
        var now=clock.instant();
        if (!pending.allow(digest("verify-ip:"+ip),100,Duration.ofMinutes(15),now)) return new Verification(Outcome.RATE_LIMITED,null);
        if (!validToken(token)) return new Verification(Outcome.INVALID,null);
        var value=pending.lock(digest(token)).orElse(null);
        if (value==null || !value.expiresAt().isAfter(now) || value.attempts()>=5) return new Verification(Outcome.INVALID,null);
        boolean correct=code!=null && code.matches("[0-9]{6}") && MessageDigest.isEqual(
                value.codeHash().getBytes(StandardCharsets.US_ASCII),codeHash(token,code).getBytes(StandardCharsets.US_ASCII));
        if (!correct) { pending.failedAttempt(value.tokenHash()); return new Verification(Outcome.INVALID,null); }
        var user=register.execute(value);
        pending.remove(value.tokenHash());
        return new Verification(Outcome.VERIFIED,user);
    }
    @Transactional
    public Resent resend(String token,String ip) {
        var now=clock.instant();
        if (!pending.allow(digest("mail-ip:"+ip),20,Duration.ofHours(1),now)) return new Resent(null,true);
        if (!validToken(token)) return new Resent(null,false);
        var value=pending.lock(digest(token)).orElse(null);
        if (value==null || !value.expiresAt().isAfter(now) || value.attempts()>=5 || value.sentAt().plusSeconds(60).isAfter(now)) return new Resent(null,false);
        boolean allowed=pending.allow(digest("mail-email:"+value.email()),5,Duration.ofHours(1),now);
        allowed &= pending.allow(digest("mail-cooldown:"+value.email()),1,Duration.ofSeconds(60),now);
        if (!allowed) return new Resent(null,false);
        String code;String hash;
        do { code=newCode();hash=codeHash(token,code); } while(hash.equals(value.codeHash()));
        // Resending rotates the code, never resets attempts, and never extends expiry.
        pending.replaceCode(value.tokenHash(),hash,now,value.expiresAt());
        return new Resent(new Delivery(value.email(),value.locale(),code,value.tokenHash(),hash),false);
    }
    @Transactional public void cancel(String token) { if(validToken(token)) pending.remove(digest(token)); }
    public static String digest(String value) { return RecoverPasswordUseCase.digest(value); }
    private static String newToken() { byte[] bytes=new byte[32];RANDOM.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private static String newCode() { return String.format(Locale.ROOT,"%06d",RANDOM.nextInt(1_000_000)); }
    private static boolean validToken(String token) { return token!=null && token.matches("[A-Za-z0-9_-]{43}"); }
    private static String codeHash(String token,String code) {
        try { var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(Base64.getUrlDecoder().decode(token),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(code.getBytes(StandardCharsets.US_ASCII))); }
        catch(GeneralSecurityException exception) { throw new IllegalStateException("Registration verification unavailable",exception); }
    }
    private static String mask(String email) { int at=email.indexOf('@');return at>0?email.substring(0,1)+"***"+email.substring(at):"***"; }
    private static String required(String value,int max) { if(value==null || value.isBlank() || !value.equals(value.strip()) || value.length()>max)throw invalid("Invalid registration details");return value; }
    private static IamDomainException invalid(String message) { return new IamDomainException(IamDomainException.Code.VALIDATION_ERROR,message); }
}
