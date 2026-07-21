/**
 * Unified Form Validator for Restaurant Management System (RMS)
 * Provides real-time field validation, custom visual feedback (red/green borders & icons),
 * blocks invalid form submission, and maps backend Spring Boot Bean Validation (ApiResponse) errors directly to inputs.
 */

class FormValidator {
    constructor(form, options = {}) {
        if (typeof form === 'string') {
            this.form = document.querySelector(form);
        } else {
            this.form = form;
        }

        if (!this.form) return;

        this.options = Object.assign({
            validateOnBlur: true,
            validateOnInput: true,
            validateOnChange: true,
            autoTrim: true,
            showValidState: true
        }, options);

        // Ensure browser native HTML validation is completely disabled
        this.form.setAttribute('novalidate', 'true');

        this.init();
    }

    /**
     * Standard Validation RegEx & Rules definitions
     */
    static RULES = {
        email: {
            regex: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
            message: 'Email không đúng định dạng'
        },
        phone: {
            regex: /^(0|\+84)[3|5|7|8|9][0-9]{8}$/,
            message: 'Số điện thoại không hợp lệ (ví dụ: 0912345678)'
        },
        username: {
            regex: /^[a-zA-Z0-9._-]{3,50}$/,
            message: 'Tên đăng nhập phải từ 3 đến 50 ký tự, không chứa ký tự đặc biệt'
        },
        otp: {
            regex: /^\d{6}$/,
            message: 'Mã OTP phải gồm đúng 6 chữ số'
        },
        price: {
            validator: (val) => !isNaN(val) && parseFloat(val) >= 0,
            message: 'Giá tiền phải là số lớn hơn hoặc bằng 0'
        },
        quantity: {
            validator: (val) => Number.isInteger(Number(val)) && parseInt(val, 10) > 0,
            message: 'Số lượng phải là số nguyên dương'
        },
        discountPercent: {
            validator: (val) => !isNaN(val) && parseFloat(val) >= 0 && parseFloat(val) <= 100,
            message: 'Phần trăm giảm giá phải từ 0 đến 100'
        },
        noSpecialChars: {
            regex: /^[a-zA-Z0-9\sÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ_-]+$/,
            message: 'Không được chứa ký tự đặc biệt'
        },
        date: {
            validator: (val) => !isNaN(Date.parse(val)),
            message: 'Ngày không hợp lệ'
        },
        time: {
            regex: /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/,
            message: 'Giờ không hợp lệ (định dạng HH:mm)'
        }
    };

    /**
     * Initialize listeners on form elements
     */
    init() {
        const fields = this.getFields();

        fields.forEach(field => {
            // Remove browser default tooltips & constraint validation attributes if present
            field.removeAttribute('required');
            field.removeAttribute('pattern');
            field.removeAttribute('minlength');
            field.removeAttribute('maxlength');
            
            // Attach event listeners for real-time validation
            if (this.options.validateOnBlur) {
                field.addEventListener('blur', () => this.validateField(field));
            }
            if (this.options.validateOnInput) {
                field.addEventListener('input', () => {
                    // If element currently has error, re-validate immediately on input
                    if (field.classList.contains('is-invalid')) {
                        this.validateField(field);
                    }
                });
            }
            if (this.options.validateOnChange) {
                field.addEventListener('change', () => this.validateField(field));
            }
        });
    }

    /**
     * Get validate-able input elements inside the form
     */
    getFields() {
        return Array.from(this.form.querySelectorAll('input, select, textarea'))
            .filter(el => el.type !== 'submit' && el.type !== 'button' && el.type !== 'reset' && el.type !== 'hidden' && !el.disabled);
    }

