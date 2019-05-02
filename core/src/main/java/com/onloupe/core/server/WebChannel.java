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

	private static final String LOG_CATEGORY = "Loupe.Server.Client";
	
	private CloseableHttpClient client;
	private boolean useSSL;
	private int port;
	private String hostName;
	private String applicationBaseDirectory;
	private boolean useCompatibilityMethods;
    private boolean useHttpVersion10 = false;
    private boolean firstRequest = true;
    private boolean enableLogging;
	private IClientLogger logger;
	private Version appProtocolVersion;
	private IWebAuthenticationProvider authenticationProvider;
	private ChannelConnectionState connectionState;
	
	private Map<String,Boolean> serverUseCompatibilitySetting = new HashMap<String,Boolean>();
	private Map<String,Boolean> serverUseHttpVersion10Setting = new HashMap<String,Boolean>();

	public WebChannel(IClientLogger logger, String hostName) {
		this(logger, false, hostName, null, null);
	}

	public WebChannel(IClientLogger logger, boolean useSsl, String hostName, String applicationBaseDirectory,
			Version appProtocolVersion) {
		this(logger, useSsl, hostName, useSsl ? 443 : 80, applicationBaseDirectory, appProtocolVersion);
	}

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

	@Override
	public byte[] downloadData(String relativeUrl) throws URISyntaxException {
		return downloadData(relativeUrl, null, null);
	}

	@Override
	public byte[] downloadData(String relativeUrl, Integer timeout) throws URISyntaxException {
		return downloadData(relativeUrl, null, timeout);
	}

	@Override
	public byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders)
			throws URISyntaxException {
		return downloadData(relativeUrl, additionalHeaders, null);
	}

	@Override
	public byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders, Integer timeout)
			throws URISyntaxException {
		return execute(new HttpGet(preProcessURI(relativeUrl)), additionalHeaders, timeout);
	}

	@Override
	public void downloadFile(String relativeUrl, String destinationFileName, Integer timeout)
			throws URISyntaxException, IOException {
		try (RandomAccessFile file = new RandomAccessFile(destinationFileName, "rw")) {
			file.write(downloadData(relativeUrl, timeout));
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public String downloadString(String relativeUrl) throws URISyntaxException {
		return downloadString(relativeUrl, null);
	}

	@Override
	public String downloadString(String relativeUrl, Integer timeout) throws URISyntaxException {
		return new String(downloadData(relativeUrl, timeout));
	}

	@Override
	public byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders) throws URISyntaxException {
		return uploadData(relativeUrl, contentType, data, additionalHeaders, null);
	}

	@Override
	public byte[] uploadData(String relativeUrl, String contentType, byte[] data) throws URISyntaxException {
		return uploadData(relativeUrl, contentType, data, null, null);
	}

	@Override
	public byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException {
		return uploadData(relativeUrl, null, contentType, data, additionalHeaders, timeout);
	}

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

	@Override
	public byte[] deleteData(String relativeUrl) throws URISyntaxException {
		return execute(new HttpDelete(preProcessURI(relativeUrl)), null, null);
	}

	@Override
	public byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath)
			throws URISyntaxException, IOException {
		return uploadFile(relativeUrl, contentType, sourceFileNamePath, null);
	}

	@Override
	public byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath, Integer timeout)
			throws URISyntaxException, IOException {
		try (RandomAccessFile file = new RandomAccessFile(sourceFileNamePath, "r")) {
			// TODO RKELLIHER not sure if this will be an issue, discuss
			byte[] data = new byte[(int) file.length()];
			file.readFully(data);
			return uploadData(relativeUrl, contentType, data, null, timeout);
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public String uploadString(String relativeUrl, String contentType, String data) throws URISyntaxException {
		return uploadString(relativeUrl, contentType, data, null);
	}

	@Override
	public String uploadString(String relativeUrl, String contentType, String data, Integer timeout)
			throws URISyntaxException {
		HttpPost request = new HttpPost(preProcessURI(relativeUrl));
		request.setEntity(new StringEntity(data, ContentType.parse(contentType)));
		return new String(execute(request, null, timeout));
	}

	@Override
	public void executeRequest(IWebRequest request, int retries) throws IOException, Exception {
		request.processRequest(this);
	}

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

	private static final RequestConfig getDefaultRequestConfig() {
		return RequestConfig.custom().setConnectTimeout(120).setSocketTimeout(60000).build();
	}

	private URI preProcessURI(String relativeUrl) throws URISyntaxException {
		try {
			return new URI(getBaseAddress() + relativeUrl);
		} catch (URISyntaxException e) {
			throw e;
		}
	}
	
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
	
	private String getProtocolName() {
		return (this.useSSL ? "https" : "http");
	}
	
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

	public String getEntryURI() {
		return getBaseAddress().toString();
	}

	public String getHostName() {
		return this.hostName;
	}

	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(this.client);
	}

	public boolean isEnableLogging() {
		return this.enableLogging;
	}

	public void setEnableLogging(boolean enableLogging) {
		this.enableLogging = enableLogging;
	}

	public CloseableHttpClient getClient() {
		return this.client;
	}

	public boolean isUseSSL() {
		return this.useSSL;
	}

	public int getPort() {
		return this.port;
	}

	public String getApplicationBaseDirectory() {
		return this.applicationBaseDirectory;
	}

	public boolean isUseCompatibilityMethods() {
		return this.useCompatibilityMethods;
	}

	public IClientLogger getLogger() {
		return this.logger;
	}

	public Version getAppProtocolVersion() {
		return this.appProtocolVersion;
	}

	public IWebAuthenticationProvider getAuthenticationProvider() {
		return this.authenticationProvider;
	}

	public void setAuthenticationProvider(IWebAuthenticationProvider authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}

	public ChannelConnectionState getConnectionState() {
		return this.connectionState;
	}

	public void setConnectionState(ChannelConnectionState connectionState) {
		this.connectionState = connectionState;
	}

}