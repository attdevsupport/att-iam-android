package com.att.api.immn.service;

public class SendMessageParams {
	
	private String[] addresses = null;
	private String message = null;
	private Boolean group = false;
	private String[] attachments = null;
	private String subject = null;
	
	public SendMessageParams(String[] addresses, String message, Boolean group,
			String[] attachments, String subject) {
		super();
		this.addresses = addresses;
		this.message = message;
		this.group = group;
		this.attachments = attachments;
		this.subject = subject;
	}
	
	public String[] getAddresses() {
		return addresses;
	}
	public void setAddresses(String[] addresses) {
		this.addresses = addresses;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Boolean getGroup() {
		return group;
	}
	public void setGroup(Boolean group) {
		this.group = group;
	}
	public String[] getAttachments() {
		return attachments;
	}
	public void setAttachments(String[] attachments) {
		this.attachments = attachments;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

}
