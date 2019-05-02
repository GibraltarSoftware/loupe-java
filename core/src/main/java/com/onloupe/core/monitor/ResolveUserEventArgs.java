package com.onloupe.core.monitor;

import java.time.OffsetDateTime;

/**
 * Arguments for the ResolveUser Event
 */
public final class ResolveUserEventArgs {
	private OffsetDateTime timestamp;
	private long sequence;
	private ApplicationUser user;

	public ResolveUserEventArgs(String userName, OffsetDateTime timestamp, long sequence) {
		this.timestamp = timestamp;
		this.sequence = sequence;
		setUserName(userName);
	}

	/**
	 * The user name being resolved
	 * 
	 * This value is treated as a key for the duration of the current session. If an
	 * ApplicationUser object is returned from this event it will be associated with
	 * this user and the event will not be raised again for this user name for the
	 * duration of this session.
	 */
	private String userName;

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String value) {
		this.userName = value;
	}

	/**
	 * The application user being populated for the current user.
	 * 
	 * Update this user with the information available. If this method is called
	 * then the configured user will be stored as the definitive information for
	 * this user name.
	 */
	public ApplicationUser getUser() {
		if (this.user == null) {
			this.user = new ApplicationUser(getUserName(), this.timestamp, this.sequence);
		}

		return this.user;
	}
}