package com.onloupe.core.messaging;

/**
 * Handler type for a LogMessage notify event.
 * 
 * @param sender The sender of this LogMessage notify event.
 * @param e      The LogMessageNotifyEventArgs.
 */
@FunctionalInterface
public interface LogMessageNotifyEventHandler {
	void invoke(Object sender, LogMessageNotifyEventArgs e);
}