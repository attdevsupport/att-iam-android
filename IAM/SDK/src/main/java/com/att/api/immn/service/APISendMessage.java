package com.att.api.immn.service;

import java.text.ParseException;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Handler;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APISendMessage implements ATTIAMListener {

	SendMessageParams sendMessageParams = null;
	String address = null;
	String[] addresses = null;
	String message = null;
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();


	public APISendMessage(String address, String message,
			IAMManager iamMgr, ATTIAMListener iamListener) {

		this.address = address;
		this.message = message;
		this.iamListener = iamListener;
		this.iamManager = iamMgr;
	}
	
	public APISendMessage(String[] addresses, String message, String subject, boolean group, String[] attachments,
						  IAMManager iamMgr, ATTIAMListener iamListener) {
		
		sendMessageParams = new SendMessageParams(addresses, message, group, attachments, subject);
		this.iamListener = iamListener;
		this.iamManager = iamMgr;
	}
	
	public void SendMessage() {

		SendMessageTask sendMessageTask = new SendMessageTask();
		sendMessageTask.execute(sendMessageParams);
	}
	
	
	

	public class SendMessageTask extends AsyncTask<SendMessageParams, Void, SendResponse> {

		@Override
		protected SendResponse doInBackground(SendMessageParams... params) {
			SendResponse sendMessageResponse = null;
			InAppMessagingError errorObj = new InAppMessagingError();

			try {
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return null;
				sendMessageResponse = IAMManager.immnSrvc.sendMessage(params[0].getAddresses(),
														   params[0].getMessage(),
														   params[0].getSubject(),
														   params[0].getGroup(),
														   params[0].getAttachments());
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
				//Log.i("APISendMessage", e.getErrorMessage());
				onError( errorObj );
			} catch (JSONException e) {
				//errorObj.setErrorMessage(e.getMessage());
				errorObj = new InAppMessagingError(e.getMessage());
				onError(errorObj);			
			} catch (ParseException e) {
				//errorObj.setErrorMessage(e.getMessage());
				errorObj = new InAppMessagingError(e.getMessage());
				onError(errorObj);		
			}
			return sendMessageResponse;
		}

		@Override
		protected void onPostExecute(SendResponse sendMessageResponse) {

			super.onPostExecute(sendMessageResponse);
			if( null != sendMessageResponse) {
				onSuccess((SendResponse) sendMessageResponse);
			} 
		}
	}

	@Override
	public void onSuccess(final Object sendMessageResponse) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (null != iamListener) {
					iamListener.onSuccess((SendResponse) sendMessageResponse);
				}
			}
		});
	}

	@Override
	public void onError(final InAppMessagingError error) {
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (null != iamListener) {
					iamListener.onError(error);
				}
			}
		});
		
	}
}
