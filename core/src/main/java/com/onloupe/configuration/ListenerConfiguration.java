package com.onloupe.configuration;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * Configuration information for the trace listener.
 */
public final class ListenerConfiguration {
	
	/**
	 * Instantiates a new listener configuration.
	 */
	public ListenerConfiguration() {

	}
	
	/**
	 * Instantiates a new listener configuration.
	 *
	 * @param props the props
	 */
	protected ListenerConfiguration(Properties props) {
		setAutoTraceRegistration(Boolean.valueOf(props.getProperty("Listener.AutoTraceRegistration", String.valueOf(autoTraceRegistration)))); 
		setEnableConsole(Boolean.valueOf(props.getProperty("Listener.EnableConsole", String.valueOf(enableConsole))));
		setEnableNetworkEvents(Boolean.valueOf(props.getProperty("Listener.EnableNetworkEvents", String.valueOf(enableNetworkEvents)))); 
		setEndSessionOnTraceClose(Boolean.valueOf(props.getProperty("Listener.EndSessionOnTraceClose", String.valueOf(endSessionOnTraceClose))));
	}
	
	/**
	 * Instantiates a new listener configuration.
	 *
	 * @param builder the builder
	 */
	private ListenerConfiguration(Builder builder) {
		this.autoTraceRegistration = builder.autoTraceRegistration;
		this.enableConsole = builder.enableConsole;
		this.enableNetworkEvents = builder.enableNetworkEvents;
		this.endSessionOnTraceClose = builder.endSessionOnTraceClose;
	}
	
	/**
	 * Configures whether Loupe should automatically make sure it is registered as a
	 * Trace Listener.
	 * 
	 * This is true by default to enable easy drop-in configuration (e.g. using the
	 * LiveLogViewer control on a form). Normally, it should not be necessary to
	 * disable this feature even when adding Loupe as a Trace Listener in an
	 * app.config or by code. But this setting can be configured to false if it is
	 * desirable to prevent Loupe from receiving Trace events directly, such as if
	 * the application is processing Trace events into the Loupe API itself.
	 */
	private boolean autoTraceRegistration = true;

	/**
	 * Gets the auto trace registration.
	 *
	 * @return the auto trace registration
	 */
	public boolean getAutoTraceRegistration() {
		return this.autoTraceRegistration;
	}

	/**
	 * Sets the auto trace registration.
	 *
	 * @param value the new auto trace registration
	 */
	public void setAutoTraceRegistration(boolean value) {
		this.autoTraceRegistration = value;
	}

	/**
	 * When true, anything written to the console out will be appended to the log.
	 * 
	 * This setting has no effect if the trace listener is not enabled.
	 */
	private boolean enableConsole = true;

	/**
	 * Gets the enable console.
	 *
	 * @return the enable console
	 */
	public boolean getEnableConsole() {
		return this.enableConsole;
	}

	/**
	 * Sets the enable console.
	 *
	 * @param value the new enable console
	 */
	public void setEnableConsole(boolean value) {
		this.enableConsole = value;
	}

	/**
	 * When true, network events (such as reconfiguration and disconnection) will be
	 * logged automatically.
	 */
	private boolean enableNetworkEvents = true;

	/**
	 * Gets the enable network events.
	 *
	 * @return the enable network events
	 */
	public boolean getEnableNetworkEvents() {
		return this.enableNetworkEvents;
	}

	/**
	 * Sets the enable network events.
	 *
	 * @param value the new enable network events
	 */
	public void setEnableNetworkEvents(boolean value) {
		this.enableNetworkEvents = value;
	}

	/**
	 * When true, the Loupe LogListener will end the Loupe log session when
	 * Trace.Close() is called.
	 * 
	 * This setting has no effect if the trace listener is not enabled. Unless
	 * disabled by setting this configuration value to false, a call to
	 * Trace.Close() to shutdown Trace logging will also be translated into a call
	 * to Gibraltar.Agent.Log.EndSession().
	 */
	private boolean endSessionOnTraceClose = true;

	/**
	 * Gets the end session on trace close.
	 *
	 * @return the end session on trace close
	 */
	public boolean getEndSessionOnTraceClose() {
		return this.endSessionOnTraceClose;
	}

	/**
	 * Sets the end session on trace close.
	 *
	 * @param value the new end session on trace close
	 */
	public void setEndSessionOnTraceClose(boolean value) {
		this.endSessionOnTraceClose = value;
	}

	/**
	 * Creates builder to build {@link ListenerConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link ListenerConfiguration}.
	 */
	public static final class Builder {
		
		/** The auto trace registration. */
		private boolean autoTraceRegistration;
		
		/** The enable console. */
		private boolean enableConsole;
		
		/** The enable network events. */
		private boolean enableNetworkEvents;
		
		/** The end session on trace close. */
		private boolean endSessionOnTraceClose;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
		}

		/**
		 * Auto trace registration.
		 *
		 * @param autoTraceRegistration the auto trace registration
		 * @return the builder
		 */
		public Builder autoTraceRegistration(boolean autoTraceRegistration) {
			this.autoTraceRegistration = autoTraceRegistration;
			return this;
		}

		/**
		 * Enable console.
		 *
		 * @param enableConsole the enable console
		 * @return the builder
		 */
		public Builder enableConsole(boolean enableConsole) {
			this.enableConsole = enableConsole;
			return this;
		}

		/**
		 * Enable network events.
		 *
		 * @param enableNetworkEvents the enable network events
		 * @return the builder
		 */
		public Builder enableNetworkEvents(boolean enableNetworkEvents) {
			this.enableNetworkEvents = enableNetworkEvents;
			return this;
		}

		/**
		 * End session on trace close.
		 *
		 * @param endSessionOnTraceClose the end session on trace close
		 * @return the builder
		 */
		public Builder endSessionOnTraceClose(boolean endSessionOnTraceClose) {
			this.endSessionOnTraceClose = endSessionOnTraceClose;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the listener configuration
		 */
		public ListenerConfiguration build() {
			return new ListenerConfiguration(this);
		}
	}
}