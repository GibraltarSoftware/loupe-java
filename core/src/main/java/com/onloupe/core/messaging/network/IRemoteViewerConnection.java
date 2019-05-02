package com.onloupe.core.messaging.network;

import java.util.UUID;

import com.onloupe.agent.SessionCriteria;
import com.onloupe.model.log.ILogMessage;

/**
 * Provides an interface to send network packets to the remote computer
 */
public interface IRemoteViewerConnection {
	/**
	 * Indicates whether a session had errors during rehydration and has lost some
	 * packets.
	 */
	boolean getHasCorruptData();

	/**
	 * Indicates how many packets were lost due to errors in rehydration.
	 */
	int getPacketsLostCount();

	/**
	 * Indicates if the remote viewer is currently connected.
	 */
	boolean isConnected();

	/**
	 * The session id
	 */
	UUID getId();

	/**
	 * Attempt to connect the live session data stream
	 */
	void connect();

	/**
	 * Sends a request to the remote agent to package and submit its data
	 * 
	 * @param criteria
	 */
	void sendToServer(SessionCriteria criteria);

	/**
	 * Load the set of log messages still in the connection buffer
	 * 
	 * @return
	 */
	ILogMessage[] getMessageBuffer();
}