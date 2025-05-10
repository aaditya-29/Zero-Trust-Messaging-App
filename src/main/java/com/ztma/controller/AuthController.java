package com.ztma.controller;

import com.ztma.model.LoginLog;
import com.ztma.model.TrustedDevice;
import com.ztma.model.User;
import com.ztma.model.UserActivityLog;
import com.ztma.repository.LoginLogRepository;
import com.ztma.repository.TrustedDeviceRepository;
import com.ztma.repository.UserActivityLogRepository;
import com.ztma.service.EmailService;
import com.ztma.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;
	@Autowired
	private LoginLogRepository loginLogRepository;

	@Autowired
	private UserActivityLogRepository userActivityLogRepository;

	@Autowired
	private TrustedDeviceRepository trustedDeviceRepository;

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
				User user = userService.loadUserByEmail(email);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
						user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);

				HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
				contextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

				// Get IP & User-Agent
				String ip = request.getRemoteAddr();
				String userAgent = request.getHeader("User-Agent");

				// 💾 Store in TrustedDevice (if not already trusted)
				List<TrustedDevice> trustedDevices = trustedDeviceRepository.findByUserEmail(email);
				boolean isNewDevice = trustedDevices.stream().noneMatch(d -> d.getIpAddress().equals(ip));
				if (isNewDevice) {
					TrustedDevice device = new TrustedDevice();
					device.setUserEmail(email);
					device.setIpAddress(ip);
					device.setUserAgent(userAgent);
					device.setLastLoginTime(new Date());
					trustedDeviceRepository.save(device);

					emailService.sendSimpleMessage(email, "New Device Login",
							"We noticed a login from a new IP address: " + ip + "\nUser Agent: " + userAgent);
				}

				UserActivityLog loginLog = new UserActivityLog();
				loginLog.setUserEmail(email);
				loginLog.setActionType("LOGIN");
				loginLog.setTimestamp(new Date());
				userActivityLogRepository.save(loginLog);

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

	@PostMapping("/api/device-fingerprint")
	@ResponseBody
	public ResponseEntity<Map<String, String>> checkDeviceAndIP(@RequestBody Map<String, String> data,
			HttpServletRequest request) throws Exception {
		String email = data.get("email");
		String deviceId = data.get("deviceId");

		Map<String, String> response = new HashMap<>();
		if (email == null || deviceId == null) {
			response.put("status", "error");
			response.put("message", "Missing email or deviceId");
			return ResponseEntity.badRequest().body(response);
		}

		User user = userService.loadUserByEmail(email);

		// DEVICE anomaly check
		boolean newDevice = false;
		Set<String> knownDevices = new HashSet<>(user.getKnownDevices());
		if (!knownDevices.contains(deviceId)) {
			knownDevices.add(deviceId);
			user.setKnownDevices(new ArrayList<>(knownDevices));
			newDevice = true;
			emailService.sendSimpleMessage(email, "⚠️ New Device Login",
					"We detected a new device login. If this wasn't you, take action.");
		}

		// IP anomaly check
		String ipAddress = request.getRemoteAddr();
		Set<String> knownIps = new HashSet<>(user.getKnownIps());
		boolean newIp = false;

		if (!knownIps.contains(ipAddress)) {
			knownIps.add(ipAddress);
			user.setKnownIps(knownIps);
			newIp = true;
			emailService.sendSimpleMessage(email, "⚠️ New IP Address Detected",
					"We detected login from a new IP: " + ipAddress);
		}

		userService.saveUser(user);

		// Return anomaly types
		if (newDevice || newIp) {
			response.put("status", "anomaly");
			response.put("deviceAnomaly", String.valueOf(newDevice));
			response.put("ipAnomaly", String.valueOf(newIp));
		} else {
			response.put("status", "ok");
		}

		return ResponseEntity.ok(response);
	}

}
