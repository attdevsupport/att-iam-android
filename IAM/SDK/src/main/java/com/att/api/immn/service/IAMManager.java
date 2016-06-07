package com.att.api.immn.service;

import android.os.AsyncTask;
import android.util.Log;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.immn.listener.AttSdkTokenUpdater;
import com.att.api.oauth.OAuthService;
import com.att.api.oauth.OAuthToken;
import com.att.api.rest.RESTException;
import com.att.api.util.TokenUpdatedListener;
/**
 * This class encapsulates the AT&amp;T REST APIs for In-App Messaging.
 * 
 * @author dg185p
 * @author ps350r
 * @author sm095n
 * 
 */
public class IAMManager {

	public static IMMNService immnSrvc = null;
	public static OAuthService osrvc = null;
	private ATTIAMListener iamListener;
	private static AttSdkTokenUpdater tokenListener = null;
	private static OAuthToken currentToken = null; // NOTE: This variable may not be required. Just immnSrvc can be used.
	private final static Object lockRefreshToken = new Object();
	private static String apiFqdn = "https://api.att.com";
	// if lowerTokenExpiryTimeTo >= 0, over rides token expiry to this value
	private static long lowerTokenExpiryTimeTo = -1; 

	
	/**
	 * Creates an IAMManager object.
	 * @param iamListener - Specifies the Listener for callbacks.
	 */
	public IAMManager(ATTIAMListener iamListener) {
		this(null, iamListener);
	}
	
	/**
	 * Creates an IAMManager object and initializes the saved access token.
	 * @param token (optional) - Can be passed as null. Overrides currentToken.
	 * @param iamListener - Specifies the Listener for callbacks.
	 */
	public IAMManager(OAuthToken token, ATTIAMListener iamListener) {
		if (token != null) {
			currentToken = token;
		}
		if (immnSrvc == null && currentToken != null) {
			immnSrvc = new IMMNService(apiFqdn, currentToken);
		}
		this.iamListener = iamListener;
	}

	/**
	 * The getMessages method gets a message based on its message Id. 
	 * 
	 * @param msgId - Specifies the message identifier of a subscriber message in the AT&amp;T
	 * Messages environment.
	 * 
	 */
	public void GetMessage(String msgId) {
		APIGetMessage getMessage = new APIGetMessage(msgId, this, iamListener);
		getMessage.GetMessage(msgId);
	}
	
	/**
	 * 
	 *  The SendMesage method sends an MMS or SMS message. 
	 * 
	 * @param addresses - Specifies the addresses where the message is sent. The addresses must be specified 
	 * in one of the following formats (at least one message must be specified): 
	 * <ul>
	 * <li> MSISDN: This format is the mobile number based on North American Numbering Plan with a maximum 
	 * length of 11 digits. It must be preceded by the following prefix: tel: 
	 * <li>Valid formats are: 
	 * 		<ul>
	 * 		<li> tel:+12012345678
	 * 		<li> tel:12012345678 
	 * 		<li> tel:2012345678
	 * 		</ul>
	 * International numbers are not supported.
	 * <li> Short code: This format is a special number between 3-8 digits long. It must be preceded by the 
	 * following prefix: short: 
	 * Valid formats are:  
	 * 		<ul>
	 * 		<li> short:123 
	 * 		<li> short:12345678
	 * 		</ul>
	 * <li> Email address: This format is the standard email address format. Validation of the address must be 
	 * performed. A maximum of 10 addresses is supported. However, this limit can be configurable at a System level.
	 * If any of the email addresses are duplicated, the message is sent to that address only once.
	 * </ul>
	 *
	 * @param message - The message to be sent.
	 * <ul>
	 * <li> If the request is detected to be an MMS message, then the following character sets are supported:
	 * 		<ul>
	 * 		<li> ASCII  
	 * 		<li> UTF-8  
	 * 		<li> UTF-16 
	 * 		<li> ISO-8859-1
	 * 		</ul>
	 * <li> If the request is detected to be an SMS message, then the following character set is supported:  ISO-8859-1
	 * The message parameter is required if no attachments are specified.
	 * </ul>
	 * 
	 * @param subject - Specifies the header for the message.
	 * @param group	- If True, indicates the message is sent to multiple recipients. If False, indicates that the message 
	 * is a broadcast mesage.
	 * @param attachments - Specifies the filenames of attachments associated with the message.
	 * 
	 */
	
