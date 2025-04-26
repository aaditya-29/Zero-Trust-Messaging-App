package com.ztma.controller;

import com.ztma.model.ChatMessage;
import com.ztma.model.SecureMessage;
import com.ztma.model.User;
import com.ztma.repository.SecureMessageRepository;
import com.ztma.repository.UserRepository;
import com.ztma.service.ChatService;
import com.ztma.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.PrivateKey;
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

    // Serve the chat page after login
    @GetMapping("/chat")
    public String chatPage(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "chat"; // loads chat.html
    }

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) throws Exception {
        SecureMessage secureMessage = chatService.encryptAndSaveMessage(message);
        messagingTemplate.convertAndSend("/topic/messages/" + message.getTo(), secureMessage);
    }

    @GetMapping("/api/messages")
    public ResponseEntity<List<String>> getMessages(@RequestParam String email) throws Exception {
        List<SecureMessage> messages = messageRepo.findByReceiver(email);
        User user = userRepo.findByEmail(email).orElseThrow();
        PrivateKey privateKey = CryptoUtil.getPrivateKeyFromBase64(user.getPrivateKey());

        List<String> decrypted = new ArrayList<>();
        for (SecureMessage m : messages) {
            String aesKey = CryptoUtil.decryptRSA(m.getEncryptedAESKey(), privateKey);
            decrypted.add(CryptoUtil.decryptAES(m.getEncryptedMessage(), aesKey));
        }
        return ResponseEntity.ok(decrypted);
    }
}
