package com.att.api.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This Class used to maintain the shared preferences usages in single place.
 * Which is having different types of data storage such as String, int, boolean,
 * long. This class is final.
 * 
 * @author ATT
 */

public final class Preferences {

	private SharedPreferences prefs;

	/**
	 * This constructor Initialize and hold the contents of the preferences file
	 * 'name', returning a SharedPreferences through which you can retrieve and
	 * modify its values. Only one instance of the SharedPreferences object is
	 * returned to any callers for the same name, meaning they will see each
	 * other's edits as soon as they are made. *
	 * 
	 * @param context the preferences container
	 */
	public Preferences(Context context) {
		prefs = context.getSharedPreferences("ADS_API", Context.MODE_PRIVATE);
	}

	/**
	 * Set a integer value in the preferences by given key.
	 * @param key preference to update
	 * @param value value to assign to updated preference
	 * @return true if preference was set
	 */
	public boolean setInt(String key, int value) {
		Editor editor = prefs.edit();
		editor.putInt(key, value);
		return editor.commit();
	}

	/**
	 * Set a long value in the preferences by given key.
	 * @param key preference to update
	 * @param value value to assign to updated preference
	 * @return true if preference was set
	 */
	public boolean setLong(String key, long value) {
		Editor editor = prefs.edit();
		editor.putLong(key, value);
		return editor.commit();
	}

	/**
	 * Set a String value in the preferences by given key. 
	 * @param key preference to update
	 * @param value value to assign to updated preference
	 * @return true if preference was set
	 */
	public boolean setString(String key, String value) {
		Editor editor = prefs.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	/**
	 * Set a boolean value in the preferences by given key. 
	 * @param key preference to update
	 * @param value value to assign to updated preference
	 * @return true if preference was set
	 */
	public boolean setBoolean(String key, Boolean value) {
		Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	/**
.	 * Retrieve a boolean value from the preferences by given key.
	 * @param key preference value to obtain
	 * @param defValue default returned if no preference value is available
	 * @return the requested preference value
	 */
	public boolean getBoolean(String key, boolean defValue) {
		return prefs.getBoolean(key, defValue);
	}

	/**
	 * Retrieve a integer value from the preferences by given key.
	 * @param key preference value to obtain
	 * @param defValue default returned if no preference value is available
	 * @return the requested preference value
	 */
	public int getInt(String key, int defValue) {
		return prefs.getInt(key, defValue);
	}

	/**
	 * Retrieve a long value from the preferences by given key.
	 * @param key preference value to obtain
	 * @param defValue default returned if no preference value is available
	 * @return the requested preference value
	 */
	public long getLong(String key, long defValue) {
		return prefs.getLong(key, defValue);
	}

	/**
	 * Retrieve a String value from the preferences by given key.
	 * @param key preference value to obtain
	 * @param defValue default returned if no preference value is available
	 * @return the requested preference value
	 */
	public String getString(String key, String defValue) {
		return prefs.getString(key, defValue);
	}

	/**
	 * Check the value availability in the preferences by given key.
	 * @param key preference to check
	 * @return true if the requested preference value exists
	 */
	public boolean containsValue(String key) {
		return prefs.contains(key);
	}

}
