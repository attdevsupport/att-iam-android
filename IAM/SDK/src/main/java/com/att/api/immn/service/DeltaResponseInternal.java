package com.att.api.immn.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public final class DeltaResponseInternal {
    public final String state;
    public final Delta[] delta;

    public DeltaResponseInternal(String state, Delta[] delta) {
        this.state = state;
        this.delta = delta;
    }

    public String getState() {
        return state;
    }

    public Delta[] getDeltas() {
        return delta;
    }

    public static DeltaResponseInternal valueOf(JSONObject jobj) throws JSONException {
        JSONObject jDeltaResponse = jobj.getJSONObject("deltaResponse");
        String state = jDeltaResponse.getString("state");

        JSONArray jdelta = jDeltaResponse.getJSONArray("delta");
        Delta[] delta = new Delta[jdelta.length()];
        for (int i = 0; i < jdelta.length(); ++i)
          delta[i] = Delta.valueOf(jdelta.getJSONObject(i));

        return new DeltaResponseInternal(state, delta);
    }
}
