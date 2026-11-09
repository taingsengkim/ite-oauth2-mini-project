package kh.edu.istad.platform.iam.web.controller;

import jakarta.validation.Valid;
import kh.edu.istad.common.domain.exception.DomainException;
import kh.edu.istad.platform.iam.application.exception.ResourceConflictException;
import kh.edu.istad.platform.iam.dto.user.ChangePasswordCommand;
import kh.edu.istad.platform.iam.dto.user.UpdateUserProfileCommand;
import kh.edu.istad.platform.iam.port.out.CurrentUserProvider;
import kh.edu.istad.platform.iam.usecase.client.ListConnectedAppsUseCase;
import kh.edu.istad.platform.iam.usecase.client.RevokeConnectedAppUseCase;
import kh.edu.istad.platform.iam.usecase.identity.ListUserIdentitiesUseCase;
import kh.edu.istad.platform.iam.usecase.identity.UnlinkIdentityUseCase;
import kh.edu.istad.platform.iam.usecase.user.*;
import kh.edu.istad.platform.iam.web.form.PasswordForm;
import kh.edu.istad.platform.iam.web.form.ProfileForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ProfilePageController {
    private final CurrentUserProvider currentUser;
    private final GetProfileDetailsUseCase profile;
    private final GetUserAuthoritiesUseCase authorities;
    private final UpdateUserProfileUseCase updateProfile;
    private final ChangePasswordUseCase changePassword;
    private final ListUserIdentitiesUseCase identities;
    private final UnlinkIdentityUseCase unlinkIdentity;
    private final ListConnectedAppsUseCase connectedApps;
    private final RevokeConnectedAppUseCase revokeApp;

    @GetMapping("/")
    public String index(Model model) {
        var uuid = currentUser.currentUserUuid();
        model.addAttribute("profile", profile.execute(uuid));
        model.addAttribute("authorities", authorities.execute(uuid));
        model.addAttribute("identityCount", identities.execute(uuid).size());
        model.addAttribute("appCount", connectedApps.execute(uuid).size());
        return "index";
    }

    @GetMapping("/profile/edit")
    public String edit(Model model) {
        var details = profile.execute(currentUser.currentUserUuid());
        var user = details.user();
        model.addAttribute("form", new ProfileForm(user.givenName(), user.familyName(),
                details.nickname(), details.phoneNumber(), user.locale()));
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String edit(@Valid @ModelAttribute("form") ProfileForm form,
                       BindingResult binding, Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "error.fields");
            return "profile-edit";
        }
        try {
            updateProfile.execute(new UpdateUserProfileCommand(currentUser.currentUserUuid(),
                    form.givenName(), form.familyName(), form.nickname(), form.locale(), form.phoneNumber()));
            redirect.addFlashAttribute("success", "success.profile");
            return "redirect:/";
        } catch (ResourceConflictException exception) {
            model.addAttribute("error", "error.profileConflict");
        } catch (DomainException exception) {
            model.addAttribute("error", "error.profile");
        }
        return "profile-edit";
    }

    @GetMapping("/profile/password")
    public String password(Model model) {
        model.addAttribute("hasPassword", profile.execute(currentUser.currentUserUuid()).hasPassword());
        model.addAttribute("form", PasswordForm.empty());
        return "change-password";
    }

    @PostMapping("/profile/password")
    public String password(@Valid @ModelAttribute("form") PasswordForm form, BindingResult binding,
                           Model model, RedirectAttributes redirect) {
        var uuid = currentUser.currentUserUuid();
        boolean hasPassword = profile.execute(uuid).hasPassword();
        model.addAttribute("hasPassword", hasPassword);
        if (!hasPassword) {
            model.addAttribute("error", "password.socialOnly");
        } else if (binding.hasErrors()) {
            model.addAttribute("error", "error.fields");
        } else if (!form.newPassword().equals(form.confirmPassword())) {
            model.addAttribute("error", "error.passwordMismatch");
        } else {
            try {
                changePassword.execute(new ChangePasswordCommand(uuid, form.currentPassword(),
                        form.newPassword(), form.confirmPassword()));
                redirect.addFlashAttribute("success", "success.password");
                return "redirect:/profile/password";
            } catch (DomainException exception) {
                model.addAttribute("error", "error.password");
            }
        }
        model.addAttribute("form", PasswordForm.empty());
        return "change-password";
    }

    @GetMapping("/profile/accounts")
    public String accounts(@RequestParam(name = "linked", required = false) String linked, Model model) {
        model.addAttribute("identities", identities.execute(currentUser.currentUserUuid()));
        if (linked != null) model.addAttribute("success", "success.linked");
        return "linked-accounts";
    }

    @PostMapping("/profile/accounts/{identityUuid}/unlink")
    public String unlink(@PathVariable("identityUuid") UUID identityUuid, RedirectAttributes redirect) {
        try {
            unlinkIdentity.execute(currentUser.currentUserUuid(), identityUuid);
            redirect.addFlashAttribute("success", "success.unlinked");
        } catch (DomainException exception) {
            redirect.addFlashAttribute("error", "error.lastSignIn");
        }
        return "redirect:/profile/accounts";
    }

    @GetMapping("/profile/apps")
    public String apps(Model model) {
        model.addAttribute("apps", connectedApps.execute(currentUser.currentUserUuid()));
        return "connected-apps";
    }

    @PostMapping("/profile/apps/{clientId}/revoke")
    public String revoke(@PathVariable("clientId") String clientId, RedirectAttributes redirect) {
        revokeApp.execute(currentUser.currentUserUuid(), clientId);
        redirect.addFlashAttribute("success", "success.revoked");
        return "redirect:/profile/apps";
    }
}
