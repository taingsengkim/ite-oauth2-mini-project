@import kh.edu.istad.platform.iam.web.view.WebUi
@import java.util.List
@import kh.edu.istad.platform.iam.dto.client.ConsentClientResult
@param ConsentClientResult client
@param List<String> scopes
@param List<String> grantedScopes
@param String state
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("consent.title"), account = false, content = @`

<div class="consent-app-icon">${ui.initials(client.name())}</div>
<h1>${ui.t("consent.title")}</h1>
<p class="page-description">${ui.t("consent.subtitle", client.name())}</p>
<form method="post" action="/oauth2/authorize" class="form-stack">
    @template.components.csrf(ui)
    <input type="hidden" name="client_id" value="${client.clientId()}">
    <input type="hidden" name="state" value="${state}">
    <h2 class="section-label">${ui.t("consent.requested")}</h2>
    <div class="scope-list">
        <div class="scope-item"><span class="scope-check" aria-hidden="true">✓</span><div><strong>${ui.t("scope.openid")}</strong><small>${"openid"}</small></div></div>
        @for(String scope : scopes)
            <label class="scope-item"><input type="checkbox" name="scope" value="${scope}" checked><span><strong>${java.util.Set.of("profile", "email").contains(scope) ? ui.t("scope." + scope) : ui.t("scope.other", scope)}</strong><small>${scope}</small></span></label>
        @endfor
    </div>
    @if(!grantedScopes.isEmpty())
        <h2 class="section-label">${ui.t("consent.alreadyGranted")}</h2>
        <div class="chip-list">@for(String scope : grantedScopes)<span class="chip">${scope}</span>@endfor</div>
    @endif
    <p class="footnote">${ui.t("consent.hint")}</p>
    <button type="submit" class="button button-primary button-full">${ui.t("consent.allow")}<span aria-hidden="true">→</span></button>
</form>
<form method="post" action="/oauth2/authorize" class="consent-cancel">
    @template.components.csrf(ui)
    <input type="hidden" name="client_id" value="${client.clientId()}">
    <input type="hidden" name="state" value="${state}">
    <button type="submit" class="button button-secondary button-full">${ui.t("consent.cancel")}</button>
</form>

`)
