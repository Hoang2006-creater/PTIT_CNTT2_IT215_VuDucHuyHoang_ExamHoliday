// Rich Dashboard JavaScript with live backend API integrations

let revenueChartInstance = null;
let statisticsDataGlobal = null;

document.addEventListener('DOMContentLoaded', async () => {
    // Mobile sidebar toggle
    const toggleSidebarBtn = document.getElementById('toggleSidebar');
    const sidebar = document.getElementById('sidebar');
    
    if (toggleSidebarBtn && sidebar) {
        toggleSidebarBtn.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }

    // Set today's date string
    setTodayDateString();

    // Load user profile details dynamically
    await loadUserProfile();

    // Load Dashboard Metrics (KPIs)
    await loadDashboardMetrics();

    // Load Statistics and Draw Initial Chart
    await loadAndRenderCharts();

    // Load Statistics Report Summary (Best sellers, Loyal customers)
    await loadReportSummary();

    // Setup chart filter change listener
    const filterSelect = document.getElementById('revenue-chart-filter');
    if (filterSelect) {
        filterSelect.addEventListener('change', (e) => {
            if (statisticsDataGlobal) {
                renderChart(e.target.value, statisticsDataGlobal);
            }
        });
    }

    // Setup logout handler
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            showLoading(true);

            try {
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

            clearTokens();
            setTimeout(() => {
                window.location.href = '/login';
            }, 500);
        });
    }
});

function setTodayDateString() {
    const todayDateStrEl = document.getElementById('today-date-str');
    if (todayDateStrEl) {
        const now = new Date();
        const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
        const dayName = days[now.getDay()];
        const dateStr = `${dayName}, ${String(now.getDate()).padStart(2, '0')}/${String(now.getMonth() + 1).padStart(2, '0')}/${now.getFullYear()}`;
        todayDateStrEl.textContent = dateStr;
    }
}

async function loadUserProfile() {
    const profileNameEl = document.getElementById('profile-name');
    const profileEmailEl = document.getElementById('profile-email');
    const roleBadgeEl = document.getElementById('role-badge');
    const welcomeUserEl = document.getElementById('welcome-user');

    if (!profileNameEl) return;

    try {
        const response = await customFetch('/api/auth/profile');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                const user = result.data;
                profileNameEl.textContent = user.username;
                profileEmailEl.textContent = user.email || 'Chưa cập nhật email';
                if (welcomeUserEl) welcomeUserEl.textContent = user.username;

                const roleMap = {
                    'ADMIN': 'ADMIN',
                    'MANAGER': 'QUẢN LÝ',
                    'WAITSTAFF': 'PHỤC VỤ',
                    'CASHIER': 'THU NGÂN',
                    'CHEF': 'ĐẦU BẾP',
                    'CUSTOMER': 'KHÁCH HÀNG'
                };

                if (roleBadgeEl) {
                    roleBadgeEl.textContent = roleMap[user.role] || user.role;
                }

                // Show/hide menu items based on role
                renderRoleMenus(user.role);
            }
        } else {
            clearTokens();
            window.location.href = '/login';
        }
    } catch (e) {
        console.error('Failed to load profile:', e);
        showToast('Không thể kết nối đến máy chủ để tải thông tin hồ sơ.', 'error');
    }
}

function renderRoleMenus(role) {
    const menuItems = {
        'admin': document.querySelectorAll('[data-role-required="ADMIN"]'),
        'manager': document.querySelectorAll('[data-role-required="MANAGER"]'),
        'cashier': document.querySelectorAll('[data-role-required="CASHIER"]'),
        'waitstaff': document.querySelectorAll('[data-role-required="WAITSTAFF"]'),
        'chef': document.querySelectorAll('[data-role-required="CHEF"]'),
        'customer': document.querySelectorAll('[data-role-required="CUSTOMER"]')
    };

    Object.values(menuItems).forEach(elements => {
        elements.forEach(el => el.classList.add('d-none'));
    });

    if (role === 'ADMIN') {
        Object.values(menuItems).forEach(elements => {
            elements.forEach(el => el.classList.remove('d-none'));
        });
    } else if (role === 'MANAGER') {
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

async function loadDashboardMetrics() {
    try {
        const response = await customFetch('/api/analytics/metrics');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                const data = result.data;
                
                // Format values
                const formattedTotalRevenue = (data.totalRevenue || 0).toLocaleString('vi-VN') + 'đ';
                const formattedTodayRevenue = (data.todayRevenue || 0).toLocaleString('vi-VN') + 'đ';

                // Set elements
                document.getElementById('kpi-total-revenue').textContent = formattedTotalRevenue;
                document.getElementById('kpi-total-orders').textContent = data.totalOrders || 0;
                document.getElementById('kpi-total-customers').textContent = data.totalCustomers || 0;
                document.getElementById('kpi-total-employees').textContent = data.totalEmployees || 0;
                document.getElementById('kpi-today-revenue').textContent = formattedTodayRevenue;
                
                const todayOrderCountEl = document.getElementById('kpi-today-order-count');
                if (todayOrderCountEl) {
                    todayOrderCountEl.innerHTML = `<i class="bi bi-cart"></i> ${data.todayOrderCount || 0} đơn mới`;
                }
            }
        }
    } catch (e) {
        console.error('Failed to load metrics:', e);
    }
}

