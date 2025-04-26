package com.ztma.model;

public class ChatMessage {
    private String from;
    private String to;
    private String content; // Encrypted with AES
    private String aesKeyEncryptedWithRSA; // AES key encrypted with RSA public key

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
}
