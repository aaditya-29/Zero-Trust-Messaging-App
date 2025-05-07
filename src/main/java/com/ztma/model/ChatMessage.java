package com.ztma.model;

public class ChatMessage {
    private String from;
    private String to;
    private String content; // Encrypted with AES
    private String aesKeyEncryptedWithRSA; // AES key encrypted with RSA public key
    private boolean sensitive; // NEW FIELD: to indicate if the message is sensitive

    // Getter and Setter for 'from'
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    // Getter and Setter for 'to'
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    // Getter and Setter for 'content'
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter and Setter for 'aesKeyEncryptedWithRSA'
    public String getAesKeyEncryptedWithRSA() {
        return aesKeyEncryptedWithRSA;
    }

    public void setAesKeyEncryptedWithRSA(String aesKeyEncryptedWithRSA) {
        this.aesKeyEncryptedWithRSA = aesKeyEncryptedWithRSA;
    }

    // Getter and Setter for 'sensitive'
    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }
}
