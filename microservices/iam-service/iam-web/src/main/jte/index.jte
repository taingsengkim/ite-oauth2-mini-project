@import kh.edu.istad.platform.iam.web.view.WebUi
@import kh.edu.istad.platform.iam.dto.user.ProfileDetailsResult
@import kh.edu.istad.platform.iam.dto.user.UserAuthoritiesResult
@param ProfileDetailsResult profile
@param UserAuthoritiesResult authorities
@param int identityCount
@param int appCount
@param String success = null
@param String error = null
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("profile.title"), account = true, content = @`

<div class="page-heading"><div><div class="eyebrow">${ui.t("nav.overview")}</div><h1>${ui.t("profile.hello", profile.user().givenName())}</h1><p class="page-description">${ui.t("profile.subtitle")}</p></div><a class="button button-secondary" href="/profile/edit">${ui.t("profile.edit")}<span aria-hidden="true">↗</span></a></div>
@template.components.feedback(ui, error, success)
<section class="profile-banner">
    <div class="avatar">${ui.initials(profile.user().givenName())}</div>
    <div><h2>${profile.user().name()}</h2><p>${profile.user().email()}</p><span class="badge badge-light">${ui.t(profile.user().enabled() ? "common.active" : "common.disabled")}</span></div>
    <div class="banner-meta"><span>${ui.t("profile.created")}</span><strong>${ui.date(profile.user().createdAt())}</strong></div>
</section>
<div class="stats-grid">
    <a class="stat-card" href="/profile/accounts"><span class="stat-icon" aria-hidden="true">↗</span><div><span>${ui.t("profile.linkedAccounts")}</span><strong>${identityCount}</strong></div><span class="stat-arrow" aria-hidden="true">↗</span></a>
    <a class="stat-card" href="/profile/apps"><span class="stat-icon stat-lilac" aria-hidden="true">▦</span><div><span>${ui.t("profile.connectedApps")}</span><strong>${appCount}</strong></div><span class="stat-arrow" aria-hidden="true">↗</span></a>
    <div class="stat-card"><span class="stat-icon stat-sand" aria-hidden="true">◈</span><div><span>${ui.t("profile.roles")}</span><strong>${authorities.roles().size()}</strong></div></div>
</div>
<div class="overview-grid">
    <section class="panel"><div class="panel-heading"><div><h2>${ui.t("profile.details")}</h2><p>${ui.t("profile.detailsHint")}</p></div></div>
        <dl class="details-list">
            <div><dt>${ui.t("field.username")}</dt><dd>${profile.user().username()}</dd></div>
            <div><dt>${ui.t("field.email")}</dt><dd>${profile.user().email()} <span class="badge">${ui.t(profile.user().emailVerified() ? "common.verified" : "common.unverified")}</span></dd></div>
            <div><dt>${ui.t("field.givenName")}</dt><dd>${profile.user().givenName()}</dd></div>
            <div><dt>${ui.t("field.familyName")}</dt><dd>${profile.user().familyName()}</dd></div>
            <div><dt>${ui.t("field.nickname")}</dt><dd>${ui.text(profile.nickname())}</dd></div>
            <div><dt>${ui.t("field.phone")}</dt><dd>${ui.text(profile.phoneNumber())}</dd></div>
            <div><dt>${ui.t("field.locale")}</dt><dd>${ui.t("km".equals(profile.user().locale()) ? "common.khmer" : "common.english")}</dd></div>
            <div><dt>${ui.t("profile.userId")}</dt><dd class="monospace">${profile.user().uuid().toString()}</dd></div>
        </dl>
    </section>
    <section class="panel"><div class="panel-heading"><div><h2>${ui.t("profile.access")}</h2><p>${ui.t("profile.accessHint")}</p></div></div>
        <h3 class="section-label">${ui.t("profile.roles")}</h3><div class="chip-list">
            @for(var role : authorities.roles())
                <span class="chip chip-role">${role.name()}</span>
            @else
                <p class="muted">${ui.t("profile.noRoles")}</p>
            @endfor
        </div>
        <h3 class="section-label">${ui.t("profile.permissions")}</h3><div class="chip-list">
            @for(String permission : authorities.effectivePermissions())
                <span class="chip monospace">${permission}</span>
            @else
                <p class="muted">${ui.t("profile.noPermissions")}</p>
            @endfor
        </div>
    </section>
</div>

`)
