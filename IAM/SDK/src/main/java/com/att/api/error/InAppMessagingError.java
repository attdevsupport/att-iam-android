package com.att.api.error;

public class InAppMessagingError {
	
	private String errorMessage = null;
	private int httpResponseCode = 0;
	private String httpResponse = null;
	
	public InAppMessagingError() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InAppMessagingError(String errorMessage, int httpResponseCode, String httpResponse) {

		this.errorMessage = errorMessage;
		this.httpResponseCode = httpResponseCode;
		this.httpResponse = httpResponse;
		
	}
	public InAppMessagingError(int httpResponseCode, String httpResponse) {

		this.httpResponseCode = httpResponseCode;
		this.httpResponse = httpResponse;
		
	}

	public InAppMessagingError(String errorMessage) {
		
		this.errorMessage = errorMessage;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	public String getHttpResponse() {
		return httpResponse;
	}

	
}
