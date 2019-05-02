package com.onloupe.core.messaging;

/**
 * EventArgs for LogMessage notify events.
 */
public class LogMessageNotifyEventArgs {
	/**
	 * The IMessengerPacket for the log message being notified about.
	 */
	public IMessengerPacket packet;

	public LogMessageNotifyEventArgs(IMessengerPacket packet) {
		this.packet = packet;
	}
}