// Common Javascript for Restaurant Management System (RMS)

const API_BASE = '/api/auth';

// Cookie Helpers
function setCookie(name, value, seconds) {
    let expires = "";
    if (seconds) {
        let date = new Date();
        date.setTime(date.getTime() + (seconds * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/; SameSite=Strict";
}

function getCookie(name) {
    let nameEQ = name + "=";
    let ca = document.cookie.split(';');
    for(let i=0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

function deleteCookie(name) {
    document.cookie = name +'=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}

// Token Storage
function saveTokens(accessToken, refreshToken, expiresIn, role, username) {
    setCookie('accessToken', accessToken, expiresIn);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('role', role);
    localStorage.setItem('username', username);
}

function clearTokens() {
    deleteCookie('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
}

// Toast Notifications
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `custom-toast ${type}`;
    
    let iconClass = 'bi-check-circle-fill';
    if (type === 'error') iconClass = 'bi-exclamation-circle-fill';
    if (type === 'warning') iconClass = 'bi-exclamation-triangle-fill';

    toast.innerHTML = `
        <i class="bi ${iconClass}"></i>
        <div>${message}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 4000);
}

// Loading Overlay
function showLoading(show) {
    let overlay = document.getElementById('loading-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'loading-overlay';
        overlay.className = 'loading-overlay';
        overlay.innerHTML = `
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
        `;
        document.body.appendChild(overlay);
    }
    if (show) {
        overlay.classList.add('show');
    } else {
        overlay.classList.remove('show');
    }
}

// Custom Fetch with Authorization headers & Auto Refresh Token support
async function customFetch(url, options = {}) {
    options.headers = options.headers || {};
    
    let token = getCookie('accessToken');
    
    // Auto-refresh token if accessToken is missing but refreshToken exists
    if (!token && localStorage.getItem('refreshToken')) {
        const refreshed = await performTokenRefresh();
        if (refreshed) {
            token = getCookie('accessToken');
        }
    }

    if (token) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }
    
    if (options.body && options.body instanceof FormData) {
        // Let browser set the Content-Type with boundary automatically
    } else {
        options.headers['Content-Type'] = options.headers['Content-Type'] || 'application/json';
    }

    try {
        let response = await fetch(url, options);
        
        // Handle 401 Unauthorized
        if (response.status === 401) {
            // Attempt token refresh and try request again
            const refreshed = await performTokenRefresh();
            if (refreshed) {
                token = getCookie('accessToken');
                options.headers['Authorization'] = `Bearer ${token}`;
                response = await fetch(url, options);
            } else {
                // If refresh fails, log out and redirect
                clearTokens();
                if (window.location.pathname !== '/login') {
                    window.location.href = '/login';
                }
            }
        }
        
        return response;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    }
}

async function performTokenRefresh() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) return false;

    try {
        const response = await fetch(`${API_BASE}/refresh`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshToken })
        });

        const result = await response.json();
        if (result.success && result.data) {
            saveTokens(
                result.data.accessToken,
                result.data.refreshToken,
                result.data.expiresIn,
                result.data.role,
                result.data.username
            );
            return true;
        }
    } catch (e) {
        console.error('Refresh token failed:', e);
    }
    
    clearTokens();
    return false;
}
