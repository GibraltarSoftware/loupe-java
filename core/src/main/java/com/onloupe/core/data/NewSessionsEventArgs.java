package com.onloupe.core.data;

import com.onloupe.model.log.LogMessageSeverity;


/**
 * Supplies summary information about new sessions that are available to be
 * retrieved or just retrieved into the repository.
 */
public class NewSessionsEventArgs {
	
	/**
	 * Create a new sessions event arguments container.
	 *
	 * @param newSessions the new sessions
	 * @param warningSessions the warning sessions
	 * @param errorSessions the error sessions
	 * @param criticalSessions the critical sessions
	 * @param maxSeverity the max severity
	 */
	public NewSessionsEventArgs(int newSessions, int warningSessions, int errorSessions, int criticalSessions,
			LogMessageSeverity maxSeverity) {
		setNewSessions(newSessions);
		setWarningSessions(warningSessions);
		setErrorSessions(errorSessions);
		setCriticalSessions(criticalSessions);
		setMaxSeverity(maxSeverity);
	}

	/** The number of new sessions affected. */
	private int newSessions;

	/**
	 * Gets the new sessions.
	 *
	 * @return the new sessions
	 */
	public final int getNewSessions() {
		return this.newSessions;
	}

	/**
	 * Sets the new sessions.
	 *
	 * @param value the new new sessions
	 */
	private void setNewSessions(int value) {
		this.newSessions = value;
	}

	/**
	 * The number of new sessions with a max severity of warning.
	 */
	private int warningSessions;

	/**
	 * Gets the warning sessions.
	 *
	 * @return the warning sessions
	 */
	public final int getWarningSessions() {
		return this.warningSessions;
	}

	/**
	 * Sets the warning sessions.
	 *
	 * @param value the new warning sessions
	 */
	private void setWarningSessions(int value) {
		this.warningSessions = value;
	}

	/**
	 * The number of new sessions with a max severity of error.
	 */
	private int errorSessions;

	/**
	 * Gets the error sessions.
	 *
	 * @return the error sessions
	 */
	public final int getErrorSessions() {
		return this.errorSessions;
	}

	/**
	 * Sets the error sessions.
	 *
	 * @param value the new error sessions
	 */
	private void setErrorSessions(int value) {
		this.errorSessions = value;
	}

	/**
	 * The number of new sessions with a max severity of critical.
	 */
	private int criticalSessions;

	/**
	 * Gets the critical sessions.
	 *
	 * @return the critical sessions
	 */
	public final int getCriticalSessions() {
		return this.criticalSessions;
	}

	/**
	 * Sets the critical sessions.
	 *
	 * @param value the new critical sessions
	 */
	private void setCriticalSessions(int value) {
		this.criticalSessions = value;
	}

	/**
	 * The maximum severity of new sessions.
	 */
	private LogMessageSeverity maxSeverity;

	/**
	 * Gets the max severity.
	 *
	 * @return the max severity
	 */
	public final LogMessageSeverity getMaxSeverity() {
		return this.maxSeverity;
	}

	/**
	 * Sets the max severity.
	 *
	 * @param value the new max severity
	 */
	private void setMaxSeverity(LogMessageSeverity value) {
		this.maxSeverity = value;
	}
}