package com.ztma.controller;

import com.ztma.model.*;
import com.ztma.repository.*;
import com.ztma.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

        messagingTemplate.convertAndSend("/topic/messages/" + message.getTo(), savedMessage);
    }

    @MessageMapping("/read")
    public void markAsRead(ReadReceipt receipt) {
        List<SecureMessage> messages = messageRepo.findBySenderAndReceiver(receipt.getSender(), receipt.getReceiver());
        messages.forEach(msg -> {
            msg.setStatus("READ");
        });
        messageRepo.saveAll(messages);

        messagingTemplate.convertAndSend("/topic/read-receipts/" + receipt.getSender(), receipt);
    }

    @MessageMapping("/typing")
    public void typingIndicator(TypingIndicator indicator) {
        messagingTemplate.convertAndSend("/topic/typing/" + indicator.getReceiver(), indicator);
    }

    @MessageMapping("/status")
    public void userStatus(UserStatus status) {
        messagingTemplate.convertAndSend("/topic/status/" + status.getUserId(), status);
    }

    @GetMapping("/api/messages")
    public ResponseEntity<List<SecureMessage>> getMessages(@RequestParam String email) {
        long now = System.currentTimeMillis();
        long expiration = 10 * 60 * 1000;
        System.out.println("Controller hit first ");

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
        System.out.println("Controller hit after");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/public-key")
    public ResponseEntity<String> getPublicKey(@RequestParam String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(user.getPublicKey());
    }
}
