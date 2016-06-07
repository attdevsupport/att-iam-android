package com.att.api.immn.service;

import java.text.ParseException;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Handler;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APIGetMessageIndexInfo implements ATTIAMListener {
	
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();

	public APIGetMessageIndexInfo(IAMManager iamMgr, ATTIAMListener attiamListener) {
		this.iamManager = iamMgr;
		this.iamListener = attiamListener;
	}
	
	public void GetMessageIndexInfo() {
		GetMessageIndexInfoTask getMessageIndexInfoTask = new GetMessageIndexInfoTask();
		getMessageIndexInfoTask.execute();
	}
	
	public class GetMessageIndexInfoTask extends AsyncTask<Void, Void, MessageIndexInfo> {

		@Override
		protected MessageIndexInfo doInBackground(Void... params) {
			// TODO Auto-generated method stub
			MessageIndexInfo messageIndexInfo = null;
			InAppMessagingError errorObj = new InAppMessagingError();

			try {
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return null;
				messageIndexInfo = IAMManager.immnSrvc.getMessageIndexInfo();
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
				onError( errorObj );
			} catch (JSONException e) {
				errorObj = new InAppMessagingError(e.getMessage());
				onError(errorObj);			
			} catch (ParseException e) {
				errorObj = new InAppMessagingError(e.getMessage());
				onError(errorObj);		
			}
			return messageIndexInfo;
		}

		@Override
		protected void onPostExecute(MessageIndexInfo messageIndexInfo) {
			// TODO Auto-generated method stub
			super.onPostExecute(messageIndexInfo);
			if(null != messageIndexInfo) {
				onSuccess((MessageIndexInfo) messageIndexInfo);
			}
		}
		
	}

	@Override
	public void onSuccess(final Object messageIndexInfo) {
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		    	 if(null != iamListener) { 
					iamListener.onSuccess((MessageIndexInfo) messageIndexInfo);
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
