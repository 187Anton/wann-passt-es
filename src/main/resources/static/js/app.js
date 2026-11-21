/*
 * Progressive enhancements for "Wann passt es?".
 * Every feature works without JavaScript; this script only improves usability.
 */
(function () {
    'use strict';

    /**
     * Location search forms carry the current selection in hidden fields.
     * Without JavaScript these hold the last submitted values; here they are
     * updated with the values currently entered in the source form.
     */
    function carryCurrentValues(form) {
        var source = document.getElementById(form.dataset.carryFrom);
        if (!source) {
            return;
        }
        form.addEventListener('submit', function () {
            var hiddenFields = form.querySelectorAll('input[type="hidden"]');
            hiddenFields.forEach(function (hidden) {
                // For radio groups this is a RadioNodeList whose value is the checked option.
                var field = source.elements.namedItem(hidden.name);
                if (field && typeof field.value === 'string') {
                    hidden.value = field.value;
                }
            });
        });
    }

    /** Asks for confirmation before submitting destructive forms. */
    function confirmBeforeSubmit(form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    }

    /** Keeps the end date at or after the start date while choosing dates. */
    function linkDateRange(startInput, endInput) {
        function sync() {
            if (startInput.value) {
                endInput.min = startInput.value > startInput.min ? startInput.value : startInput.min;
                if (endInput.value && endInput.value < startInput.value) {
                    endInput.value = startInput.value;
                }
            }
        }
        // Only react to user changes, so values shown with an error message stay untouched.
        startInput.addEventListener('change', sync);
    }

    /** Shows a loading state while the weather search is running. */
    function showLoadingState(form) {
        form.addEventListener('submit', function (event) {
            var submitter = event.submitter;
            // Only the main search button triggers the loading state, not "save profile".
            if (submitter && submitter.hasAttribute('formaction')) {
                return;
            }
            var button = form.querySelector('[data-loading-text]');
            if (!button) {
                return;
            }
            button.dataset.originalText = button.textContent;
            button.setAttribute('aria-busy', 'true');
            button.textContent = button.dataset.loadingText;
            document.body.classList.add('is-loading');
        });
    }

    document.querySelectorAll('form[data-carry-from]').forEach(carryCurrentValues);
    document.querySelectorAll('form[data-confirm]').forEach(confirmBeforeSubmit);
    document.querySelectorAll('form[data-loading-form]').forEach(showLoadingState);

    var startDate = document.getElementById('startDate');
    var endDate = document.getElementById('endDate');
    if (startDate && endDate) {
        linkDateRange(startDate, endDate);
    }

    // Reset the loading state when the page is restored from the back/forward cache.
    window.addEventListener('pageshow', function (event) {
        if (!event.persisted) {
            return;
        }
        document.body.classList.remove('is-loading');
        document.querySelectorAll('[data-loading-text][aria-busy="true"]').forEach(function (button) {
            button.removeAttribute('aria-busy');
            button.textContent = button.dataset.originalText;
        });
    });
}());
