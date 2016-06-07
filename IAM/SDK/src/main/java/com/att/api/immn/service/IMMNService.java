
package com.att.api.immn.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.att.api.oauth.OAuthToken;
import com.att.api.rest.APIResponse;
import com.att.api.rest.RESTClient;
import com.att.api.rest.RESTException;
import com.att.api.service.APIService;
//import com.att.api.error.InAppMessagingError;

public class IMMNService extends APIService {

	public IMMNService(String fqdn, OAuthToken token) {
        super(fqdn, token);
    }

    
    public SendResponse sendMessage(String address, String msg) throws RESTException, JSONException, ParseException {
        String[] addrs = {address};
        return this.sendMessage(addrs, msg);
    }

    
    public SendResponse sendMessage(String[] addresses, String msg) throws RESTException, JSONException, ParseException {
        return this.sendMessage(addresses, msg, null, false, null);     //SMS

    }

    public SendResponse sendMessage(String address, String subject, 
            boolean group) throws RESTException, JSONException, ParseException {
        return sendMessage(address, null, subject, group);     //Group Message
    }

    public SendResponse sendMessage(String address, String msg, String subject, 
            boolean group) throws RESTException, JSONException, ParseException {
        String[] addrs = {address};
        return sendMessage(addrs, null, subject, group); //Group Message
    }

    public SendResponse sendMessage(String[] addresses, String subject, 
            boolean group) throws RESTException, JSONException, ParseException {
        return sendMessage(addresses, null, subject, group);
    }

    public SendResponse sendMessage(String[] addresses, String msg, 
            String subject, boolean group) throws RESTException, JSONException, ParseException {
        return sendMessage(addresses, msg, subject, group, null);
    }

    public SendResponse sendMessage(String address, String msg, 
            String subject, boolean group, String[] attachments) throws RESTException, JSONException, ParseException {
        String[] addrs = {address};
        return sendMessage(addrs, msg, subject, group, attachments); // MMS
    }

    public SendResponse sendMessage(String[] addresses, String msg, 
            String subject, boolean group, String[] attachments) throws RESTException, JSONException,ParseException {

        final String endpoint = getFQDN() + "/myMessages/v2/messages";

        JSONObject jsonBody = new JSONObject();
        JSONObject body = new JSONObject();
        addresses = formatAddresses(addresses);

        if (msg != null ){
            body.put("text", msg);
        }

        if (subject != null)
            body.put("subject", subject);

        // group messages must specify multiple addresses
        if (addresses.length <= 1)
            group = false;
        body.put("isGroup", group);
   	
        JSONArray jaddrs = new JSONArray();
        for (String addr : addresses)
        	if(addr != null)
        		jaddrs.put(addr);

        body.put("addresses", jaddrs);

        if ( attachments != null ) {
        	JSONArray jattach = new JSONArray();
        	
        	int totalSizeOfAttachments = 0;
        	for( String fattach : attachments) {
        		if(fattach != null) {
        			File f = new File(fattach);
        			totalSizeOfAttachments += f.length();
        		}
        	}
        
        	if (totalSizeOfAttachments > ( 1024 * 1024 ) ) {
        		throw new RESTException("Attachment exceeds size limit of 1MB");	 
			}
			
        	
        	for( String fattach : attachments) {
        		if(fattach != null) {
        			JSONObject attachBody = new JSONObject();
        			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        			String contentType = null;
        			String fileName = null;
        			String fattchSplit[] = fattach.split("/");  
        			String fileNameWithExtension = fattchSplit[fattchSplit.length -1];
        			String fileNameWithoutExtension[] =  fileNameWithExtension.split("\\.");
        			String fattachExtension = fileNameWithoutExtension[0].replaceAll("[^a-zA-Z.]+", "");
        			String urlToGetExtension = fattachExtension + "." + fileNameWithoutExtension[1];
        			String extension = MimeTypeMap.getFileExtensionFromUrl(urlToGetExtension);
        			MimeTypeMap mType =  MimeTypeMap.getSingleton();
        			String mimeType = mType.getMimeTypeFromExtension(extension.toLowerCase());
        			        	    
	        		if( mimeType.contains("image") ) {
	        			Bitmap bm = BitmapFactory.decodeFile(fattach);	        			
	        			boolean success = bm.compress(Bitmap.CompressFormat.JPEG, 0, baos); //bm is the bitmap object 
	        			
	        			contentType = mimeType.toString();
	        			fileName = fattchSplit[fattchSplit.length -1];
	        		
	        		} else if(mimeType.contains("audio") || (mimeType.contains("video"))) {
	        				if(mimeType.contains("audio") ){
	        					contentType = mimeType.toString();
	    	        			fileName = fattchSplit[fattchSplit.length -1];

	        				}
	        					else {
	        					contentType = mimeType.toString();
	    	        			fileName = fattchSplit[fattchSplit.length -1];

	        				}
	        				File inputFile = new File(fattach);
	        				FileInputStream fis;					
	        				try {
	        					fis = new FileInputStream(inputFile);	
	        					while ( true ) {
	        						byte[] buf = new byte[4096];
	        						int count = fis.read(buf, 0, 4096);
	        						if ( count == -1 ) {
	        							break;
	        						}
	        						baos.write(buf,0,count);
	        					}
	        				}					
	        				catch (IOException ex) {
	        					Log.e("AudioMMS", " exception", ex);
	        				}
	        		}
        			
					byte[] b = baos.toByteArray();
	
					String encodedBytes = Base64.encodeToString(b, Base64.URL_SAFE);
					encodedBytes = encodedBytes.replace('-', '+');
					encodedBytes = encodedBytes.replace('_', '/');
					encodedBytes = encodedBytes.replace("\n", "");
					attachBody.put("body", encodedBytes);
					attachBody.put("fileName", fileName);
					attachBody.put("content-type", contentType);
					attachBody.put("content-transfer-encoding", "BASE64");
					jattach.put(attachBody);
        		}
        	}
        body.put("messageContent", jattach);
        }
        
        jsonBody.put("messageRequest", body);
        
        final RESTClient rest = new RESTClient(endpoint)
            .setHeader("Accept", "application/json")
            .setHeader("Content-Type", "application/json")
            .addAuthorizationHeader(this.getToken());

        APIResponse response = null;
        JSONObject jobj = null;
        
        		try {
			response = rest.httpPost(jsonBody.toString());	
			jobj = new JSONObject(response.getResponseBody());	
			return SendResponse.valueOf(jobj);			
		} catch (RESTException e) {
			throw e;
			//e.printStackTrace();
		} 
    }
   
