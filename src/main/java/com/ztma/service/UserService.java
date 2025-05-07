
package com.ztma.service;

import com.ztma.model.User;
import com.ztma.repository.UserRepository;
import com.ztma.util.CryptoUtil;
import com.ztma.util.OtpUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.security.KeyPair;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Register a new user
    public void registerUser(User user) throws Exception {
        // Generate RSA Key Pair for encryption
        KeyPair pair = CryptoUtil.generateRSAKeyPair();
        user.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
        user.setPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        user.setVerified(false);
        user.setPassword(encoder.encode(user.getPassword()));

        // Generate a verification code for email
        String code = UUID.randomUUID().toString().substring(0, 6);
        user.setVerificationCode(code);

        // Generate TOTP secret for MFA (Google Authenticator)
        String totpSecret = OtpUtil.generateSecret();
        user.setTotpSecret(totpSecret);

        // Save the user data to the repository
        userRepo.save(user);

        // Log the verification code (can be sent to the user via email)
        System.out.println("Registration verification code is: " + code);
        // emailService.sendVerificationEmail(user.getEmail(), code);  // Uncomment to send verification email
    }

    // Verify the code entered during registration
    public boolean verifyCode(String email, String code) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (code.equals(user.getVerificationCode())) {
                user.setVerified(true);
                user.setVerificationCode(null);  // Clear the verification code
                userRepo.save(user);
                return true;
            }
        }
        return false;
    }

    // Load a user by email
    public User loadUserByEmail(String email) throws Exception {
        return userRepo.findByEmail(email).orElseThrow(() -> new Exception("User not found"));
    }

    // Check if the password matches
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    // Generate a One-Time Password (OTP) for login
    public String generateOTP() {
        return UUID.randomUUID().toString().substring(0, 6);  // Random 6-digit OTP
    }

    // Validate OTP entered during login
    public boolean validateOtp(String email, String otp) throws Exception {
        User user = loadUserByEmail(email);
        return OtpUtil.verifyCode(user.getTotpSecret(), otp);
    }

    // Validate OTP for TOTP (Google Authenticator)
    public boolean validateTotpOtp(String otp, String totpSecret) throws Exception {
        return OtpUtil.verifyCode(totpSecret, otp);
    }
}
