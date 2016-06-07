package com.att.api.immn.listener;

import com.att.api.oauth.OAuthToken;

public interface AttSdkTokenUpdater {	
	public void onTokenUpdate(OAuthToken newToken);
	public void onTokenDelete();
}