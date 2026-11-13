@import kh.edu.istad.platform.iam.web.view.WebUi
@param WebUi ui

<div class="notice notice-error" role="alert" aria-labelledby="link-required-title">
    <span class="notice-dot" aria-hidden="true"></span>
    <div>
        <h2 id="link-required-title">${ui.t("login.linkRequiredTitle")}</h2>
        <p>${ui.t("login.linkRequired")}</p>
        <ol>
            <li>${ui.t("login.linkRequiredSignIn")}</li>
            <li>${ui.t("login.linkRequiredConnect")}</li>
        </ol>
        <a class="button button-secondary" href="/profile/accounts">${ui.t("login.linkRequiredAction")}</a>
    </div>
</div>
