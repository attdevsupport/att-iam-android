package com.att.iamsampleapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.att.api.error.InAppMessagingError;
import com.att.api.immn.listener.ATTIAMListener;
import com.att.api.immn.service.IAMManager;
import com.att.api.immn.service.SendResponse;
import com.att.api.oauth.OAuthToken;

public class NewMessage extends Utils {

	private static final String TAG = "IAM_NewMessage";
	ListView attachmentsListView;
	AttachmentListAdapter adapter;
	int numAttachments = 0;
	String attachments[] = new String[Config.maxAttachments];
	String attMimeType[] = new String[Config.maxAttachments];
	ProgressDialog pDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_message);
	}

	public void onResume() {
		super.onResume();

		attachmentsListView = (ListView) findViewById(R.id.attachmentList);
		setupAttachmentListListener();
	}

	// MessageList Listener
	public void setupAttachmentListListener() {

		attachmentsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						NewMessage.this);
				builder.setPositiveButton("Delete",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								System.arraycopy(attachments,position+1,attachments,position,attachments.length-1-position);
								System.arraycopy(attMimeType,position+1,attMimeType,position,attMimeType.length-1-position);
								numAttachments--;
								adapter.setNewAttachmentList(attachments, attMimeType);
								adapter.notifyDataSetChanged();
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
				
				if( null != attachments[position]) {
					builder.setTitle("Delete Attachment?");
					String filePathSplit[] = attachments[position].split("/");
					builder.setMessage(filePathSplit[filePathSplit.length-1]);				
					AlertDialog dialog = builder.create();
					dialog.show();
				} 
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_new_message, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent = new Intent();
		String attachmentContentType;
		String title;
		int requestCode;
		switch (item.getItemId()) {

		case R.id.action_ImageAttachment: {
			attachmentContentType = "image/*";
			title = "Choose image file";
			requestCode = SELECT_PICTURE;
			break;
		}
		case R.id.action_AudioAttachment: {
			attachmentContentType = "audio/*";
			title = "Choose audio file";
			requestCode = SELECT_AUDIO;
			break;
		}
		case R.id.action_VideoAttachment: {
			attachmentContentType = "video/*";
			title = "Choose video file";
			requestCode = SELECT_VIDEO;
			break;
		}
		default:
			return super.onOptionsItemSelected(item);
		}

		intent.setType(attachmentContentType);
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		startActivityForResult(Intent.createChooser(intent, title), requestCode);

		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intentContact) {
		super.onActivityResult(requestCode, resultCode, intentContact);
		if (requestCode == SELECT_PICTURE || requestCode == SELECT_AUDIO
				|| requestCode == SELECT_VIDEO) {
			if (resultCode == RESULT_OK) {
				Uri pickedAttachment = intentContact.getData();
				String mime = getContentResolver().getType(pickedAttachment);
				String filePath = getRealPathFromURI(pickedAttachment);
				if (numAttachments < Config.maxAttachments && null != filePath) {

					attachments[numAttachments] = filePath;
					attMimeType[numAttachments] = mime;

					if (adapter != null)
						adapter.setNewAttachmentList(attachments, attMimeType);
					else {
						adapter = new AttachmentListAdapter(
								getApplicationContext(), attachments,
								attMimeType);
						attachmentsListView.setAdapter(adapter);
					}
					adapter.notifyDataSetChanged();

					numAttachments++;
				} else {
					infoDialog("Attachments exceeded the size limit of 21. Unable to fetch the attachment. !!", false);
				}

				Utils.toastHere(getApplicationContext(), TAG, "File Path : "
						+ filePath);
			}
		}
	}
	/*
	 * The application sends the SMS/MMS on behalf of the subscriber with  his consent
	 * authToken will  be used to get access to sendMessage of InApp Messaging
	 * 
	 * The response will be handled by the listener : sendMessageListener()
	 */

	public void sendMessage(View v) {

		EditText contactsWidget = (EditText) findViewById(R.id.contacts);
		EditText messageWidget = (EditText) findViewById(R.id.message);
		EditText subjectWidget = (EditText) findViewById(R.id.subject);

		if (contactsWidget.getText().toString().equalsIgnoreCase("")) {
			infoDialog("Enter the contacts !!", false);
			return;
		}
		
		if (messageWidget.getText().toString().equalsIgnoreCase("")) {
			infoDialog("No Message Content !!", false);
			return;
		}

		IAMManager iamManager = new IAMManager(new sendMessageListener());

		Boolean isGroup = false;

		String addresses[] = (contactsWidget.getText().toString().replace(" ",
				"")).split(",");
		if (addresses.length > Config.maxRecipients) {
			infoDialog(
					"Maximum recipients is + "
							+ String.valueOf(Config.maxRecipients) + " !!",
					false);
			return;
		}

		if (!checkNumberandEmail(addresses)) {
			infoDialog("Invalid PhoneNumber or EmailId", false);
			return;
		}

		isGroup = (addresses.length > 1) ? true : false;

		CheckBox chkBroadcast = (CheckBox) findViewById(R.id.broadcastCheckBox);
		isGroup = !(chkBroadcast.isChecked());

		iamManager.SendMessage(addresses, messageWidget.getText().toString(),
				subjectWidget.getText().toString(), isGroup, attachments);

		showProgressDialog("Sending Message ...");
	}
	
	/*
	 *  sendMessageListener will be called on getting the response from  SendMessage(..)
	 *  
	 *  onSuccess : 
	 *  The message will be sent to the recipient
	 *  
	 *  onError:
	 *  This is called  when message cannot be sent  
	 *  The Error along with the error code is displayed to the user
	 */
	
	protected class sendMessageListener extends AttSdkSampleListener {
	
		public sendMessageListener() {
			super("sendMessage");
		}

		@Override
		public void onSuccess(Object arg0) {
			SendResponse msg = (SendResponse) arg0;
			if (null != msg) {
				Toast toast = Toast.makeText(getApplicationContext(),
						"Message Sent", Toast.LENGTH_SHORT);
				toast.show();

				sendMessageResponsetoParentActivity(msg.getId());
			}
		}
		
		@Override
		public void onError(InAppMessagingError error) {
			dismissProgressDialog();
			infoDialog("Message send failed !!"+ "\n" + error.getHttpResponse() , false );
			Utils.toastOnError(getApplicationContext(), error);
			super.onError(error); // in the end so that error messages can be displayed.
		}
	}

	public void showProgressDialog(String dialogMessage) {

		if (null == pDialog)
			pDialog = new ProgressDialog(this);
		pDialog.setCancelable(false);
		pDialog.setMessage(dialogMessage);
		pDialog.show();
	}

	public void dismissProgressDialog() {
		if (null != pDialog) {
			pDialog.dismiss();
		}
	}

	void sendMessageResponsetoParentActivity(String responseID) {

		Intent newMessageIntent = new Intent();
		newMessageIntent.putExtra("MessageResponse", responseID);
		setResult(RESULT_OK, newMessageIntent);
		finish();
	}

	public void addAttachment(View v) {

		openImageIntent();
	}

	private static final int SELECT_PICTURE = 1;
	private static final int SELECT_AUDIO = 2;
	private static final int SELECT_VIDEO = 3;

	void openImageIntent() {

		Intent pickIntent = new Intent();
		pickIntent.setType("image/*");
		pickIntent.setAction(Intent.ACTION_GET_CONTENT);

		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		String pickTitle = "Select or take a new Picture";
		Intent chooserIntent = Intent.createChooser(pickIntent, pickTitle);
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				new Intent[] { takePhotoIntent });

		startActivityForResult(chooserIntent, SELECT_PICTURE);
	}

	// Info Dialog
	protected void infoDialog(String message, final boolean doFinish) {
		doDialog("Info: ", message, doFinish);
	}

	private void doDialog(String prefix, String message, final boolean doFinish) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(prefix);

		// set dialog message
		alertDialogBuilder.setMessage(message).setCancelable(false)
				.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						if (doFinish == true)
							// if this button is clicked, Close the Application.
							finish();
						else
							// if this button is clicked, just close the dialog
							// box.
							dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
