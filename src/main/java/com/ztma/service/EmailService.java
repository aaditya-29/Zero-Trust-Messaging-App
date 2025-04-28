package com.ztma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // For registration verification
    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("ZTMA Email Verification Code");
        msg.setText("Your verification code is: " + code);
        mailSender.send(msg);
        System.out.println("Verification email sent to " + to + " with code: " + code);  // Added logging
    }

    // For login OTP (MFA)
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        System.out.println("OTP email sent to " + to + ": " + text);  // Added logging for OTP email
//        mailSender.send(msg);
    }
}
