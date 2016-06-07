package com.att.api.immn.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Delta {
    private final String type;
    private final DeltaChangeInternal[] adds;
    private final DeltaChangeInternal[] deletes;
    private final DeltaChangeInternal[] updates;

    public Delta(String type, DeltaChangeInternal[] adds, DeltaChangeInternal[] deletes,
            DeltaChangeInternal[] updates) {
            
        this.type = type;
        this.adds = adds;
        this.deletes = deletes;
        this.updates = updates;
    }

    public String getType() {
        return type;
    }

    public DeltaChangeInternal[] getAdds() {
        return adds;
    }

    public DeltaChangeInternal[] getDeletes() {
        return deletes;
    }

    public DeltaChangeInternal[] getUpdates() {
        return updates;
    }

    public static Delta valueOf(JSONObject jobj) throws JSONException {
        String type = jobj.getString("type");

        JSONArray jadds = jobj.getJSONArray("adds");
        DeltaChangeInternal[] adds = new DeltaChangeInternal[jadds.length()];
        for (int i = 0; i < jadds.length(); ++i) {
            JSONObject jchange = jadds.getJSONObject(i);
            adds[i] = DeltaChangeInternal.valueOf(jchange);
        }

        JSONArray jdeletes = jobj.getJSONArray("deletes");
        DeltaChangeInternal[] deletes = new DeltaChangeInternal[jdeletes.length()];
        for (int i = 0; i < jdeletes.length(); ++i) {
            JSONObject jchange = jdeletes.getJSONObject(i);
            deletes[i] = DeltaChangeInternal.valueOf(jchange);
        }

        JSONArray jupdates = jobj.getJSONArray("updates");
        DeltaChangeInternal[] updates = new DeltaChangeInternal[jupdates.length()];
        for (int i = 0; i < jupdates.length(); ++i) {
            JSONObject jchange = jupdates.getJSONObject(i);
            updates[i] = DeltaChangeInternal.valueOf(jchange);
        }

        return new Delta(type, adds, deletes, updates);
    }

}