    /**
     * Validate a single input field
     */
    validateField(field) {
        let value = field.value;

        // Check auto trim
        if (this.options.autoTrim && typeof value === 'string' && field.type !== 'password') {
            value = value.trim();
        }

        const labelName = field.getAttribute('data-label') || field.getAttribute('placeholder') || field.name || 'Trường này';
        const rulesString = field.getAttribute('data-validate');

        // Rule 1: No all-whitespace check if not empty
        if (field.value.length > 0 && field.value.trim().length === 0) {
            this.showError(field, `${labelName} không được chứa toàn khoảng trắng`);
            return false;
        }

        // Parse dataset rules or direct HTML dataset properties
        const rules = rulesString ? rulesString.split('|') : [];

        // Check required rule
        const isRequired = rules.includes('required') || field.hasAttribute('data-rule-required');
        if (isRequired) {
            if (!value || value.length === 0) {
                this.showError(field, `${labelName} không được để trống`);
                return false;
            }
        } else if (!value || value.length === 0) {
            // If optional and empty, clear error state and exit
            this.clearFieldStatus(field);
            return true;
        }

        // Check minLength
        const minLen = field.getAttribute('data-min-length') || field.getAttribute('data-min');
        if (minLen && value.length < parseInt(minLen, 10)) {
            this.showError(field, `${labelName} phải có ít nhất ${minLen} ký tự`);
            return false;
        }

        // Check maxLength
        const maxLen = field.getAttribute('data-max-length') || field.getAttribute('data-max');
        if (maxLen && value.length > parseInt(maxLen, 10)) {
            this.showError(field, `${labelName} không được vượt quá ${maxLen} ký tự`);
            return false;
        }

        // Check Password Min Length default (8)
        if (rules.includes('password') || field.type === 'password' || field.id === 'password' || field.id === 'newPassword') {
            if (value.length < 8) {
                this.showError(field, 'Mật khẩu phải có ít nhất 8 ký tự');
                return false;
            }
        }

        // Check Confirm Password / Match Field
        const matchTarget = field.getAttribute('data-match');
        if (matchTarget) {
            const targetEl = this.form.querySelector(matchTarget);
            if (targetEl && field.value !== targetEl.value) {
                this.showError(field, 'Xác nhận mật khẩu không trùng khớp');
                return false;
            }
        }

        // Check Range Min / Max numeric
        const numMin = field.getAttribute('data-range-min');
        const numMax = field.getAttribute('data-range-max');
        if (numMin !== null || numMax !== null) {
            const valNum = parseFloat(value);
            if (isNaN(valNum)) {
                this.showError(field, `${labelName} phải là một số hợp lệ`);
                return false;
            }
            if (numMin !== null && valNum < parseFloat(numMin)) {
                this.showError(field, `${labelName} không được nhỏ hơn ${numMin}`);
                return false;
            }
            if (numMax !== null && valNum > parseFloat(numMax)) {
                this.showError(field, `${labelName} không được lớn hơn ${numMax}`);
                return false;
            }
        }

        // Check Specific Rules (email, phone, username, otp, price, quantity, discountPercent, etc.)
        for (let ruleKey of rules) {
            if (ruleKey === 'required') continue;

            const ruleObj = FormValidator.RULES[ruleKey];
            if (ruleObj) {
                if (ruleObj.regex && !ruleObj.regex.test(value)) {
                    const customMsg = field.getAttribute(`data-msg-${ruleKey}`) || ruleObj.message;
                    this.showError(field, customMsg);
                    return false;
                }
                if (ruleObj.validator && !ruleObj.validator(value)) {
                    const customMsg = field.getAttribute(`data-msg-${ruleKey}`) || ruleObj.message;
                    this.showError(field, customMsg);
                    return false;
                }
            }
        }

        // If field passed all checks, mark as valid
        this.showSuccess(field);
        return true;
    }

    /**
     * Display field error (Red border + warning icon + red text below field)
     */
    showError(field, message) {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');

        const parent = this.getFieldContainer(field);
        let feedback = parent.querySelector('.invalid-feedback');
        
        if (!feedback) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            parent.appendChild(feedback);
        }

