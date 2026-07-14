// Dashboard JavaScript

document.addEventListener('DOMContentLoaded', async () => {
    // Mobile sidebar toggle
    const toggleSidebarBtn = document.getElementById('toggleSidebar');
    const sidebar = document.getElementById('sidebar');
    
    if (toggleSidebarBtn && sidebar) {
        toggleSidebarBtn.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }

    // Load user profile details dynamically
    await loadUserProfile();

    // Setup logout handler
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            showLoading(true);

            try {
                // Logout request
                const response = await customFetch('/api/auth/logout', {
                    method: 'POST'
                });

                showLoading(false);
                if (response.ok) {
                    showToast('Đăng xuất thành công!', 'success');
                }
            } catch (err) {
                console.error('Logout error:', err);
                showLoading(false);
            }

            // Always clear tokens locally and redirect to login
            clearTokens();
            setTimeout(() => {
                window.location.href = '/login';
            }, 500);
        });
    }
});

async function loadUserProfile() {
    const profileNameEl = document.getElementById('profile-name');
    const profileEmailEl = document.getElementById('profile-email');
    const profileRoleEl = document.getElementById('profile-role');
    const roleBadgeEl = document.getElementById('role-badge');
    const welcomeUserEl = document.getElementById('welcome-user');

    if (!profileNameEl) return; // Not on dashboard page

    try {
        const response = await customFetch('/api/auth/profile');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                const user = result.data;

                // Update text elements
                profileNameEl.textContent = user.username;
                profileEmailEl.textContent = user.email || 'Chưa cập nhật email';
                profileRoleEl.textContent = user.role;
                if (welcomeUserEl) welcomeUserEl.textContent = user.username;

                // Map roles to Vietnamese display text
                const roleMap = {
                    'ADMIN': 'Quản trị viên',
                    'MANAGER': 'Quản lý',
                    'WAITSTAFF': 'Nhân viên phục vụ',
                    'CASHIER': 'Thu ngân',
                    'CHEF': 'Đầu bếp',
                    'CUSTOMER': 'Khách hàng'
                };

                if (roleBadgeEl) {
                    roleBadgeEl.textContent = roleMap[user.role] || user.role;
                }

                // Show/hide menu items based on role
                renderRoleMenus(user.role);
            }
        } else {
            // Unauthenticated or session expired
            clearTokens();
            window.location.href = '/login';
        }
    } catch (e) {
        console.error('Failed to load profile:', e);
        showToast('Không thể kết nối đến máy chủ để tải thông tin hồ sơ.', 'error');
    }
}

function renderRoleMenus(role) {
    // Role-specific sections hide/show
    const menuItems = {
        'admin': document.querySelectorAll('[data-role-required="ADMIN"]'),
        'manager': document.querySelectorAll('[data-role-required="MANAGER"]'),
        'cashier': document.querySelectorAll('[data-role-required="CASHIER"]'),
        'waitstaff': document.querySelectorAll('[data-role-required="WAITSTAFF"]'),
        'chef': document.querySelectorAll('[data-role-required="CHEF"]'),
        'customer': document.querySelectorAll('[data-role-required="CUSTOMER"]')
    };

    // Hide all role-specific items first
    Object.values(menuItems).forEach(elements => {
        elements.forEach(el => el.classList.add('d-none'));
    });

    // Show menu items based on current role permissions
    // Admin has access to all functions
    if (role === 'ADMIN') {
        Object.values(menuItems).forEach(elements => {
            elements.forEach(el => el.classList.remove('d-none'));
        });
    } else if (role === 'MANAGER') {
        // Manager can access employee, menu, promo, and stats
        menuItems['manager'].forEach(el => el.classList.remove('d-none'));
        menuItems['waitstaff'].forEach(el => el.classList.remove('d-none'));
        menuItems['chef'].forEach(el => el.classList.remove('d-none'));
    } else if (role === 'CASHIER') {
        menuItems['cashier'].forEach(el => el.classList.remove('d-none'));
    } else if (role === 'WAITSTAFF') {
        menuItems['waitstaff'].forEach(el => el.classList.remove('d-none'));
    } else if (role === 'CHEF') {
        menuItems['chef'].forEach(el => el.classList.remove('d-none'));
    } else if (role === 'CUSTOMER') {
        menuItems['customer'].forEach(el => el.classList.remove('d-none'));
    }
}
