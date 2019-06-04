package com.onloupe.core.monitor;

import java.time.OffsetDateTime;

// TODO: Auto-generated Javadoc
/**
 * Arguments for the ResolveUser Event.
 */
public final class ResolveUserEventArgs {
	
	/** The timestamp. */
	private OffsetDateTime timestamp;
	
	/** The sequence. */
	private long sequence;
	
	/** The user. */
	private ApplicationUser user;

	/**
	 * Instantiates a new resolve user event args.
	 *
	 * @param userName the user name
	 * @param timestamp the timestamp
	 * @param sequence the sequence
	 */
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

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param value the new user name
	 */
	public void setUserName(String value) {
		this.userName = value;
	}

	/**
	 * The application user being populated for the current user.
	 * 
	 * Update this user with the information available. If this method is called
	 * then the configured user will be stored as the definitive information for
	 * this user name.
	 *
	 * @return the user
	 */
	public ApplicationUser getUser() {
		if (this.user == null) {
			this.user = new ApplicationUser(getUserName(), this.timestamp, this.sequence);
		}

		return this.user;
	}
}