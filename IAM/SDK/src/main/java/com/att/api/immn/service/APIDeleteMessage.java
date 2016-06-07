package com.att.api.immn.service;

import android.os.AsyncTask;
import android.os.Handler;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APIDeleteMessage implements ATTIAMListener {
	
	Boolean isSuccessful = false;
	String msgId = null;
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();
	
	public  APIDeleteMessage(String msgId, IAMManager iamMgr, ATTIAMListener iamListener) {
		
		this.msgId = msgId;
		this.iamManager = iamMgr;
		this.iamListener = iamListener;
	}
	public void DeleteMessage() {
		
		DeleteMessageTask deleteMessageTask = new DeleteMessageTask();
		deleteMessageTask.execute(msgId);		
	}
	
	public class DeleteMessageTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			Boolean isSuccessful = false;
			InAppMessagingError errorObj = new InAppMessagingError();

			try {
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return false;
				IAMManager.immnSrvc.deleteMessage(msgId);
				isSuccessful = true;
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
				onError( errorObj );
			}
			return isSuccessful;
		}

		@Override
		protected void onPostExecute(Boolean isSuccessful) {
			// TODO Auto-generated method stub
			super.onPostExecute(isSuccessful);
			if(isSuccessful) {
				onSuccess((Boolean) isSuccessful);
			}
			
		}
		
	}



	@Override
	public void onSuccess(final Object isSuccessful) {
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		    	 if(null != iamListener) { 
					iamListener.onSuccess((Boolean) isSuccessful);
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


