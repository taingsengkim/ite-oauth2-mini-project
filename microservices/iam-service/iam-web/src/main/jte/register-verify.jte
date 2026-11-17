@import kh.edu.istad.platform.iam.web.view.WebUi
@import java.time.Instant
@param WebUi ui
@param String maskedEmail
@param Instant expiresAt
@param long resendSeconds
@param String error = null
@param String notice = null

@template.layout.page(ui = ui, title = ui.t("verification.title"), account = false, content = @`
<h1>${ui.t("verification.title")}</h1>
<p class="page-description">${ui.t("verification.subtitle", maskedEmail)}</p>
@template.components.feedback(ui, error, notice)
<form method="post" action="/register/verify" class="form-stack">
    @template.components.csrf(ui)
    <div class="field"><label for="verification-code">${ui.t("verification.code")}</label><input class="verification-code" id="verification-code" name="code" type="text" inputmode="numeric" autocomplete="one-time-code" pattern="[0-9]{6}" minlength="6" maxlength="6" required autofocus aria-describedby="verification-help"></div>
    <p class="verification-help" id="verification-help">${ui.t("verification.help")}<br>${ui.t("verification.expires")} <time datetime="${expiresAt.toString()}">${ui.date(expiresAt)}</time></p>
    <button type="submit" class="button button-primary button-full">${ui.t("verification.submit")}</button>
</form>
<div class="verification-actions">
    <form method="post" action="/register/resend">@template.components.csrf(ui)<button type="submit" class="button button-secondary button-full" data-resend-cooldown="${resendSeconds}">${ui.t("verification.resend")} <span data-resend-seconds aria-live="off"></span></button></form>
    <form method="post" action="/register/cancel">@template.components.csrf(ui)<button type="submit" class="button button-quiet button-full">${ui.t("verification.restart")}</button></form>
</div>
<p class="auth-alternate">${ui.t("register.haveAccount")} <a href="/login">${ui.t("login.submit")}</a></p>
`)
