package com.ztma.model;

public class FileTransferMessage {

	private String fileId;
	private String from;
	private String to;

	private String fileName;
	private String fileType;
	private long fileSize;
	private int totalChunks;
	private String encryptedAESKey;

	private int chunkIndex;
	private String encryptedChunk;
	private String signature;

	private long timestamp;
	private boolean sensitive;

	private boolean fileMetadata;
	private boolean fileChunk;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
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

	public int getChunkIndex() {
		return chunkIndex;
	}

	public void setChunkIndex(int chunkIndex) {
		this.chunkIndex = chunkIndex;
	}

	public String getEncryptedChunk() {
		return encryptedChunk;
	}

	public void setEncryptedChunk(String encryptedChunk) {
		this.encryptedChunk = encryptedChunk;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
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

	public boolean isFileMetadata() {
		return fileMetadata;
	}

	public void setFileMetadata(boolean fileMetadata) {
		this.fileMetadata = fileMetadata;
	}

	public boolean isFileChunk() {
		return fileChunk;
	}

	public void setFileChunk(boolean fileChunk) {
		this.fileChunk = fileChunk;
	}

}
