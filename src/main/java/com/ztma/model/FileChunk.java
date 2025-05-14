package com.ztma.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "file_chunks")
public class FileChunk {

	@Id
	private String id;

	private String fileId;
	private int chunkIndex;

	private String encryptedChunk;
	private String signature;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
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

}
