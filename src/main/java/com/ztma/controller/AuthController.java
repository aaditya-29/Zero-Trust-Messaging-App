package com.ztma.controller;

import com.ztma.model.User;
import com.ztma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

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


	@GetMapping("/login")
	public String showLogin(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			@RequestParam(value = "verified", required = false) String verified, Model model) {
		if (error != null) {
			model.addAttribute("error", "Invalid username or password.");
		}
		if (logout != null) {
			model.addAttribute("message", "You have been logged out successfully.");
		}
		if (verified != null) {
			model.addAttribute("message", "Email verified successfully! You can now login.");
		}
		return "login";
	}
	
	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user) {
	    try {
	        userService.registerUser(user);
	        // After registration, send to verify-email page with email parameter
	        return "redirect:/verify-email?email=" + user.getEmail();
	    } catch (Exception e) {
	        return "redirect:/register?error=" + e.getMessage();
	    }
	}

	@GetMapping("/verify-email")
	public String showVerifyEmailPage(@RequestParam(required = false) String email,
	                                  @RequestParam(required = false) String error,
	                                  Model model) {
	    model.addAttribute("email", email); // <-- important to pass email
	    if (error != null) {
	        model.addAttribute("error", error);
	    }
	    return "verify-email";
	}

	@PostMapping("/verify")
	public String verifyCode(@RequestParam String code,
	                         @RequestParam String email) {
	    if (userService.verifyCode(email, code)) {
	        return "redirect:/login?verified";
	    } else {
	        return "redirect:/verify-email?email=" + email + "&error=Invalid verification code.";
	    }
	}

}
