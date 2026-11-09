@import kh.edu.istad.platform.iam.web.view.WebUi
@import java.util.List
@import kh.edu.istad.platform.iam.dto.client.ConnectedAppResult
@param List<ConnectedAppResult> apps
@param String success = null
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("profile.connectedApps"), account = true, content = @`

<div class="page-heading"><div><div class="eyebrow">${ui.t("common.account")}</div><h1>${ui.t("profile.connectedApps")}</h1><p class="page-description">${ui.t("apps.subtitle")}</p></div></div>
@template.components.feedback(ui, null, success)
@if(apps.isEmpty())
    <section class="panel empty-state empty-large"><span class="empty-icon" aria-hidden="true">▦</span><h2>${ui.t("apps.emptyTitle")}</h2><p>${ui.t("apps.emptyHint")}</p><a class="button button-secondary" href="/">${ui.t("common.back")}</a></section>
@else
    <div class="apps-list">
    @for(var app : apps)
        <section class="panel application-card"><span class="app-icon" aria-hidden="true">${ui.initials(app.name())}</span><div class="application-details"><h2>${app.name()}</h2><p class="monospace muted client-id">${app.clientId()}</p><h3 class="section-label">${ui.t("apps.scopes")}</h3><div class="chip-list">@for(String scope : new java.util.TreeSet<>(app.scopes()))<span class="chip">${scope}</span>@endfor</div></div><form method="post" action="${"/profile/apps/" + app.id() + "/revoke"}" data-confirm="${ui.t("apps.confirmRevoke")}">@template.components.csrf(ui)<button class="button button-danger" type="submit">${ui.t("apps.revoke")}</button></form></section>
    @endfor
    </div>
    <p class="footnote">${ui.t("apps.revokeHint")}</p>
@endif

`)
