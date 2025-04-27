package com.ztma.controller;

import com.ztma.model.SecureMessage;
import com.ztma.model.User;
import com.ztma.repository.SecureMessageRepository;
import com.ztma.repository.UserRepository;
import com.ztma.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
		if (email != null) {
			model.addAttribute("email", email);
		} else {
			// Handle case when email is not present
			model.addAttribute("error", "Email not found in request");
			return "error"; // or redirect to login page
		}

		User user = userRepo.findByEmail(email).orElseThrow();
		model.addAttribute("privateKey", user.getPrivateKey()); // Pass private key to frontend

		return "chat"; // loads chat.html
	}

	@MessageMapping("/chat")
	public void processMessage(@RequestBody com.ztma.model.ChatMessage message) {
		SecureMessage secureMessage = new SecureMessage();
		secureMessage.setSender(message.getFrom());
		secureMessage.setReceiver(message.getTo());
		secureMessage.setEncryptedMessage(message.getContent());
		secureMessage.setEncryptedAESKey(message.getAesKeyEncryptedWithRSA());
		secureMessage.setTimestamp(System.currentTimeMillis());

		messageRepo.save(secureMessage);

		messagingTemplate.convertAndSend("/topic/messages/" + message.getTo(), secureMessage);
	}

	@GetMapping("/api/messages")
	public ResponseEntity<List<SecureMessage>> getMessages(@RequestParam String email) {
		List<SecureMessage> messages = messageRepo.findByReceiver(email);
		return ResponseEntity.ok(messages);
	}

	@GetMapping("/api/public-key")
	public ResponseEntity<String> getPublicKey(@RequestParam String email) {
		User user = userRepo.findByEmail(email).orElseThrow();
		return ResponseEntity.ok(user.getPublicKey());
	}
}
