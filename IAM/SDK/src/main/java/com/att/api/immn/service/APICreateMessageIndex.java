package com.att.api.immn.service;


import android.os.AsyncTask;
import android.os.Handler;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APICreateMessageIndex implements ATTIAMListener {
	
	Boolean isSuccessful = false;
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();
	
	public APICreateMessageIndex(IAMManager iamMgr, ATTIAMListener iamListener) {
		
		this.iamManager = iamMgr;
		this.iamListener = iamListener;
	}
	
	public void CreateMessageIndex() {
		
		CreateMessageIndexTask createMessageIndexTask = new CreateMessageIndexTask();
		createMessageIndexTask.execute();
	}
	
	public class CreateMessageIndexTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Boolean isSuccesful = false;
			InAppMessagingError errorObj = new InAppMessagingError();

			try {
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return false;
				IAMManager.immnSrvc.createMessageIndex();
				isSuccesful = true;
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
				onError( errorObj );
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
