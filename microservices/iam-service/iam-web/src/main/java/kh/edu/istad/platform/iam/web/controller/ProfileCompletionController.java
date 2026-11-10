package kh.edu.istad.platform.iam.web.controller;

import jakarta.validation.Valid;
import kh.edu.istad.common.domain.exception.DomainException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.identity.CompleteFederatedProfileCommand;
import kh.edu.istad.platform.iam.port.out.identity.FederatedProfileCompletion;
import kh.edu.istad.platform.iam.web.form.FederatedProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ProfileCompletionController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProfileCompletionController.class);
    private final FederatedProfileCompletion completion;

    @GetMapping("/register/complete")
    public String page(Model model) {
        var pending = completion.pending();
        if (pending.isEmpty()) return "redirect:/login?error=profile_expired";
        var draft = pending.get();
        model.addAttribute("draft", draft);
        model.addAttribute("form", new FederatedProfileForm(draft.proofToken(), draft.email(),
                draft.givenName(), draft.familyName(), draft.locale()));
        return "profile-complete";
    }

    @PostMapping("/register/complete")
    public String submit(@Valid @ModelAttribute("form") FederatedProfileForm form,
                         BindingResult binding, Model model) {
        var pending = completion.pending();
        if (pending.isEmpty()) return "redirect:/login?error=profile_expired";
        model.addAttribute("draft", pending.get());
        if (binding.hasErrors()) {
            model.addAttribute("error", "error.fields");
            return "profile-complete";
        }
        try {
            return "redirect:" + completion.complete(new CompleteFederatedProfileCommand(
                    form.proofToken(), form.email(), form.givenName(), form.familyName(), form.locale()));
        } catch (ResourceConflictException exception) {
            log.debug("Federated profile completion conflicted", exception);
            model.addAttribute("error", "completion.accountExists");
        } catch (DomainException exception) {
            log.debug("Federated profile completion failed", exception);
            model.addAttribute("error", "error.profile");
        }
        return "profile-complete";
    }

    @PostMapping("/register/complete/cancel")
    public String cancel(@RequestParam("proofToken") String proofToken) {
        if (completion.pending().isPresent()) completion.cancel(proofToken);
        return "redirect:/login";
    }
}
