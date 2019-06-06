package com.onloupe.core.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.onloupe.core.NameValuePair;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.system.Version;


/**
 * Provides in-order communication with a remote web server.
 */
public class WebChannel implements IWebChannelConnection, Closeable {

	/** The Constant LOG_CATEGORY. */
	private static final String LOG_CATEGORY = "Loupe.Server.Client";
	
	/** The client. */
	private CloseableHttpClient client;
	
	/** The use SSL. */
	private boolean useSSL;
	
	/** The port. */
	private int port;
	
	/** The host name. */
	private String hostName;
	
	/** The application base directory. */
	private String applicationBaseDirectory;
	
	/** The use compatibility methods. */
	private boolean useCompatibilityMethods;
    
    /** The use http version 10. */
    private boolean useHttpVersion10 = false;
    
    /** The first request. */
    private boolean firstRequest = true;
    
    /** The enable logging. */
    private boolean enableLogging;
	
	/** The logger. */
	private IClientLogger logger;
	
	/** The app protocol version. */
	private Version appProtocolVersion;
	
	/** The authentication provider. */
	private IWebAuthenticationProvider authenticationProvider;
	
	/** The connection state. */
	private ChannelConnectionState connectionState;
	
	/** The server use compatibility setting. */
	private Map<String,Boolean> serverUseCompatibilitySetting = new HashMap<String,Boolean>();
	
	/** The server use http version 10 setting. */
	private Map<String,Boolean> serverUseHttpVersion10Setting = new HashMap<String,Boolean>();

	/**
	 * Instantiates a new web channel.
	 *
	 * @param logger the logger
	 * @param hostName the host name
	 */
	public WebChannel(IClientLogger logger, String hostName) {
		this(logger, false, hostName, null, null);
	}

	/**
	 * Instantiates a new web channel.
	 *
	 * @param logger the logger
	 * @param useSsl the use ssl
	 * @param hostName the host name
	 * @param applicationBaseDirectory the application base directory
	 * @param appProtocolVersion the app protocol version
	 */
	public WebChannel(IClientLogger logger, boolean useSsl, String hostName, String applicationBaseDirectory,
			Version appProtocolVersion) {
		this(logger, useSsl, hostName, useSsl ? 443 : 80, applicationBaseDirectory, appProtocolVersion);
	}

