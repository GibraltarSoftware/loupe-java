package com.onloupe.core.monitor;

import com.onloupe.model.log.ILogMessage;


/**
 * EventArgs for Message Filter events.
 */
public class MessageFilterEventArgs {
	/**
	 * A new log message received for possible display by the (LiveLogViewer) sender
	 * of this event.
	 */
	public ILogMessage message;

	/**
	 * Cancel (block) this message from being displayed to users by the
	 * (LiveLogViewer) sender of this event.
	 */
	public boolean cancel;

	/**
	 * Instantiates a new message filter event args.
	 *
	 * @param message the message
	 */
	public MessageFilterEventArgs(ILogMessage message) {
		this.message = message;
		this.cancel = false;
	}
}