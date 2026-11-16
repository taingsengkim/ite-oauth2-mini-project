(() => {
    const root = document.documentElement;
    let preference;
    try { preference = localStorage.getItem('iam-theme'); } catch { /* Storage may be unavailable. */ }
    const apply = dark => {
        root.classList.toggle('dark', dark);
        root.style.colorScheme = dark ? 'dark' : 'light';
    };
    apply(preference === 'dark' || (preference !== 'light' && matchMedia('(prefers-color-scheme: dark)').matches));
    document.addEventListener('DOMContentLoaded', () => {
        document.querySelectorAll('[data-theme-toggle]').forEach(button => {
            button.addEventListener('click', () => {
                const dark = !root.classList.contains('dark');
                apply(dark);
                try { localStorage.setItem('iam-theme', dark ? 'dark' : 'light'); } catch { /* Storage may be unavailable. */ }
            });
        });
    });
})();
