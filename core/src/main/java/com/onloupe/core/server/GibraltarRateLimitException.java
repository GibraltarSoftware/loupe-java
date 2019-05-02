package com.onloupe.core.server;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.onloupe.model.exception.GibraltarException;

///#pragma warning disable CS1591 // Missing XML comment for publicly visible type or member

public class GibraltarRateLimitException extends GibraltarException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

	private OffsetDateTime timestamp;

	public final OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	private void setTimestamp(OffsetDateTime value) {
		this.timestamp = value;
	}

	private OffsetDateTime retryAfter;

	public final OffsetDateTime getRetryAfter() {
		return this.retryAfter;
	}

	private void setRetryAfter(OffsetDateTime value) {
		this.retryAfter = value;
	}

	/**
	 * The number of seconds to delay before retrying
	 */
	private Duration delay;

	public final Duration getDelay() {
		return this.delay;
	}

	private void setDelay(Duration value) {
		this.delay = value;
	}
}