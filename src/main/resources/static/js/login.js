// Unified Login flow JavaScript (Password & OTP)

let otpTimerInterval;
const OTP_COOLDOWN = 300; // 5 minutes in seconds

document.addEventListener('DOMContentLoaded', () => {
    // 1. Password visibility toggle
    const togglePasswordBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');

    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', () => {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            togglePasswordBtn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    // 2. Tab switcher between login modes (Tài khoản vs. OTP)
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
            // Reset fields
            clearValidationErrors(formPass);
        });

        tabOtp.addEventListener('click', () => {
            tabOtp.classList.add('active');
            tabPass.classList.remove('active');
            formOtp.classList.remove('d-none');
            formPass.classList.add('d-none');
            // Reset fields
            clearValidationErrors(formOtp);
        });
    }

    // 3. Login Username/Password Submit Form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            // Clear previous validation messages
            clearValidationErrors(loginForm);

            const username = document.getElementById('username').value.trim();
            const password = passwordInput ? passwordInput.value : '';

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

                    const redirectUrl = result.data.role === 'CUSTOMER' ? '/' : '/dashboard';
                    setTimeout(() => {
                        window.location.href = redirectUrl;
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

    // 4. Request OTP
    const requestOtpForm = document.getElementById('requestOtpForm');
    const verifyOtpForm = document.getElementById('verifyOtpForm');
    const requestOtpSection = document.getElementById('request-otp-section');
    const verifyOtpSection = document.getElementById('verify-otp-section');

    const otpTargetText = document.getElementById('otp-target-text');
    const resendOtpBtn = document.getElementById('resendOtpBtn');

    if (requestOtpForm) {
        requestOtpForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearValidationErrors(requestOtpForm);

            const identifier = document.getElementById('otpIdentifier').value.trim();

            if (!identifier) {
                showFieldError('otpIdentifier', 'Thông tin đăng nhập không được để trống');
                return;
            }

            showLoading(true);

            try {
                const response = await fetch('/api/auth/otp/request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ identifier })
                });

                const result = await response.json();
                showLoading(false);

                if (response.ok && result.success) {
                    showToast(result.message, 'success');
                    
                    // Show verify screen
                    if (otpTargetText) {
                        otpTargetText.textContent = result.data.maskedTarget || identifier;
                    }
                    if (requestOtpSection && verifyOtpSection) {
                        requestOtpSection.classList.add('d-none');
                        verifyOtpSection.classList.remove('d-none');
                    }
                    
                    // Start countdown
                    startOtpTimer(result.data.expiresInSeconds || OTP_COOLDOWN);
                } else {
                    showToast(result.message || 'Không thể gửi mã OTP. Vui lòng thử lại.', 'error');
                }
            } catch (err) {
                showLoading(false);
                showToast('Không thể kết nối đến máy chủ.', 'error');
            }
        });
    }

    // 5. Verify OTP and login
    if (verifyOtpForm) {
        verifyOtpForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearValidationErrors(verifyOtpForm);

            const identifier = document.getElementById('otpIdentifier').value.trim();
            const otpCode = document.getElementById('otpCode').value.trim();

            let isValid = true;
            if (!otpCode) {
                showFieldError('otpCode', 'Mã OTP không được để trống');
                isValid = false;
            } else if (!/^\d{6}$/.test(otpCode)) {
                showFieldError('otpCode', 'Mã OTP phải gồm đúng 6 chữ số');
                isValid = false;
            }

            if (!isValid) return;

            showLoading(true);

            try {
                const response = await fetch('/api/auth/otp/verify', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ identifier, otpCode })
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

                    const redirectUrl = result.data.role === 'CUSTOMER' ? '/' : '/dashboard';
                    setTimeout(() => {
                        window.location.href = redirectUrl;
                    }, 800);
                } else {
                    showToast(result.message || 'Mã OTP không hợp lệ hoặc đã hết hạn.', 'error');
                }
            } catch (err) {
                showLoading(false);
                showToast('Không thể kết nối đến máy chủ.', 'error');
            }
        });
    }

    // 6. Resend OTP
    if (resendOtpBtn) {
        resendOtpBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            if (resendOtpBtn.disabled) return;

            const identifier = document.getElementById('otpIdentifier').value.trim();
            showLoading(true);

            try {
                const response = await fetch('/api/auth/otp/request', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ identifier })
                });

                const result = await response.json();
                showLoading(false);

                if (response.ok && result.success) {
                    showToast('Mã OTP mới đã được gửi.', 'success');
                    startOtpTimer(result.data.expiresInSeconds || OTP_COOLDOWN);
                } else {
                    showToast(result.message || 'Không thể gửi lại mã OTP.', 'error');
                }
            } catch (err) {
                showLoading(false);
                showToast('Không thể kết nối đến máy chủ.', 'error');
            }
        });
    }
});

// Countdown Timer logic
function startOtpTimer(durationSeconds) {
    const display = document.getElementById('otp-timer');
    const resendBtn = document.getElementById('resendOtpBtn');
    
    if (!display || !resendBtn) return;
    
    clearInterval(otpTimerInterval);
    resendBtn.disabled = true;
    resendBtn.classList.add('text-muted');

    let timer = durationSeconds;
    
    const updateDisplay = () => {
        const minutes = Math.floor(timer / 60);
        const seconds = timer % 60;
        display.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        if (timer <= 0) {
            clearInterval(otpTimerInterval);
            display.textContent = "00:00";
            resendBtn.disabled = false;
            resendBtn.classList.remove('text-muted');
            showToast('Mã OTP của bạn đã hết hạn. Hãy nhấp gửi lại mã OTP.', 'warning');
        }
        timer--;
    };

    updateDisplay();
    otpTimerInterval = setInterval(updateDisplay, 1000);
}

// Helper validation functions
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.add('is-invalid');
        
        // Highlight parent input-group if it exists
        const inputGroup = field.closest('.input-group');
        if (inputGroup) {
            inputGroup.classList.add('is-invalid');
        }
        
        // Find closest form-row or mb-3 container to locate the invalid feedback
        const parent = field.closest('.mb-3');
        if (parent) {
            const feedback = parent.querySelector('.invalid-feedback');
            if (feedback) {
                feedback.textContent = message;
                feedback.style.display = 'block';
            }
        }
    }
}

function clearValidationErrors(form) {
    form.querySelectorAll('.form-control-glass').forEach(input => {
        input.classList.remove('is-invalid');
    });
    form.querySelectorAll('.input-group').forEach(group => {
        group.classList.remove('is-invalid');
    });
    form.querySelectorAll('.invalid-feedback').forEach(feedback => {
        feedback.textContent = '';
        feedback.style.display = 'none';
    });
}
