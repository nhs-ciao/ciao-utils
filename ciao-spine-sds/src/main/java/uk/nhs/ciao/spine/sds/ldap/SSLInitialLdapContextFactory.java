package uk.nhs.ciao.spine.sds.ldap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.ldap.InitialLdapContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.camel.util.jsse.SSLContextParameters;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring factory bean to construct SSL {@link InitialLdapContext} instances configured via Camel {@link SSLContextParameters}.
 * <p>
 * Configures the context with a backing {@link SSLSocketFactoryWrapper}.
 * <p>
 * <strong>Due to the use of a static delegate, only one socket factory instance can be configured per JVM.</strong>
 */
public class SSLInitialLdapContextFactory implements FactoryBean<InitialLdapContext> {
	private SSLContextParameters sslContextParameters;
	private final Hashtable<String, Object> environment = new Hashtable<String, Object>();
	
	@Override
	public boolean isSingleton() {
		return false;
	}
	
	@Override
	public Class<?> getObjectType() {
		return InitialLdapContext.class;
	}
	
	@Override
	public InitialLdapContext getObject() throws Exception {
		configureSDSSSLSocketFactory();		
		configureEnvironment();
		
		return new InitialLdapContext(environment, null);
	}
	
	public void setEnvironment(final Map<String, Object> environment) {
		this.environment.clear();
		this.environment.putAll(environment);
	}
	
	public void setSslContextParameters(final SSLContextParameters sslContextParameters) {
		this.sslContextParameters = sslContextParameters;
	}
	
	private void configureSDSSSLSocketFactory() throws GeneralSecurityException, IOException {
		if (SSLSocketFactoryWrapper.getDelegate() == null) {
			final SSLSocketFactory delegate = sslContextParameters.createSSLContext().getSocketFactory();
			SSLSocketFactoryWrapper.setDelegate(delegate);
		}
	}
	
	private void configureEnvironment() {
		environment.put("java.naming.ldap.factory.socket", SSLSocketFactoryWrapper.class.getCanonicalName());
	}
	
	/**
	 * SSLSocketFactory wrapper around a single <strong>static</strong> delegate SSLSocketFactory.
	 * <p>
	 * Required in order to inject SSL properties into an InitialLdapContext via the
	 * <code>java.naming.ldap.factory.socket</code> property. Due to the static nature of
	 * the wrapped delegate - each created SDS SSLSocketFactory is backed by the <strong>same</strong>
	 * factory instance.
	 * <p>
	 * Before using an SDS SSLSocketFactory instance, the delegate MUST be configured via {@link #setDelegate(SSLSocketFactory)}
	 * 
	 * @see http://camel.apache.org/ldap.html
	 */
	public static class SSLSocketFactoryWrapper extends SSLSocketFactory {
		private static volatile SSLSocketFactory DELEGATE;
		
		public static SSLSocketFactory getDelegate() {
			return DELEGATE;
		}
		
		public static void setDelegate(final SSLSocketFactory delegate) {
			DELEGATE = delegate;
		}

		@Override
		public Socket createSocket() throws IOException {
			return DELEGATE.createSocket();
		}

		@Override
		public Socket createSocket(final InetAddress address, final int port,
				final InetAddress localAddress, final int localPort) throws IOException {
			return DELEGATE.createSocket(address, port, localAddress, localPort);
		}

		@Override
		public Socket createSocket(final InetAddress host, final int port) throws IOException {
			return DELEGATE.createSocket(host, port);
		}

		@Override
		public Socket createSocket(final String host, final int port, final InetAddress localHost,
				final int localPort) throws IOException, UnknownHostException {
			return DELEGATE.createSocket(host, port, localHost, localPort);
		}

		@Override
		public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
			return DELEGATE.createSocket(host, port);
		}
		
		@Override
		public Socket createSocket(final Socket s, final String host, final int port,
				final boolean autoClose) throws IOException {
			return DELEGATE.createSocket(s, host, port, autoClose);
		}

		@Override
		public String[] getDefaultCipherSuites() {
			return DELEGATE.getDefaultCipherSuites();
		}

		@Override
		public String[] getSupportedCipherSuites() {
			return DELEGATE.getSupportedCipherSuites();
		}
	}
}
