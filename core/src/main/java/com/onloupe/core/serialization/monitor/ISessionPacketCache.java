package com.onloupe.core.serialization.monitor;

import com.onloupe.core.monitor.ApplicationUserCollection;

// TODO: Auto-generated Javadoc
/**
 * Provides lookup services for packet factories to find other session-related
 * packets
 * 
 * Implemented by the session object and the network viewer client.
 */
public interface ISessionPacketCache {

	/**
	 * Gets the users.
	 *
	 * @return the users
	 */
	ApplicationUserCollection getUsers();
}