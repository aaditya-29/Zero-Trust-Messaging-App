package com.ztma.service;

import com.ztma.model.ChatMessage;
import com.ztma.model.SecureMessage;
import com.ztma.model.User;
import com.ztma.repository.SecureMessageRepository;
import com.ztma.repository.UserRepository;
import com.ztma.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SecureMessageRepository messageRepo;

    public SecureMessage encryptAndSaveMessage(ChatMessage message) throws Exception {
        User recipient = userRepo.findByEmail(message.getTo()).orElseThrow();
        PublicKey publicKey = CryptoUtil.getPublicKeyFromBase64(recipient.getPublicKey());

        String aesKey = UUID.randomUUID().toString().substring(0, 16); // 128-bit AES key
        String encryptedMsg = CryptoUtil.encryptAES(message.getContent(), aesKey);
        String encryptedAESKey = CryptoUtil.encryptRSA(aesKey, publicKey);

        SecureMessage secureMessage = new SecureMessage();
        secureMessage.setSender(message.getFrom());
        secureMessage.setReceiver(message.getTo());
        secureMessage.setEncryptedMessage(encryptedMsg);
        secureMessage.setEncryptedAESKey(encryptedAESKey);
        secureMessage.setTimestamp(System.currentTimeMillis());

        return messageRepo.save(secureMessage);
    }
}
