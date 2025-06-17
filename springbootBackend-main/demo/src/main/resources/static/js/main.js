document.addEventListener('DOMContentLoaded', function() {
    // Sidebar toggle
    const toggle = document.getElementById('header-toggle');
    const nav = document.getElementById('navbar');
    if (toggle && nav) {
        toggle.addEventListener('click', function() {
            nav.classList.toggle('show-menu');
        });
    } else {
        console.warn('Elements header-toggle or navbar not found');
    }

    // Active link highlighting
    const links = document.querySelectorAll('.nav__link, .nav__dropdown-item');
    if (links.length > 0) {
        links.forEach(link => {
            if (link.href === window.location.href) {
                link.classList.add('active');
            }
        });
    } else {
        console.warn('No nav__link or nav__dropdown-item elements found');
    }
});
function toggleDropdown() {
    const dropdown = document.getElementById('userDropdown');
    dropdown.classList.toggle('show');
}
