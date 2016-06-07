package com.att.iamsampleapp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.att.api.error.InAppMessagingError;
import com.att.api.immn.service.ChangeType;
import com.att.api.immn.service.DeltaChange;
import com.att.api.immn.service.DeltaResponse;
import com.att.api.immn.service.IAMManager;
import com.att.api.immn.service.Message;
import com.att.api.immn.service.MessageIndexInfo;
import com.att.api.immn.service.MessageList;
import com.att.api.immn.service.MmsContent;
import com.att.api.oauth.OAuthService;
import com.att.api.oauth.OAuthToken;
import com.att.api.util.Preferences;
import com.att.api.util.TokenUpdatedListener;

public class ConversationList extends Activity {

	private static final String TAG = "Conversation List";

	private ListView messageListView;
	private MessageListAdapter adapter;
	private IAMManager iamManager;
	private final int NEW_MESSAGE = 2;
	private final int OAUTH_CODE = 1;
	private MessageIndexInfo msgIndexInfo;
	private DeltaResponse delta;
	private MessageList msgList;
	private ArrayList<Message> messageList;
	private String prevMailboxState;
	private String deleteMessageID;
	private int prevIndex;
	private ProgressDialog pDialog;
	
	private void GetUserConsentAuthCode() {
		// Read custom_param from Preferences
		String strStoredCustomParam = Config.customParam;
		Preferences prefs = new Preferences(getApplicationContext());
		if (prefs != null) {
			strStoredCustomParam = prefs.getString(TokenUpdatedListener.customParamSettingName, "");
			if (strStoredCustomParam.length() <= 0) {
				strStoredCustomParam = Config.customParam;
				prefs.setString(TokenUpdatedListener.customParamSettingName, strStoredCustomParam);
			}
		}
		Intent i = new Intent(this,
				com.att.api.consentactivity.UserConsentActivity.class);
		i.putExtra("fqdn", Config.fqdn);
		i.putExtra("clientId", Config.clientID);
		i.putExtra("clientSecret", Config.secretKey);
		i.putExtra("redirectUri", Config.redirectUri);
		i.putExtra("appScope", Config.appScope);
		i.putExtra("customParam", strStoredCustomParam);

		startActivityForResult(i, OAUTH_CODE);		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		OAuthToken savedToken = null;
		String strStoredToken = null;

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_conversation_list);
		showProgressDialog("Loading Messages .. ");
		messageListView = (ListView) findViewById(R.id.messageListViewItem);

		// Create service for requesting an OAuth token
		IAMManager.osrvc = new OAuthService(Config.fqdn, Config.clientID, Config.secretKey);
		
		Preferences prefs = new Preferences(getApplicationContext());
		if (prefs != null) {
			strStoredToken = prefs.getString(TokenUpdatedListener.accessTokenSettingName, "");
			if (strStoredToken.length() > 0) {
				savedToken = new OAuthToken(strStoredToken, 
						prefs.getLong(TokenUpdatedListener.tokenExpirySettingName, 0), 
						prefs.getString(TokenUpdatedListener.refreshTokenSettingName, ""), 0);
			}
		}
		
		// Initialize the AabManager also:
		IAMManager.SetApiFqdn(Config.fqdn);
		IAMManager.SetTokenUpdatedListener(new TokenUpdatedListener(getApplicationContext()));
		IAMManager.SetLowerTokenExpiryTimeTo(Config.lowerTokenExpiryTimeTo); // This step is optional.;
		
		//savedToken = null; // Set it to null due to some UI issue.
		
