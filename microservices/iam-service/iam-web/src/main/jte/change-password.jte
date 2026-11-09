@import kh.edu.istad.platform.iam.web.view.WebUi
@import kh.edu.istad.platform.iam.web.form.PasswordForm
@param PasswordForm form
@param boolean hasPassword
@param String error = null
@param String success = null
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("password.title"), account = true, content = @`

<div class="page-heading"><div><div class="eyebrow">${ui.t("common.account")}</div><h1>${ui.t("password.title")}</h1><p class="page-description">${ui.t("password.subtitle")}</p></div></div>
<div class="settings-grid"><section class="panel form-panel">
    @template.components.feedback(ui, error, success)
    @if(hasPassword)
        <form method="post" action="/profile/password" class="form-stack">
            @template.components.csrf(ui)
            <div class="field"><label for="currentPassword">${ui.t("field.currentPassword")}</label><input id="currentPassword" name="currentPassword" type="password" autocomplete="current-password" required></div>
            <div class="field"><label for="newPassword">${ui.t("field.newPassword")}</label><input id="newPassword" name="newPassword" type="password" autocomplete="new-password" maxlength="72" required></div>
            <div class="field"><label for="confirmPassword">${ui.t("field.confirmNewPassword")}</label><input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" maxlength="72" required></div>
            <div class="form-actions"><a class="button button-secondary" href="/">${ui.t("common.cancel")}</a><button class="button button-primary" type="submit">${ui.t("password.submit")}</button></div>
        </form>
    @else
        <div class="empty-state"><span class="empty-icon" aria-hidden="true">⌑</span><p>${ui.t("password.socialOnly")}</p><a class="button button-primary" href="/profile/accounts">${ui.t("profile.linkedAccounts")}</a></div>
    @endif
</section><aside class="help-card"><span class="help-icon" aria-hidden="true">⌑</span><h3>${ui.t("password.title")}</h3><p>${ui.t("field.passwordHint")}</p></aside></div>

`)
