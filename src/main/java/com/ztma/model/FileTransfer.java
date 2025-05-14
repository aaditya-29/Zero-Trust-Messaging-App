package com.ztma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "file_transfers")
public class FileTransfer {

	@Id
	private String fileId;

	private String sender;
	private String receiver;
	private String fileName;
	private String fileType;
	private long fileSize;
	private int totalChunks;
	private String encryptedAESKey;

	private long timestamp;
	private boolean sensitive;
	private boolean expired;

	private String status; // e.g., PENDING, DELIVERED, READ

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public int getTotalChunks() {
		return totalChunks;
	}

	public void setTotalChunks(int totalChunks) {
		this.totalChunks = totalChunks;
	}

	public String getEncryptedAESKey() {
		return encryptedAESKey;
	}

	public void setEncryptedAESKey(String encryptedAESKey) {
		this.encryptedAESKey = encryptedAESKey;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSensitive() {
		return sensitive;
	}

	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