		if (savedToken == null) {	
			GetUserConsentAuthCode();
		} else {
			IAMManager.SetCurrentToken(savedToken);	
			Log.i("gotSavedToken", "Saved Token: " + TokenUpdatedListener.tokenDisplayString(savedToken.getAccessToken()));
			getMessageIndexInfo();	
		}
		setupMessageListListener();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == NEW_MESSAGE) {
			if (resultCode == RESULT_OK) {
				Utils.toastHere(getApplicationContext(), TAG, "Message Sent : "
						+ data.getStringExtra("MessageResponse"));				
			}
		} else if (requestCode == OAUTH_CODE) {
			String oAuthCode = null;
			if (resultCode == RESULT_OK) {
				oAuthCode = data.getStringExtra("oAuthCode");
				Log.i("mainActivity", "oAuthCode:" + oAuthCode);
				if (null != oAuthCode) {
					/*
					 * STEP 1: Getting the oAuthToken
					 * 
					 * Get the OAuthToken using the oAuthCode,obtained from the
					 * Authentication page The Success/failure will be handled
					 * by the listener : getTokenListener()
					 */
					IAMManager.osrvc.getOAuthToken(oAuthCode, new getTokenListener());
				} else {
					Log.i("mainActivity", "oAuthCode: is null");

				}
			} else if(resultCode == RESULT_CANCELED) {
				String errorMessage = null;
				if(null != data) {
					 errorMessage = data.getStringExtra("ErrorMessage");
				} else 
					errorMessage = getResources().getString(R.string.title_close_application);
				new AlertDialog.Builder(ConversationList.this)
				.setTitle("Error")
				.setMessage(errorMessage)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						finish();
					}
				}).show();				
			}
		} 
	}
	
	/*
	 * getTokenListener will be called on getting the response from
	 * osrvc.getOAuthToken(..)
	 * 
	 * onSuccess : This is called when the oAuthToken is available. The
	 * AccessToken is extracted from oAuthToken and stored in Config.token.
	 * authToken will then be used to get access to any of the twelve methods
	 * supported by InApp Messaging.
	 * 
	 * onError: This is called when the oAuthToken is not generated/incorrect
	 * APP_KEY/ APP_SECRET /APP_SCOPE / REDIRECT_URI The Error along with the
	 * error code is displayed to the user
	 */
	private class getTokenListener extends AttSdkSampleListener {

		public getTokenListener() {
			super("getTokenListener");
		}

		@Override
		public void onSuccess(Object response) {
			OAuthToken adjustedAuthToken = null;
			OAuthToken authToken = (OAuthToken) response;
			if (null != authToken) {
				if (IAMManager.GetLowerTokenExpiryTimeTo() >= 0) {
					adjustedAuthToken = new OAuthToken(authToken.getAccessToken(),
							IAMManager.GetLowerTokenExpiryTimeTo(),
							authToken.getRefreshToken(), (System.currentTimeMillis() / 1000));
				} else {
					adjustedAuthToken = authToken;
				}
				IAMManager.SetCurrentToken(adjustedAuthToken);
				TokenUpdatedListener.UpdateSavedToken(adjustedAuthToken); // Store the token in preferences
				Log.i("getTokenListener", "onSuccess Message : " + TokenUpdatedListener.tokenDisplayString(authToken.getAccessToken()));
				/*
				 * STEP 2: Getting the MessageIndexInfo
				 * 
				 * Message count, state and status of the index cache is
				 * obtained by calling getMessageIndexInfo The response will be
				 * handled by the listener : getMessageIndexInfoListener()
				 * 
				 */
				getMessageIndexInfo();	
	
			}
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			dismissProgressDialog();
			Utils.toastOnError(getApplicationContext(), error);
		}
	}

	/*
	 * This operation allows the developer to get the state, status and message
	 * count of the index cache for the subscriber's inbox. authToken will be
	 * used to get access to GetMessageIndexInfo of InApp Messaging.
	 * 
	 * The response will be handled by the listener :
	 * getMessageIndexInfoListener()
	 */
	public void getMessageIndexInfo() {

		iamManager = new IAMManager(new getMessageIndexInfoListener());
		iamManager.GetMessageIndexInfo();

	}

	/*
	 * getMessageIndexInfoListener will be called on getting the response from
	 * GetMessageIndexInfo()
	 * 
	 * onSuccess : This is called when the response : status,state and message
	 * count of the inbox is avaialble
	 * 
	 * onError: This is called when the msgIndexInfo returns null An index cache
	 * has to be created for the inbox by calling createMessageIndex The Error
	 * along with the error code is displayed to the user
	 */

	private class getMessageIndexInfoListener extends AttSdkSampleListener {

		public getMessageIndexInfoListener() {
			super("getMessageIndexInfo");
		}

		@Override
		public void onSuccess(Object response) {
			msgIndexInfo = (MessageIndexInfo) response;
			if (null != msgIndexInfo) {
				getMessageList();
				return;
			}
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			createMessageIndex();
			Utils.toastOnError(getApplicationContext(), error);
		}
	}

	/*
	 * This operation allows the developer to create an index cache for the
	 * subscriber's inbox. authToken will be used to get access to
	 * CreateMessageIndex of InApp Messaging.
	 * 
	 * The response will be handled by the listener :
	 * createMessageIndexListener()
	 */

	public void createMessageIndex() {
		iamManager = new IAMManager(new createMessageIndexListener());
		iamManager.CreateMessageIndex();
	}

	/*
	 * createMessageIndexListener will be called on getting the response from
	 * createMessageIndex()
	 * 
	 * onSuccess : This is called when the Index is created for the subscriber&#8217;s
	 * inbox. A list of messages will be fetched from the GetMessageList of
	 * InApp messaging.
	 * 
	 * onError: This is called when the index info is not created successfully
	 * The Error along with the error code is displayed to the user
	 */

	private class createMessageIndexListener extends AttSdkSampleListener {

		public createMessageIndexListener() {
			super("createMessageIndex");
		}

		@Override
		public void onSuccess(Object response) {
			Boolean msg = (Boolean) response;
			if (msg) {
				getMessageList();
			}
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			Utils.toastOnError(getApplicationContext(), error);
			dismissProgressDialog();
		}
	}

	/*
	 * The Application will request a block of messages from the AT&T Systems by
	 * providing count, limited to 500 and an offset value . authToken will be
	 * used to get access to GetMessageList of InApp Messaging.
	 * 
	 * The response will be handled by the listener : getMessageListListener()
	 */
	public void getMessageList() {
		iamManager = new IAMManager(new getMessageListListener());
		iamManager.GetMessageList(Config.messageLimit, Config.messageOffset);
	}

	/*
	 * getMessageListListener will be called on getting the response from
	 * GetMessageList(..)
	 * 
	 * onSuccess : This is called when the response returned is a MessageList A
	 * Listview is set with the response MessageList
	 * 
	 * onError: This is called when the response is incorrect The Error along
	 * with the error code is displayed to the user
	 */

	private class getMessageListListener extends AttSdkSampleListener {

		public getMessageListListener() {
			super("getMessageList");
		}

		@Override
		public void onSuccess(Object response) {
			
			msgList = (MessageList) response;
			prevMailboxState = msgList.getState();
			if (null != msgList && null != msgList.getMessages()
					&& msgList.getMessages().size() > 0) {
				messageList = msgList.getMessages();
				adapter = new MessageListAdapter(getApplicationContext(),
						messageList);
				messageListView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				dismissProgressDialog();
			}
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			dismissProgressDialog();
			Utils.toastOnError(getApplicationContext(), error);
		}

	}

	/*
	 * This request will check for updates by passing in a client state -
	 * prevMailboxState authToken will be used to get access to GetDelta of
	 * InApp Messaging.
	 * 
	 * The response will be handled by the listener : getDeltaListener()
	 */
	public void updateDelta() {

		if (msgList != null && msgList.getState() != null) {
			iamManager = new IAMManager(new getDeltaListener());
			iamManager.GetDelta(prevMailboxState);
		}
	}

	/*
	 * getDeltaListener will be called on getting the response from GetDelta(..)
	 * 
	 * onSuccess : If there is any update in the mailbox, messageList will be
	 * updated. The mailBox state is stored
	 * 
	 * onError: This is called when the response is incorrect The Error along
	 * with the error code is displayed to the user
	 */

	private class getDeltaListener extends AttSdkSampleListener {

		public getDeltaListener() {
			super("getDelta");
		}

		@Override
		public void onSuccess(Object response) {

			delta = (DeltaResponse) response;

			if (null != delta) {
				prevMailboxState = delta.getState();
				updateMessageList(delta);
			} else {
				dismissProgressDialog();
			}
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			dismissProgressDialog();
			Utils.toastOnError(getApplicationContext(), error);
		}
	}

	/*
	 * This will allow to update the flags associated with the collection of
	 * messages Any number of messages can be passed authToken will be used to
	 * get access to UpdateMessages of InApp Messaging.
	 * 
	 * The response will be handled by the listener :
	 * updateMessageStatusListener()
	 */

	public void updateMessageStatus(DeltaChange[] statusChange) {
		iamManager = new IAMManager(new updateMessageStatusListener());
		iamManager.UpdateMessages(statusChange);

	}

	/*
	 * updateMessageStatusListener will be called on getting the response from
	 * UpdateMessages(..)
	 * 
	 * onSuccess : If there is any update, delete / get the message
	 * 
	 * onError: This is called when the response is false The Error along with
	 * the error code is displayed to the user
	 */

	private class updateMessageStatusListener extends AttSdkSampleListener {

		public updateMessageStatusListener() {
			super("updateMessageStatus");
		}

		@Override
		public void onSuccess(Object response) {

			Boolean msg = (Boolean) response;
			if (msg) {
				deleteMessageFromList(deleteMessageID);
				iamManager = new IAMManager(new getMessageListener());
				iamManager.GetMessage(deleteMessageID);
				deleteMessageID = null;
			}

		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			Utils.toastOnError(getApplicationContext(), error);
		}
	}

	/*
	 * The messageId will be passed to get the message associated with that ID
	 * This will get a single message from the message inbox. authToken will be
	 * used to get access to GetMessage of InApp Messaging.
	 * 
	 * The response will be handled by the listener : getMessageListener()
	 */
	public void getMessage(String messageID) {
		iamManager = new IAMManager(new getMessageListener());
		iamManager.GetMessage(messageID);
	}

	/*
	 * getMessageListener will be called on getting the response from
	 * GetMessage(..)
	 * 
	 * onSuccess : Fetches the message with the specified ID Sets the
	 * messageListView adapter
	 * 
	 * onError: This is called when the response is incorrect The Error along
	 * with the error code is displayed to the user
	 */

	private class getMessageListener extends AttSdkSampleListener {

		public getMessageListener() {
			super("getMessage");
		}

		@Override
		public void onSuccess(Object arg0) {

			Message msg = (Message) arg0;
			if (null != msg) {

				messageList.add(prevIndex, msg);
				prevIndex = 0;
				adapter = new MessageListAdapter(getApplicationContext(),
						messageList);
				Parcelable state = messageListView.onSaveInstanceState();
				messageListView.setAdapter(adapter);
				messageListView.onRestoreInstanceState(state);
				adapter.notifyDataSetChanged();
				dismissProgressDialog();
			}
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			dismissProgressDialog();
			Utils.toastOnError(getApplicationContext(), error);
		}

	}

	/*
	 * The message passed will be deleted from the message inbox authToken will
	 * be used to get access to DeleteMessage(..) of InApp Messaging.
	 * 
	 * The response will be handled by the listener : deleteMessagesListener()
	 */

	public void deleteMessage(Message msg) {

		deleteMessageID = msg.getMessageId();
		iamManager = new IAMManager(new deleteMessagesListener());
		iamManager.DeleteMessage(deleteMessageID);
	}

	/*
	 * deleteMessagesListener will be called on getting the response from
	 * DeleteMessage(..)
	 * 
	 * onSuccess : deletes the specified message
	 * 
	 * onError: This is called when the response is incorrect The Error along
	 * with the error code is displayed to the user
	 */

	private class deleteMessagesListener extends AttSdkSampleListener {

		public deleteMessagesListener() {
			super("deleteMessages");
		}

		@Override
		public void onSuccess(Object response) {

			Boolean msg = (Boolean) response;
			if (msg) {
				deleteMessageFromList(deleteMessageID);
				deleteMessageID = null;
			}
			dismissProgressDialog();
		}

		@Override
		public void onError(InAppMessagingError error) {
			super.onError(error);
			Utils.toastOnError(getApplicationContext(), error);
			dismissProgressDialog();
		}
	}

	public void onResume() {
		super.onResume();
		updateDelta();
	}

	// MessageList Listener
	public void setupMessageListListener() {

		messageListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(((Message) messageListView.getItemAtPosition(position)).isUnread()) {
					DeltaChange[] statusChange = new DeltaChange[1];
					statusChange[0] = new DeltaChange(((Message) messageListView.getItemAtPosition(position)).getMessageId(),
													  ((Message) messageListView.getItemAtPosition(position)).isFavorite(), false);
					deleteMessageID = ((Message) messageListView.getItemAtPosition(position)).getMessageId();
					updateMessageStatus(statusChange);
				}
				
								
				if (((Message) messageListView.getItemAtPosition(position))
						.getType().equalsIgnoreCase("MMS")) {
					Message mmsMessage = (Message) messageListView
							.getItemAtPosition(position);
					ArrayList<MmsContent> mmsContent = mmsMessage
							.getMmsContents();
					Log.d(TAG, "MMS Attachments : " + mmsContent.size());

					String[] mmsContentName = new String[mmsContent.size()], mmsContentType = new String[mmsContent
							.size()], mmsContentUrl = new String[mmsContent
							.size()];

					for (int n = 0; n < mmsContent.size(); n++) {
						MmsContent tmpMmsContent = mmsContent.get(n);
						mmsContentName[n] = tmpMmsContent.getContentName();
						mmsContentType[n] = tmpMmsContent.getContentType();
						mmsContentUrl[n] = tmpMmsContent.getContentUrl();
					}

					/*
					 * STEP 5: getting the contents of the message
					 * 
					 * Returns the contents associated with the identifier
					 * provided in the request.
					 */

					Intent i = new Intent(ConversationList.this,
							MMSContent.class);
					i.putExtra("MMSContentName", mmsContentName);
					i.putExtra("MMSContentType", mmsContentType);
					i.putExtra("MMSContentUrl", mmsContentUrl);
					startActivity(i);

				} else {
					infoDialog((Message) messageListView
							.getItemAtPosition(position));
				}
			}
		});
		
		messageListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					public boolean onItemLongClick(AdapterView<?> arg0, View v,
							int position, long arg3) {

						Message msg = (Message) messageListView
								.getItemAtPosition(position);

						CharSequence popUpList[] = new CharSequence[] {
								"Delete Message", "Add to favorites",
								"Mark as Unread" };
						if (msg.isFavorite())
							popUpList[1] = "Remove favorite";
						if (msg.isUnread())
							popUpList[2] = "Mark as Read";

						popUpActionList(popUpList, msg, position);

						return true;
					}
				});
	}

	public void infoDialog(Message selMessage) {
		
		String date = Utils.getDate(selMessage.getTimeStamp().replace('T', ' '));
		
		new AlertDialog.Builder(ConversationList.this)
				.setTitle("Message details")
				.setMessage(
						"Type : " + selMessage.getType() + "\n" + "From : "
								+ selMessage.getFrom() + "\n" + "Received : "
								+ date)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();					
					}
				}).show();
	}

	public void popUpActionList(final CharSequence popUpList[],
			final Message msg, int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Message Options");
		builder.setItems(popUpList, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DeltaChange[] statusChange = new DeltaChange[1];
				// the user clicked on colors[which]
				switch (which) {
				case 0:
					deleteMessage(msg);
					showProgressDialog("Deleting Message");
					break;

				case 1: {
					if (popUpList[1].toString().equalsIgnoreCase(
							"Add to favorites")) {
						// Set message as favorite
						statusChange[0] = new DeltaChange(msg.getMessageId(),
								true, msg.isUnread());
						showProgressDialog("Adding to favorites");
					} else {
						// Remove favorite
						statusChange[0] = new DeltaChange(msg.getMessageId(),
								false, msg.isUnread());
						showProgressDialog("Removing favorites");
					}
					deleteMessageID = msg.getMessageId();
					updateMessageStatus(statusChange);
				}
					break;

				case 2: {
					if (popUpList[2].toString().equalsIgnoreCase(
							"Mark as Unread")) {
						// Set as unread
						statusChange[0] = new DeltaChange(msg.getMessageId(),
								msg.isFavorite(), true);
						showProgressDialog("Marking as unread ...");
					} else {
						// Mark as read
						statusChange[0] = new DeltaChange(msg.getMessageId(),
								msg.isFavorite(), false);
						showProgressDialog("Marking as read ...");
					}
					deleteMessageID = msg.getMessageId();
					updateMessageStatus(statusChange);
				}
					break;

				default:
					break;
				}
			}
		});
		builder.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_conversation_list, menu);

		return super.onCreateOptionsMenu(menu);
	}

	private boolean ProcessMenuCommand(int menuItemId) {
		// Take appropriate action for each action item click
		switch (menuItemId) {

		case R.id.action_new_message: {
			/*
			 * STEP 4: creating a new message
			 * 
			 * A new message will be created and sent to the recipient mentioned
			 * in the TO list
			 */

			startActivityForResult(new Intent(ConversationList.this,
					NewMessage.class), NEW_MESSAGE);
			break;
		}
		case R.id.action_logout: {
			ConversationList.RevokeToken("refresh_token");					
			Preferences prefs = new Preferences(getApplicationContext());		
			prefs.setString(TokenUpdatedListener.accessTokenSettingName,"");  
			prefs.setString(TokenUpdatedListener.refreshTokenSettingName,"");  
			finish();
			break;
		}
		case R.id.action_refresh: {
			updateDelta();
			break;
		}
		
		case R.id.action_debug_settings:
	   	 	startActivity(new Intent(getApplicationContext(), 
	   	 			DebugSettingsPage.class));
			break;

		default:
			return false;
		}
		return true;		
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (ProcessMenuCommand(item.getItemId())) {
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (ProcessMenuCommand(item.getItemId())) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Progress Dialog
	public void showProgressDialog(String dialogMessage) {

		if (null == pDialog)
			pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		pDialog.setMessage(dialogMessage);
		pDialog.show();
	}

	public void dismissProgressDialog() {
		if (null != pDialog) {
			pDialog.dismiss();
		}
	}

	public void onMessageListReady(MessageList messageList) {

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	public void clearCache() {
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}

	public void updateMessageList(DeltaResponse deltaResponse) {

		int nChanges = deltaResponse.getDeltaChanges().length;

		String messageID[] = new String[nChanges];

		for (int n = 0; n < nChanges; n++) {
			messageID[n] = deltaResponse.getDeltaChanges()[n].getMessageId();

			ChangeType chType = deltaResponse.getDeltaChanges()[n]
					.getChangeType();

			switch (chType) {
			case ADD: {
				iamManager = new IAMManager(new getMessageListener());
				iamManager.GetMessage(messageID[n]);
			}
				break;
			case DELETE: {
				deleteMessageFromList(deltaResponse.getDeltaChanges()[n]
						.getMessageId());
			}
				break;
			case NONE:
				break;
			case UPDATE: {

				deleteMessageFromList(deltaResponse.getDeltaChanges()[n]
						.getMessageId());
				adapter.notifyDataSetChanged();
				iamManager = new IAMManager(new getMessageListener());
				iamManager.GetMessage(messageID[n]);
			}
				break;
			default:
				dismissProgressDialog();
				break;
			}
		}
	}

	public void deleteMessageFromList(String msgID) {

		int deleteNthMessage;
		for (deleteNthMessage = 0; deleteNthMessage < messageList.size(); deleteNthMessage++) {
			if (messageList.get(deleteNthMessage).getMessageId()
					.equalsIgnoreCase(msgID))
				break;
		}
		if (deleteNthMessage < messageList.size()) {
			prevIndex = deleteNthMessage;
			adapter.deleteItem(deleteNthMessage);
			adapter.notifyDataSetChanged();
		}
		dismissProgressDialog();
	}		
	
	public static void RevokeToken(final String hint) {
		class revokeTokenListener extends AttSdkSampleListener {		
			public revokeTokenListener() {
				super("revokeToken");
			}
			@Override
			public void onSuccess(Object response) {
				Log.i("revokeTokenListener", hint + " was successfully revoked.");
			}

			@Override
			public void onError(InAppMessagingError error) {
				Log.i("revokeTokenListener", "Error:"+ hint + " revocation failed. " + error.getHttpResponse());
			}
		}
		
		IAMManager iamManager = new IAMManager(new revokeTokenListener());
		if (hint.equalsIgnoreCase("access_token")) {
			iamManager.RevokeAccessToken();
		} else {
			iamManager.RevokeToken(hint);		
		}
	}
}
