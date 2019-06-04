package com.onloupe.core.messaging.network;

import java.util.UUID;

import com.onloupe.agent.SessionCriteria;
import com.onloupe.model.log.ILogMessage;

// TODO: Auto-generated Javadoc
/**
 * Provides an interface to send network packets to the remote computer.
 */
public interface IRemoteViewerConnection {
	
	/**
	 * Indicates whether a session had errors during rehydration and has lost some
	 * packets.
	 *
	 * @return the checks for corrupt data
	 */
	boolean getHasCorruptData();

	/**
	 * Indicates how many packets were lost due to errors in rehydration.
	 *
	 * @return the packets lost count
	 */
	int getPacketsLostCount();

	/**
	 * Indicates if the remote viewer is currently connected.
	 *
	 * @return true, if is connected
	 */
	boolean isConnected();

	/**
	 * The session id.
	 *
	 * @return the id
	 */
	UUID getId();

	/**
	 * Attempt to connect the live session data stream.
	 */
	void connect();

	/**
	 * Sends a request to the remote agent to package and submit its data.
	 *
	 * @param criteria the criteria
	 */
	void sendToServer(SessionCriteria criteria);

	/**
	 * Load the set of log messages still in the connection buffer.
	 *
	 * @return the message buffer
	 */
	ILogMessage[] getMessageBuffer();
}