package com.att.iamsampleapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.att.api.error.InAppMessagingError;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.MediaStore;
import android.widget.Toast;

public class Utils extends Activity{

	public static int UnreadBG = 0xFFF1F1F1;
	public static int ReadBG = 0xFFFFFFFF;

	public static String getContactName(Context context, String phoneNumber) {
		ContentResolver cr = context.getContentResolver();
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor cursor = cr.query(uri,
				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		if (cursor == null) {
			return null;
		}
		String contactName = null;
		if (cursor.moveToFirst()) {
			contactName = cursor.getString(cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME));
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		return contactName;
	}

	public static void toastHere(Context ctx, String TAG, String message) {
		Toast toast = Toast.makeText(ctx, "Message : " + message,
				Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public boolean checkNumberandEmail(String[] addresses){
		boolean isValid = true;
		String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
		Pattern numberPattern = Pattern.compile(" *[^0-9] *");
		for(int n = 0; n<addresses.length && isValid == true ; n++)
			isValid = ((!numberPattern.matcher(addresses[n]).matches()) || (addresses[n].matches(emailPattern)));
		return isValid;
	}

	public String getRealPathFromURI(Uri contentUri) {
		String path = null;
		String[] proj = { MediaStore.MediaColumns.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj, null,
				null, null);
		if (cursor != null && cursor.moveToFirst()) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
			path = cursor.getString(column_index);
			cursor.close();
		}
		return path;
	}
	
	public static void toastOnError(Context ctx,InAppMessagingError errorObject) {
		if(null != errorObject.getErrorMessage()) {
			Toast toast = Toast.makeText(ctx, "Error Code :" + " " + errorObject.getHttpResponseCode() + "," 
									 + " " + "Error : " + " " + errorObject.getErrorMessage(), Toast.LENGTH_LONG);
			toast.show();
		} else if (null != errorObject.getHttpResponse()) {
			Toast toast = Toast.makeText(ctx, "Error Code :" + " " + errorObject.getHttpResponseCode() + "," 
					 + " " + "Error : " + " " + errorObject.getHttpResponse(), Toast.LENGTH_LONG);
			toast.show();
		} 
		else {
			Toast toast = Toast.makeText(ctx, "Error Code :" + " " + errorObject.getHttpResponseCode() + "," 
					 + " " + "Error : " + " " + "MessageContentError", Toast.LENGTH_LONG);
			toast.show();

		}
			
			
	}
	public static String getDate(String selDate) {
		
		SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date parsed = null;
		try {
			 parsed = sourceFormat.parse(selDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		destFormat.setTimeZone(TimeZone.getDefault());		
		String date = destFormat.format(parsed);
		
		return date;
		
	}
}
