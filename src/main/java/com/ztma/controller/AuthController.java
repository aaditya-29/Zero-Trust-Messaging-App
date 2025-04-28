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
import jakarta.servlet.http.HttpSession;
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

	// -------------------- Login --------------------
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

	@PostMapping("/process-login")
	public String processLogin(@RequestParam String email, @RequestParam String password, Model model) {

		try {
			// Check if the user exists and validate credentials
			User user = userService.loadUserByEmail(email);
			if (userService.checkPassword(password, user.getPassword())) {
				// Password correct → Generate and send OTP

				String otp = userService.generateOTP();
				loginOtps.put(email, otp);
				System.out.println("Login OTP: " + otp); // Log the OTP
				emailService.sendSimpleMessage(email, "Your Login OTP", "Your OTP is: " + otp);

				// Redirect to OTP verification page with email in the URL
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

	@GetMapping("/verify-otp")
	public String showLoginOtpPage(@RequestParam String email, Model model) {
		// Ensure the email parameter is passed correctly

		if (email == null || email.isEmpty()) {
			model.addAttribute("error", "Email parameter is missing.");

			return "login"; // Or redirect back to login with an error
		}

		model.addAttribute("email", email);
		return "verify-otp";
	}

	@PostMapping("/process-login-otp")
	public String processLoginOtp(@RequestParam String email, @RequestParam String otp, HttpServletRequest request,
			HttpServletResponse response, Model model) {

		String storedOtp = loginOtps.get(email);
		if (storedOtp != null && storedOtp.equals(otp)) {
			loginOtps.remove(email); // Remove OTP once used

			try {
				User user = userService.loadUserByEmail(email);

				// Create authentication token
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
						user.getAuthorities());

				// Set in SecurityContextHolder
				SecurityContextHolder.getContext().setAuthentication(authentication);

				// ⚡ Save Authentication into Session (IMPORTANT!!!)
				HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
				securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

				// Redirect to chat
				return "redirect:/chat?email=" + email;
			} catch (Exception e) {
				model.addAttribute("error", "Authentication failed.");
				return "login";
			}
		} else {
			model.addAttribute("error", "Invalid OTP. Try again.");
			model.addAttribute("email", email);
			return "verify-otp";
		}
	}

}