        feedback.innerHTML = `<i class="bi bi-exclamation-circle-fill me-1"></i>${message}`;
        feedback.style.display = 'block';
    }

    /**
     * Display field valid status (Green border + checkmark icon)
     */
    showSuccess(field) {
        field.classList.remove('is-invalid');
        
        const parent = this.getFieldContainer(field);
        const feedback = parent.querySelector('.invalid-feedback');
        if (feedback) {
            feedback.style.display = 'none';
            feedback.textContent = '';
        }

        if (this.options.showValidState) {
            field.classList.add('is-valid');
        }
    }

    /**
     * Clear field status
     */
    clearFieldStatus(field) {
        field.classList.remove('is-invalid', 'is-valid');
        const parent = this.getFieldContainer(field);
        const feedback = parent.querySelector('.invalid-feedback');
        if (feedback) {
            feedback.style.display = 'none';
            feedback.textContent = '';
        }
    }

    /**
     * Get parent container of field (.mb-3, .form-group, .input-group, or parentNode)
     */
    getFieldContainer(field) {
        return field.closest('.input-group') || field.closest('.mb-3') || field.closest('.form-group') || field.parentElement;
    }

    /**
     * Validate entire form on submit
     */
    validate() {
        const fields = this.getFields();
        let isValid = true;
        let firstInvalidField = null;

        fields.forEach(field => {
            const fieldValid = this.validateField(field);
            if (!fieldValid) {
                isValid = false;
                if (!firstInvalidField) {
                    firstInvalidField = field;
                }
            }
        });

        if (!isValid && firstInvalidField) {
            firstInvalidField.focus();
            firstInvalidField.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        return isValid;
    }

    /**
     * Clear all validation states in the form
     */
    reset() {
        const fields = this.getFields();
        fields.forEach(field => this.clearFieldStatus(field));
    }

    /**
     * Static Helper: Attach FormValidator to form submit event automatically
     */
    static bind(formSelector, onValidSubmit, options = {}) {
        const formEl = typeof formSelector === 'string' ? document.querySelector(formSelector) : formSelector;
        if (!formEl) return null;

        const validator = new FormValidator(formEl, options);

        formEl.addEventListener('submit', function (e) {
            e.preventDefault();
            if (validator.validate()) {
                if (typeof onValidSubmit === 'function') {
                    onValidSubmit(formEl, e);
                }
            }
        });

        return validator;
    }

    /**
     * Static Helper: Display Backend Validation Errors (from Spring Boot ApiResponse)
     * Maps error fields in response.data { "username": "Tên đăng nhập đã tồn tại", ... } directly to form inputs
     */
    static handleBackendErrors(formEl, apiResponse) {
        if (!formEl || !apiResponse) return;
        const form = typeof formEl === 'string' ? document.querySelector(formEl) : formEl;
        if (!form) return;

        const errors = apiResponse.data;
        if (errors && typeof errors === 'object' && !Array.isArray(errors)) {
            Object.keys(errors).forEach(fieldName => {
                const message = errors[fieldName];
                
                // Find input by id or name
                const field = form.querySelector(`#${fieldName}`) || 
                              form.querySelector(`[name="${fieldName}"]`) ||
                              form.querySelector(`[data-field="${fieldName}"]`);
                
                if (field) {
                    field.classList.remove('is-valid');
                    field.classList.add('is-invalid');

                    const parent = field.closest('.input-group') || field.closest('.mb-3') || field.closest('.form-group') || field.parentElement;
                    let feedback = parent.querySelector('.invalid-feedback');
                    if (!feedback) {
                        feedback = document.createElement('div');
                        feedback.className = 'invalid-feedback';
                        parent.appendChild(feedback);
                    }

                    feedback.innerHTML = `<i class="bi bi-exclamation-circle-fill me-1"></i>${message}`;
                    feedback.style.display = 'block';
                }
            });
        }
    }
}

// Auto-initialize all forms with [data-form-validate] attribute or class 'needs-validation'
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('form[data-form-validate], form.needs-validation').forEach(form => {
        if (!form._validator) {
            form._validator = new FormValidator(form);
        }
    });
});
