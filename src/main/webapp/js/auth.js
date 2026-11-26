'use strict';

document.addEventListener('DOMContentLoaded', () => {
    bindImagePreviewSelects();
    bindRegisterValidation();
    bindProfileValidation();
});

function bindImagePreviewSelects() {
    document.querySelectorAll('select[data-preview-target]').forEach((select) => {
        const target = document.getElementById(select.dataset.previewTarget);
        if (!target) {
            return;
        }
        select.addEventListener('change', () => {
            const selected = select.options[select.selectedIndex];
            const previewSrc = selected ? selected.dataset.previewSrc : '';
            if (previewSrc) {
                target.src = previewSrc;
            }
        });
    });
}

function setFieldFeedback(elementId, message) {
    const el = document.getElementById(elementId);
    if (!el) {
        return;
    }
    el.textContent = message;
    el.className = message ? 'message-red' : '';
}

function bindRegisterValidation() {
    const form = document.getElementById('regForm');
    if (!form) {
        return;
    }

    const validate = () => {
        const username = (form.username?.value || '').trim();
        const email = (form.email?.value || '').trim();
        const password = form.password?.value || '';
        const confirmPassword = form.confirm_password?.value || '';

        const errUser = username ? '' : 'Please enter a username.';
        const errEmail = email && email.includes('@') ? '' : 'Please enter a valid email address.';
        const errPass = password.length >= 6 ? '' : 'Password must be at least 6 characters long.';
        const errMatch = !errPass && password !== confirmPassword ? 'Passwords must match.' : '';

        setFieldFeedback('msgUsername', errUser);
        setFieldFeedback('msgEmail', errEmail);
        setFieldFeedback('msgPassword', errPass);
        setFieldFeedback('msgPasswordNotEqual', errMatch);

        return !(errUser || errEmail || errPass || errMatch);
    };

    form.password?.addEventListener('input', validate);
    form.confirm_password?.addEventListener('input', validate);
    form.addEventListener('submit', (event) => {
        if (!validate()) {
            event.preventDefault();
        }
    });
}

function bindProfileValidation() {
    const form = document.getElementById('profile-form');
    if (!form) {
        return;
    }

    const validate = () => {
        const password = form.password?.value || '';
        const confirmPassword = form.confirm_password?.value || '';

        if (!password && !confirmPassword) {
            setFieldFeedback('msgPassword', '');
            setFieldFeedback('msgPasswordNotEqual', '');
            return true;
        }

        const errPass = password.length >= 6 ? '' : 'Password must be at least 6 characters long.';
        const errMatch = password === confirmPassword ? '' : 'Passwords must match.';

        setFieldFeedback('msgPassword', errPass);
        setFieldFeedback('msgPasswordNotEqual', errMatch);
        return !(errPass || errMatch);
    };

    form.password?.addEventListener('input', validate);
    form.confirm_password?.addEventListener('input', validate);
    form.addEventListener('submit', (event) => {
        if (!validate()) {
            event.preventDefault();
        }
    });
}
