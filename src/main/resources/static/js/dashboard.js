// Rich Dashboard JavaScript with live backend API integrations

let revenueChartInstance = null;
let orderStatusChartInstance = null;
let categorySalesChartInstance = null;
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

    // Load Dashboard Metrics (KPIs) & Real-time Table Grid
    await loadDashboardMetrics();

    // Load Statistics and Draw Initial Charts (Revenue, Order Status, Category Sales)
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

                document.getElementById('kpi-serving-tables').textContent = `${data.servingTablesCount || 0} / ${data.totalTables || 0}`;
                document.getElementById('kpi-empty-tables').textContent = `${data.emptyTablesCount || 0} / ${data.totalTables || 0}`;
                document.getElementById('kpi-kitchen-pending').textContent = `${data.kitchenPendingItemsCount || 0} món`;

                const utilRatio = (data.totalTables || 0) > 0 ? Math.round(((data.servingTablesCount || 0) / data.totalTables) * 100) : 0;
                const utilRatioEl = document.getElementById('table-utilization-ratio');
                if (utilRatioEl) {
                    utilRatioEl.textContent = utilRatio + '%';
                }

                const todayOrderCountEl = document.getElementById('kpi-today-order-count');
                if (todayOrderCountEl) {
                    todayOrderCountEl.innerHTML = `<i class="bi bi-cart"></i> ${data.todayOrderCount || 0} đơn mới`;
                }

                // Render visual Table Status Grid Map
                renderDashboardTableGrid(data.tables || []);
            }
        }
    } catch (e) {
        console.error('Failed to load metrics:', e);
    }
}


function renderDashboardTableGrid(tables) {
    const tableGridEl = document.getElementById('dashboard-table-grid');
    if (!tableGridEl) return;

    if (!tables || tables.length === 0) {
        tableGridEl.innerHTML = `<div class="col-12 text-center text-muted py-3">Chưa có thông tin bàn ăn trong cơ sở dữ liệu</div>`;
        return;
    }

    const statusMap = {
        'EMPTY': { class: 'status-empty', label: 'Trống', dotBg: '#10b981' },
        'RESERVED': { class: 'status-reserved', label: 'Đã đặt', dotBg: '#f59e0b' },
        'OCCUPIED': { class: 'status-serving', label: 'Đang dùng', dotBg: '#ef4444' }
    };

    tableGridEl.innerHTML = tables.map(table => {
        const conf = statusMap[table.status] || statusMap['EMPTY'];
        const locationStr = table.location ? `<div class="text-muted" style="font-size: 10px;">${table.location}</div>` : '';
        return `
            <div class="table-status-card ${conf.class}">
                <span class="table-badge-dot" style="background-color: ${conf.dotBg};"></span>
                <div class="table-num">${table.tableNumber}</div>
                <div class="table-capacity">${table.capacity} Chỗ</div>
                ${locationStr}
                <div class="table-time font-weight-bold" style="margin-top: 4px;">${conf.label}</div>
            </div>
        `;
    }).join('');
}

async function loadAndRenderCharts() {
    try {
        const response = await customFetch('/api/analytics/statistics');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                statisticsDataGlobal = result.data;
                renderChart('day', statisticsDataGlobal);
                renderOrderStatusChart(statisticsDataGlobal.orderStatusDistribution || []);
                renderCategorySalesChart(statisticsDataGlobal.categorySales || []);
                renderRecentActivities(statisticsDataGlobal.recentActivities || []);
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

function renderOrderStatusChart(dataList) {
    const ctx = document.getElementById('orderStatusChart');
    if (!ctx) return;

    if (orderStatusChartInstance) {
        orderStatusChartInstance.destroy();
    }

    const labels = dataList.map(item => item.label);
    const counts = dataList.map(item => item.count);

    orderStatusChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: counts,
                backgroundColor: [
                    'rgba(59, 130, 246, 0.85)',  // OPEN (blue)
                    'rgba(16, 185, 129, 0.85)',  // COMPLETED (green)
                    'rgba(239, 68, 68, 0.85)'    // CANCELLED (red)
                ],
                borderColor: 'rgba(255, 255, 255, 0.1)',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#94a3b8',
                        font: { family: 'Inter', size: 11 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.label}: ${context.parsed} đơn`;
                        }
                    }
                }
            }
        }
    });
}

function renderCategorySalesChart(categorySales) {
    const ctx = document.getElementById('categorySalesChart');
    if (!ctx) return;

    if (categorySalesChartInstance) {
        categorySalesChartInstance.destroy();
    }

    const labels = categorySales.map(item => item.categoryName);
    const revenues = categorySales.map(item => item.revenue);

    const colors = [
        'rgba(230, 126, 34, 0.85)',
        'rgba(139, 30, 63, 0.85)',
        'rgba(16, 185, 129, 0.85)',
        'rgba(59, 130, 246, 0.85)',
        'rgba(245, 158, 11, 0.85)',
        'rgba(168, 85, 247, 0.85)'
    ];

    categorySalesChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels.length > 0 ? labels : ['Chưa có dữ liệu'],
            datasets: [{
                data: revenues.length > 0 ? revenues : [0],
                backgroundColor: colors.slice(0, Math.max(labels.length, 1)),
                borderColor: 'rgba(255, 255, 255, 0.1)',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#94a3b8',
                        font: { family: 'Inter', size: 11 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.label}: ${(context.parsed || 0).toLocaleString('vi-VN')}đ`;
                        }
                    }
                }
            }
        }
    });
}

