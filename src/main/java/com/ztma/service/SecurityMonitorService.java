package com.ztma.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ztma.model.UserActivityLog;
import com.ztma.repository.UserActivityLogRepository;

@Service
public class SecurityMonitorService {

    @Autowired
    private UserActivityLogRepository activityRepo;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 5 * 60 * 1000) // Every 5 minutes
    public void detectAnomalies() {
        long cutoffMillis = System.currentTimeMillis() - (10 * 60 * 1000); // Last 10 minutes
        Date cutoffDate = new Date(cutoffMillis);  // Convert long -> Date

        List<UserActivityLog> logs = activityRepo.findRecentLogs(cutoffDate);

        Map<String, Long> messageCounts = logs.stream()
            .filter(log -> "SEND_MESSAGE".equals(log.getActionType()))
            .collect(Collectors.groupingBy(UserActivityLog::getUserEmail, Collectors.counting()));

        for (Map.Entry<String, Long> entry : messageCounts.entrySet()) {
            if (entry.getValue() > 20) {
                emailService.sendSimpleMessage(
                    entry.getKey(),
                    "Suspicious Activity Alert",
                    "Your account has sent over " + entry.getValue() +
                    " messages in the last 10 minutes. If this wasn't you, please change your password."
                );
            }
        }
    }
}
