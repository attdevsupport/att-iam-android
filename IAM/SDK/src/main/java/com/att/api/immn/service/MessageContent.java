package com.att.api.immn.service;

import java.io.InputStream;

public final class MessageContent {
    private final String contentType;
    private final String contentLength;
    private final InputStream stream;

    public MessageContent(String ctype, String clength, InputStream stream) {
        this.contentType = ctype;
        this.contentLength = clength;
        this.stream = stream;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentLength() {
        return contentLength;
    }

       public InputStream getStream() {
    	return stream;
    }
}
