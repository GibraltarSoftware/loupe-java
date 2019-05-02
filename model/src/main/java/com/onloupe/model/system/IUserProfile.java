package com.onloupe.model.system;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 
 A single user of the system
*/
public interface IUserProfile
{
	/** 
	 The unique key for this user
	 
	 This id is globally unique and invariant for a specific user account.
	*/
	UUID getId();

	/** 
	 The name used to authenticate to the system.
	 
	 This is an alternate key for the user but is editable.
	*/
	String getUserName();

	/** 
	 A display caption for the user - often their full name.
	*/
	String getUserCaption();

	/** 
	 The email address used to communicate with the user.
	*/
	String getEmailAddress();

	/** 
	 Indicates if the user has requested HTML-formatted email when possible.
	*/
	boolean getUseHtmlEmail();

	/** 
	 Indicates if the user account has been deleted.  
	 
	 Loupe does a soft-delete in many cases to preserve the history of actions.
	*/
	boolean getDeleted();

	/** 
	 The number of unsuccessful authentication attempts since the last successful log in.
	*/
	int getPasswordFailures();

	/** 
	 Indicates if the account has been locked out due to having too many password failures in a row.
	*/
	boolean getIsLockedOut();

	/** 
	 Indicates if the account has been approved and is now active.
	 
	 This is used for deferred user creation
	*/
	boolean getIsApproved();

	/** 
	 Optional, The name of the time zone the user has elected to view timestamps in.   Overrides the repository default.
	 
	 If null the repository-wide setting will be used.
	*/
	String getTimeZoneCode();

	/** 
	 The last time the user performed an authenticated action
	 
	 For performance reasons this timestamp isn't updated on every single action, but will
	 be within a few minutes of the last time they executed an API request.
	*/
	OffsetDateTime getLastAccessTimestamp();

	/** 
	 The timestamp of when the user was created
	*/
	OffsetDateTime getCreatedTimestamp();

	/** 
	 Optional.  The timestamp of the last time the account was locked out.
	*/
	OffsetDateTime getLastLockedOutTimestamp();

	/** 
	 Optional.  The timestamp of the last time the password was changed.
	*/
	OffsetDateTime getLastPasswordChangeTimestamp();

	/** 
	 Optional.  The timestamp of the last time the user failed to authenticate
	*/
	OffsetDateTime getLastPasswordFailureTimestamp();

	/** 
	 The type of user - ranging from system to virtual.
	 
	 Not all user types can log into the system.  User type incorporate the role the user has with respect to the repository.
	*/
	UserType getType();
}