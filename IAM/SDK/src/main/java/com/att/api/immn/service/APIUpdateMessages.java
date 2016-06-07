package com.att.api.immn.service;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Handler;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APIUpdateMessages implements ATTIAMListener {
	
	DeltaChange[] messages = null;
	Boolean isSuccesful = false;
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();
	
	public APIUpdateMessages(DeltaChange[] messages, IAMManager iamMgr, ATTIAMListener iamListener) {
		this.messages = messages;
		this.iamManager = iamMgr;
		this.iamListener = iamListener;	
	}
	
	public void UpdateMessages() {
		APIUpdateMessagesTask updateMessagestask = new APIUpdateMessagesTask();
		updateMessagestask.execute(messages);
	}

	public class APIUpdateMessagesTask extends AsyncTask<DeltaChange, Void, Boolean> {

		@Override
		protected Boolean doInBackground(DeltaChange... messages) {
			InAppMessagingError errorObj = new InAppMessagingError();

			// TODO Auto-generated method stub
			try {
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return false;
				IAMManager.immnSrvc.updateMessages(messages);
				isSuccesful = true;
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
				onError( errorObj );
			} catch (JSONException e) {
				errorObj = new InAppMessagingError(e.getMessage());
				onError(errorObj);			
			}
			return isSuccesful;
		}

		@Override
		protected void onPostExecute(Boolean isSuccesful) {
			// TODO Auto-generated method stub
			super.onPostExecute(isSuccesful);
			if(isSuccesful) {
				onSuccess((Boolean) isSuccesful);
			}
			
		}
		
	}
	
	@Override
	public void onSuccess(final Object isSuccesful) {
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		    	 if(null != iamListener) { 
					iamListener.onSuccess((Boolean) isSuccesful);
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
