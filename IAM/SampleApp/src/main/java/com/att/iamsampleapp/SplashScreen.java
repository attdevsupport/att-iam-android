package com.att.iamsampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class SplashScreen extends Activity {

	private final int SPLASH_DISPLAY_LENGTH = 2000;
	protected Handler handler = new Handler();
	Runnable myRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		/*
		 * New Handler to start the Menu-Activity and close this Splash-Screen
		 * after some seconds.
		 */

		myRunnable = new Runnable() {
			@Override
			public void run() {
				/* Create an Intent that will start the Menu-Activity. */
				Intent mainIntent = new Intent(getApplicationContext(),
						ConversationList.class);
				startActivity(mainIntent);
				finish();
			}
		};
		handler.postDelayed(myRunnable, SPLASH_DISPLAY_LENGTH);
	}

	
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	     if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	 if (null != myRunnable) {
	    		 handler.removeCallbacks(myRunnable);
	    		 finish();
	    	 }
	 		return true;
	     }
	     return super.onKeyDown(keyCode, event);
	 }
	
}
