package com.onloupe.core.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.onloupe.core.data.SessionHeader;

// TODO: Auto-generated Javadoc
/**
 * Tracks session headers and fragments from one or more fragments
 * 
 * Designed to help assemble a virtual index from a set of file fragments.
 *
 * @param <T> the generic type
 */
public class SessionFileInfo<T> {
	
	/** The session id. */
	private UUID sessionId;
	
	/** The session fragments. */
	private final Map<Integer, T> sessionFragments = new TreeMap<Integer, T>();

	/** The session header. */
	private SessionHeader sessionHeader;

	/**
	 * Create a new file information tracking object.
	 *
	 * @param sessionHeader the session header
	 * @param fileInfo the file info
	 * @param isNew the is new
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
	 * The session id.
	 *
	 * @return the id
	 */
	public final UUID getId() {
		return this.sessionId;
	}

	/** Indicates if the session is actually running (regardless of its session state). */
	private boolean running;

	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public final boolean isRunning() {
		return this.running;
	}

	/**
	 * Sets the checks if is running.
	 *
	 * @param value the new checks if is running
	 */
	public final void setIsRunning(boolean value) {
		this.running = value;
	}

	/**
	 * The best session header from all the loaded fragments.
	 *
	 * @return the header
	 */
	public final SessionHeader getHeader() {
		return this.sessionHeader;
	}

	/**
	 * The list of fragments that have been found.
	 *
	 * @return the fragments
	 */
	public final List<T> getFragments() {
		return new ArrayList<T>(this.sessionFragments.values());
	}

	/**
	 * Add another fragment to this session's information.
	 *
	 * @param sessionHeader the session header
	 * @param fileInfo the file info
	 * @param isNew the is new
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