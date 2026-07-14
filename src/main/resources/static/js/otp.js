// OTP Flow JavaScript

let otpTimerInterval;
const OTP_COOLDOWN = 300; // 5 minutes in seconds

document.addEventListener('DOMContentLoaded', () => {
    const requestOtpForm = document.getElementById('requestOtpForm');
    const verifyOtpForm = document.getElementById('verifyOtpForm');
    const requestOtpSection = document.getElementById('request-otp-section');
    const verifyOtpSection = document.getElementById('verify-otp-section');

    const otpTargetText = document.getElementById('otp-target-text');
    const otpTimerDisplay = document.getElementById('otp-timer');
    const resendOtpBtn = document.getElementById('resendOtpBtn');

    // 1. Request OTP
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
                    otpTargetText.textContent = result.data.maskedTarget || identifier;
                    requestOtpSection.classList.add('d-none');
                    verifyOtpSection.classList.remove('d-none');
                    
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

    // 2. Verify OTP and login
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

                    setTimeout(() => {
                        window.location.href = '/dashboard';
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

    // 3. Resend OTP
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
