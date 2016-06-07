package com.att.iamsampleapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.att.api.immn.service.Message;
import com.att.api.immn.service.MmsContent;

public class MessageListAdapter extends BaseAdapter {

	private static ArrayList<Message> messageList;
	private LayoutInflater mInflater;
	private Context ctx;
	private String date;

	public MessageListAdapter(Context context, ArrayList<Message> results) {
		messageList = results;
		this.ctx = context;
		mInflater = LayoutInflater.from(context);
	}

	public ArrayList<Message> deleteItem(int nIndex) {

		messageList.remove(nIndex);
		return messageList;
	}

	public int getCount() {
		return messageList.size();
	}

	public Object getItem(int position) {
		return messageList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		String contactName;

		if (convertView == null) {

			convertView = mInflater.inflate(R.layout.conversationlist_row_view,
					null);

			holder = new ViewHolder();
			holder.txtName = (TextView) convertView.findViewById(R.id.name);
			holder.txtMessage = (TextView) convertView
					.findViewById(R.id.message);
			holder.txtTime = (TextView) convertView.findViewById(R.id.time);
			holder.imgFavorite = (ImageButton) convertView
					.findViewById(R.id.favorite);
			holder.imgAttachment = (ImageView) convertView
					.findViewById(R.id.attachment);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Update message From
		contactName = Utils.getContactName(ctx, messageList.get(position)
				.getFrom());
		if (null == contactName)
			holder.txtName.setText(messageList.get(position).getFrom());
		else
			holder.txtName.setText(contactName);

		// Update message
		if (null == messageList.get(position).getText())
			holder.txtMessage.setText("< Empty Message >");
		else if (messageList.get(position).getText().equalsIgnoreCase(""))
			holder.txtMessage.setText("< Empty Message >");
		else {
			holder.txtMessage.setText(messageList.get(position).getText());
		}

		if (messageList.get(position).getType().equalsIgnoreCase("MMS")) {
			String str = (null != messageList.get(position).getSubject() && messageList
					.get(position).getSubject().length() > 0) ? ("MMS - <Sub : "
					+ messageList.get(position).getSubject() + ">")
					: "MMS Attachments Available";
			holder.txtMessage.setText(str);
		}
		// Update message time
		
		date = Utils.getDate(messageList.get(position).getTimeStamp().replace('T', ' '));
		holder.txtTime.setText(date);
		
		// Update favorite message
		if (messageList.get(position).isFavorite())
			holder.imgFavorite.setBackgroundResource(R.drawable.btn_favorite);
		else
			holder.imgFavorite
					.setBackgroundResource(R.drawable.btn_notfavorite);

		ArrayList<MmsContent> mmsArrayList = messageList.get(position)
				.getMmsContents();
		boolean isMediaContentAvailable = false;
		if (mmsArrayList != null && mmsArrayList.size() > 0) {
			for (int n = 0; n < mmsArrayList.size()
					&& isMediaContentAvailable == false; n++) {
				if (mmsArrayList.get(n).getContentType()
						.contains("IMAGE")
						|| mmsArrayList.get(n).getContentType()
								.contains("AUDIO")
						|| mmsArrayList.get(n).getContentType()
								.contains("VIDEO"))
					isMediaContentAvailable = true;
			}
		}

		// Update Attachment
		if (isMediaContentAvailable == true)
			holder.imgAttachment
					.setBackgroundResource(R.drawable.ic_attachment);
		else
			holder.imgAttachment
					.setBackgroundResource(R.drawable.ic_transparent);

		// Message Read
		int typeFace;
		if (messageList.get(position).isUnread()) {
			typeFace = Typeface.BOLD_ITALIC;
			convertView.setBackgroundColor(Utils.UnreadBG);
		} else {
			typeFace = Typeface.NORMAL;
			convertView.setBackgroundColor(Utils.ReadBG);
		}
		holder.txtName.setTypeface(null, typeFace);
		holder.txtMessage.setTypeface(null, typeFace);
		holder.txtTime.setTypeface(null, typeFace);

		return convertView;
	}

	static class ViewHolder {
		TextView txtName;
		TextView txtMessage;
		TextView txtTime;
		ImageButton imgFavorite;
		ImageView imgAttachment;
	}
}
