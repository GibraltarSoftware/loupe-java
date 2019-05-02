package com.onloupe.core.server;

import com.onloupe.core.server.data.DataConverter;
import com.onloupe.core.server.data.HubConfigurationXml;

/**
 * Get the current hub configuration information for the hub
 * 
 * We rely on this being anonymously accessible. First, for performance reasons
 * and second because it's used as a Ping by the agent.
 */
public class HubConfigurationGetRequest extends WebChannelRequestBase {
	/**
	 * Create a new sessions version request
	 */
	public HubConfigurationGetRequest() {
		super(false, false);
	}

	/**
	 * The current hub configuration from the hub.
	 */
	private HubConfigurationXml configuration;

	public final HubConfigurationXml getConfiguration() {
		return this.configuration;
	}

	private void setConfiguration(HubConfigurationXml value) {
		this.configuration = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 * 
	 * @param connection
	 * @throws Exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws Exception {
		byte[] requestedHubConfigurationRawData = connection.downloadData("/Hub/Configuration.xml");

		// and now do it without using XMLSerializer since that doesn't work in the
		// agent.
		setConfiguration(DataConverter.byteArrayToHubConfigurationXml(requestedHubConfigurationRawData));
	}
}