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

// Convenience countdown only; the server independently enforces resend throttling.
document.querySelectorAll('[data-resend-cooldown]').forEach(button => {
    let seconds = Math.min(60, Math.max(0, Number(button.dataset.resendCooldown) || 0));
    const label = button.querySelector('[data-resend-seconds]');
    const update = () => { button.disabled = seconds > 0; if (label) label.textContent = seconds > 0 ? '(' + seconds + 's)' : ''; };
    update();
    if (seconds > 0) { const timer = window.setInterval(() => { seconds--; update(); if (seconds <= 0) window.clearInterval(timer); }, 1000); }
});
