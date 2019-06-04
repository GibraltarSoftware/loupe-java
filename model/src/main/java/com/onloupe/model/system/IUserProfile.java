package com.onloupe.model.system;

import java.time.OffsetDateTime;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 *  
 *  A single user of the system.
 */
public interface IUserProfile
{

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	UUID getId();

	/**
	 *  
	 * 	 The name used to authenticate to the system.
	 * 	 
	 * 	 This is an alternate key for the user but is editable.
	 *
	 * @return the user name
	 */
	String getUserName();

	/**
	 *  
	 * 	 A display caption for the user - often their full name.
	 *
	 * @return the user caption
	 */
	String getUserCaption();

	/**
	 *  
	 * 	 The email address used to communicate with the user.
	 *
	 * @return the email address
	 */
	String getEmailAddress();

	/**
	 *  
	 * 	 Indicates if the user has requested HTML-formatted email when possible.
	 *
	 * @return the use html email
	 */
	boolean getUseHtmlEmail();

	/**
	 *  
	 * 	 Indicates if the user account has been deleted.  
	 * 	 
	 * 	 Loupe does a soft-delete in many cases to preserve the history of actions.
	 *
	 * @return the deleted
	 */
	boolean getDeleted();

	/**
	 *  
	 * 	 The number of unsuccessful authentication attempts since the last successful log in.
	 *
	 * @return the password failures
	 */
	int getPasswordFailures();

	/**
	 *  
	 * 	 Indicates if the account has been locked out due to having too many password failures in a row.
	 *
	 * @return the checks if is locked out
	 */
	boolean getIsLockedOut();

	/**
	 *  
	 * 	 Indicates if the account has been approved and is now active.
	 * 	 
	 * 	 This is used for deferred user creation
	 *
	 * @return the checks if is approved
	 */
	boolean getIsApproved();

	/**
	 *  
	 * 	 Optional, The name of the time zone the user has elected to view timestamps in.   Overrides the repository default.
	 * 	 
	 * 	 If null the repository-wide setting will be used.
	 *
	 * @return the time zone code
	 */
	String getTimeZoneCode();

	/**
	 *  
	 * 	 The last time the user performed an authenticated action
	 * 	 
	 * 	 For performance reasons this timestamp isn't updated on every single action, but will
	 * 	 be within a few minutes of the last time they executed an API request.
	 *
	 * @return the last access timestamp
	 */
	OffsetDateTime getLastAccessTimestamp();

	/**
	 *  
	 * 	 The timestamp of when the user was created.
	 *
	 * @return the created timestamp
	 */
	OffsetDateTime getCreatedTimestamp();

	/**
	 *  
	 * 	 Optional.  The timestamp of the last time the account was locked out.
	 *
	 * @return the last locked out timestamp
	 */
	OffsetDateTime getLastLockedOutTimestamp();

	/**
	 *  
	 * 	 Optional.  The timestamp of the last time the password was changed.
	 *
	 * @return the last password change timestamp
	 */
	OffsetDateTime getLastPasswordChangeTimestamp();

	/**
	 *  
	 * 	 Optional.  The timestamp of the last time the user failed to authenticate
	 *
	 * @return the last password failure timestamp
	 */
	OffsetDateTime getLastPasswordFailureTimestamp();

	/**
	 *  
	 * 	 The type of user - ranging from system to virtual.
	 * 	 
	 * 	 Not all user types can log into the system.  User type incorporate the role the user has with respect to the repository.
	 *
	 * @return the type
	 */
	UserType getType();
}