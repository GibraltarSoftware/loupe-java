package com.onloupe.core.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.onloupe.core.data.SessionHeader;

/**
 * Tracks session headers and fragments from one or more fragments
 * 
 * Designed to help assemble a virtual index from a set of file fragments.
 */
public class SessionFileInfo<T> {
	private UUID sessionId;
	private final Map<Integer, T> sessionFragments = new TreeMap<Integer, T>();

	private SessionHeader sessionHeader;

	/**
	 * Create a new file information tracking object
	 */
	public SessionFileInfo(SessionHeader sessionHeader, T fileInfo, boolean isNew) {
		assert sessionHeader != null;
		assert fileInfo != null;

		this.sessionId = sessionHeader.getId();

		// this will be our best session header since it's the first
		this.sessionHeader = sessionHeader;
		this.sessionHeader.setIsNew(isNew);
		this.sessionFragments.put(sessionHeader.getFileSequence(), fileInfo);
	}

	/**
	 * The session id
	 */
	public final UUID getId() {
		return this.sessionId;
	}

	/**
	 * Indicates if the session is actually running (regardless of its session
	 * state)
	 */
	private boolean running;

	public final boolean isRunning() {
		return this.running;
	}

	public final void setIsRunning(boolean value) {
		this.running = value;
	}

	/**
	 * The best session header from all the loaded fragments
	 */
	public final SessionHeader getHeader() {
		return this.sessionHeader;
	}

	/**
	 * The list of fragments that have been found
	 */
	public final List<T> getFragments() {
		return new ArrayList<T>(this.sessionFragments.values());
	}

	/**
	 * Add another fragment to this session's information
	 * 
	 * @param sessionHeader
	 * @param fileInfo
	 * @param isNew
	 */
	public final void addFragment(SessionHeader sessionHeader, T fileInfo, boolean isNew) {
		synchronized (this.sessionFragments) {
			// if a file got duplicated or copied for some reason (which can happen if
			// someone is messing around in the log directory)
			// then we could get a duplicate item. We need to make sure we don't process
			// that.
			if (!this.sessionFragments.containsKey(sessionHeader.getFileSequence())) {
				// If this header is newer than our previous best it takes over (headers are
				// cumulative)
				if (sessionHeader.getFileSequence() > this.sessionHeader.getFileSequence()) {
					sessionHeader.setIsNew(this.sessionHeader.isNew()); // preserve our existing setting...
					this.sessionHeader = sessionHeader;
				}

				this.sessionHeader.setIsNew(this.sessionHeader.isNew() || isNew); // if any are new, it's new.

				// and we add this file info to our set in its correct order.
				this.sessionFragments.put(sessionHeader.getFileSequence(), fileInfo);
			}
		}
	}
}