package com.re.examholiday.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return "dashboard/index";
    }

    @GetMapping("/profile")
    public String profile() {
        return "auth/profile";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "auth/change-password";
    }

    // ==================== ACCOUNT MANAGEMENT ====================
    @GetMapping("/accounts")
    public String accounts() {
        return "account/list";
    }

    @GetMapping("/accounts/create")
    public String createAccount() {
        return "account/create";
    }

    @GetMapping("/accounts/edit/{id}")
    public String editAccount() {
        return "account/edit";
    }

    @GetMapping("/accounts/detail/{id}")
    public String detailAccount() {
        return "account/detail";
    }

    // ==================== EMPLOYEE MANAGEMENT ====================
    @GetMapping("/employees")
    public String employees() {
        return "employee/list";
    }

    @GetMapping("/employees/create")
    public String createEmployee() {
        return "employee/create";
    }

    @GetMapping("/employees/edit/{id}")
    public String editEmployee() {
        return "employee/edit";
    }

    @GetMapping("/employees/detail/{id}")
    public String detailEmployee() {
        return "employee/detail";
    }

    // ==================== CUSTOMER MANAGEMENT ====================
    @GetMapping("/customers")
    public String customers() {
        return "customer/list";
    }

    @GetMapping("/customers/create")
    public String createCustomer() {
        return "customer/create";
    }

    @GetMapping("/customers/edit/{id}")
    public String editCustomer() {
        return "customer/edit";
    }

    @GetMapping("/customers/detail/{id}")
    public String detailCustomer() {
        return "customer/detail";
    }

    // ==================== TABLE MANAGEMENT ====================
    @GetMapping("/tables")
    public String tables() {
        return "table/list";
    }

    @GetMapping("/tables/map")
    public String tablesMap() {
        return "table/map";
    }

    // ==================== RESERVATIONS ====================
    @GetMapping("/reservations")
    public String reservations() {
        return "reservation/list";
    }

    @GetMapping("/reservations/create")
    public String createReservation() {
        return "reservation/create";
    }

    // ==================== MENU & CATEGORIES ====================
    @GetMapping("/menu")
    public String menu() {
        return "menu/list";
    }

    @GetMapping("/categories")
    public String categories() {
        return "menu/category";
    }

    // ==================== ORDERS ====================
    @GetMapping("/orders")
    public String orders() {
        return "order/list";
    }

    // ==================== KITCHEN ====================
    @GetMapping("/kitchen")
    public String kitchen() {
        return "kitchen/index";
    }

    // ==================== PAYMENTS ====================
    @GetMapping("/payments")
    public String payments() {
        return "payment/list";
    }

    // ==================== PROMOTIONS ====================
    @GetMapping("/promotions")
    public String promotions() {
        return "promotion/list";
    }

    // ==================== REPORTS & STATISTICS ====================
    @GetMapping("/reports")
    public String reports() {
        return "report/list";
    }

    @GetMapping("/dashboard/statistics")
    public String statistics() {
        return "dashboard/statistics";
    }
}
