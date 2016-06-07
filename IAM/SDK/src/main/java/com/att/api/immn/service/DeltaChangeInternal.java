package com.att.api.immn.service;

import org.json.JSONException;
import org.json.JSONObject;

public final class DeltaChangeInternal {
    private final String messageId;
    private final Boolean isFavorite;
    private final Boolean isUnread;
   
    public DeltaChangeInternal(String messageId, Boolean isFavorite, Boolean isUnread) {
        this.messageId = messageId;
        this.isFavorite = isFavorite;
        this.isUnread = isUnread;
    }

    public String getMessageId() {
        return messageId;
    }

    // alias for isFavorite
    public Boolean getFavorite() {
        return isFavorite;
    }

    public Boolean isFavorite() {
        return isFavorite;
    }

    // alias for isUnread
    public Boolean getUnread() {
        return isUnread;
    }

    public Boolean isUnread() {
        return isUnread;
    }

    public static DeltaChangeInternal valueOf(JSONObject jobj) {
        String msgId = null;
		try {
			msgId = jobj.getString("messageId");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Boolean isFavorite = null;
		try {
			isFavorite = jobj.getBoolean("isFavorite");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Boolean isUnread = null;
		try {
			isUnread = jobj.getBoolean("isUnread");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return new DeltaChangeInternal(msgId, isFavorite, isUnread);
    }
}
