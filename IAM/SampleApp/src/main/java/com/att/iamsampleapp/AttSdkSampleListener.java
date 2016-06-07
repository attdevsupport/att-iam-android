package com.att.iamsampleapp;

import android.util.Log;

import com.att.api.error.InAppMessagingError;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.util.TokenUpdatedListener;

public class AttSdkSampleListener implements ATTIAMListener {
	protected String operationName = "Unknown";
	
	public AttSdkSampleListener(String name) {
        this.operationName = name;
    }
	
	@Override
	public void onSuccess(Object response) {
		Log.i("Failed: Please override " + operationName + " onSuccess method.",  "Response=\n" + response.toString());
	}

	@Override
	public void onError(InAppMessagingError error) {
		Log.i(operationName + " on error", "Error:" + error.getErrorMessage() + 
				".\nHttpResponseCode:" + error.getHttpResponseCode() + 
				". HttpResponse: " + error.getHttpResponse());
		
		// Delete the token and restart the application for unauthorized access
		switch (error.getHttpResponseCode()) {
		case 400: // invalid_grant
		case 401: // UnAuthorized Request
			TokenUpdatedListener.DeleteSavedToken();
			break;
		}
	}

}
