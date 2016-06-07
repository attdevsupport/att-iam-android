package com.att.iamsampleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AttachmentListAdapter extends BaseAdapter {

	private static String[] attachmentList;
	private static String[] attMimeType;
	private LayoutInflater mInflater;

	public AttachmentListAdapter(Context context, String[] attFilePath, String[] mimeType) {
		attachmentList = attFilePath;
		attMimeType = mimeType;
		mInflater = LayoutInflater.from(context);
	}
	
	public void setNewAttachmentList(String [] attFilePath, String[] mimeType){
		attachmentList = attFilePath;
		attMimeType = mimeType;
	}

	public int getCount() {
		return attachmentList.length;
	}

	public Object getItem(int position) {
		return attachmentList[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.attachmentlist_row_view, null);

			holder = new ViewHolder();
			holder.attName = (TextView) convertView.findViewById(R.id.filename);
			holder.imgAttachment = (ImageView) convertView
					.findViewById(R.id.attachmentItem);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if(null == attachmentList[position]){
			holder.attName.setText("");
			holder.imgAttachment.setBackgroundResource(R.drawable.ic_transparent);
			return convertView;
		}
		
		String filePathSplit[] = attachmentList[position].split("/");
		
		holder.attName.setText(filePathSplit[filePathSplit.length-1]);

		String mimeType = attMimeType[position];
		if(mimeType.contains("image/")){
			// Update the image attachment
			/*Bitmap bmImg = BitmapFactory.decodeFile(attachmentList[position]);
			if(bmImg != null)
				holder.imgAttachment.setImageBitmap(bmImg);
			else*/
				holder.imgAttachment.setBackgroundResource(R.drawable.ic_image_black);
		}else if(mimeType.contains("video/")){
			holder.imgAttachment.setBackgroundResource(R.drawable.ic_video_black);
		}else if(mimeType.contains("audio/")){
			holder.imgAttachment.setBackgroundResource(R.drawable.ic_audio_black);
		}else{
			holder.imgAttachment.setBackgroundResource(R.drawable.ic_transparent);
		}
		
		return convertView;
	}

	static class ViewHolder {
		TextView attName;
		ImageView imgAttachment;
	}
}