package com.ztma.controller;

import java.util.List;
import org.springframework.ui.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ztma.model.TrustedDevice;
import com.ztma.model.UserActivityLog;
import com.ztma.repository.TrustedDeviceRepository;
import com.ztma.repository.UserActivityLogRepository;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

	@Autowired
	private TrustedDeviceRepository trustedDeviceRepository;

	@Autowired
	private UserActivityLogRepository userActivityLogRepository;

	@GetMapping("/dashboard")
	public String showDashboard(Model model) {
		List<TrustedDevice> devices = trustedDeviceRepository.findAll();
		List<UserActivityLog> activities = userActivityLogRepository.findAll();

		model.addAttribute("devices", devices);
		model.addAttribute("activities", activities);
		return "admin-dashboard";
	}
}
