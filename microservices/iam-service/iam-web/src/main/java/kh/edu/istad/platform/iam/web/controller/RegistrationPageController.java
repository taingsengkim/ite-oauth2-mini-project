package kh.edu.istad.platform.iam.web.controller;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import kh.edu.istad.common.domain.exception.DomainException;
import kh.edu.istad.platform.iam.dto.user.RegisterUserCommand;
import kh.edu.istad.platform.iam.port.out.user.RegistrationRequests;
import kh.edu.istad.platform.iam.web.form.RegistrationForm;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.validation.PasswordPolicy;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;

@Controller
@RequiredArgsConstructor
public class RegistrationPageController {
    private final RegistrationRequests registration;
    private final Clock clock;
    private final PasswordPolicy passwordPolicy;
    private final org.springframework.context.MessageSource messages;
    private String text(String key) {return messages.getMessage(key,null,LocaleContextHolder.getLocale());}
    private static final String CHALLENGE="iam.registrationChallenge";
    private static final String RESEND_AT="iam.registrationResendAt";
    @GetMapping("/register")
    public String page(Model model,HttpServletRequest request,HttpServletResponse response) {
        noStore(response);var challenge=challenge(request);
        if(challenge!=null && challenge.expiresAt().isAfter(clock.instant()))return "redirect:/register/verify";
        clearSession(request);
        model.addAttribute("form",RegistrationForm.empty(LocaleContextHolder.getLocale().getLanguage()));
        if(!registration.enabled())model.addAttribute("error","verification.unavailable");
        else if(request.getParameterMap().containsKey("expired"))model.addAttribute("error","verification.expired");
        else if(request.getParameterMap().containsKey("conflict"))model.addAttribute("error","verification.conflict");
        return "register";
    }
    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("form") RegistrationForm form,BindingResult binding,Model model,
                         HttpServletRequest request,HttpServletResponse response) {
        noStore(response);
        if(!registration.enabled()){model.addAttribute("error","verification.unavailable");return "register";}
        Map<String,String> errors=new LinkedHashMap<>();
        binding.getFieldErrors().forEach(field -> errors.putIfAbsent(field.getField(),field.getDefaultMessage()));
        try { passwordPolicy.validate(form.password()); }
        catch(DomainException invalid) {errors.put("password",invalid.getMessage());}
        if(form.password()!=null && !form.password().equals(form.confirmPassword()))errors.put("confirmPassword",text("error.passwordMismatch"));
        if(!errors.isEmpty()){model.addAttribute("fieldErrors",errors);model.addAttribute("error","error.fields");return "register";}
        var previous=challenge(request);
        if(previous!=null && previous.expiresAt().isAfter(clock.instant()))return "redirect:/register/verify";
        try {
            var challenge=registration.start(new RegisterUserCommand(form.username(),form.email(),form.password(),form.confirmPassword(),form.givenName(),form.familyName(),form.locale()),request.getRemoteAddr());
            var session=request.getSession();session.setAttribute(CHALLENGE,challenge);session.setAttribute(RESEND_AT,clock.instant().plusSeconds(60));
            return "redirect:/register/verify";
        } catch(RegistrationRequests.RateLimited limited){model.addAttribute("error","verification.rateLimit");}
        catch(ResourceConflictException conflict){
            String field=conflict.getMessage().startsWith("Email")?"email":"username";
            model.addAttribute("fieldErrors",Map.of(field,text("registration."+field+"Taken")));
            model.addAttribute("error","error.fields");
        }
        catch(DomainException invalid){model.addAttribute("error","error.registration");}
        return "register";
    }
    @GetMapping("/register/verify")
    public String verificationPage(Model model,HttpServletRequest request,HttpServletResponse response) {
        noStore(response);var challenge=challenge(request);
        if(challenge==null || !challenge.expiresAt().isAfter(clock.instant())){clearSession(request);return "redirect:/register?expired";}
        return verification(model,request,challenge,null);
    }
    @PostMapping("/register/verify")
    public String verify(@RequestParam(name="code",defaultValue="") String code,Model model,HttpServletRequest request,HttpServletResponse response) {
        noStore(response);var challenge=challenge(request);
        if(challenge==null)return "redirect:/register?expired";
        if(!registration.enabled())return verification(model,request,challenge,"verification.unavailable");
        var result=registration.verify(challenge.token(),code,request.getRemoteAddr());
        if(result.outcome()==RegistrationRequests.Outcome.VERIFIED){clearSession(request);return "redirect:/login?registered";}
        if(result.outcome()==RegistrationRequests.Outcome.CONFLICT){clearSession(request);return "redirect:/register?conflict";}
        return verification(model,request,challenge,result.outcome()==RegistrationRequests.Outcome.RATE_LIMITED?"verification.rateLimit":"verification.invalid");
    }
    @PostMapping("/register/resend")
    public String resend(HttpServletRequest request,HttpServletResponse response,RedirectAttributes redirect) {
        noStore(response);var challenge=challenge(request);
        if(challenge==null || !challenge.expiresAt().isAfter(clock.instant()))return "redirect:/register?expired";
        if(!registration.enabled())redirect.addFlashAttribute("error","verification.unavailable");
        else if(!registration.resend(challenge.token(),request.getRemoteAddr()))redirect.addFlashAttribute("error","verification.rateLimit");
        else {
            request.getSession().setAttribute(RESEND_AT,clock.instant().plusSeconds(60));
            redirect.addFlashAttribute("notice","verification.resent");
        }
        return "redirect:/register/verify";
    }
    @PostMapping("/register/cancel")
    public String cancel(HttpServletRequest request,HttpServletResponse response) {
        noStore(response);var challenge=challenge(request);
        if(challenge!=null)registration.cancel(challenge.token());clearSession(request);return "redirect:/register";
    }
    private String verification(Model model,HttpServletRequest request,RegistrationRequests.Challenge challenge,String error) {
        model.addAttribute("maskedEmail",challenge.maskedEmail());model.addAttribute("expiresAt",challenge.expiresAt());
        var session=request.getSession(false);var next=session==null?null:session.getAttribute(RESEND_AT);
        model.addAttribute("resendSeconds",next instanceof Instant instant?Math.max(0,(Duration.between(clock.instant(),instant).toMillis()+999)/1000):0L);
        if(error!=null)model.addAttribute("error",error);
        return "register-verify";
    }
    private RegistrationRequests.Challenge challenge(HttpServletRequest request) {
        var session=request.getSession(false);var value=session==null?null:session.getAttribute(CHALLENGE);
        return value instanceof RegistrationRequests.Challenge challenge?challenge:null;
    }
    private void clearSession(HttpServletRequest request) {var session=request.getSession(false);if(session!=null){session.removeAttribute(CHALLENGE);session.removeAttribute(RESEND_AT);}}
    // no-referrer can turn browser form POST origins into "null", which CORS rejects.
    private static void noStore(HttpServletResponse response) {response.setHeader("Cache-Control","no-store");response.setHeader("Referrer-Policy","same-origin");}
}
