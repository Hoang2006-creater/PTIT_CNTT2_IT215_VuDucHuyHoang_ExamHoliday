// Registration flow JavaScript

document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('register-form');
    const passwordInput = document.getElementById('reg-password');
    const confirmPasswordInput = document.getElementById('reg-confirm-password');

    // 1. Password Visibility Toggle
    const togglePasswordBtn = document.getElementById('toggleRegPassword');
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', () => {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            togglePasswordBtn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    const toggleConfirmPasswordBtn = document.getElementById('toggleConfirmPassword');
    if (toggleConfirmPasswordBtn && confirmPasswordInput) {
        toggleConfirmPasswordBtn.addEventListener('click', () => {
            const isPassword = confirmPasswordInput.getAttribute('type') === 'password';
            confirmPasswordInput.setAttribute('type', isPassword ? 'text' : 'password');
            toggleConfirmPasswordBtn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    // 2. Real-time Password Strength Check
    if (passwordInput) {
        passwordInput.addEventListener('input', () => {
            const val = passwordInput.value;
            const strength = checkPasswordStrength(val);
            updateStrengthMeter(strength);
            updateRequirementsChecklist(val);
        });
    }

    // 3. Form Submission
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            clearValidationErrors(registerForm);

            const username = document.getElementById('reg-username').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const phone = document.getElementById('reg-phone').value.trim();
            const password = passwordInput ? passwordInput.value : '';
            const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : '';
            
            // Client-side validations
            let isValid = true;

            // Username validation
            if (!username) {
                showFieldError('reg-username', 'Tên đăng nhập không được để trống');
                isValid = false;
            } else if (username.length < 3 || username.length > 50) {
                showFieldError('reg-username', 'Tên đăng nhập phải từ 3 đến 50 ký tự');
                isValid = false;
            }

            // Email validation
            const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!email) {
                showFieldError('reg-email', 'Địa chỉ email không được để trống');
                isValid = false;
            } else if (!emailPattern.test(email)) {
                showFieldError('reg-email', 'Địa chỉ email không đúng định dạng');
                isValid = false;
            }

            // Phone validation
            const phonePattern = /^(0[3|5|7|8|9])+([0-9]{8})$/;
            if (!phone) {
                showFieldError('reg-phone', 'Số điện thoại không được để trống');
                isValid = false;
            } else if (!phonePattern.test(phone)) {
                showFieldError('reg-phone', 'Số điện thoại phải gồm 10 chữ số (bắt đầu bằng 03, 05, 07, 08, 09)');
                isValid = false;
            }

            // Password validation
            if (!password) {
                showFieldError('reg-password', 'Mật khẩu không được để trống');
                isValid = false;
            } else if (password.length < 8) {
                showFieldError('reg-password', 'Mật khẩu phải từ 8 ký tự trở lên');
                isValid = false;
            }

            // Confirm Password validation
            if (confirmPasswordInput) {
                if (!confirmPassword) {
                    showFieldError('reg-confirm-password', 'Vui lòng xác nhận mật khẩu');
                    isValid = false;
                } else if (password !== confirmPassword) {
                    showFieldError('reg-confirm-password', 'Mật khẩu xác nhận không trùng khớp');
                    isValid = false;
                }
            }

            if (!isValid) return;

            showLoading(true);
            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, email, phone, password })
                });
                
                showLoading(false);
                const result = await response.json();
                
                if (response.ok && result.success) {
                    showToast('Đăng ký thành công! Đang chuyển hướng sang Đăng nhập...', 'success');
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 1500);
                } else {
                    // Backend Validation / Error handling
                    if (result.data && typeof result.data === 'object') {
                        // Field validation errors
                        Object.keys(result.data).forEach(field => {
                            // Map backend fields to frontend IDs
                            const targetId = `reg-${field}`;
                            showFieldError(targetId, result.data[field]);
                        });
                    } else {
                        showToast(result.message || 'Đăng ký thất bại.', 'error');
                    }
                }
            } catch (err) {
                showLoading(false);
                console.error(err);
                showToast('Không thể kết nối đến máy chủ. Vui lòng thử lại.', 'error');
            }
        });
    }
});

