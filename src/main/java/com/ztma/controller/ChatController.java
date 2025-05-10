package com.ztma.controller;

import com.ztma.model.*;
import com.ztma.repository.*;
import com.ztma.service.ChatService;
import com.ztma.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SecureMessageRepository messageRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MessageLogRepository messageLogRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserActivityLogRepository userActivityLogRepository;

    @GetMapping("/chat")
    public String chatPage(@RequestParam(value = "email", required = false) String email, Model model) {
        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "Email not found in request");
            return "error";
        }

        User user = userRepo.findByEmail(email).orElseThrow();
        model.addAttribute("email", email);
        model.addAttribute("privateKey", user.getPrivateKey());

        return "chat";
    }

    @MessageMapping("/chat")
    public void processMessage(ChatMessage message) {
        SecureMessage secureMessage = new SecureMessage();
        secureMessage.setSender(message.getFrom());
        secureMessage.setReceiver(message.getTo());
        secureMessage.setEncryptedMessage(message.getContent());
        secureMessage.setEncryptedAESKey(message.getAesKeyEncryptedWithRSA());
        secureMessage.setTimestamp(System.currentTimeMillis());
        secureMessage.setStatus("DELIVERED");
        secureMessage.setSensitive(message.isSensitive());
        secureMessage.setExpired(false);
        secureMessage.setRead(false);

        SecureMessage savedMessage = messageRepo.save(secureMessage);

        // Log basic message metrics
        MessageLog msgLog = new MessageLog();
        msgLog.setSender(message.getFrom());
        msgLog.setTimestamp(System.currentTimeMillis());
        msgLog.setSize(message.getContent().length());
        messageLogRepository.save(msgLog);

        // ✏️ Log user activity for Admin Dashboard
        UserActivityLog activity = new UserActivityLog();
        activity.setUserEmail(message.getFrom());
        activity.setActionType("MESSAGE_SENT");
        activity.setTimestamp(new Date());
        userActivityLogRepository.save(activity);

        // 📣 Detect spam-like behavior (> 10 messages in 60s)
        long cutoff = System.currentTimeMillis() - 60_000;
        List<MessageLog> recent = messageLogRepository.findBySenderAndTimestampAfter(message.getFrom(), cutoff);
        if (recent.size() > 10) {
            emailService.sendSimpleMessage(message.getFrom(), "Unusual Activity Detected",
                    "You have sent a high number of messages in a short time. If this wasn't you, please secure your account.");

            System.out.println("Unusual Activity Detected for " + message.getFrom());
        }

        messagingTemplate.convertAndSend("/topic/messages/" + message.getTo(), savedMessage);
    }


    @MessageMapping("/status")
    public void userStatus(UserStatus status) {
        messagingTemplate.convertAndSend("/topic/status/" + status.getUserId(), status);
    }

    @GetMapping("/api/messages")
    public ResponseEntity<List<SecureMessage>> getMessages(@RequestParam String email) {
        long now = System.currentTimeMillis();
        long expiration = 10 * 60 * 1000;

        List<SecureMessage> messages = messageRepo.findByReceiver(email);
        List<SecureMessage> result = new ArrayList<>();

        for (SecureMessage msg : messages) {
            if (msg.isExpired()) {
                continue;
            }
            System.out.println("Controller hit second");
            if (msg.isSensitive() && now > msg.getTimestamp() + expiration) {
                msg.setExpired(true);
                messageRepo.save(msg);
                continue;
            }

            msg.setStatus("READ");
            msg.setRead(true);
            messageRepo.save(msg);
            result.add(msg);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/public-key")
    public ResponseEntity<String> getPublicKey(@RequestParam String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(user.getPublicKey());
    }
}
