package com.att.api.rest;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


/*
 * added to solve the trusted authentication exception while sending data to
 * server and thread-safe client singleton.
 * 
 * @author ATT
 */
public class HttpClientFactory {
	private static DefaultHttpClient client;
	private static SSLSocketFactory sslSocketFactory;

	/*
	 * Creates a new HTTP client from parameters and a new thread safe
	 * connection manager.
	 * 
	 * @return client
	 */
	public synchronized static DefaultHttpClient getThreadSafeClient() {
		if (client != null) {
			return client;
		}
		client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params,Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params,Constants.SOCKET_TIMEOUT);
		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,getAdSchemeRegistry(client)), params);
		return client;
	}

	/*
	 * Creates a new HTTP client from parameters and a new thread safe
	 * connection manager.
	 * 
	 * @return client
	 */
	public synchronized static DefaultHttpClient getSimpleClient() {
		if (client != null) {
			return client;
		}
		client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params,Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params,Constants.SOCKET_TIMEOUT);
		//client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,getAdSchemeRegistry(client)), params);
		return client;
	}	
	/*
	 * Reset the HTTP client and clear the cookie store.
	 */
	public static void resetClient() {
		DefaultHttpClient httpClient = HttpClientFactory.getThreadSafeClient();
		if (httpClient != null && httpClient.getCookieStore() != null) {
			httpClient.getCookieStore().clear();
		}
	}

	/*
	 * get the scheme register used by this connection manager.
	 * 
	 * @param client
	 * @return
	 */
	private static SchemeRegistry getAdSchemeRegistry(DefaultHttpClient client) {
		if (sslSocketFactory == null) {
			return client.getConnectionManager().getSchemeRegistry();
		} else {
			SchemeRegistry registry = new SchemeRegistry();
			// The default port 80 for this SocketFactory scheme.
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			// The default port 443 for this SSLSocketFactory scheme.
			registry.register(new Scheme("https", sslSocketFactory, 443));
			return registry;
		}
	}

	/*
	 * set the SSLSocketFactory can be used to validate the identity of the
	 * HTTPS server against a list of trusted certificates and to authenticate
	 * to the HTTPS server using a private key.
	 * 
	 * 
	 * @param socketFactory
	 */
	public static void setSslSocketFactory(SSLSocketFactory socketFactory) {
		sslSocketFactory = socketFactory;
	}
}