	/**
	 * Instantiates a new web channel.
	 *
	 * @param logger the logger
	 * @param useSsl the use ssl
	 * @param hostName the host name
	 * @param port the port
	 * @param applicationBaseDirectory the application base directory
	 * @param appProtocolVersion the app protocol version
	 */
	public WebChannel(IClientLogger logger, boolean useSsl, String hostName, int port, String applicationBaseDirectory,
			Version appProtocolVersion) {
		if (logger == null)
			throw new GibraltarException("Logger required.");

		if (hostName != null)
			this.hostName = hostName.trim();

		if (TypeUtils.isBlank(hostName))
			throw new GibraltarException("A server name must be provided for a connection");

		this.logger = logger;
		this.useSSL = useSsl;
		this.port = port;
		this.applicationBaseDirectory = applicationBaseDirectory;
		this.appProtocolVersion = appProtocolVersion;

		// format up base directory in case we get something we can't use. It has to
		// have leading & trailing slashes.
		if (TypeUtils.isNotBlank(applicationBaseDirectory)) {
			applicationBaseDirectory = applicationBaseDirectory.trim();
			if (!applicationBaseDirectory.startsWith("/")) {
				applicationBaseDirectory = "/" + applicationBaseDirectory;
			}

			if (!applicationBaseDirectory.endsWith("/")) {
				applicationBaseDirectory = applicationBaseDirectory + "/";
			}
		}

		this.client = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler(10, true))
				.setDefaultRequestConfig(getDefaultRequestConfig()).evictExpiredConnections()
				.evictIdleConnections(5, TimeUnit.SECONDS).build();

	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadData(java.lang.String)
	 */
	@Override
	public byte[] downloadData(String relativeUrl) throws URISyntaxException {
		return downloadData(relativeUrl, null, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadData(java.lang.String, java.lang.Integer)
	 */
	@Override
	public byte[] downloadData(String relativeUrl, Integer timeout) throws URISyntaxException {
		return downloadData(relativeUrl, null, timeout);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadData(java.lang.String, java.util.List)
	 */
	@Override
	public byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders)
			throws URISyntaxException {
		return downloadData(relativeUrl, additionalHeaders, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadData(java.lang.String, java.util.List, java.lang.Integer)
	 */
	@Override
	public byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders, Integer timeout)
			throws URISyntaxException {
		return execute(new HttpGet(preProcessURI(relativeUrl)), additionalHeaders, timeout);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadFile(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public void downloadFile(String relativeUrl, String destinationFileName, Integer timeout)
			throws URISyntaxException, IOException {
		try (RandomAccessFile file = new RandomAccessFile(destinationFileName, "rw")) {
			file.write(downloadData(relativeUrl, timeout));
		} catch (IOException e) {
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadString(java.lang.String)
	 */
	@Override
	public String downloadString(String relativeUrl) throws URISyntaxException {
		return downloadString(relativeUrl, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#downloadString(java.lang.String, java.lang.Integer)
	 */
	@Override
	public String downloadString(String relativeUrl, Integer timeout) throws URISyntaxException {
		return new String(downloadData(relativeUrl, timeout));
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadData(java.lang.String, java.lang.String, byte[], java.util.List)
	 */
	@Override
	public byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders) throws URISyntaxException {
		return uploadData(relativeUrl, contentType, data, additionalHeaders, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadData(java.lang.String, java.lang.String, byte[])
	 */
	@Override
	public byte[] uploadData(String relativeUrl, String contentType, byte[] data) throws URISyntaxException {
		return uploadData(relativeUrl, contentType, data, null, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadData(java.lang.String, java.lang.String, byte[], java.util.List, java.lang.Integer)
	 */
	@Override
	public byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException {
		return uploadData(relativeUrl, null, contentType, data, additionalHeaders, timeout);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadData(java.lang.String, java.lang.String, java.lang.String, byte[], java.util.List, java.lang.Integer)
	 */
	@Override
	public byte[] uploadData(String relativeUrl, String method, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException {
		if (TypeUtils.isBlank(method) || HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
			HttpPost request = new HttpPost(preProcessURI(relativeUrl));
			request.setEntity(new ByteArrayEntity(data, ContentType.parse(contentType)));
			return execute(request, additionalHeaders, timeout);
		} else if (HttpPut.METHOD_NAME.equalsIgnoreCase(method)) {
			HttpPut request = new HttpPut(preProcessURI(relativeUrl));
			request.setEntity(new ByteArrayEntity(data, ContentType.parse(contentType)));
			return execute(request, additionalHeaders, timeout);
		}
		throw new UnsupportedOperationException("Method " + method + " is invalid for uploadData.");
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#deleteData(java.lang.String)
	 */
	@Override
	public byte[] deleteData(String relativeUrl) throws URISyntaxException {
		return execute(new HttpDelete(preProcessURI(relativeUrl)), null, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadFile(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath)
			throws URISyntaxException, IOException {
		return uploadFile(relativeUrl, contentType, sourceFileNamePath, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadFile(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath, Integer timeout)
			throws URISyntaxException, IOException {
		try (RandomAccessFile file = new RandomAccessFile(sourceFileNamePath, "r")) {
			byte[] data = new byte[(int) file.length()];
			file.readFully(data);
			return uploadData(relativeUrl, contentType, data, null, timeout);
		} catch (IOException e) {
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadString(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String uploadString(String relativeUrl, String contentType, String data) throws URISyntaxException {
		return uploadString(relativeUrl, contentType, data, null);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#uploadString(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public String uploadString(String relativeUrl, String contentType, String data, Integer timeout)
			throws URISyntaxException {
		HttpPost request = new HttpPost(preProcessURI(relativeUrl));
		request.setEntity(new StringEntity(data, ContentType.parse(contentType)));
		return new String(execute(request, null, timeout));
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.server.IWebChannelConnection#executeRequest(com.onloupe.core.server.IWebRequest, int)
	 */
	@Override
	public void executeRequest(IWebRequest request, int retries) throws IOException, Exception {
		request.processRequest(this);
	}

	/**
	 * Execute.
	 *
	 * @param request the request
	 * @param additionalHeaders the additional headers
	 * @param timeout the timeout
	 * @return the byte[]
	 */
	private byte[] execute(HttpRequestBase request, List<NameValuePair<String>> additionalHeaders, Integer timeout) {
		request = preProcessRequest(request, additionalHeaders);
		
		if (timeout != null) {
			request.setConfig(RequestConfig.copy(getDefaultRequestConfig()).setConnectTimeout(timeout).build());
		}

		try (CloseableHttpResponse response = this.client.execute(request)) {
			ensureRequestSuccessful(response.getStatusLine(), request.getURI());
			return EntityUtils.toByteArray(response.getEntity());
		} catch (WebChannelMethodNotAllowedException e) {
			if (!useCompatibilityMethods)
            {
                //most likely we did a delete or put and the caller doesn't support that, enable compatibility methods.
                useCompatibilityMethods = true;
                setUseCompatiblilityMethodsOverride(hostName, useCompatibilityMethods);
                    //so we don't have to repeatedly run into this for this server
				if (enableLogging) {
					logger.write(LogMessageSeverity.INFORMATION, e, true, LOG_CATEGORY,
							"Switching to HTTP method compatibility mode",
							"Because we got an HTTP 405 error from the server we're going to turn on Http method compatibility translation and try again.  Status Description:\r\n%s",
							e.getMessage());
				}
            }
            throw e;
		} catch (WebChannelExpectationFailedException e) {
            if (!useHttpVersion10)
            {
                //most likely we are talking to an oddball proxy that doesn't support keepalive (like on a train or plane, seriously..)
                useHttpVersion10 = true;
                setUseHttpVersion10Override(hostName, useHttpVersion10);
                    //so we don't have to repeatedly run into this for this server
                if (enableLogging)
                    logger.write(LogMessageSeverity.INFORMATION, e, true, LOG_CATEGORY,
                        "Switching to HTTP 1.0 compatibility mode",
                        "Because we got an HTTP 417 error from the server we're going to turn on Http 1.0 compatibility translation and try again.  Status Description:\r\n%s",
                        e.getMessage());
            }
            throw e;
        } catch (UnsupportedOperationException | IOException e) {
			if (this.enableLogging)
				this.logger.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
						"Unable to authenticate communication channel",
						"There is no authentication provider available to process the current authentication request.");

			throw new GibraltarException(e);
		}
	}

	/**
	 * Ensure request successful.
	 *
	 * @param status the status
	 * @param requestUri the request uri
	 */
	private void ensureRequestSuccessful(StatusLine status, URI requestUri) {
		switch (status.getStatusCode()) {
		case HttpStatus.SC_NOT_FOUND:
			throw new WebChannelFileNotFoundException(status.getReasonPhrase(), status, requestUri);
		case HttpStatus.SC_UNAUTHORIZED:
			throw new WebChannelAuthorizationException(status.getReasonPhrase(), status, requestUri);
		case HttpStatus.SC_METHOD_NOT_ALLOWED:
			throw new WebChannelMethodNotAllowedException(status.getReasonPhrase(), status, requestUri);
		case HttpStatus.SC_EXPECTATION_FAILED:
			throw new WebChannelExpectationFailedException(status.getReasonPhrase(), status, requestUri);			
		}
	}

	/**
	 * Authenticate.
	 *
	 * @throws Exception the exception
	 */
	public void authenticate() throws Exception {
		if (this.enableLogging)
			this.logger.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
					"Attempting to authenticate communication channel", null);

		if (this.authenticationProvider == null) {
			if (this.enableLogging)
				this.logger.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY,
						"Unable to authenticate communication channel",
						"There is no authentication provider available to process the current authentication request.");
			return; // nothing to do.
		}

		this.authenticationProvider.login(this, this.client);
	}

	/**
	 * Gets the default request config.
	 *
	 * @return the default request config
	 */
	private static final RequestConfig getDefaultRequestConfig() {
		return RequestConfig.custom().setConnectTimeout(120).setSocketTimeout(60000).build();
	}

	/**
	 * Pre process URI.
	 *
	 * @param relativeUrl the relative url
	 * @return the uri
	 * @throws URISyntaxException the URI syntax exception
	 */
	private URI preProcessURI(String relativeUrl) throws URISyntaxException {
		try {
			return new URI(getBaseAddress() + relativeUrl);
		} catch (URISyntaxException e) {
			throw e;
		}
	}
	
	/**
	 * Pre process request.
	 *
	 * @param request the request
	 * @param additionalHeaders the additional headers
	 * @return the http request base
	 */
	private HttpRequestBase preProcessRequest(HttpRequestBase request, List<NameValuePair<String>> additionalHeaders) {
		if (additionalHeaders != null && !additionalHeaders.isEmpty()) {
			for (NameValuePair<String> header : additionalHeaders) {
				request.addHeader(new BasicHeader(header.getName(), header.getValue()));
			}
		}

		if (this.appProtocolVersion != null) {
			request.addHeader(new BasicHeader("X-Request-App-Protocol", this.appProtocolVersion.toString()));
		}

		request.addHeader(new BasicHeader("X-Request-Timestamp",
				OffsetDateTime.now(ZoneOffset.UTC).format(TimeConversion.CS_DATETIMEOFFSET_FORMAT)));
		
        if (firstRequest)
        {
            useCompatibilityMethods = getUseCompatiblilityMethodsOverride(hostName);
            useHttpVersion10 = getUseHttpVersion10Override(hostName);

            firstRequest = false;
        }
        
		request.setProtocolVersion((useHttpVersion10) ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1);
        
        if (useCompatibilityMethods)
        {
            if (HttpPut.METHOD_NAME.equals(request.getMethod()) || HttpDelete.METHOD_NAME.equals(request.getMethod()))
            {
            	HttpPost compatibleRequest = new HttpPost(request.getURI());
            	
                compatibleRequest.setHeaders(request.getAllHeaders());
                
                compatibleRequest.removeHeaders("X-Request-Method");
                compatibleRequest.addHeader(new BasicHeader("X-Request-Method", request.getMethod()));
                
                compatibleRequest.setProtocolVersion(request.getProtocolVersion());
                
                if (request instanceof HttpPut) {
                	HttpPut put = (HttpPut)request;
                	compatibleRequest.setEntity(put.getEntity());
                }
				
                return compatibleRequest;
            }
        }
		
		return request;
	}

	/**
	 * Gets the base address.
	 *
	 * @return the base address
	 */
	private String getBaseAddress() {
		boolean usePort = true;
		if ((!this.useSSL) && ((this.port == 0) || (this.port == 80))) {
			usePort = false;
		} else if ((this.useSSL) && ((this.port == 0) || (this.port == 443))) {
			usePort = false;
		}

		StringBuilder baseAddress = new StringBuilder(1024);

		baseAddress.append(getProtocolName() + "://" + this.hostName);

		if (usePort) {
			baseAddress.append(":" + this.port);
		}

		if (TypeUtils.isNotBlank(this.applicationBaseDirectory)) {
			baseAddress.append(this.applicationBaseDirectory);
		}

		return baseAddress.toString();
	}
	
	/**
	 * Gets the protocol name.
	 *
	 * @return the protocol name
	 */
	private String getProtocolName() {
		return (this.useSSL ? "https" : "http");
	}
	
    /**
     * Gets the use compatiblility methods override.
     *
     * @param server the server
     * @return the use compatiblility methods override
     */
    private Boolean getUseCompatiblilityMethodsOverride(String server)
    {
        //don't forget that we have to lock shared collections, they aren't thread safe
        Boolean useCompatibilityMethods = true; //in the end it was just too painful to get all those exceptions.
        synchronized (serverUseCompatibilitySetting)
        {
            if (serverUseCompatibilitySetting.containsKey(server))
            {
                useCompatibilityMethods = serverUseCompatibilitySetting.get(server);
            }

            serverUseCompatibilitySetting.notifyAll();;
        }

        return useCompatibilityMethods;
    }

    /**
     * Gets the use http version 10 override.
     *
     * @param server the server
     * @return the use http version 10 override
     */
    private Boolean getUseHttpVersion10Override(String server)
    {
        //don't forget that we have to lock shared collections, they aren't threadsafe
        Boolean useHttpVerison10 = useHttpVersion10;
        synchronized (serverUseHttpVersion10Setting)
        {
            if (serverUseHttpVersion10Setting.containsKey(server))
            {
                useHttpVerison10 = serverUseHttpVersion10Setting.get(server);
            }

            serverUseHttpVersion10Setting.notifyAll();;
        }

        return useHttpVerison10;
    }
    
    /**
     * Sets the use compatiblility methods override.
     *
     * @param server the server
     * @param useCompatibilityMethods the use compatibility methods
     */
    private void setUseCompatiblilityMethodsOverride(String server, Boolean useCompatibilityMethods)
    {
        //remember: generic collections are not thread safe.
        synchronized (serverUseCompatibilitySetting)
        {
            if (serverUseCompatibilitySetting.containsKey(server))
            {
                serverUseCompatibilitySetting.replace(server, useCompatibilityMethods);
            }
            else
            {
                serverUseCompatibilitySetting.put(server, useCompatibilityMethods);
            }

            serverUseCompatibilitySetting.notifyAll();
        }
    }
    
    /**
     * Sets the use http version 10 override.
     *
     * @param server the server
     * @param useHttpVersion10 the use http version 10
     */
    private void setUseHttpVersion10Override(String server, Boolean useHttpVersion10)
    {
        //remember: generic collections are not threadsafe.
        synchronized (serverUseHttpVersion10Setting)
        {
            if (serverUseHttpVersion10Setting.containsKey(server))
            {
                serverUseHttpVersion10Setting.replace(server, useHttpVersion10);
            }
            else
            {
                serverUseHttpVersion10Setting.put(server, useHttpVersion10);
            }

            serverUseHttpVersion10Setting.notifyAll();
        }
    }

	/**
	 * Gets the entry URI.
	 *
	 * @return the entry URI
	 */
	public String getEntryURI() {
		return getBaseAddress().toString();
	}

	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
	public String getHostName() {
		return this.hostName;
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(this.client);
	}

	/**
	 * Checks if is enable logging.
	 *
	 * @return true, if is enable logging
	 */
	public boolean isEnableLogging() {
		return this.enableLogging;
	}

	/**
	 * Sets the enable logging.
	 *
	 * @param enableLogging the new enable logging
	 */
	public void setEnableLogging(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}

	/**
	 * Gets the client.
	 *
	 * @return the client
	 */
	public CloseableHttpClient getClient() {
		return this.client;
	}

	/**
	 * Checks if is use SSL.
	 *
	 * @return true, if is use SSL
	 */
	public boolean isUseSSL() {
		return this.useSSL;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Gets the application base directory.
	 *
	 * @return the application base directory
	 */
	public String getApplicationBaseDirectory() {
		return this.applicationBaseDirectory;
	}

	/**
	 * Checks if is use compatibility methods.
	 *
	 * @return true, if is use compatibility methods
	 */
	public boolean isUseCompatibilityMethods() {
		return this.useCompatibilityMethods;
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public IClientLogger getLogger() {
		return this.logger;
	}

	/**
	 * Gets the app protocol version.
	 *
	 * @return the app protocol version
	 */
	public Version getAppProtocolVersion() {
		return this.appProtocolVersion;
	}

	/**
	 * Gets the authentication provider.
	 *
	 * @return the authentication provider
	 */
	public IWebAuthenticationProvider getAuthenticationProvider() {
		return this.authenticationProvider;
	}

	/**
	 * Sets the authentication provider.
	 *
	 * @param authenticationProvider the new authentication provider
	 */
	public void setAuthenticationProvider(IWebAuthenticationProvider authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}

	/**
	 * Gets the connection state.
	 *
	 * @return the connection state
	 */
	public ChannelConnectionState getConnectionState() {
		return this.connectionState;
	}

	/**
	 * Sets the connection state.
	 *
	 * @param connectionState the new connection state
	 */
	public void setConnectionState(ChannelConnectionState connectionState) {
		this.connectionState = connectionState;
	}

}