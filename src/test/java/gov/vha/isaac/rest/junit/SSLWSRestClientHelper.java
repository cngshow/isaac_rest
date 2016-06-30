package gov.vha.isaac.rest.junit;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.testng.log4testng.Logger;

public class SSLWSRestClientHelper
{
	private static final Logger log = Logger.getLogger(SSLWSRestClientHelper.class);

	public static Client configureClient()
	{
		TrustManager[] certs = new TrustManager[] { new X509TrustManager()
		{
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
			{
			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
			{
			}

			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
		}};
		
		SSLContext sslContext = null;
		try
		{
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, certs, new SecureRandom());
		}
		catch (java.security.GeneralSecurityException ex)
		{
			log.error("Error:", ex);
			throw new RuntimeException(ex);
		}
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		HostnameVerifier hv = new HostnameVerifier()
		{
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			}
		};

		return ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(hv).build();

	}

	public WebTarget getWebTarget(String wsUri)
	{
		return SSLWSRestClientHelper.configureClient().target(wsUri);
	}
}
