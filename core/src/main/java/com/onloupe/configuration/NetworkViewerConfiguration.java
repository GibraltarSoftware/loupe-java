package com.onloupe.configuration;

import java.util.Properties;

/**
 * Network Messenger Configuration
 */
public class NetworkViewerConfiguration implements IMessengerConfiguration {

	public NetworkViewerConfiguration() {

	}
	
	protected NetworkViewerConfiguration(Properties props) {
		setAllowLocalClients(Boolean.valueOf(props.getProperty("NetworkViewer.AllowLocalClients", String.valueOf(allowLocalClients))));
		setAllowRemoteClients(Boolean.valueOf(props.getProperty("NetworkViewer.AllowRemoteClients", String.valueOf(allowRemoteClients))));
		setMaxQueueLength(Integer.valueOf(props.getProperty("NetworkViewer.MaxQueueLength", String.valueOf(maxQueueLength))));
		setEnabled(Boolean.valueOf(props.getProperty("NetworkViewer.Enabled", String.valueOf(enabled))));
	}
	
	private NetworkViewerConfiguration(Builder builder) {
		this.allowLocalClients = builder.allowLocalClients;
		this.allowRemoteClients = builder.allowRemoteClients;
		this.maxQueueLength = builder.maxQueueLength;
		this.enabled = builder.enabled;
	}
	
	/**
	 * True by default, enables connecting a viewer on the local computer when true.
	 */
	private boolean allowLocalClients = true;

	public final boolean getAllowLocalClients() {
		return this.allowLocalClients;
	}

	public final void setAllowLocalClients(boolean value) {
		this.allowLocalClients = value;
	}

	/**
	 * False by default, enables connecting a viewer from another computer when
	 * true.
	 * 
	 * Requires a server configuration section
	 */
	private boolean allowRemoteClients = false;

	public final boolean getAllowRemoteClients() {
		return this.allowRemoteClients;
	}

	public final void setAllowRemoteClients(boolean value) {
		this.allowRemoteClients = value;
	}

	/**
	 * The maximum number of queued messages waiting to be processed by the network
	 * messenger
	 * 
	 * Once the total number of messages waiting to be processed exceeds the maximum
	 * queue length unsent messages will be dropped.
	 */
	private int maxQueueLength = 2000;

	@Override
	public final int getMaxQueueLength() {
		return this.maxQueueLength;
	}

	@Override
	public final void setMaxQueueLength(int value) {
		this.maxQueueLength = value;
	}

	/**
	 * False by default. When false, the network messenger is disabled even if
	 * otherwise configured.
	 * 
	 * This allows for explicit disable/enable without removing the existing
	 * configuration or worrying about the default configuration.
	 */
	private boolean enabled = true;

	@Override
	public final boolean getEnabled() {
		return this.enabled;
	}

	@Override
	public final void setEnabled(boolean value) {
		this.enabled = value;
	}

	/**
	 * When true, the session file will treat all write requests as write-through
	 * requests.
	 * 
	 * This overrides the write through request flag for all published requests,
	 * acting as if they are set true. This will slow down logging and change the
	 * degree of parallelism of multithreaded applications since each log message
	 * will block until it is committed.
	 */
	@Override
	public final boolean getForceSynchronous() {
		return false; // this messenger isn't safe in synchronous mode.
	}

	@Override
	public final void setForceSynchronous(boolean value) {
		throw new UnsupportedOperationException("The network messenger does not support synchronous operation.");
	}

	/**
	 * Normalize configuration
	 */
	public final void sanitize() {
		if (getMaxQueueLength() <= 0) {
			setMaxQueueLength(2000);
		} else if (getMaxQueueLength() > 50000) {
			setMaxQueueLength(50000);
		}
	}

	/**
	 * Creates builder to build {@link NetworkViewerConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link NetworkViewerConfiguration}.
	 */
	public static final class Builder {
		private boolean allowLocalClients;
		private boolean allowRemoteClients;
		private int maxQueueLength;
		private boolean enabled;

		private Builder() {
		}

		public Builder allowLocalClients(boolean allowLocalClients) {
			this.allowLocalClients = allowLocalClients;
			return this;
		}

		public Builder allowRemoteClients(boolean allowRemoteClients) {
			this.allowRemoteClients = allowRemoteClients;
			return this;
		}

		public Builder maxQueueLength(int maxQueueLength) {
			this.maxQueueLength = maxQueueLength;
			return this;
		}

		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public NetworkViewerConfiguration build() {
			return new NetworkViewerConfiguration(this);
		}
	}
}