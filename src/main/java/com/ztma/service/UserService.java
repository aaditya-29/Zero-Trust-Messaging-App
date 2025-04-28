package com.ztma.service;

import java.security.KeyPair;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.ztma.model.User;
import com.ztma.repository.UserRepository;
import com.ztma.util.CryptoUtil;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void registerUser(User user) throws Exception {
        KeyPair pair = CryptoUtil.generateRSAKeyPair();
        user.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        user.setPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        user.setVerified(false);
        user.setPassword(encoder.encode(user.getPassword()));
        String code = UUID.randomUUID().toString().substring(0, 6);
        user.setVerificationCode(code);
        userRepo.save(user);

        System.out.println("Registration code is: " + code);
//        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    public boolean verifyCode(String email, String code) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (code.equals(user.getVerificationCode())) {
                user.setVerified(true);
                user.setVerificationCode(null);
                userRepo.save(user);
                return true;
            }
        }
        return false;
    }

    public User loadUserByEmail(String email) throws Exception {
        return userRepo.findByEmail(email).orElseThrow(() -> new Exception("User not found"));
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        System.out.println("Generated OTP: " + otp);
        return String.valueOf(otp);
    }
}
