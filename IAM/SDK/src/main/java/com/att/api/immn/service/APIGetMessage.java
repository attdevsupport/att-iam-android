package com.att.api.immn.service;

import java.text.ParseException;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APIGetMessage implements ATTIAMListener {

	String msgId = null;
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();

	public APIGetMessage(String msgId, IAMManager iamMgr, ATTIAMListener iamListener) {
		
		this.msgId = msgId;		
		this.iamListener = iamListener;
		this.iamManager = iamMgr;
	}

	public void GetMessage(String msgId) {
		
		GetMessageTask getMessageTask = new GetMessageTask();
		getMessageTask.execute(msgId);
	}
	
	public class  GetMessageTask extends AsyncTask<String,Void,Message> {

		@Override
		protected Message doInBackground(String... msgId) {
			Message message = null;
			InAppMessagingError errorObj = new InAppMessagingError();

			try {
				Log.d("IAMSDK", "Async Task : " +  msgId[0]);
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return null;
				message = IAMManager.immnSrvc.getMessage(msgId[0]);
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
			if(null != message)
				Log.d("IAMSDK", "M not null");
			else
				Log.d("IAMSDK", "M is null");
			return message;
		}

		@Override
		protected void onPostExecute(Message message) {
			
			super.onPostExecute(message);
			if(null != message) {
				onSuccess((Message)message);
			}
		}
	}

	@Override
	public void onSuccess(final Object message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (null != iamListener) {
					iamListener.onSuccess((Message)message);
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