    public MessageList  getMessageList(int limit, int offset) throws RESTException, JSONException, ParseException {
        return getMessageList(new MessageListArgs.Builder(limit, offset).build());
    }
   
    public MessageList getMessageList(MessageListArgs args) throws RESTException, JSONException, ParseException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages";

        final RESTClient client = new RESTClient(endpoint)    
            .addAuthorizationHeader(getToken())
            .setHeader("Accept", "application/json")
            .setParameter("limit", "" + args.getLimit())
            .setParameter("offset", "" + args.getOffset());

        if (args.getMessageIds() != null) {
            String msgIds = StringUtils.join(args.getMessageIds(), ",");
            client.addParameter("messageIds", msgIds);
        }

        if (args.isFavorite() != null)
            client.addParameter("isFavorite", args.isFavorite() ? "true" : "false");

        if (args.isUnread() != null)
            client.addParameter("isUnread", args.isUnread() ? "true" : "false" );

        if (args.getType() != null)
            client.addParameter("type", args.getType().getString());

        if (args.getKeyword() != null)
            client.addParameter("keyword", args.getKeyword());

        if (args.isIncoming() != null)
            client.addParameter("isIncoming", args.isIncoming() ? "true" : "false" );

        APIResponse response  = null;
        JSONObject	jobj = null;
        
