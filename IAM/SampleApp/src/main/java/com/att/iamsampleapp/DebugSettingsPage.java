package com.att.iamsampleapp;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.att.api.immn.service.IAMManager;
import com.att.api.oauth.OAuthToken;
import com.att.api.util.Preferences;
import com.att.api.util.TokenUpdatedListener;

public class DebugSettingsPage extends Activity {

	private CheckBox m_forceOffNetCheckBox = null;
	private CheckBox m_suppressCheckBox = null;
	private CheckBox m_clearCookiesCheckBox = null;
	private CheckBox m_clearPreferencesCheckBox = null;
	private CheckBox m_revokeAccessTokenCheckBox = null;
	private CheckBox m_revokeRefreshTokenCheckBox = null;
	private EditText m_accessToken = null;
	private EditText m_refreshToken = null;
	private EditText m_tokenExpiresIn = null;
	
	protected void InitializeStateFromPreferences() {

		m_accessToken = (EditText)findViewById(R.id.curAC);
		m_accessToken.setEnabled(false);
		m_refreshToken = (EditText)findViewById(R.id.refreshToken);
		m_tokenExpiresIn = (EditText)findViewById(R.id.curACTime);
		m_forceOffNetCheckBox = (CheckBox) findViewById(R.id.forceOffNetCheckBox);
		m_suppressCheckBox = (CheckBox) findViewById(R.id.forceSuppressCheckBox);		
		m_clearCookiesCheckBox = (CheckBox) findViewById(R.id.clearCookiesCheckBox);		
		m_clearPreferencesCheckBox = (CheckBox) findViewById(R.id.clearPreferencesCheckBox);
		m_revokeAccessTokenCheckBox = (CheckBox) findViewById(R.id.revokeAccessTokenCheckBox);
		m_revokeRefreshTokenCheckBox = (CheckBox) findViewById(R.id.revokeRefreshTokenCheckBox);

		Preferences prefs = new Preferences(getApplicationContext());		
		if (prefs != null) {
			m_accessToken.setText(prefs.getString(TokenUpdatedListener.accessTokenSettingName, ""));
			m_refreshToken.setText(prefs.getString(TokenUpdatedListener.refreshTokenSettingName, ""));
			m_tokenExpiresIn.setText(String.valueOf(prefs.getLong(TokenUpdatedListener.tokenExpirySettingName, 0) - 
					(System.currentTimeMillis() / 1000)));

			String savedCustomParam = prefs.getString(TokenUpdatedListener.customParamSettingName, "");
			m_forceOffNetCheckBox.setChecked(savedCustomParam.contains("bypass_onnetwork_auth"));
			m_suppressCheckBox.setChecked(savedCustomParam.contains("suppress_landing_page"));
		}		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug_settings_page);
		
		InitializeStateFromPreferences();

		Button applyButton = (Button) findViewById(R.id.applyButton);
		applyButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String customParamValue = "";
				OAuthToken token = null;

				Preferences prefs = new Preferences(getApplicationContext());		
				if (m_forceOffNetCheckBox.isChecked()) {
					customParamValue = "bypass_onnetwork_auth";
				} 
				if (m_suppressCheckBox.isChecked()) {
					customParamValue = customParamValue.length() > 0 ? customParamValue + "," + "suppress_landing_page" : "suppress_landing_page";
				}
				prefs.setString(TokenUpdatedListener.customParamSettingName, customParamValue);
				
				// This section sets the new expiry time for the current token
				long newExpiresInValue = Long.parseLong(m_tokenExpiresIn.getText().toString().trim());

				token = new OAuthToken(m_accessToken.getText().toString().trim(), 
						newExpiresInValue, 
						m_refreshToken.getText().toString().trim(), (System.currentTimeMillis() / 1000));
				if (token != null) {
					prefs.setString(TokenUpdatedListener.accessTokenSettingName, token.getAccessToken());
					prefs.setString(TokenUpdatedListener.refreshTokenSettingName, token.getRefreshToken());
					prefs.setLong(TokenUpdatedListener.tokenExpirySettingName, token.getAccessTokenExpiry());

					Log.i("updateSavedToken", "Saved Token: " + TokenUpdatedListener.tokenDisplayString(token.getAccessToken()));
					Log.i("tokenExpiryTime", new Date(token.getAccessTokenExpiry()*1000).toString());
					
					IAMManager.SetCurrentToken(token);
				}	
				if (m_clearPreferencesCheckBox.isChecked()){
					prefs.setString(TokenUpdatedListener.accessTokenSettingName, "");
					prefs.setString(TokenUpdatedListener.refreshTokenSettingName, "");
					prefs.setLong(TokenUpdatedListener.tokenExpirySettingName, 0);
					prefs.setString(TokenUpdatedListener.customParamSettingName, "");
				}
				if (m_clearCookiesCheckBox.isChecked()){
					CookieSyncManager.createInstance(getApplicationContext());
					CookieManager cookieManager = CookieManager.getInstance();
					cookieManager.removeAllCookie();
					cookieManager.removeSessionCookie(); 
				}

				if (m_revokeAccessTokenCheckBox.isChecked()){
					ConversationList.RevokeToken("access_token");
				} else if (m_revokeRefreshTokenCheckBox.isChecked()) {
					ConversationList.RevokeToken("refresh_token");					
				}

				finish();	 
			}
		});
		
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}	

	@Override
	public void onResume() {
		super.onResume();
		InitializeStateFromPreferences();
	}
}
