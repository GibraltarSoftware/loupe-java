package com.onloupe.core.server;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.onloupe.model.exception.GibraltarException;

// TODO: Auto-generated Javadoc
///#pragma warning disable CS1591 // Missing XML comment for publicly visible type or member

/**
 * The Class GibraltarRateLimitException.
 */
public class GibraltarRateLimitException extends GibraltarException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new gibraltar rate limit exception.
	 *
	 * @param message the message
	 * @param delay the delay
	 */
	public GibraltarRateLimitException(String message, Duration delay) {
		super(message);
		setTimestamp(OffsetDateTime.now());
		setDelay(delay);

		if (delay != null) {
			setRetryAfter(getTimestamp().plus(delay));
		} else {
			setRetryAfter(getTimestamp().plusSeconds(1));
		}
	}

	/** The timestamp. */
	private OffsetDateTime timestamp;

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public final OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param value the new timestamp
	 */
	private void setTimestamp(OffsetDateTime value) {
		this.timestamp = value;
	}

	/** The retry after. */
	private OffsetDateTime retryAfter;

	/**
	 * Gets the retry after.
	 *
	 * @return the retry after
	 */
	public final OffsetDateTime getRetryAfter() {
		return this.retryAfter;
	}

	/**
	 * Sets the retry after.
	 *
	 * @param value the new retry after
	 */
	private void setRetryAfter(OffsetDateTime value) {
		this.retryAfter = value;
	}

	/** The number of seconds to delay before retrying. */
	private Duration delay;

	/**
	 * Gets the delay.
	 *
	 * @return the delay
	 */
	public final Duration getDelay() {
		return this.delay;
	}

	/**
	 * Sets the delay.
	 *
	 * @param value the new delay
	 */
	private void setDelay(Duration value) {
		this.delay = value;
	}
}