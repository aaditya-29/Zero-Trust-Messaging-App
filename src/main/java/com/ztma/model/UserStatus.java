package com.ztma.model;

import lombok.Data;

@Data
public class UserStatus {
	private String userId;
	private String status; // ONLINE, OFFLINE

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
