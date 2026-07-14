// Authentication JavaScript for Username/Password flow

document.addEventListener('DOMContentLoaded', () => {
    // Password visibility toggle
    const togglePasswordBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', () => {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            togglePasswordBtn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    // Tab switcher between login modes
    const tabPass = document.getElementById('tab-password');
    const tabOtp = document.getElementById('tab-otp');
    const formPass = document.getElementById('login-pass-form');
    const formOtp = document.getElementById('login-otp-form');

    if (tabPass && tabOtp && formPass && formOtp) {
        tabPass.addEventListener('click', () => {
            tabPass.classList.add('active');
            tabOtp.classList.remove('active');
            formPass.classList.remove('d-none');
            formOtp.classList.add('d-none');
        });

        tabOtp.addEventListener('click', () => {
            tabOtp.classList.add('active');
            tabPass.classList.remove('active');
            formOtp.classList.remove('d-none');
            formPass.classList.add('d-none');
        });
    }

    // Login Username/Password Submit Form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            // Clear previous validation messages
            clearValidationErrors(loginForm);

            const username = document.getElementById('username').value.trim();
            const password = passwordInput.value;

            // Client-side Validation
            let isValid = true;
            if (!username) {
                showFieldError('username', 'Tên đăng nhập không được để trống');
                isValid = false;
            } else if (username.length < 3 || username.length > 50) {
                showFieldError('username', 'Tên đăng nhập phải từ 3 đến 50 ký tự');
                isValid = false;
            }

            if (!password) {
                showFieldError('password', 'Mật khẩu không được để trống');
                isValid = false;
            } else if (password.length < 8 || password.length > 128) {
                showFieldError('password', 'Mật khẩu phải từ 8 đến 128 ký tự');
                isValid = false;
            }

            if (!isValid) return;

            showLoading(true);

            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                const result = await response.json();
                showLoading(false);

                if (response.ok && result.success) {
                    showToast('Đăng nhập thành công!', 'success');
                    saveTokens(
                        result.data.accessToken,
                        result.data.refreshToken,
                        result.data.expiresIn,
                        result.data.role,
                        result.data.username
                    );

                    // Redirect to dashboard after a short delay
                    setTimeout(() => {
                        window.location.href = '/dashboard';
                    }, 800);
                } else {
                    // Backend Validation / Authentication Error handling
                    if (result.data && typeof result.data === 'object') {
                        // Field validation errors
                        Object.keys(result.data).forEach(field => {
                            showFieldError(field, result.data[field]);
                        });
                    } else {
                        // General authentication error
                        showToast(result.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại.', 'error');
                    }
                }
            } catch (err) {
                showLoading(false);
                showToast('Không thể kết nối đến máy chủ. Vui lòng thử lại sau.', 'error');
            }
        });
    }
});

// Helper validation functions
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.add('is-invalid');
        const feedback = field.closest('.mb-3').querySelector('.invalid-feedback');
        if (feedback) {
            feedback.textContent = message;
            feedback.style.display = 'block';
        }
    }
}

function clearValidationErrors(form) {
    form.querySelectorAll('.form-control-glass').forEach(input => {
        input.classList.remove('is-invalid');
    });
    form.querySelectorAll('.invalid-feedback').forEach(feedback => {
        feedback.textContent = '';
        feedback.style.display = 'none';
    });
}
