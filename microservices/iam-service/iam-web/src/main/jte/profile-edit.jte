@import kh.edu.istad.platform.iam.web.view.WebUi
@import kh.edu.istad.platform.iam.web.form.ProfileForm
@param ProfileForm form
@param String error = null
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("profile.edit"), account = true, content = @`

<div class="page-heading"><div><div class="eyebrow">${ui.t("common.account")}</div><h1>${ui.t("profile.edit")}</h1><p class="page-description">${ui.t("profile.editSubtitle")}</p></div></div>
<div class="settings-grid"><section class="panel form-panel">
    @template.components.feedback(ui, error)
    <form method="post" action="/profile/edit" class="form-stack">
        @template.components.csrf(ui)
        <div class="field-grid"><div class="field"><label for="givenName">${ui.t("field.givenName")}</label><input id="givenName" name="givenName" value="${form.givenName()}" maxlength="100" autocomplete="given-name" required></div><div class="field"><label for="familyName">${ui.t("field.familyName")}</label><input id="familyName" name="familyName" value="${form.familyName()}" maxlength="100" autocomplete="family-name" required></div></div>
        <div class="field"><label for="nickname">${ui.t("field.nickname")}<span>${ui.t("common.optional")}</span></label><input id="nickname" name="nickname" value="${form.nickname()}" maxlength="100" autocomplete="nickname"></div>
        <div class="field"><label for="phoneNumber">${ui.t("field.phone")}<span>${ui.t("common.optional")}</span></label><input id="phoneNumber" name="phoneNumber" type="tel" value="${form.phoneNumber()}" maxlength="20" autocomplete="tel"></div>
        <div class="field"><label for="locale">${ui.t("field.locale")}</label><select id="locale" name="locale"><option value="en" selected="${"en".equals(form.locale())}">${ui.t("common.english")}</option><option value="km" selected="${"km".equals(form.locale())}">${ui.t("common.khmer")}</option></select></div>
        <div class="form-actions"><a class="button button-secondary" href="/">${ui.t("common.cancel")}</a><button class="button button-primary" type="submit">${ui.t("common.save")}</button></div>
    </form>
</section><aside class="help-card"><span class="help-icon" aria-hidden="true">◎</span><h3>${ui.t("profile.details")}</h3><p>${ui.t("profile.emailHint")}</p><p>${ui.t("common.required")}</p></aside></div>

`)
