package com.ztma.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import org.jboss.aerogear.security.otp.Totp;

public class OtpUtil {

    private static final String TOTP_ALGORITHM  = "HmacSHA1";
    private static final int TOTP_TIME_STEP = 30; // TOTP time step in seconds

    // Generate a new secret key for TOTP (used by Google Authenticator)
    public static String generateSecret() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(TOTP_ALGORITHM);
        keyGenerator.init(160, new SecureRandom()); // 160 bits for SHA1
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Verify the TOTP code (compare it with the current time-based OTP)
    public static boolean verifyCode(String secretBase64, String inputCode) throws Exception {
        // Create a TOTP instance using the secret key
        Totp totp = new Totp(secretBase64);
        // Validate the OTP using the provided code and the current timestamp
        return totp.verify(inputCode); // Returns true if the inputCode is valid
    }
}
