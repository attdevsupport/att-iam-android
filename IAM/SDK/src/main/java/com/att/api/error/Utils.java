package com.att.api.error;

import com.att.api.rest.RESTException;

public class Utils {
	
	public static InAppMessagingError 
	CreateErrorObjectFromException(RESTException exception) {
    	InAppMessagingError errorResponse = null;
    		if(exception == null) {
    			errorResponse = new InAppMessagingError("The size exceeded the limit");     
    		} else {
    			errorResponse = new InAppMessagingError(exception.getStatusCode(), exception.getErrorMessage() );
    		}
    	return errorResponse;
    }

}
