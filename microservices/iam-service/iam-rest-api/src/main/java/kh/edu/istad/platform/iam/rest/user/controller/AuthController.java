package kh.edu.istad.platform.iam.rest.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.edu.istad.platform.iam.rest.user.dto.RegisterUserRequest;
import kh.edu.istad.platform.iam.rest.user.mapper.UserApiMapper;
import kh.edu.istad.platform.iam.port.out.user.RegistrationRequests;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegistrationRequests registration;
    private final UserApiMapper mapper;
    public record Accepted(String message,RegistrationRequests.Challenge challenge) {
        @Override public String toString(){return "AcceptedRegistration[REDACTED]";}
    }
    public record VerificationRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{43}") String token,
                                      @NotBlank @Pattern(regexp="[0-9]{6}") String code) {
        @Override public String toString(){return "VerificationRequest[REDACTED]";}
    }
    public record ResendRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{43}") String token) {
        @Override public String toString(){return "RegistrationResend[REDACTED]";}
    }
    @PostMapping("/register")
    public ResponseEntity<Accepted> register(@Valid @RequestBody RegisterUserRequest body,HttpServletRequest request) {
        requireEnabled();
        try {
            var challenge=registration.start(mapper.toCommand(body),request.getRemoteAddr());
            return ResponseEntity.accepted().cacheControl(CacheControl.noStore())
                    .body(new Accepted("If registration is available, a verification code will be emailed.",challenge));
        } catch(RegistrationRequests.RateLimited limited) { throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many registration requests. Try again later."); }
    }
    @PostMapping("/register/verify")
    public ResponseEntity<RegistrationRequests.Verification> verify(@Valid @RequestBody VerificationRequest body,HttpServletRequest request) {
        requireEnabled();var result=registration.verify(body.token(),body.code(),request.getRemoteAddr());
        if(result.outcome()==RegistrationRequests.Outcome.RATE_LIMITED)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many verification attempts. Try again later.");
        if(result.outcome()!=RegistrationRequests.Outcome.VERIFIED)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Code is invalid, expired, or registration is unavailable. Start again if necessary.");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }
    @PostMapping("/register/resend")
    public ResponseEntity<Void> resend(@Valid @RequestBody ResendRequest body,HttpServletRequest request) {
        requireEnabled();
        if(!registration.resend(body.token(),request.getRemoteAddr()))throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many email requests. Try again later.");
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore()).build();
    }
    private void requireEnabled() {
        if(!registration.enabled())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Email registration is not configured");
    }
}
