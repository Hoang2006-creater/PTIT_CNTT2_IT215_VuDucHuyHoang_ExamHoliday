// Sidebar JS
document.addEventListener('DOMContentLoaded', () => {
    // Highlight the active menu item based on current URL path
    const path = window.location.pathname;
    const links = document.querySelectorAll('.sidebar-menu-link');
    
    links.forEach(link => {
        const href = link.getAttribute('href');
        if (href === path || (href !== '/dashboard' && path.startsWith(href))) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Handle Collapse Sidebar
    const toggleBtn = document.getElementById('toggleSidebar');
    const sidebar = document.getElementById('sidebar');
    
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            // Check if mobile or desktop view
            if (window.innerWidth >= 992) {
                sidebar.classList.toggle('collapsed');
                const mainContent = document.querySelector('.main-content');
                if (mainContent) {
                    if (sidebar.classList.contains('collapsed')) {
                        mainContent.style.marginLeft = '80px';
                    } else {
                        mainContent.style.marginLeft = '260px';
                    }
                }
            } else {
                sidebar.classList.toggle('show');
            }
        });
    }
});