         try {
			response = client.httpGet();
			 jobj = new JSONObject(response.getResponseBody());
			return MessageList.valueOf(jobj);
		} catch (RESTException e) {
			// TODO Auto-generated catch block
			throw  e;	
		}
		
    }

    public Message getMessage(final String msgId) throws RESTException, JSONException, ParseException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages/" + msgId;

        final APIResponse response = new RESTClient(endpoint)
            .addAuthorizationHeader(getToken())
            .setHeader("Accept", "application/json")
            .httpGet();

        JSONObject jobj = new JSONObject(response.getResponseBody());
		return Message.valueOf(jobj.getJSONObject("message"));
    }

    public MessageContent getMessageContent(String msgId, String partNumber)
            throws RESTException {

        final String endpoint = getFQDN() + "/myMessages/v2/messages/" + msgId
                + "/parts/" + partNumber;
      
        final APIResponse getMessageContentResponse = new RESTClient(endpoint)
        .addAuthorizationHeader(getToken())
        .setHeader("Accept", "application/json")
        .httpGetMessageContent();

        String ctype = getMessageContentResponse.getHeader("Content-Type");
        String clength = getMessageContentResponse.getHeader("Content-Length");
        
		if ( Integer.parseInt(clength) > ( 1024 * 1024 ) ) {
			 throw new RESTException("Attachment exceeds size limit of 1MB");
		} else {
			InputStream stream = null;
			try {
				stream = getMessageContentResponse.getResponseStream();
			} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
				e1.printStackTrace();			
			}
			return new MessageContent(ctype, clength, stream);
		}
    }

    public DeltaResponseInternal getDelta(final String state) throws RESTException, JSONException, ParseException {
        final String endpoint = getFQDN() + "/myMessages/v2/delta";

        final APIResponse response = new RESTClient(endpoint)
            .addAuthorizationHeader(getToken())
            .setHeader("Accept", "application/json")
            .addParameter("state", state)
            .httpGet();

        JSONObject jobj = new JSONObject(response.getResponseBody());
		return DeltaResponseInternal.valueOf(jobj);
    }

    public void updateMessages(DeltaChange[] messages) throws RESTException, JSONException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages";

        JSONArray jmsgs = new JSONArray();
        for (final DeltaChange change : messages) {
            JSONObject jchange = new JSONObject();
            jchange.put("messageId", change.getMessageId());
            
            if (change.isUnread() != null)
                jchange.put("isUnread", change.isUnread());

            if (change.isFavorite() != null)
                jchange.put("isFavorite", change.isFavorite());

            jmsgs.put(jchange);
        }

        JSONObject jobj = new JSONObject();
        jobj.put("messages", jmsgs);

        final APIResponse response = new RESTClient(endpoint)
            .addAuthorizationHeader(getToken())
            .setHeader("Accept", "application/json")
            .setHeader("Content-Type", "application/json")
            .httpPut(jobj.toString());

        if (response.getStatusCode() != 204) {
            final int code = response.getStatusCode();
            final String body = response.getResponseBody();
            throw new RESTException(code, body);
        }
    }

    public void updateMessage(String msgId, Boolean isUnread,
            Boolean isFavorite) throws RESTException, JSONException {

        final String endpoint = getFQDN() + "/myMessages/v2/messages/" + msgId;

        JSONObject jmsg = new JSONObject();
        if (isUnread != null) jmsg.put("isUnread", isUnread);
        if (isFavorite != null) jmsg.put("isFavorite", isFavorite);
        
        JSONObject jobj = new JSONObject();
        jobj.put("message", jmsg);

        final APIResponse response = new RESTClient(endpoint)
            .addAuthorizationHeader(getToken())
            .setHeader("Accept", "application/json")
            .setHeader("Content-Type", "application/json")
            .httpPut(jobj.toString());

        if (response.getStatusCode() != 204) {
            final int code = response.getStatusCode();
            final String body = response.getResponseBody();
            throw new RESTException(code, body);
        }
    }

    public void deleteMessages(String[] msgIds) throws RESTException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages";

        String msgIdsStr = StringUtils.join(msgIds, ",");

        final APIResponse response = new RESTClient(endpoint)
            .setHeader("Accept", "application/json")
            .addAuthorizationHeader(getToken())
            .addParameter("messageIds", msgIdsStr)
            .httpDeleteMessages();

        if (response.getStatusCode() != 204) {
            final int code = response.getStatusCode();
            final String body = response.getResponseBody();
            throw new RESTException(code, body);
        }
    }

    public void deleteMessage(String msgId) throws RESTException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages/" + msgId;

        final APIResponse response = new RESTClient(endpoint)
            .setHeader("Accept", "application/json")
            .addAuthorizationHeader(getToken())
            .httpDeleteMessage();

        if (response.getStatusCode() != 204) {
            final int code = response.getStatusCode();
            final String body = response.getResponseBody();
            throw new RESTException(code, body);
        }
    }

    public void createMessageIndex() throws RESTException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages/index";

        final APIResponse response = new RESTClient(endpoint)
            .setHeader("Accept", "application/json")
            .addAuthorizationHeader(getToken())
            .httpPost();

        if (response.getStatusCode() != 202) {
            final int code = response.getStatusCode();
            final String body = response.getResponseBody();
            throw new RESTException(code, body);
        }
    }

    public MessageIndexInfo getMessageIndexInfo() throws RESTException, JSONException, ParseException {
        final String endpoint = getFQDN() + "/myMessages/v2/messages/index/info";

        final APIResponse response = new RESTClient(endpoint)
            .setHeader("Accept", "application/json")
            .addAuthorizationHeader(getToken())
            .httpGet();

        JSONObject jobj = new JSONObject(response.getResponseBody());

		return MessageIndexInfo.valueOf(jobj);
    }

}
