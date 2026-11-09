@import kh.edu.istad.platform.iam.web.view.WebUi
@import java.util.List
@import kh.edu.istad.platform.iam.dto.identity.IdentityResult
@param List<IdentityResult> identities
@param String error = null
@param String success = null
@param WebUi ui

@template.layout.page(ui = ui, title = ui.t("profile.linkedAccounts"), account = true, content = @`

<div class="page-heading"><div><div class="eyebrow">${ui.t("common.account")}</div><h1>${ui.t("profile.linkedAccounts")}</h1><p class="page-description">${ui.t("accounts.subtitle")}</p></div></div>
@template.components.feedback(ui, error, success)
<div class="notice notice-info"><span class="notice-dot" aria-hidden="true"></span>${ui.t("accounts.hint")}</div>
<div class="account-grid">
    @for(String provider : java.util.List.of("google", "github", "facebook", "telegram"))
        !{var linked = identities.stream().filter(value -> value.provider().name().equalsIgnoreCase(provider)).findFirst().orElse(null);}
        <section class="panel provider-card">
            <div class="provider-card-heading"><span class="provider-icon provider-icon-large ${provider}" aria-hidden="true">${provider.substring(0, 1).toUpperCase()}</span><span class="badge ${linked != null ? "badge-green" : ""}">${ui.t(linked != null ? "accounts.connected" : "accounts.notConnected")}</span></div>
            <h2>${ui.t("provider." + provider)}</h2>
            @if(linked != null)
                <p class="provider-account">${ui.text(linked.email() != null ? linked.email() : linked.username() != null ? linked.username() : linked.displayName())}</p>
                <dl class="provider-dates"><div><dt>${ui.t("accounts.linkedAt")}</dt><dd>${ui.date(linked.linkedAt())}</dd></div><div><dt>${ui.t("accounts.lastLogin")}</dt><dd>${ui.date(linked.lastLoginAt())}</dd></div></dl>
                <form method="post" action="${"/profile/accounts/" + linked.uuid() + "/unlink"}" data-confirm="${ui.t("accounts.confirmUnlink")}">@template.components.csrf(ui)<button type="submit" class="button button-danger button-full">${ui.t("accounts.unlink")}</button></form>
            @else
                <p class="muted provider-description">${ui.t("login.continueWith", ui.t("provider." + provider))}</p>
                <form method="post" action="${"/account/identities/link/" + provider}">@template.components.csrf(ui)<button type="submit" class="button button-secondary button-full">${ui.t("accounts.link")}<span aria-hidden="true">↗</span></button></form>
            @endif
        </section>
    @endfor
</div>

`)
