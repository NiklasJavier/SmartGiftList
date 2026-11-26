/* ################### SECONDARY HEADER ################### */
document.addEventListener('DOMContentLoaded', function() {
    var menuIcon = document.querySelector('.menu-icon');
    var nav = document.querySelector('.nav');
    var openIcon = document.getElementById('open-icon');
    var closeIcon = document.getElementById('close-icon');

    if (!menuIcon || !nav || !openIcon || !closeIcon) {
        return;
    }

    menuIcon.addEventListener('click', function() {
        nav.classList.toggle('expanded');

        if (nav.classList.contains('expanded')) {
            openIcon.style.display = 'none';
            closeIcon.style.display = 'block';
        } else {
            openIcon.style.display = 'block';
            closeIcon.style.display = 'none';
        }
    });

    document.addEventListener('click', function(event) {
        var isClickInsideMenu = menuIcon.contains(event.target) || nav.contains(event.target);

        if (!isClickInsideMenu && nav.classList.contains('expanded')) {
            nav.classList.remove('expanded');
            openIcon.style.display = 'block';
            closeIcon.style.display = 'none';
        }
    });
});
