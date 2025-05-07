package com.ztma.controller;

import com.ztma.model.User;
import com.ztma.service.EmailService;
import com.ztma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Temporary storage for login OTPs
    private Map<String, String> loginOtps = new HashMap<>();

    // -------------------- Registration --------------------
    @GetMapping("/register")
    public String showRegisterForm(Model model, @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success) {
        model.addAttribute("user", new User());
        if (error != null) {
            model.addAttribute("error", "Registration failed. Try again.");
        }
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please check your email for verification.");
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        try {
            userService.registerUser(user);
            return "redirect:/verify-email?email=" + user.getEmail();
        } catch (Exception e) {
            return "redirect:/register?error=" + e.getMessage();
        }
    }

    @GetMapping("/verify-email")
    public String showVerifyEmailPage(@RequestParam(required = false) String email,
            @RequestParam(required = false) String error, Model model) {
        model.addAttribute("email", email);
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "verify-email";
    }

    @PostMapping("/verify")
    public String verifyCode(@RequestParam String code, @RequestParam String email) {
        if (userService.verifyCode(email, code)) {
            return "redirect:/login?verified";
        } else {
            return "redirect:/verify-email?email=" + email + "&error=Invalid verification code.";
        }
    }

    // --------------- Show Login Page ---------------
    @GetMapping("/login")
    public String showLoginPage(Model model, @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    // --------------- Process Login ---------------
    @PostMapping("/process-login")
    public String processLogin(@RequestParam String email, @RequestParam String password, Model model) {
        try {
            User user = userService.loadUserByEmail(email);
            if (userService.checkPassword(password, user.getPassword())) {
                // Password correct → Send OTP
                String otp = userService.generateOTP();
                loginOtps.put(email, otp);

                System.out.println("Login OTP: " + otp);
                emailService.sendSimpleMessage(email, "Your Login OTP", "Your OTP is: " + otp);

                return "redirect:/verify-otp?email=" + email;
            } else {
                model.addAttribute("error", "Invalid credentials.");
                return "login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials.");
            return "login";
        }
    }

    // --------------- Show OTP Verification Page ---------------
    @GetMapping("/verify-otp")
    public String showLoginOtpPage(@RequestParam String email, Model model) {
        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "Email parameter is missing.");
            return "login";
        }
        model.addAttribute("email", email);
        return "verify-otp";
    }

    // --------------- Process OTP Verification ---------------
    @PostMapping("/process-login-otp")
    public String processLoginOtp(@RequestParam String email, @RequestParam String otp, HttpServletRequest request,
            HttpServletResponse response, Model model) {
        System.out.println("Inside process-login-otp before starting OTP validation");

        String storedOtp = loginOtps.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            loginOtps.remove(email); // OTP is used once

            System.out.println("Email OTP validated successfully.");

            try {
                // OTP correct → Now fully authenticate the user
                User user = userService.loadUserByEmail(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                        user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
                securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

                System.out.println("Authentication successful. Redirecting to chat...");
                return "redirect:/chat?email=" + email;

            } catch (Exception e) {
                model.addAttribute("error", "Authentication failed. Please login again.");
                return "login";
            }
        } else {
            model.addAttribute("error", "Invalid OTP. Please check your email and try again.");
            model.addAttribute("email", email);
            return "verify-otp";
        }
    }
}
