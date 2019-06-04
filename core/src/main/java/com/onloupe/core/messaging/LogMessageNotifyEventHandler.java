package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
/**
 * Handler type for a LogMessage notify event.
 * 
 */
@FunctionalInterface
public interface LogMessageNotifyEventHandler {
	
	/**
	 * Invoke.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	void invoke(Object sender, LogMessageNotifyEventArgs e);
}