function renderRecentActivities(activities) {
    const listEl = document.getElementById('recent-activities-list');
    if (!listEl) return;

    if (!activities || activities.length === 0) {
        listEl.innerHTML = `<div class="text-center text-muted py-3">Chưa có hoạt động nào được ghi nhận</div>`;
        return;
    }

    const typeClassMap = {
        'SUCCESS': 'notif-icon-success',
        'INFO': 'notif-icon-info',
        'WARNING': 'notif-icon-warning',
        'DANGER': 'notif-icon-danger'
    };

    listEl.innerHTML = activities.map(act => {
        const iconClass = typeClassMap[act.type] || 'notif-icon-info';
        return `
            <div class="notif-item">
                <div class="notif-icon-wrapper ${iconClass}">
                    <i class="bi ${act.icon || 'bi-bell'}"></i>
                </div>
                <div class="notif-content">
                    <p class="notif-text"><strong>${act.title}</strong></p>
                    <span class="notif-time">${act.description} • ${act.timeStr}</span>
                </div>
            </div>
        `;
    }).join('');
}

async function loadReportSummary() {
    try {
        const response = await customFetch('/api/reports/summary');
        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                const data = result.data;

                // 1. Populate Report KPI Cards (for report/list.html)
                const monthRevEl = document.getElementById('report-month-revenue');
                if (monthRevEl) {
                    monthRevEl.textContent = (data.thisMonthRevenue || 0).toLocaleString('vi-VN') + 'đ';
                }

                const monthRevGrowthEl = document.getElementById('report-month-revenue-growth');
                if (monthRevGrowthEl) {
                    const growth = data.revenueGrowthPercent || 0;
                    if (growth >= 0) {
                        monthRevGrowthEl.innerHTML = `<i class="bi bi-arrow-up-right text-success me-1"></i>+${growth}% so với tháng trước`;
                    } else {
                        monthRevGrowthEl.innerHTML = `<i class="bi bi-arrow-down-right text-danger me-1"></i>${growth}% so với tháng trước`;
                    }
                }

                const monthOrdersEl = document.getElementById('report-month-orders');
                if (monthOrdersEl) {
                    monthOrdersEl.textContent = (data.thisMonthOrderCount || 0) + ' đơn';
                }

                const monthOrdersGrowthEl = document.getElementById('report-month-orders-growth');
                if (monthOrdersGrowthEl) {
                    const growth = data.orderGrowthPercent || 0;
                    if (growth >= 0) {
                        monthOrdersGrowthEl.innerHTML = `<i class="bi bi-arrow-up-right text-success me-1"></i>+${growth}% so với tháng trước`;
                    } else {
                        monthOrdersGrowthEl.innerHTML = `<i class="bi bi-arrow-down-right text-danger me-1"></i>${growth}% so với tháng trước`;
                    }
                }

                const memberRatioEl = document.getElementById('member-customer-ratio');
                if (memberRatioEl) {
                    memberRatioEl.textContent = (data.memberCustomerRatio || 0) + '%';
                }

                // 2. Populate Best Sellers Table
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

                // 3. Populate Loyal Customers Table
                const loyalCustomersTbody = document.getElementById('loyal-customers-tbody');
                if (loyalCustomersTbody) {
                    const customers = data.loyalCustomers || [];
                    if (customers.length === 0) {
                        loyalCustomersTbody.innerHTML = `<tr><td colspan="3" class="text-center text-secondary py-3">Chưa có khách hàng giao dịch</td></tr>`;
                    } else {
                        loyalCustomersTbody.innerHTML = customers.map(cust => `
                            <tr>
                                <td><strong>${cust.fullName}</strong></td>
                                <td><small class="text-muted">${cust.phone || 'Chưa có SĐT'}</small></td>
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


