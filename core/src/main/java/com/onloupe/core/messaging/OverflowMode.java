package com.onloupe.core.messaging;

/**
 * The behavior of the messenger when there are too many messages in the queue.
 */
public enum OverflowMode {
	/**
	 * Do the default overflow behavior (OverflowQueueThenBlock)
	 */
	DEFAULT,

	/**
	 * Use the overflow queue then block if there are too many messages
	 */
	OVERFLOW_QUEUE_THEN_BLOCK,

	/**
	 * Drop the newest messages instead of using the overflow queue
	 */
	DROP;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static OverflowMode forValue(int value) {
		return values()[value];
	}
}