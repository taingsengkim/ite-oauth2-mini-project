@import gg.jte.Content
@import kh.edu.istad.platform.iam.web.view.WebUi
@param WebUi ui
@param String title
@param boolean account
@param boolean loginPage = false
@param Content content

<!DOCTYPE html>
<html lang="${ui.language()}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light dark">
    <title>${title} · ${ui.t("brand.name")}</title>
    <script src="/assets/theme.js"></script>
    <link rel="stylesheet" href="/assets/account.css">
    <link rel="stylesheet" href="/assets/kantumruy.css">
    <script src="/assets/account.js" defer></script>
</head>
<body>
<a class="skip-link" href="#main-content">${ui.t("common.skip")}</a>
@if(account)
    <div class="app-shell">
        <aside class="sidebar">
            <a class="brand" href="/"><img class="brand-seal" src="/assets/branding/istad-seal.png" alt="ISTAD" width="48" height="48"><span>${ui.t("brand.name")}<small>${ui.t("common.account")}</small></span></a>
            <p class="nav-caption">${ui.t("nav.settings")}</p>
            <nav aria-label="${ui.t("common.navigation")}">
                <a class="${ui.active("/")}" href="/"><span class="nav-symbol" aria-hidden="true">◈</span>${ui.t("nav.overview")}</a>
                <a class="${ui.active("/profile/edit")}" href="/profile/edit"><span class="nav-symbol" aria-hidden="true">◎</span>${ui.t("nav.profile")}</a>
                <a class="${ui.active("/profile/password")}" href="/profile/password"><span class="nav-symbol" aria-hidden="true">⌑</span>${ui.t("nav.security")}</a>
                <a class="${ui.active("/profile/accounts")}" href="/profile/accounts"><span class="nav-symbol" aria-hidden="true">↗</span>${ui.t("nav.accounts")}</a>
                <a class="${ui.active("/profile/apps")}" href="/profile/apps"><span class="nav-symbol" aria-hidden="true">▦</span>${ui.t("nav.apps")}</a>
            </nav>
            <div class="sidebar-footer">
                <span class="secure-dot"></span><span>${ui.t("brand.footer")}</span>
                <form class="sidebar-logout" method="post" action="/logout">@template.components.csrf(ui)<input type="hidden" name="lang" value="${ui.language()}"><button class="button button-secondary button-full" type="submit">${ui.t("common.logout")}</button></form>
            </div>
        </aside>
        <div class="workspace">
            <header class="topbar">
                <span class="breadcrumb">${ui.t("common.account")}<span aria-hidden="true">/</span><strong>${title}</strong></span>
                <div class="topbar-actions">
                    <nav class="language-switch" aria-label="${ui.t("common.language")}">
                        <a href="${ui.englishUrl()}" lang="en" class="${ui.language().equals("en") ? "selected" : ""}" aria-label="${ui.t("common.english")}" title="${ui.t("common.english")}"><img src="/assets/flags/united-kingdom.png" alt="" width="32" height="20"></a>
                        <a href="${ui.khmerUrl()}" lang="km" class="${ui.language().equals("km") ? "selected" : ""}" aria-label="${ui.t("common.khmer")}" title="${ui.t("common.khmer")}"><img src="/assets/flags/cambodia.webp" alt="" width="32" height="20"></a>
                    </nav>
                </div>
            </header>
            <main id="main-content" class="page-content">${content}</main>
            <footer class="workspace-footer">${ui.t("brand.name")}<span>${ui.t("brand.footer")}</span></footer>
        </div>
    </div>
@else
    <div class="auth-shell auth-shell-neutral ${loginPage ? "auth-login" : ""}">
        <div class="auth-main">
            <header class="auth-topbar"><button type="button" class="theme-toggle" data-theme-toggle aria-label="${ui.t("common.toggleTheme")}" title="${ui.t("common.toggleTheme")}"><svg class="theme-moon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M20.9 13A9 9 0 0 1 11 3.1 9 9 0 1 0 20.9 13Z"/></svg><svg class="theme-sun" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M2 12h2m16 0h2M5 5l1.5 1.5m11 11L19 19M5 19l1.5-1.5m11-11L19 5"/></svg></button><nav class="language-switch" aria-label="${ui.t("common.language")}">
                <a href="${ui.englishUrl()}" lang="en" class="${ui.language().equals("en") ? "selected" : ""}" aria-label="${ui.t("common.english")}" title="${ui.t("common.english")}"><img src="/assets/flags/united-kingdom.png" alt="" width="32" height="20"></a>
                <a href="${ui.khmerUrl()}" lang="km" class="${ui.language().equals("km") ? "selected" : ""}" aria-label="${ui.t("common.khmer")}" title="${ui.t("common.khmer")}"><img src="/assets/flags/cambodia.webp" alt="" width="32" height="20"></a>
            </nav></header>
            <div class="auth-stage">
                @if(loginPage)
                    <aside class="auth-welcome">
                        <a class="auth-welcome-brand" href="/login"><img src="/assets/branding/istad-seal.png" alt="ISTAD" width="80" height="80"><span>${ui.t("brand.name")}</span></a>
                        <h2>${ui.t("brand.workspace")}</h2>
                        <p>${ui.t("brand.description")}</p>
                        <ul>@for(String feature : java.util.List.of("brand.feature1", "brand.feature2", "brand.feature3"))<li><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="5"/><path d="m8 12 3 3 5-6"/></svg>${ui.t(feature)}</li>@endfor</ul>
                    </aside>
                @endif
                <main id="main-content" class="auth-card"><a class="auth-card-brand" href="/login"><img src="/assets/branding/istad-seal.png" alt="ISTAD" width="64" height="64"><span>${ui.t("brand.name")}</span></a>${content}</main>
            </div>
            <footer class="auth-footer">${ui.t("brand.footer")}</footer>
        </div>
    </div>
@endif
</body>
</html>
