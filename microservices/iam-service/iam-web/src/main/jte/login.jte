@import kh.edu.istad.platform.iam.web.view.WebUi
@param boolean loginError
@param boolean loggedOut
@param boolean registered
@param String loginErrorKey = "login.error"
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("login.title"), account = false, content = @`

<div class="eyebrow">${ui.t("common.account")}</div>
<h1>${ui.t("login.title")}</h1>
<p class="page-description">${ui.t("login.subtitle")}</p>
@template.components.feedback(ui, loginError ? loginErrorKey : null, registered ? "login.registered" : loggedOut ? "login.loggedOut" : null)
<form method="post" action="/login" class="form-stack">
    @template.components.csrf(ui)
    <div class="field"><label for="username">${ui.t("login.username")}</label><input id="username" name="username" autocomplete="username" maxlength="255" required autofocus></div>
    <div class="field"><label for="password">${ui.t("login.password")}</label><div class="password-field"><input id="password" name="password" type="password" autocomplete="current-password" required><button type="button" class="password-toggle" data-password-toggle="password" aria-label="${ui.t("common.showPassword")}" data-show="${ui.t("common.showPassword")}" data-hide="${ui.t("common.hidePassword")}"><span aria-hidden="true">◎</span></button></div></div>
    <button type="submit" class="button button-primary button-full">${ui.t("login.submit")}<span aria-hidden="true">→</span></button>
</form>
<div class="divider"><span>${ui.t("login.or")}</span></div>
<div class="social-buttons">
    @for(String provider : java.util.List.of("google", "github", "facebook", "telegram"))
        <a class="social-button" href="${"/oauth2/authorization/" + provider}" aria-label="${ui.t("login.continueWith", ui.t("provider." + provider))}"><span class="provider-icon ${provider}" aria-hidden="true">${provider.substring(0, 1).toUpperCase()}</span>${ui.t("provider." + provider)}</a>
    @endfor
</div>
<p class="auth-alternate">${ui.t("login.noAccount")} <a href="/register">${ui.t("login.register")} <span aria-hidden="true">↗</span></a></p>

`)
