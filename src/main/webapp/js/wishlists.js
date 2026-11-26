'use strict';

document.addEventListener('DOMContentLoaded', () => {
    restoreScrollPosition();
    bindScrollPreservation();
    bindModals();
    bindCoverPreviews();
    bindGiftTableSelection();
    bindGiftTypeParticipants();
    bindReadonlyCopy();
    scrollChatToLatest();
});

function restoreScrollPosition() {
    const saved = sessionStorage.getItem('sgl.scrollY');
    if (saved) {
        sessionStorage.removeItem('sgl.scrollY');
        window.scrollTo(0, parseInt(saved, 10) || 0);
    }
}

function bindScrollPreservation() {
    document.querySelectorAll('form[data-preserve-scroll]').forEach((form) => {
        form.addEventListener('submit', () => {
            sessionStorage.setItem('sgl.scrollY', String(window.scrollY || 0));
        });
    });
}

function bindModals() {
    const openModal = (modal) => {
        if (!modal) {
            return;
        }
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    };

    const closeModal = (modal) => {
        if (!modal) {
            return;
        }
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    };

    document.querySelectorAll('[data-modal-open]').forEach((trigger) => {
        trigger.addEventListener('click', () => {
            openModal(document.getElementById(trigger.dataset.modalOpen));
        });
    });

    document.querySelectorAll('.modal-backdrop').forEach((modal) => {
        modal.querySelectorAll('[data-modal-close]').forEach((btn) => {
            btn.addEventListener('click', () => closeModal(modal));
        });
        modal.addEventListener('click', (event) => {
            if (event.target === modal) {
                closeModal(modal);
            }
        });
        if (modal.dataset.autoOpen === 'true') {
            openModal(modal);
        }
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            document.querySelectorAll('.modal-backdrop.is-open').forEach(closeModal);
        }
    });
}

function bindCoverPreviews() {
    document.querySelectorAll('select[data-preview-target]').forEach((select) => {
        const preview = document.getElementById(select.dataset.previewTarget);
        if (!preview) {
            return;
        }
        select.addEventListener('change', () => {
            const option = select.options[select.selectedIndex];
            const src = option ? option.dataset.previewSrc : '';
            if (src) {
                preview.src = src;
            }
        });
    });
}

function bindGiftTableSelection() {
    const checkboxes = document.querySelectorAll('.owlp-select-checkbox');
    const rows = document.querySelectorAll('.owlp-table tbody tr');

    const uncheckOthers = (active) => {
        checkboxes.forEach((box) => {
            if (box !== active) {
                box.checked = false;
            }
        });
    };

    checkboxes.forEach((box) => {
        box.addEventListener('change', () => {
            if (box.checked) {
                uncheckOthers(box);
            }
        });
    });

    rows.forEach((row) => {
        row.addEventListener('click', (event) => {
            const box = row.querySelector('.owlp-select-checkbox');
            if (!box || event.target === box) {
                return;
            }
            box.checked = !box.checked;
            if (box.checked) {
                uncheckOthers(box);
            }
        });
    });
}

function bindGiftTypeParticipants() {
    const typeSelect = document.getElementById('add-gift-type');
    const peopleInput = document.getElementById('add-gift-people');
    if (!typeSelect || !peopleInput) {
        return;
    }

    const syncState = () => {
        if (typeSelect.value === 'single') {
            peopleInput.value = '1';
            peopleInput.readOnly = true;
            peopleInput.style.backgroundColor = '#f3f3f3';
        } else {
            if (parseInt(peopleInput.value, 10) < 2) {
                peopleInput.value = '2';
            }
            peopleInput.readOnly = false;
            peopleInput.style.backgroundColor = '';
        }
    };

    typeSelect.addEventListener('change', syncState);
    syncState();
}

function bindReadonlyCopy() {
    document.querySelectorAll('.readonly-field').forEach((field) => {
        field.addEventListener('click', () => {
            if (typeof field.select === 'function') {
                field.select();
            }
            if (navigator.clipboard && typeof field.value === 'string' && field.value) {
                navigator.clipboard.writeText(field.value).catch(() => {});
            }
        });
    });
}

function scrollChatToLatest() {
    const chat = document.querySelector('.chat-messages');
    if (chat) {
        chat.scrollTop = chat.scrollHeight;
    }
}
