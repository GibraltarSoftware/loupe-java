package com.onloupe.core.logging;

import com.onloupe.configuration.AgentConfiguration;

/**
 * Event arguments for the Log.Initializing event of the Gibraltar Agent Logging
 * class.
 */
public class LogInitializingEventArgs {
	public LogInitializingEventArgs(AgentConfiguration configuration) {
		setConfiguration(configuration);
	}

	/**
	 * If set to true the initialization process will not complete and the agent
	 * will stay dormant.
	 */
	private boolean cancel;

	public final boolean getCancel() {
		return this.cancel;
	}

	public final void setCancel(boolean value) {
		this.cancel = value;
	}

	/**
	 * The configuration for the agent to start with
	 * 
	 * The configuration will reflect the effect of the current application
	 * configuration file and Agent default values.
	 */
	private AgentConfiguration configuration;

	public final AgentConfiguration getConfiguration() {
		return this.configuration;
	}

	private void setConfiguration(AgentConfiguration value) {
		this.configuration = value;
	}
}