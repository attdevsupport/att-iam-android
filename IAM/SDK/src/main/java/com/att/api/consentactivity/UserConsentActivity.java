package com.att.api.consentactivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.att.api.error.InAppMessagingError;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.oauth.OAuthService;
import com.att.api.rest.RESTException;

public class UserConsentActivity extends Activity implements ATTIAMListener{

	OAuthService osrvc;
	private ATTIAMListener iamListener;
	protected Handler handler = new Handler();
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams llParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		linearLayout.setLayoutParams(llParams);
		setContentView(linearLayout);
		
		WebView webView = new WebView(this);
		webView.setLayoutParams(llParams);
		linearLayout.addView(webView);
				
		 Intent i = getIntent();
		 String fqdn = i.getStringExtra("fqdn");
		 String clientId = i.getStringExtra("clientId");
		 String clientSecret =  i.getStringExtra("clientSecret");
		 String appScope = i.getStringExtra("appScope");
		 String redirectUri = i.getStringExtra("redirectUri");
		 String customParam = "";
		 String customParamValue = i.getStringExtra("customParam");
		 if (customParamValue != null && customParamValue.length() > 0) {
			 customParam = "&custom_param=" + customParamValue;
		 }
		
		 osrvc = new OAuthService(fqdn, clientId, clientSecret);

		
		webView.clearFormData();
		webView.clearCache(true);
		webView.clearHistory();
		webView.clearView();
		webView.clearSslPreferences();
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAppCacheEnabled(false);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.loadUrl(fqdn +"/oauth/v4/authorize?client_id=" + clientId + "&scope=" + appScope + "&redirect_uri=" + redirectUri + customParam);
		webView.setWebViewClient(new myWebViewClient()); 	
	}
	private class myWebViewClient extends WebViewClient {
		
		InAppMessagingError errorObj = new InAppMessagingError();
		
		 @Override
		    public boolean shouldOverrideUrlLoading(WebView view, String smsUrl) {
	    	Log.i("shouldOverrideUrlLoad", "shouldOverrideUrlLoading() in the MyWebViewClient "+ smsUrl);
	    	if(smsUrl.contains("sms:")) {
				String[] splitNumber = smsUrl.split(":");
				String phNumber = splitNumber[1];
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.setType("vnd.android-dir/mms-sms");
				intent.putExtra("address", phNumber);
				startActivity(intent);
				return true;
	    	} else 
	    		return false;
	    }
		
    	@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.i("onPageStarted", "Start : " + url);
			super.onPageStarted(view, url, favicon);
			if(url.contains("code=")) {				
				String encodedURL;
				try {
					encodedURL = URLEncoder.encode(url, "UTF-8");
					Log.i("onPageStarted", "encodedURL: " + encodedURL);

					String encodedURLSplits[] = encodedURL.split("code%3D");
					if(encodedURLSplits.length > 1) {
						String twiceEncodedAuthCode = encodedURLSplits[1];
						String encodedAuthCode = URLDecoder.decode(twiceEncodedAuthCode, "UTF-8");
						String authCode = URLDecoder.decode(encodedAuthCode, "UTF-8");
						Log.i("onPageStarted", "authCode: " + authCode);

						Intent returnIntent = new Intent();
						returnIntent.putExtra("oAuthCode", authCode);
						setResult(RESULT_OK,returnIntent);
						finish();
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(url.contains("error=")) {
				try {
					throw new RESTException("Incorrect Url! Unable to open the Authorization page." +
											"Check the APP_KEY,APP_SECRET,APP_SCOPE and REDIRECT_URI");
				} catch (RESTException e) {
					// TODO Auto-generated catch block
					errorObj = new InAppMessagingError(e.getMessage());
					onError(errorObj);
				}																			
			}
    	}	
    }

	@Override
	public void onSuccess(Object response) {
	}

	@Override
	public void onError(final InAppMessagingError error) {
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (null != iamListener) {
					iamListener.onError(error);
				}
			}
		});	
		
		Intent returnIntent = new Intent();
		returnIntent.putExtra("ErrorMessage", error.getErrorMessage());
		setResult(RESULT_CANCELED,returnIntent);
		finish();
	}	
}
