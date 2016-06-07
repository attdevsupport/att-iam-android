package com.att.api.immn.service;
import com.att.api.immn.service.DeltaChange;


public class DeltaResponse {
	 public final String state;
	 public final DeltaChange[] deltaChanges;
	 
	public DeltaResponse(String state, DeltaChange[] deltaChanges) {
		super();
		this.state = state;
		this.deltaChanges = deltaChanges;
	}

	public String getState() {
		return state;
	}

	public DeltaChange[] getDeltaChanges() {
		return deltaChanges;
	}
}
