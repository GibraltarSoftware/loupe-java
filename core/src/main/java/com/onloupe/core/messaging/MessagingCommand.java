package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
/**
 * Different types of commands.
 */
public enum MessagingCommand {
	/**
	 * Not a command.
	 */
	NONE(0),

	/** Flush the queue. */
	FLUSH(1),

	/** Close the current file (and open a new one because the session isn't ending). */
	CLOSE_FILE(2),

	/**
	 * Alert the messaging system to process all remaining packets and exit.
	 * 
	 */
	SHUTDOWN(3),

	/**
	 * Close the messenger (and don't restart it)
	 * 
	 * This has been deprecated for the java version. We only support background threads,
	 * so shutdown will queue the command packet and close the session after the queue
	 * has emptied.
	 */
	@Deprecated
	CLOSE_MESSENGER(4),

	/**
	 * Cause the Gibraltar Live View form to be (generated if necessary and) shown.
	 */
	SHOW_LIVE_VIEW(5),

	/** Causes the network messenger to connect out to a remote viewer. */
	OPEN_REMOTE_VIEWER(6);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, MessagingCommand> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, MessagingCommand> getMappings() {
		if (mappings == null) {
			synchronized (MessagingCommand.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, MessagingCommand>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new messaging command.
	 *
	 * @param value the value
	 */
	private MessagingCommand(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.intValue;
	}

	/**
	 * For value.
	 *
	 * @param value the value
	 * @return the messaging command
	 */
	public static MessagingCommand forValue(int value) {
		return getMappings().get(value);
	}
}