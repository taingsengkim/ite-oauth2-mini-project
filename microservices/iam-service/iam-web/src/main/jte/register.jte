@import kh.edu.istad.platform.iam.web.view.WebUi
@import kh.edu.istad.platform.iam.web.form.RegistrationForm
@param RegistrationForm form
@param String error = null
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("register.title"), account = false, content = @`

<div class="eyebrow">${ui.t("common.account")}</div>
<h1>${ui.t("register.title")}</h1>
<p class="page-description">${ui.t("register.subtitle")}</p>
@template.components.feedback(ui, error)
<form method="post" action="/register" class="form-stack">
    @template.components.csrf(ui)
    <div class="field-grid">
        <div class="field"><label for="givenName">${ui.t("field.givenName")}</label><input id="givenName" name="givenName" value="${form.givenName()}" maxlength="100" autocomplete="given-name" required></div>
        <div class="field"><label for="familyName">${ui.t("field.familyName")}</label><input id="familyName" name="familyName" value="${form.familyName()}" maxlength="100" autocomplete="family-name" required></div>
    </div>
    <div class="field"><label for="username">${ui.t("field.username")}</label><input id="username" name="username" value="${form.username()}" maxlength="50" autocomplete="username" required></div>
    <div class="field"><label for="email">${ui.t("field.email")}</label><input id="email" name="email" type="email" value="${form.email()}" maxlength="255" autocomplete="email" required></div>
    <div class="field-grid">
        <div class="field"><label for="password">${ui.t("login.password")}</label><input id="password" name="password" type="password" autocomplete="new-password" maxlength="72" required></div>
        <div class="field"><label for="confirmPassword">${ui.t("field.confirmPassword")}</label><input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" maxlength="72" required></div>
    </div>
    <div class="field"><label for="locale">${ui.t("field.locale")}</label><select id="locale" name="locale"><option value="en" selected="${"en".equals(form.locale())}">${ui.t("common.english")}</option><option value="km" selected="${"km".equals(form.locale())}">${ui.t("common.khmer")}</option></select></div>
    <button type="submit" class="button button-primary button-full">${ui.t("register.submit")}<span aria-hidden="true">→</span></button>
</form>
<p class="auth-alternate">${ui.t("register.haveAccount")} <a href="/login">${ui.t("login.submit")}</a></p>

`)
