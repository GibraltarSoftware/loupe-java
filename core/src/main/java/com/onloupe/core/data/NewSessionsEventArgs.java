package com.onloupe.core.data;

import com.onloupe.model.log.LogMessageSeverity;

/**
 * Supplies summary information about new sessions that are available to be
 * retrieved or just retrieved into the repository
 */
public class NewSessionsEventArgs {
	/**
	 * Create a new sessions event arguments container
	 * 
	 * @param newSessions
	 * @param warningSessions
	 * @param errorSessions
	 * @param criticalSessions
	 * @param maxSeverity
	 */
	public NewSessionsEventArgs(int newSessions, int warningSessions, int errorSessions, int criticalSessions,
			LogMessageSeverity maxSeverity) {
		setNewSessions(newSessions);
		setWarningSessions(warningSessions);
		setErrorSessions(errorSessions);
		setCriticalSessions(criticalSessions);
		setMaxSeverity(maxSeverity);
	}

	/**
	 * The number of new sessions affected
	 */
	private int newSessions;

	public final int getNewSessions() {
		return this.newSessions;
	}

	private void setNewSessions(int value) {
		this.newSessions = value;
	}

	/**
	 * The number of new sessions with a max severity of warning.
	 */
	private int warningSessions;

	public final int getWarningSessions() {
		return this.warningSessions;
	}

	private void setWarningSessions(int value) {
		this.warningSessions = value;
	}

	/**
	 * The number of new sessions with a max severity of error.
	 */
	private int errorSessions;

	public final int getErrorSessions() {
		return this.errorSessions;
	}

	private void setErrorSessions(int value) {
		this.errorSessions = value;
	}

	/**
	 * The number of new sessions with a max severity of critical.
	 */
	private int criticalSessions;

	public final int getCriticalSessions() {
		return this.criticalSessions;
	}

	private void setCriticalSessions(int value) {
		this.criticalSessions = value;
	}

	/**
	 * The maximum severity of new sessions.
	 */
	private LogMessageSeverity maxSeverity;

	public final LogMessageSeverity getMaxSeverity() {
		return this.maxSeverity;
	}

	private void setMaxSeverity(LogMessageSeverity value) {
		this.maxSeverity = value;
	}
}