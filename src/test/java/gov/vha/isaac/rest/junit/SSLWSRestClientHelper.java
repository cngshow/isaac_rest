package gov.vha.isaac.rest.junit;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.testng.log4testng.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class SSLWSRestClientHelper {
	private static final Logger log = Logger.getLogger(SSLWSRestClientHelper.class);

	public static ClientConfig configureClient() {
		TrustManager[] certs = new TrustManager[] { new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException { }

			public X509Certificate[] getAcceptedIssuers() { return null; }
		} };
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, certs, new SecureRandom());
		} catch (java.security.GeneralSecurityException ex) {
			log.error("Error:", ex);
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

		ClientConfig config = new DefaultClientConfig();
		try {
			config.getProperties().put( HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) { return true; }
					}, ctx));
		} catch (Exception e) {
			log.error("Error:", e);
		}
		return config;
	}

    public WebResource getWebResource(String wsUri) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, Integer connectTimeoutMillis, Integer readTimeoutMillis) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
        client.setConnectTimeout(connectTimeoutMillis);
        client.setReadTimeout(readTimeoutMillis);
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, String username, String password) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
    	setHTTPBasicAuthFilter(client, username, password);
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, String username, String password, Integer connectTimeoutMillis, Integer readTimeoutMillis) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
    	setHTTPBasicAuthFilter(client, username, password);
        client.setConnectTimeout(connectTimeoutMillis);
        client.setReadTimeout(readTimeoutMillis);
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, Map<String, List<Object>> requestHeaders) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
    	addHeaders(client, requestHeaders);
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, Map<String, List<Object>> requestHeaders, Integer connectTimeoutMillis, Integer readTimeoutMillis) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
    	addHeaders(client, requestHeaders);
        client.setConnectTimeout(connectTimeoutMillis);
        client.setReadTimeout(readTimeoutMillis);
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, String username, String password, Map<String, List<Object>> requestHeaders) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
    	setHTTPBasicAuthFilter(client, username, password);
    	addHeaders(client, requestHeaders);
        return client.resource(wsUri);
    }
    
    public WebResource getWebResource(String wsUri, String username, String password, Map<String, List<Object>> requestHeaders, Integer connectTimeoutMillis, Integer readTimeoutMillis) {
    	Client client = Client.create(SSLWSRestClientHelper.configureClient());
    	setHTTPBasicAuthFilter(client, username, password);
    	addHeaders(client, requestHeaders);
        client.setConnectTimeout(connectTimeoutMillis);
        client.setReadTimeout(readTimeoutMillis);
        return client.resource(wsUri);
    }
    
    public static void setHTTPBasicAuthFilter(Client client, String username, String password) {
    	if(client != null && username != null && password != null) {
    		client.addFilter(new HTTPBasicAuthFilter(username, password));
    	}
    }
    
    public static void addHeaders(Client client, Map<String, List<Object>> requestHeaders) {
    	if(client != null && requestHeaders != null && !requestHeaders.isEmpty()) {
    		Set<String> keySet = requestHeaders.keySet();
    		for(final String key : keySet) {
    			final List<Object> valueList = requestHeaders.get(key);
    			client.addFilter(new ClientFilter() {
    	    		@Override
    	    		public ClientResponse handle(final ClientRequest request) {
    	    			final Map<String, List<Object>> headers = request.getHeaders();
    	    			headers.put(key, valueList);
    	    			return getNext().handle(request);
    	    		}
    	    	});
    		}
    	}
    }
}
