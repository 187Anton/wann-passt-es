for (const form of document.forms) {
    const status = form.querySelector('[data-form-status]');
    const confirmation = form.dataset.confirm;

    if (status || confirmation) {
        form.addEventListener('submit', (event) => {
            if (confirmation && !window.confirm(confirmation)) {
                event.preventDefault();
                return;
            }
            if (status && event.submitter?.dataset.loading) {
                status.textContent = event.submitter.dataset.loading;
            }
        });
    }
}

window.addEventListener('pageshow', () => {
    for (const status of document.querySelectorAll('[data-form-status]')) {
        status.textContent = '';
    }
});
