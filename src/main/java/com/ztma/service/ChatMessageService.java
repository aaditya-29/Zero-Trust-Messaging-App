package com.ztma.service;

import com.ztma.model.SecureMessage;
import com.ztma.model.User;
import com.ztma.repository.UserRepository;
import com.ztma.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;

@Service
public class ChatMessageService {

    @Autowired
    private UserRepository userRepo;

    public String decryptMessage(SecureMessage message, String userEmail) throws Exception {
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        PrivateKey privateKey = CryptoUtil.getPrivateKeyFromBase64(user.getPrivateKey());
        String aesKey = CryptoUtil.decryptRSA(message.getEncryptedAESKey(), privateKey);
        return CryptoUtil.decryptAES(message.getEncryptedMessage(), aesKey);
    }
}