	public void SendMessage(String[] addresses, String message, String subject, boolean group, String[] attachments) {
		APISendMessage sendMessage = new APISendMessage(addresses, message, subject, group, attachments, 
														this, iamListener);
		sendMessage.SendMessage();
	}

	
	/**
	 * The GetMessageContent method gets a message attachment based on the attachment 
	 * and message identifier. 
	 * 
	 * @param msgId - Specifies the identifier of a subscriber message in the AT&amp;T Messages environment.
	 * @param partNumber - Specifies the content identifier of the attachment to be retrieved.
	 * 
	 */
	public void GetMessageContent(String msgId, String partNumber) {
		APIGetMessageContent getMessageContent = new APIGetMessageContent(msgId, partNumber, this, iamListener);
		getMessageContent.GetMessageContent();	
	}
	
	/**
	 * The GetMessageList method gets a block of messages based on an offset value and the number 
	 * of messages to retrieve. The list of messages is returned in the order that they were received, starting 
	 * with the most recent. 
	 * 
	 * @param limit - Specifies the number of messages to return. A maximum value of 500 is supported.
	 * @param offset - Specifies the offset from the beginning of the ordered set of messages.
	 * 
	 */
	public void GetMessageList(int limit, int offset) {
		APIGetMessageList getMessageList = new APIGetMessageList(limit, offset, this, iamListener);
		getMessageList.GetMessageList();
	}
	
	/**
	 * The GetDelta method checks to see if the client is in a specific state.
	 * 
	 * 
	 * @param state - Specifies the state of the client. This string is returned by either the 
	 * GetMessageIndex or GetMessageList method.
	 * 
	 */
	public void GetDelta(String state) {
		APIGetDelta getDelta = new APIGetDelta(state, this,iamListener);
		getDelta.GetDelta();
	}
	
	/**
	 * The GetMessageIndexInfo method gets the state, status, and message count of the index cache for the 
	 * inbox of the subscriber. 
	 * 
	 */
	public void GetMessageIndexInfo() {
		APIGetMessageIndexInfo getMessageIndexInfo = new APIGetMessageIndexInfo(this, iamListener);
		getMessageIndexInfo.GetMessageIndexInfo();
	}
	
	/**
	 * The CreateMessageIndex method creates an index cache for the inbox of the subscriber.
	 * This method must be called before any of the other operations are used. 
	 * In addition, if a message index is inactive for 30 or more days, then the index cache
	 * must be recreated.
	 * 
	 */
	public void CreateMessageIndex() {		
		APICreateMessageIndex createMessageIndex = new APICreateMessageIndex(this, iamListener);
		createMessageIndex.CreateMessageIndex();	
	}
	
	/**
	 * The DeleteMessage method deletes a specific message from an inbox.
	 * 
	 * @param msgId - Specifies the Id of the message to be deleted.
	 * 
	 */
	public void DeleteMessage(String msgId) {		
		APIDeleteMessage deleteMessage = new APIDeleteMessage(msgId, this, iamListener);
		deleteMessage.DeleteMessage();		
	}
	/**
	 * The DeleteMessages method deletes multiple messages from an inbox. The messagee identifiers are 
	 * specified in the query string in the request. 
	 * 
	 * @param msgIds - Specifies a comma delimited list of message identifiers.
	 *  
	 */
	public void DeleteMessages(String[] msgIds) {
		APIDeleteMessages deleteMessages = new APIDeleteMessages(msgIds, this, iamListener);
		deleteMessages.DeleteMessages();
	}
	
	/**
	 * The UpdateMessages method updates the flags that are associated with multiple messages. 
	 * Any number of messages can be updated. 
	 * 
	 * @param messages - Specifies the messages to be updated and the flags to be updated.
	 * 
	 */
	public void UpdateMessages(DeltaChange[] messages) {
		APIUpdateMessages updateMessages = new APIUpdateMessages(messages, this, iamListener);
		updateMessages.UpdateMessages();
	}
	/**
	 * The UpdateMessage method updates the flags of a single message. 
	 * 
	 * @param msgId - Specifies the identifier of the message to be updated.
	 * @param isUnread - (Optional) Indicates whether the message has (True) or has not (False) been read.
	 * @param isFavorite - (Optional) Indicates whether the message is (True) or is not (False) a favorite.
	 * 
	 */
	public void UpdateMessage(String msgId, Boolean isUnread, Boolean isFavorite) {
		APIUpdateMessage updateMessage = new APIUpdateMessage();
		APIUpdateMessage.APIUpdateMessageParams params = 
					updateMessage.new APIUpdateMessageParams(msgId, isUnread, isFavorite );

		updateMessage.set(params, this, iamListener);
		updateMessage.UpdateMessage();
	}
	
	/**
	 * The SetCurrentToken method updates the current access token used for the subsequent API calls.
	 * @param token - Overrides the default OAuth token used for authorization.
	 */	
	public static void SetCurrentToken(OAuthToken token) {
		currentToken = token;
		immnSrvc = new IMMNService(apiFqdn, token);
	}
	
