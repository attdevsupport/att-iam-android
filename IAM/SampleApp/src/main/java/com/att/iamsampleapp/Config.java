package com.att.iamsampleapp;

public class Config {
	
	public static final String clientID = COPY_YOUR_CLIENT_ID_HERE;
	public static final String secretKey = COPY_YOUR_SECRET_KEY_HERE;
	public static final String redirectUri = COPY_YOUR_REDIRECT_URI_HERE;
	public static final String appScope	 			= 	 "IMMN,MIM";
	public static final String fqdn		 			= 	 "https://api.att.com";

	public static final String iamDownloadDirectory = "InAppMessagingDownloads";	
	public static final int messageLimit 			= 	500;
	public static final int messageOffset 			= 	0;
	public static final int maxRecipients 			= 	10;
	public static final int	maxAttachments			= 	21;
	
	// Testers can test the customParam functionality by setting this config value to following combinations
	// "" - default behavior - do not send any custom_param
	// "bypass_onnetwork_auth"
	// "suppress_landing_page"
	// "bypass_onnetwork_auth,suppress_landing_page"
	public static final String customParam = "";
	// lowerTokenExpiryTimeTo parameter can be used to developer to lower access token expiry time
	// keep it as -1, if you do not want to override the expiration value returned by AT&T's API
	public static final long lowerTokenExpiryTimeTo = -1;
}