@import gg.jte.Content
@import kh.edu.istad.platform.iam.web.view.WebUi
@param WebUi ui
@param String title
@param boolean account
@param Content content

<!DOCTYPE html>
<html lang="${ui.language()}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light">
    <title>${title} · ${ui.t("brand.name")}</title>
    <link rel="stylesheet" href="/assets/account.css">
    <script src="/assets/account.js" defer></script>
</head>
<body>
<a class="skip-link" href="#main-content">${ui.t("common.skip")}</a>
@if(account)
    <div class="app-shell">
        <aside class="sidebar">
            <a class="brand" href="/"><span class="brand-mark">${ui.t("brand.short")}</span><span>${ui.t("brand.name")}<small>${ui.t("common.account")}</small></span></a>
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
            </div>
        </aside>
        <div class="workspace">
            <header class="topbar">
                <span class="breadcrumb">${ui.t("common.account")}<span aria-hidden="true">/</span><strong>${title}</strong></span>
                <div class="topbar-actions">
                    <nav class="language-switch" aria-label="${ui.t("common.language")}">
                        <a href="${ui.englishUrl()}" lang="en" class="${ui.language().equals("en") ? "selected" : ""}">${ui.t("common.english")}</a>
                        <a href="${ui.khmerUrl()}" lang="km" class="${ui.language().equals("km") ? "selected" : ""}">${ui.t("common.khmer")}</a>
                    </nav>
                    <form method="post" action="/logout">@template.components.csrf(ui)<button class="button button-quiet" type="submit">${ui.t("common.logout")}</button></form>
                </div>
            </header>
            <main id="main-content" class="page-content">${content}</main>
            <footer class="workspace-footer">${ui.t("brand.name")}<span>${ui.t("brand.footer")}</span></footer>
        </div>
    </div>
@else
    <div class="auth-shell">
        <aside class="auth-story">
            <a class="brand brand-light" href="/login"><span class="brand-mark">${ui.t("brand.short")}</span><span>${ui.t("brand.name")}</span></a>
            <div class="story-body">
                <div class="orbit-art" aria-hidden="true"><span class="orbit orbit-one"></span><span class="orbit orbit-two"></span><span class="orbit-core">◈</span><span class="orbit-node node-one">◎</span><span class="orbit-node node-two">↗</span><span class="orbit-node node-three">▦</span></div>
                <h2>${ui.t("brand.workspace")}</h2>
                <p>${ui.t("brand.description")}</p>
                <ul class="story-features"><li>${ui.t("brand.feature1")}</li><li>${ui.t("brand.feature2")}</li><li>${ui.t("brand.feature3")}</li></ul>
            </div>
            <p class="story-footer">${ui.t("brand.footer")}</p>
        </aside>
        <div class="auth-main">
            <header class="auth-topbar"><nav class="language-switch" aria-label="${ui.t("common.language")}">
                <a href="${ui.englishUrl()}" lang="en" class="${ui.language().equals("en") ? "selected" : ""}">${ui.t("common.english")}</a>
                <a href="${ui.khmerUrl()}" lang="km" class="${ui.language().equals("km") ? "selected" : ""}">${ui.t("common.khmer")}</a>
            </nav></header>
            <main id="main-content" class="auth-card">${content}</main>
            <footer class="auth-footer">${ui.t("brand.name")}</footer>
        </div>
    </div>
@endif
</body>
</html>
