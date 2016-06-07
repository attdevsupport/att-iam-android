package com.att.api.immn.service;

import java.text.ParseException;

import org.json.JSONException;

import android.os.AsyncTask;
import android.os.Handler;

import com.att.api.error.InAppMessagingError;
import com.att.api.error.Utils;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.rest.RESTException;

public class APIGetDelta implements ATTIAMListener {
	
	String state = null;
	private ATTIAMListener iamListener;
	IAMManager iamManager;
	protected Handler handler = new Handler();

	public APIGetDelta(String state, IAMManager iamMgr, ATTIAMListener iamListener) {
		this.state = state;
		this.iamManager = iamMgr;
		this.iamListener = iamListener;
	}

	public void GetDelta() {
		GetDeltaTask getDelta =  new GetDeltaTask();
		getDelta.execute(state);		
	}
	public class GetDeltaTask extends AsyncTask<String, Void, DeltaResponse> {

		@Override
		protected DeltaResponse doInBackground(String... params) {
			// TODO Auto-generated method stub
			DeltaResponseInternal deltaResponseInternal = null;
			InAppMessagingError errorObj = new InAppMessagingError();

			try {
				if (!iamManager.CheckAndRefreshExpiredTokenAsync()) return null;
				deltaResponseInternal = IAMManager.immnSrvc.getDelta(params[0]);
			} catch (RESTException e) {
				errorObj = Utils.CreateErrorObjectFromException( e );
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
			
			// SM: Fixed the bug to handle null value of Delta Response
			if (deltaResponseInternal == null) return null;
			
			String state = deltaResponseInternal.getState();
			int numChanges = 0;
			for(int i = 0; i < deltaResponseInternal.getDeltas().length; ++i ) {
				Delta tmpDeltaObj = deltaResponseInternal.getDeltas()[i];
				numChanges += tmpDeltaObj.getAdds().length +
						      tmpDeltaObj.getDeletes().length +
						      tmpDeltaObj.getUpdates().length;
			}
			
			DeltaChange deltaChanges[] = new DeltaChange[ numChanges ];
			int count = 0;
			for(int i = 0; i < deltaResponseInternal.getDeltas().length; ++i ) {
				
				Delta tmpDeltaObj = deltaResponseInternal.getDeltas()[i];
				String type = tmpDeltaObj.getType();
				for( int j = 0; j < tmpDeltaObj.getAdds().length; ++j) {
					DeltaChangeInternal tmpDeltaChangeInternal = tmpDeltaObj.getAdds()[j];
					DeltaChange tmpDeltaChangeObj = new DeltaChange(tmpDeltaChangeInternal.getMessageId(),
																	tmpDeltaChangeInternal.isFavorite(),
																	tmpDeltaChangeInternal.isUnread(),
																	type,
																	ChangeType.ADD );
					deltaChanges[count++] = tmpDeltaChangeObj;
				}
				for( int j = 0; j < tmpDeltaObj.getDeletes().length; ++j) {
					DeltaChangeInternal tmpDeltaChangeInternal = tmpDeltaObj.getDeletes()[j];
					DeltaChange tmpDeltaChangeObj = new DeltaChange(tmpDeltaChangeInternal.getMessageId(),
																	tmpDeltaChangeInternal.isFavorite(),
																	tmpDeltaChangeInternal.isUnread(),
																	type,
																	ChangeType.DELETE );
					deltaChanges[count++] = tmpDeltaChangeObj;
				}
				for( int j = 0; j < tmpDeltaObj.getUpdates().length; ++j) {
					DeltaChangeInternal tmpDeltaChangeInternal = tmpDeltaObj.getUpdates()[j];
					DeltaChange tmpDeltaChangeObj = new DeltaChange(tmpDeltaChangeInternal.getMessageId(),
																	tmpDeltaChangeInternal.isFavorite(),
																	tmpDeltaChangeInternal.isUnread(),
																	type,
																	ChangeType.UPDATE );
					deltaChanges[count++] = tmpDeltaChangeObj;
				}
			}
			
			DeltaResponse deltaResponse = new DeltaResponse( state, deltaChanges);
			return deltaResponse;
		}

		@Override
		protected void onPostExecute(DeltaResponse deltaResponse) {
			// TODO Auto-generated method stub
			super.onPostExecute(deltaResponse);
			if(null != deltaResponse) {
				onSuccess((DeltaResponse) deltaResponse);
			}
			
		}
		
	}

	@Override
	public void onSuccess(final Object deltaResponse) {
		// TODO Auto-generated method stub
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(null != iamListener) {
					iamListener.onSuccess((DeltaResponse) deltaResponse);
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
