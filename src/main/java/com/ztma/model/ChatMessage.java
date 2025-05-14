package com.ztma.model;

public class ChatMessage {

	private String from;
	private String to;
	private String content; // Encrypted with AES
	private String aesKeyEncryptedWithRSA; // AES key encrypted with RSA public key
	private boolean sensitive; // NEW FIELD: to indicate if the message is sensitive
	private String signature; // digital signature
	private String ephemeralPublicKey; // For FS
    private long timestamp;

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getEphemeralPublicKey() {
		return ephemeralPublicKey;
	}

	public void setEphemeralPublicKey(String ephemeralPublicKey) {
		this.ephemeralPublicKey = ephemeralPublicKey;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAesKeyEncryptedWithRSA() {
		return aesKeyEncryptedWithRSA;
	}

	public void setAesKeyEncryptedWithRSA(String aesKeyEncryptedWithRSA) {
		this.aesKeyEncryptedWithRSA = aesKeyEncryptedWithRSA;
	}

	public boolean isSensitive() {
		return sensitive;
	}

	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}


}
