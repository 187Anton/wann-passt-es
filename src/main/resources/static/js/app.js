/*
 * Progressive enhancement only: every core action also works without this file.
 */
(function () {
    'use strict';

    document.documentElement.classList.add('js');

    document.querySelectorAll('[data-confirm]').forEach(function (button) {
        var form = button.form;
        if (!form) {
            return;
        }
        form.addEventListener('submit', function (event) {
            if (!window.confirm(button.getAttribute('data-confirm'))) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var submitter = event.submitter;
            if (!submitter || submitter.classList.contains('is-busy')) {
                return;
            }
            submitter.classList.add('is-busy');
            submitter.setAttribute('aria-busy', 'true');
        });
    });

    var startDate = document.getElementById('startDate');
    var endDate = document.getElementById('endDate');
    if (startDate && endDate) {
        var syncEndDate = function () {
            if (!startDate.value) {
                endDate.removeAttribute('min');
                return;
            }
            endDate.min = startDate.value;
            if (endDate.value && endDate.value < startDate.value) {
                endDate.value = startDate.value;
            }
        };
        startDate.addEventListener('change', syncEndDate);
        syncEndDate();
    }
}());
