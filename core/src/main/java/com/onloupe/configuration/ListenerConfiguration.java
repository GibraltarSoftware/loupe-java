package com.onloupe.configuration;

import java.util.Properties;

/**
 * Configuration information for the trace listener.
 */
public final class ListenerConfiguration {
	
	public ListenerConfiguration() {

	}
	
	protected ListenerConfiguration(Properties props) {
		setAutoTraceRegistration(Boolean.valueOf(props.getProperty("Listener.AutoTraceRegistration", String.valueOf(autoTraceRegistration)))); 
		setEnableConsole(Boolean.valueOf(props.getProperty("Listener.EnableConsole", String.valueOf(enableConsole))));
		setEnableNetworkEvents(Boolean.valueOf(props.getProperty("Listener.EnableNetworkEvents", String.valueOf(enableNetworkEvents)))); 
		setEndSessionOnTraceClose(Boolean.valueOf(props.getProperty("Listener.EndSessionOnTraceClose", String.valueOf(endSessionOnTraceClose))));
	}
	
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

	public boolean getAutoTraceRegistration() {
		return this.autoTraceRegistration;
	}

	public void setAutoTraceRegistration(boolean value) {
		this.autoTraceRegistration = value;
	}

	/**
	 * When true, anything written to the console out will be appended to the log.
	 * 
	 * This setting has no effect if the trace listener is not enabled.
	 */
	private boolean enableConsole = true;

	public boolean getEnableConsole() {
		return this.enableConsole;
	}

	public void setEnableConsole(boolean value) {
		this.enableConsole = value;
	}

	/**
	 * When true, network events (such as reconfiguration and disconnection) will be
	 * logged automatically.
	 */
	private boolean enableNetworkEvents = true;

	public boolean getEnableNetworkEvents() {
		return this.enableNetworkEvents;
	}

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

	public boolean getEndSessionOnTraceClose() {
		return this.endSessionOnTraceClose;
	}

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
		private boolean autoTraceRegistration;
		private boolean enableConsole;
		private boolean enableNetworkEvents;
		private boolean endSessionOnTraceClose;

		private Builder() {
		}

		public Builder autoTraceRegistration(boolean autoTraceRegistration) {
			this.autoTraceRegistration = autoTraceRegistration;
			return this;
		}

		public Builder enableConsole(boolean enableConsole) {
			this.enableConsole = enableConsole;
			return this;
		}

		public Builder enableNetworkEvents(boolean enableNetworkEvents) {
			this.enableNetworkEvents = enableNetworkEvents;
			return this;
		}

		public Builder endSessionOnTraceClose(boolean endSessionOnTraceClose) {
			this.endSessionOnTraceClose = endSessionOnTraceClose;
			return this;
		}

		public ListenerConfiguration build() {
			return new ListenerConfiguration(this);
		}
	}
}