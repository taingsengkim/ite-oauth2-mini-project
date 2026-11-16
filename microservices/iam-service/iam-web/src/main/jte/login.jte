@import kh.edu.istad.platform.iam.web.view.WebUi
@param boolean loginError
@param boolean loggedOut
@param boolean registered
@param String loginErrorKey = "login.error"
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("login.title"), account = false, loginPage = true, content = @`

<h1>${ui.t("login.title")}</h1>
<p class="page-description">${ui.t("login.subtitle")}</p>
@if(loginError && "login.linkRequired".equals(loginErrorKey))
    @template.components.linkRequired(ui)
@else
    @template.components.feedback(ui, loginError ? loginErrorKey : null, registered ? "login.registered" : loggedOut ? "login.loggedOut" : null)
@endif
<form method="post" action="/login" class="form-stack">
    @template.components.csrf(ui)
    <div class="field"><label for="username">${ui.t("login.username")}</label><input id="username" name="username" autocomplete="username" maxlength="255" required autofocus></div>
    <div class="field"><label for="password">${ui.t("login.password")}<a class="forgot-link" href="/dashboard/recovery.html">${ui.t("login.forgotPassword")}</a></label><div class="password-field"><input id="password" name="password" type="password" autocomplete="current-password" required><button type="button" class="password-toggle" data-password-toggle="password" aria-label="${ui.t("common.showPassword")}" data-show="${ui.t("common.showPassword")}" data-hide="${ui.t("common.hidePassword")}"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg></button></div></div>
    <button type="submit" class="button button-primary button-full">${ui.t("login.submit")}<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6"/></svg></button>
</form>
<div class="divider"><span>${ui.t("login.or")}</span></div>
<div class="social-buttons">
    @for(String provider : java.util.List.of("google", "github", "facebook", "telegram"))
        <a class="social-button" href="${"/oauth2/authorization/" + provider}" aria-label="${ui.t("login.continueWith", ui.t("provider." + provider))}"><span class="provider-icon ${provider}" aria-hidden="true"><img src="${"/assets/providers/" + provider + ".png"}" alt="" width="24" height="24"></span>${ui.t("provider." + provider)}</a>
    @endfor
</div>
<p class="auth-alternate">${ui.t("login.noAccount")} <a href="/register">${ui.t("login.register")} <span aria-hidden="true">↗</span></a></p>

`)
