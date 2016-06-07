package com.att.api.immn.service;

public class DeltaChange {

	private final String messageId;
    private final Boolean isFavorite;
    private final Boolean isUnread;
    private final String type;
    private final ChangeType changeType;
    
	public DeltaChange(String messageId, Boolean isFavorite, Boolean isUnread,
			String type, ChangeType changeType) {
		super();
		this.messageId = messageId;
		this.isFavorite = isFavorite;
		this.isUnread = isUnread;
		this.type = type;
		this.changeType = changeType;
	}
	
	public DeltaChange(String messageId, Boolean isFavorite, Boolean isUnread ) {
		super();
		this.messageId = messageId;
		this.isFavorite = isFavorite;
		this.isUnread = isUnread;
		this.type = "";
		this.changeType = ChangeType.NONE;
	}

	public String getMessageId() {
		return messageId;
	}

	public Boolean isFavorite() {
		return isFavorite;
	}

	public Boolean isUnread() {
		return isUnread;
	}

	public String getType() {
		return type;
	}

	public ChangeType getChangeType() {
		return changeType;
	}
    
}
