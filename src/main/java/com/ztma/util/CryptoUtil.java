package com.ztma.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;
import java.util.Base64;

public class CryptoUtil {

	public static KeyPair generateRSAKeyPair() throws Exception {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		gen.initialize(2048);
		return gen.generateKeyPair();
	}

//	public static String encryptRSA(String plaintext, PublicKey key) throws Exception {
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.ENCRYPT_MODE, key);
//		return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes()));
//	}

//	public static String decryptRSA(String ciphertext, PrivateKey key) throws Exception {
//		Cipher cipher = Cipher.getInstance("RSA");
//		cipher.init(Cipher.DECRYPT_MODE, key);
//		return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));
//	}

//	public static String encryptAES(String plaintext, String secret) throws Exception {
//		SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
//		Cipher cipher = Cipher.getInstance("AES");
//		cipher.init(Cipher.ENCRYPT_MODE, key);
//		return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes()));
//	}

//	public static String decryptAES(String ciphertext, String secret) throws Exception {
//		SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "AES");
//		Cipher cipher = Cipher.getInstance("AES");
//		cipher.init(Cipher.DECRYPT_MODE, key);
//		return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));
//	}

//	public static PublicKey getPublicKeyFromBase64(String base64Key) throws Exception {
//		byte[] keyBytes = Base64.getDecoder().decode(base64Key);
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
//	}

//	public static PrivateKey getPrivateKeyFromBase64(String base64Key) throws Exception {
//		byte[] keyBytes = Base64.getDecoder().decode(base64Key);
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
//	}

//	// 🔥 New: Decode TOTP Secret Key for OTP validation
//	public static SecretKey getSecretKeyFromBase64(String base64Secret) throws Exception {
//		byte[] decodedKey = Base64.getDecoder().decode(base64Secret);
//		return new SecretKeySpec(decodedKey, "HmacSHA1");
//	}
}
