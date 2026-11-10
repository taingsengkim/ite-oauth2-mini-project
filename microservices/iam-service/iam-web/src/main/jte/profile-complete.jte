@import kh.edu.istad.platform.iam.web.view.WebUi
@import kh.edu.istad.platform.iam.dto.identity.FederatedProfileDraft
@import kh.edu.istad.platform.iam.web.form.FederatedProfileForm
@param WebUi ui
@param FederatedProfileDraft draft
@param FederatedProfileForm form
@param String error = null

@template.layout.page(ui = ui, title = ui.t("completion.title"), account = false, content = @`
<div class="eyebrow">${ui.t("login.continueWith", ui.t("provider." + draft.provider().name().toLowerCase(java.util.Locale.ROOT)))}</div>
<h1>${ui.t("completion.title")}</h1>
<p class="page-description">${ui.t("completion.subtitle")}</p>
@template.components.feedback(ui, error)
<form method="post" action="/register/complete" class="form-stack">
    @template.components.csrf(ui)
    <input type="hidden" name="proofToken" value="${form.proofToken()}">
    <div class="field"><label for="email">${ui.t("field.email")}</label><input id="email" name="email" type="email" value="${form.email()}" maxlength="255" autocomplete="email" required></div>
    <div class="field-grid">
        <div class="field"><label for="givenName">${ui.t("field.givenName")}</label><input id="givenName" name="givenName" value="${form.givenName()}" maxlength="100" autocomplete="given-name" required></div>
        <div class="field"><label for="familyName">${ui.t("field.familyName")}</label><input id="familyName" name="familyName" value="${form.familyName()}" maxlength="100" autocomplete="family-name" required></div>
    </div>
    <div class="field"><label for="locale">${ui.t("field.locale")}</label><select id="locale" name="locale"><option value="en" selected="${"en".equals(form.locale())}">${ui.t("common.english")}</option><option value="km" selected="${"km".equals(form.locale())}">${ui.t("common.khmer")}</option></select></div>
    <p class="footnote">${ui.t("completion.emailHint")}</p>
    <button type="submit" class="button button-primary button-full">${ui.t("completion.submit")}<span aria-hidden="true">→</span></button>
</form>
<form method="post" action="/register/complete/cancel" class="consent-cancel">
    @template.components.csrf(ui)
    <input type="hidden" name="proofToken" value="${form.proofToken()}">
    <button type="submit" class="button button-secondary button-full">${ui.t("common.cancel")}</button>
</form>
<p class="auth-alternate">${ui.t("register.haveAccount")} <a href="/login">${ui.t("login.submit")}</a></p>
`)
