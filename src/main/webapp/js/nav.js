'use strict';

document.addEventListener('DOMContentLoaded', () => {
    const menuToggle = document.getElementById('nav-menu-toggle');
    const nav = document.getElementById('primary-nav');
    const openIcon = document.getElementById('open-icon');
    const closeIcon = document.getElementById('close-icon');

    if (!menuToggle || !nav || !openIcon || !closeIcon) {
        return;
    }

    const setExpanded = (expanded) => {
        nav.classList.toggle('expanded', expanded);
        menuToggle.setAttribute('aria-expanded', String(expanded));
        openIcon.style.display = expanded ? 'none' : 'block';
        closeIcon.style.display = expanded ? 'block' : 'none';
    };

    menuToggle.addEventListener('click', () => {
        setExpanded(!nav.classList.contains('expanded'));
    });

    document.addEventListener('click', (event) => {
        if (!menuToggle.contains(event.target) && !nav.contains(event.target) && nav.classList.contains('expanded')) {
            setExpanded(false);
        }
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && nav.classList.contains('expanded')) {
            setExpanded(false);
        }
    });
});