// Password strength assessment algorithm
function checkPasswordStrength(password) {
    let score = 0;
    if (!password) return { score: 0, text: 'Trống', class: '' };

    // Length check
    if (password.length >= 8) score++;
    
    // Lower and uppercase mix
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
    
    // Numbers check
    if (/\d/.test(password)) score++;
    
    // Special chars check
    if (/[@$!%*?&]/.test(password)) score++;

    // Translate score to strength properties
    if (score <= 1) {
        return { score: 1, text: 'Yếu', class: 'strength-weak' };
    } else if (score === 2 || score === 3) {
        return { score: 2, text: 'Trung bình', class: 'strength-medium' };
    } else {
        return { score: 3, text: 'Mạnh', class: 'strength-strong' };
    }
}

// Update UI strength segments and indicator text
function updateStrengthMeter(strength) {
    const textLabel = document.getElementById('strength-text');
    const segments = document.querySelectorAll('.strength-segment');
    
    if (!textLabel || segments.length === 0) return;

    // Reset all segments
    segments.forEach(seg => {
        seg.className = 'strength-segment';
    });

    if (strength.score === 0) {
        textLabel.textContent = 'Trống';
        textLabel.style.color = 'var(--text-muted)';
        return;
    }

    textLabel.textContent = strength.text;
    
    if (strength.class === 'strength-weak') {
        textLabel.style.color = 'var(--error-color)';
        segments[0].classList.add('strength-weak');
    } else if (strength.class === 'strength-medium') {
        textLabel.style.color = 'var(--warning-color)';
        segments[0].classList.add('strength-medium');
        segments[1].classList.add('strength-medium');
    } else if (strength.class === 'strength-strong') {
        textLabel.style.color = 'var(--success-color)';
        segments[0].classList.add('strength-strong');
        segments[1].classList.add('strength-strong');
        segments[2].classList.add('strength-strong');
    }
}

// Field validation styling helpers
function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.add('is-invalid');
        
        // Highlight parent input-group if it exists
        const inputGroup = field.closest('.input-group');
        if (inputGroup) {
            inputGroup.classList.add('is-invalid');
        }
        
        // Find closest mb-3 container to locate the invalid feedback
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
    // Remove individual feedback containers or hide them
    form.querySelectorAll('.invalid-feedback').forEach(feedback => {
        feedback.textContent = '';
        feedback.style.display = 'none';
    });
}

function updateRequirementsChecklist(password) {
    const reqLength = document.getElementById('req-length');
    const reqCase = document.getElementById('req-case');
    const reqSpecial = document.getElementById('req-special');

    if (!reqLength || !reqCase || !reqSpecial) return;

    if (!password) {
        // Reset to initial neutral state
        resetReq(reqLength, 'Tối thiểu 8 ký tự');
        resetReq(reqCase, 'Chữ hoa & chữ thường');
        resetReq(reqSpecial, 'Số hoặc ký tự đặc biệt');
        return;
    }

    // 1. Length check
    if (password.length >= 8) {
        setValid(reqLength, 'Tối thiểu 8 ký tự');
    } else {
        setInvalid(reqLength, 'Tối thiểu 8 ký tự');
    }

    // 2. Case check
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) {
        setValid(reqCase, 'Chữ hoa & chữ thường');
    } else {
        setInvalid(reqCase, 'Chữ hoa & chữ thường');
    }

    // 3. Special/number check
    if (/\d/.test(password) || /[@$!%*?&]/.test(password)) {
        setValid(reqSpecial, 'Số hoặc ký tự đặc biệt');
    } else {
        setInvalid(reqSpecial, 'Số hoặc ký tự đặc biệt');
    }
}

function setValid(el, text) {
    el.className = 'validation-item valid';
    el.innerHTML = `<i class="bi bi-check-circle-fill"></i> ${text}`;
}

function setInvalid(el, text) {
    el.className = 'validation-item invalid';
    el.innerHTML = `<i class="bi bi-x-circle-fill"></i> ${text}`;
}

function resetReq(el, text) {
    el.className = 'validation-item';
    el.innerHTML = `<i class="bi bi-circle"></i> ${text}`;
}

