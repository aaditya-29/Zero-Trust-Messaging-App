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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private FileTransferRepository fileTransferRepository; // New repository for file transfers

	@Autowired
	private FileChunkRepository fileChunkRepository; // New repository for file chunks

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
		secureMessage.setSignature(message.getSignature());
		secureMessage.setEphemeralPublicKey(message.getEphemeralPublicKey());
		secureMessage.setTimestamp(message.getTimestamp());
		secureMessage.setStatus("DELIVERED");
		secureMessage.setSensitive(message.isSensitive());
		secureMessage.setExpired(false);
		secureMessage.setRead(false);

		messageRepo.save(secureMessage);
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

		// 📣 Detect spam-like behavior (> 5 messages in 60s)
		long cutoff = System.currentTimeMillis() - 60_000;
		List<MessageLog> recent = messageLogRepository.findBySenderAndTimestampAfter(message.getFrom(), cutoff);
		if (recent.size() > 5) {
			emailService.sendSimpleMessage(
				    message.getFrom(),
				    "⚠️ Unusual Messaging Activity Detected",
				    "Hello,\n\n" +
				    "We noticed a high volume of messages sent from your account within a short time frame.\n\n" +
				    "If this activity was expected, no action is needed. However, if you suspect unauthorized access, we recommend you:\n" +
				    "- Change your password immediately\n" +
				    "- Review your recent activity\n" +
				    "- Enable two-factor authentication if available\n\n" +
				    "Stay secure,\n" +
				    "Your SecureChat Team"
				);

			System.out.println("Unusual Activity Detected for " + message.getFrom());
		}

		messagingTemplate.convertAndSend("/topic/messages/" + message.getTo(), secureMessage);
	}

	// New endpoint to handle file transfers
	@MessageMapping("/file-transfer")
	public void processFileTransfer(FileTransferMessage fileMessage) {
		// Log user activity for file transfers
		UserActivityLog activity = new UserActivityLog();
		activity.setUserEmail(fileMessage.getFrom());
		activity.setActionType("FILE_TRANSFER");
		activity.setTimestamp(new Date());
		userActivityLogRepository.save(activity);

		// Handle file metadata
		if (fileMessage.isFileMetadata()) {
			FileTransfer fileTransfer = new FileTransfer();
			fileTransfer.setFileId(fileMessage.getFileId());
			fileTransfer.setSender(fileMessage.getFrom());
			fileTransfer.setReceiver(fileMessage.getTo());
			fileTransfer.setFileName(fileMessage.getFileName());
			fileTransfer.setFileType(fileMessage.getFileType());
			fileTransfer.setFileSize(fileMessage.getFileSize());
			fileTransfer.setTotalChunks(fileMessage.getTotalChunks());
			fileTransfer.setEncryptedAESKey(fileMessage.getEncryptedAESKey());
			fileTransfer.setTimestamp(fileMessage.getTimestamp());
			fileTransfer.setSensitive(fileMessage.isSensitive());
			fileTransfer.setStatus("PENDING"); // Status starts as pending
			fileTransfer.setExpired(false);

			fileTransferRepository.save(fileTransfer);

			// Forward metadata to recipient
			messagingTemplate.convertAndSend("/topic/messages/" + fileMessage.getTo(), fileMessage);
			
			// Log larger file transfer for security monitoring
			if (fileMessage.getFileSize() > 10 * 1024 * 1024) { // Files > 10MB
				emailService.sendSimpleMessage(
					    fileMessage.getFrom(),
					    "📁 Large File Transfer Alert",
					    "Hello,\n\n" +
					    "We detected a large file transfer from your account:\n\n" +
					    "📄 File Name: " + fileMessage.getFileName() + "\n" +
					    "📦 File Size: " + (fileMessage.getFileSize() / (1024 * 1024)) + " MB\n" +
					    "📤 Sent To: " + fileMessage.getTo() + "\n\n" +
					    "If you initiated this transfer, no action is needed. Otherwise, we strongly recommend that you:\n" +
					    "- Review your recent activity\n" +
					    "- Change your password immediately\n" +
					    "- Enable two-factor authentication (if available)\n\n" +
					    "For assistance, contact our support team.\n\n" +
					    "Stay secure,\n" +
					    "Your SecureChat Team"
					);
	}
			
			return;
		}

		// Handle file chunks
		if (fileMessage.isFileChunk()) {
			FileChunk chunk = new FileChunk();
			chunk.setFileId(fileMessage.getFileId());
			chunk.setChunkIndex(fileMessage.getChunkIndex());
			chunk.setEncryptedChunk(fileMessage.getEncryptedChunk());
			chunk.setSignature(fileMessage.getSignature());
			fileChunkRepository.save(chunk);

			// Forward chunk to recipient
			messagingTemplate.convertAndSend("/topic/messages/" + fileMessage.getTo(), fileMessage);
			
			// Update file status if this is the last chunk
			FileTransfer fileTransfer = fileTransferRepository.findByFileId(fileMessage.getFileId());
			if (fileTransfer != null) {
				long chunkCount = fileChunkRepository.countByFileId(fileMessage.getFileId());
				if (chunkCount == fileTransfer.getTotalChunks()) {
					fileTransfer.setStatus("DELIVERED");
					fileTransferRepository.save(fileTransfer);
				}
			}
		}
	}

	@MessageMapping("/status")
	public void userStatus(UserStatus status) {
		messagingTemplate.convertAndSend("/topic/status/" + status.getUserId(), status);
	}

	@MessageMapping("/read-receipt")
	public void processReadReceipt(ReadReceipt receipt) {
		// Handle read receipt for text messages
		if (receipt.getMessageId() != null && !receipt.getMessageId().startsWith("file-")) {
			SecureMessage message = messageRepo.findById(receipt.getMessageId()).orElse(null);
			if (message != null) {
				message.setStatus("READ");
				message.setRead(true);
				messageRepo.save(message);
			}
		} 
		// Handle read receipt for files
		else if (receipt.getMessageId() != null && receipt.getMessageId().startsWith("file-")) {
			String fileId = receipt.getMessageId().substring(5); // Remove "file-" prefix
			FileTransfer fileTransfer = fileTransferRepository.findByFileId(fileId);
			if (fileTransfer != null) {
				fileTransfer.setStatus("READ");
				fileTransferRepository.save(fileTransfer);
			}
		}

		// Send read receipt to sender
		messagingTemplate.convertAndSend("/topic/read-receipts/" + receipt.getSender(), receipt);
	}

	@GetMapping("/api/messages")
	public ResponseEntity<List<Object>> getMessages(@RequestParam String email) {
		long now = System.currentTimeMillis();
		long expiration = 10 * 60 * 1000; // 10 minutes expiration time
		List<Object> result = new ArrayList<>();

		// Get text messages
		List<SecureMessage> messages = messageRepo.findByReceiver(email);
		for (SecureMessage msg : messages) {
			if (msg.isExpired()) {
				continue;
			}
			
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

		// Get file transfers
		List<FileTransfer> fileTransfers = fileTransferRepository.findByReceiver(email);
		for (FileTransfer fileTransfer : fileTransfers) {
			if (fileTransfer.isExpired()) {
				continue;
			}
			
			if (fileTransfer.isSensitive() && now > fileTransfer.getTimestamp() + expiration) {
				fileTransfer.setExpired(true);
				fileTransferRepository.save(fileTransfer);
				continue;
			}

			// Build complete file transfer object with metadata
			Map<String, Object> fileTransferObj = new HashMap<>();
			fileTransferObj.put("fileTransfer", true);
			fileTransferObj.put("fileMetadata", true);
			fileTransferObj.put("fileId", fileTransfer.getFileId());
			fileTransferObj.put("from", fileTransfer.getSender());
			fileTransferObj.put("to", fileTransfer.getReceiver());
			fileTransferObj.put("fileName", fileTransfer.getFileName());
			fileTransferObj.put("fileType", fileTransfer.getFileType());
			fileTransferObj.put("fileSize", fileTransfer.getFileSize());
			fileTransferObj.put("totalChunks", fileTransfer.getTotalChunks());
			fileTransferObj.put("encryptedAESKey", fileTransfer.getEncryptedAESKey());
			fileTransferObj.put("timestamp", fileTransfer.getTimestamp());
			fileTransferObj.put("sensitive", fileTransfer.isSensitive());
			
			result.add(fileTransferObj);
			
			// Get all chunks for this file transfer
			List<FileChunk> chunks = fileChunkRepository.findByFileIdOrderByChunkIndexAsc(fileTransfer.getFileId());
			for (FileChunk chunk : chunks) {
				Map<String, Object> chunkObj = new HashMap<>();
				chunkObj.put("fileTransfer", true);
				chunkObj.put("fileChunk", true);
				chunkObj.put("fileId", chunk.getFileId());
				chunkObj.put("chunkIndex", chunk.getChunkIndex());
				chunkObj.put("encryptedChunk", chunk.getEncryptedChunk());
				chunkObj.put("signature", chunk.getSignature());
				chunkObj.put("from", fileTransfer.getSender());
				chunkObj.put("to", fileTransfer.getReceiver());
				chunkObj.put("timestamp", fileTransfer.getTimestamp());
				
				result.add(chunkObj);
			}
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/api/public-key")
	public ResponseEntity<String> getPublicKey(@RequestParam String email) {
		User user = userRepo.findByEmail(email).orElseThrow();
		return ResponseEntity.ok(user.getPublicKey());
	}

	@GetMapping("/api/x25519-public-key")
	public ResponseEntity<String> getX25519PublicKey(@RequestParam String email) {
		User user = userRepo.findByEmail(email).orElseThrow();
		return ResponseEntity.ok(user.getX25519PublicKey()); // Must be stored in DB
	}
}