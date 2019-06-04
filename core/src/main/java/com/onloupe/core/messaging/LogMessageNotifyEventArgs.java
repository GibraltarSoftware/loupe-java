package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
/**
 * EventArgs for LogMessage notify events.
 */
public class LogMessageNotifyEventArgs {
	/**
	 * The IMessengerPacket for the log message being notified about.
	 */
	public IMessengerPacket packet;

	/**
	 * Instantiates a new log message notify event args.
	 *
	 * @param packet the packet
	 */
	public LogMessageNotifyEventArgs(IMessengerPacket packet) {
		this.packet = packet;
	}
}