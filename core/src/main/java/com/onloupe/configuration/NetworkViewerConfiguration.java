package com.onloupe.configuration;

import java.util.Properties;


/**
 * Network Messenger Configuration.
 */
public class NetworkViewerConfiguration implements IMessengerConfiguration {

	/**
	 * Instantiates a new network viewer configuration.
	 */
	public NetworkViewerConfiguration() {

	}
	
	/**
	 * Instantiates a new network viewer configuration.
	 *
	 * @param props the props
	 */
	protected NetworkViewerConfiguration(Properties props) {
		setAllowLocalClients(Boolean.valueOf(props.getProperty("NetworkViewer.AllowLocalClients", String.valueOf(allowLocalClients))));
		setAllowRemoteClients(Boolean.valueOf(props.getProperty("NetworkViewer.AllowRemoteClients", String.valueOf(allowRemoteClients))));
		setMaxQueueLength(Integer.valueOf(props.getProperty("NetworkViewer.MaxQueueLength", String.valueOf(maxQueueLength))));
		setEnabled(Boolean.valueOf(props.getProperty("NetworkViewer.Enabled", String.valueOf(enabled))));
	}
	
	/**
	 * Instantiates a new network viewer configuration.
	 *
	 * @param builder the builder
	 */
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

	/**
	 * Gets the allow local clients.
	 *
	 * @return the allow local clients
	 */
	public final boolean getAllowLocalClients() {
		return this.allowLocalClients;
	}

	/**
	 * Sets the allow local clients.
	 *
	 * @param value the new allow local clients
	 */
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

	/**
	 * Gets the allow remote clients.
	 *
	 * @return the allow remote clients
	 */
	public final boolean getAllowRemoteClients() {
		return this.allowRemoteClients;
	}

	/**
	 * Sets the allow remote clients.
	 *
	 * @param value the new allow remote clients
	 */
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

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#getMaxQueueLength()
	 */
	@Override
	public final int getMaxQueueLength() {
		return this.maxQueueLength;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#setMaxQueueLength(int)
	 */
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

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#getEnabled()
	 */
	@Override
	public final boolean getEnabled() {
		return this.enabled;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#setEnabled(boolean)
	 */
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
	 *
	 * @return the force synchronous
	 */
	@Override
	public final boolean getForceSynchronous() {
		return false; // this messenger isn't safe in synchronous mode.
	}

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#setForceSynchronous(boolean)
	 */
	@Override
	public final void setForceSynchronous(boolean value) {
		throw new UnsupportedOperationException("The network messenger does not support synchronous operation.");
	}

	/**
	 * Normalize configuration.
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
		
		/** The allow local clients. */
		private boolean allowLocalClients;
		
		/** The allow remote clients. */
		private boolean allowRemoteClients;
		
		/** The max queue length. */
		private int maxQueueLength;
		
		/** The enabled. */
		private boolean enabled;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
		}

		/**
		 * Allow local clients.
		 *
		 * @param allowLocalClients the allow local clients
		 * @return the builder
		 */
		public Builder allowLocalClients(boolean allowLocalClients) {
			this.allowLocalClients = allowLocalClients;
			return this;
		}

		/**
		 * Allow remote clients.
		 *
		 * @param allowRemoteClients the allow remote clients
		 * @return the builder
		 */
		public Builder allowRemoteClients(boolean allowRemoteClients) {
			this.allowRemoteClients = allowRemoteClients;
			return this;
		}

		/**
		 * Max queue length.
		 *
		 * @param maxQueueLength the max queue length
		 * @return the builder
		 */
		public Builder maxQueueLength(int maxQueueLength) {
			this.maxQueueLength = maxQueueLength;
			return this;
		}

		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the network viewer configuration
		 */
		public NetworkViewerConfiguration build() {
			return new NetworkViewerConfiguration(this);
		}
	}
}