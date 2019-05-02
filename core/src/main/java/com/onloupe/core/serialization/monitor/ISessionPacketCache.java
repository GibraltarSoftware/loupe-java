package com.onloupe.core.serialization.monitor;

import com.onloupe.core.monitor.ApplicationUserCollection;

/**
 * Provides lookup services for packet factories to find other session-related
 * packets
 * 
 * Implemented by the session object and the network viewer client
 */
public interface ISessionPacketCache {

	ApplicationUserCollection getUsers();
}