async function loadAndRenderCharts() {
    try {
        const response = await customFetch('/api/analytics/statistics');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                statisticsDataGlobal = result.data;
                renderChart('day', statisticsDataGlobal);
            }
        }
    } catch (e) {
        console.error('Failed to load charts statistics:', e);
    }
}

function renderChart(type, stats) {
    let dataPoints = [];
    if (type === 'day') {
        dataPoints = stats.revenueByDay || [];
    } else if (type === 'month') {
        dataPoints = stats.revenueByMonth || [];
    } else if (type === 'year') {
        dataPoints = stats.revenueByYear || [];
    }

    const labels = dataPoints.map(dp => dp.label);
    const data = dataPoints.map(dp => dp.revenue);

    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;

    if (revenueChartInstance) {
        revenueChartInstance.destroy();
    }

    revenueChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Doanh thu',
                data: data,
                borderColor: '#E67E22',
                backgroundColor: 'rgba(230, 126, 34, 0.15)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#8B1E3F',
                pointBorderColor: '#white',
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `Doanh thu: ${context.parsed.y.toLocaleString('vi-VN')}đ`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)'
                    },
                    ticks: {
                        color: '#94a3b8',
                        font: {
                            family: 'Inter'
                        }
                    }
                },
                y: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)'
                    },
                    ticks: {
                        color: '#94a3b8',
                        font: {
                            family: 'Inter'
                        },
                        callback: function(value) {
                            return value.toLocaleString('vi-VN') + 'đ';
                        }
                    }
                }
            }
        }
    });
}

async function loadReportSummary() {
    try {
        const response = await customFetch('/api/reports/summary');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                const data = result.data;

                // 1. Populate Best Sellers Table
                const bestSellersTbody = document.getElementById('best-selling-items-tbody');
                if (bestSellersTbody) {
                    const items = data.bestSellingItems || [];
                    if (items.length === 0) {
                        bestSellersTbody.innerHTML = `<tr><td colspan="3" class="text-center text-secondary py-3">Chưa có giao dịch món ăn nào</td></tr>`;
                    } else {
                        bestSellersTbody.innerHTML = items.map(item => `
                            <tr>
                                <td><i class="bi bi-egg-fill text-warning me-2"></i><strong>${item.name}</strong></td>
                                <td>${item.price.toLocaleString('vi-VN')}đ</td>
                                <td class="text-end text-success"><strong>${item.totalSold} suất</strong></td>
                            </tr>
                        `).join('');
                    }
                }

                // 2. Populate Loyal Customers Table
                const loyalCustomersTbody = document.getElementById('loyal-customers-tbody');
                if (loyalCustomersTbody) {
                    const customers = data.loyalCustomers || [];
                    if (customers.length === 0) {
                        loyalCustomersTbody.innerHTML = `<tr><td colspan="2" class="text-center text-secondary py-3">Chưa có khách hàng giao dịch</td></tr>`;
                    } else {
                        loyalCustomersTbody.innerHTML = customers.map(cust => `
                            <tr>
                                <td><strong>${cust.fullName}</strong><br><small class="text-muted">${cust.phone || ''}</small></td>
                                <td class="text-end text-warning"><strong>${cust.totalSpent.toLocaleString('vi-VN')}đ</strong></td>
                            </tr>
                        `).join('');
                    }
                }
            }
        }
    } catch (e) {
        console.error('Failed to load report summary:', e);
    }
}
