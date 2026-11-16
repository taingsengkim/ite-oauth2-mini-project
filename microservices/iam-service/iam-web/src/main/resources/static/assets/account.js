document.querySelectorAll("[data-password-toggle]").forEach(button => {
    button.addEventListener("click", () => {
        const input = document.getElementById(button.dataset.passwordToggle);
        const showing = input.type === "password";
        input.type = showing ? "text" : "password";
        button.setAttribute("aria-label", showing ? button.dataset.hide : button.dataset.show);
        button.setAttribute("aria-pressed", String(showing));
    });
});
document.querySelectorAll("form[data-confirm]").forEach(form => {
    form.addEventListener("submit", event => {
        if (!window.confirm(form.dataset.confirm)) event.preventDefault();
    });
});
// Recheck server authentication when Back restores a cached login/account page.
window.addEventListener("pageshow", event => {
    if (event.persisted) window.location.reload();
});