	/**
	 * The SetLowerTokenExpiryTimeTo method updates the current value for the token expiry override time.
	 * @param value - Override expiry time in seconds.
	 */	
	public static void SetLowerTokenExpiryTimeTo (long value) {
		lowerTokenExpiryTimeTo = value;
	}
	
	/**
	 * The GetLowerTokenExpiryTimeTo method returns the current expiry time override value.
	 *
	 * @return the current expiry time override value
	 */	
	public static long GetLowerTokenExpiryTimeTo () {
		return lowerTokenExpiryTimeTo;
	}
	
	/**
	 * The SetTokenUpdatedListener method updates the listener to call back when the access token is updated.
	 * @param listener - AttSdkTokenUpdater object.
	 */	
	public static void SetTokenUpdatedListener(AttSdkTokenUpdater listener) {
		tokenListener = listener;
	}
	
	/**
	 * The isCurrentTokenExpired method checks if the current access token is expired.
	 *
	 * @return true is the token is expired
	 */	
	public static Boolean isCurrentTokenExpired() {
		return (currentToken.getAccessTokenExpiry() < (System.currentTimeMillis() / 1000));		
	}
	
	/**
	 * The SetApiFqdn method updates the FQDN of the AT&amp;T API end point.
	 * @param fqdn - fully qualified domain name e.g. https://api.att.com
	 */	
	public static void SetApiFqdn(String fqdn) {
		apiFqdn = fqdn;
	}
	
	/**
	 * The CheckAndRefreshExpiredTokenAsync method automatically updates the current token using the RefreshToken.
	 *
	 * @return true if the current auth token is valid
	 */	
	public Boolean CheckAndRefreshExpiredTokenAsync() {
		try {
			OAuthToken authToken = null;
			OAuthToken adjustedAuthToken = null;
			synchronized (lockRefreshToken) {
				if (isCurrentTokenExpired()) {
					String refreshTokenValue = currentToken.getRefreshToken();
					currentToken = null;
					InAppMessagingError errorObj = new InAppMessagingError();
					try {
						if (osrvc == null) throw new Exception("Failed during token refresh. osrvc not initiazed.");
						authToken = osrvc.refreshToken(refreshTokenValue);
						if (authToken != null) {
							if (lowerTokenExpiryTimeTo >= 0) {
								adjustedAuthToken = new OAuthToken(authToken.getAccessToken(), lowerTokenExpiryTimeTo,
										authToken.getRefreshToken(), (System.currentTimeMillis() / 1000));
							} else {
								adjustedAuthToken = authToken;
							}
							SetCurrentToken(adjustedAuthToken);
							Log.i("getRefreshTokenListener",
									"onSuccess Message : " + 
									TokenUpdatedListener.tokenDisplayString(adjustedAuthToken.getAccessToken()));
							if (tokenListener != null) {
								tokenListener.onTokenUpdate(adjustedAuthToken);
							}
						} else {
							throw new Exception("Failed during token refresh.");
						}
					} catch (RESTException e) {
						Log.i("getRefreshTokenListener", "REST Error:" + e.getMessage());
						errorObj = Utils.CreateErrorObjectFromException( e );
					} catch (Exception e) {
						Log.i("getRefreshTokenListener", "Error:" + e.getMessage());
					}
					if (currentToken == null) {
						if (iamListener != null) {
							iamListener.onError(errorObj);
						}						
						if (tokenListener != null) {
							tokenListener.onTokenDelete();
						}			
					}
				}
			}
		} catch (Exception /*InterruptedException*/ e) {
			currentToken = null;
		}	
		return (currentToken != null);
	}
	
    /**
     * The RevokeToken method revokes the current token.
     * 
     * @param hint a hint for the type of token to revoke
     *
     */
    public void RevokeToken(String hint) {
		RevokeTokenTask task = new RevokeTokenTask();
		if (hint.equalsIgnoreCase("access_token")) {
			task.execute(currentToken.getAccessToken(), hint);
		} else if (hint.equalsIgnoreCase("refresh_token")) {
			task.execute(currentToken.getRefreshToken(), hint);			
		} else {
			if (null != iamListener) {
				iamListener.onError(new InAppMessagingError("Invalid token hint passed to the RevokeToken method."));
			}			
		}
    }
    
    /**
     * The RevokeAccessToken method revokes the access token.
     *
     */
    public void RevokeAccessToken() {
    	this.RevokeToken("access_token");    	
    }

	public class RevokeTokenTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String result = null;
			InAppMessagingError errorObj = new InAppMessagingError("RevokeToken error.");
			try {
				osrvc.revokeToken(params[0], params[1]);
				result = "Success";
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
				if (null != iamListener) {
					iamListener.onError(errorObj);
				}
			}		
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(null != result) {
				if (null != iamListener) {
					iamListener.onSuccess(result);
				}
			}
		}
    	
    }
}
