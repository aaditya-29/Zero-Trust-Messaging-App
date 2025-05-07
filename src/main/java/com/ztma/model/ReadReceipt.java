package com.ztma.model;

import lombok.Data;

@Data
public class ReadReceipt {
    private String sender; 
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
	private String receiver; 
}
