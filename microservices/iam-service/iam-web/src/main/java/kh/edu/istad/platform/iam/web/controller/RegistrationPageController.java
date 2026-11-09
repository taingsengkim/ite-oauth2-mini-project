package kh.edu.istad.platform.iam.web.controller;

import jakarta.validation.Valid;
import kh.edu.istad.common.domain.exception.DomainException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.user.RegisterUserCommand;
import kh.edu.istad.platform.iam.usecase.user.RegisterUserUseCase;
import kh.edu.istad.platform.iam.web.form.RegistrationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class RegistrationPageController {
    private final RegisterUserUseCase register;

    @GetMapping("/register")
    public String page(Model model) {
        model.addAttribute("form", RegistrationForm.empty(LocaleContextHolder.getLocale().getLanguage()));
        return "register";
    }

    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("form") RegistrationForm form,
                         BindingResult binding, Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "error.fields");
            return "register";
        }
        if (!form.password().equals(form.confirmPassword())) {
            model.addAttribute("error", "error.passwordMismatch");
            return "register";
        }
        try {
            register.execute(new RegisterUserCommand(form.username(), form.email(), form.password(),
                    form.confirmPassword(), form.givenName(), form.familyName(), form.locale()));
            return "redirect:/login?registered";
        } catch (ResourceConflictException exception) {
            model.addAttribute("error", "error.accountExists");
        } catch (DomainException exception) {
            model.addAttribute("error", "error.registration");
        }
        return "register";
    }
}
