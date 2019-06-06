package com.onloupe.core.data;

import java.io.IOException;
import java.util.EnumSet;
import java.util.function.Predicate;

import com.onloupe.agent.SessionCriteria;
import com.onloupe.core.logging.Log;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.session.SessionStatus;


/**
 * Compares sessions to the supplied session criteria to determine if they match.
 */
public class SessionCriteriaPredicate implements Predicate<ISessionSummary> {
	
	/** The product name. */
	private String productName;
	
	/** The application name. */
	private String applicationName;
	
	/** The criteria. */
	private EnumSet<SessionCriteria> criteria;

	/** The active session. */
	private boolean activeSession;
	
	/** The new sessions. */
	private boolean newSessions;
	
	/** The completed sessions. */
	private boolean completedSessions;
	
	/** The crashed sessions. */
	private boolean crashedSessions;
	
	/** The critical sessions. */
	private boolean criticalSessions;
	
	/** The error sessions. */
	private boolean errorSessions;
	
	/** The warning sessions. */
	private boolean warningSessions;

	/**
	 * Instantiates a new session criteria predicate.
	 *
	 * @param productName the product name
	 * @param applicationName the application name
	 * @param criteria the criteria
	 */
	public SessionCriteriaPredicate(String productName, String applicationName, EnumSet<SessionCriteria> criteria) {
		this.productName = productName;
		this.applicationName = applicationName;
		this.criteria = criteria;

		// now parse out the criteria
		this.activeSession = criteria.contains(SessionCriteria.ALL_SESSIONS) 
				|| criteria.contains(SessionCriteria.ACTIVE);
		this.newSessions = criteria.contains(SessionCriteria.NEW);
		this.completedSessions = criteria.contains(SessionCriteria.ALL_SESSIONS) 
				|| criteria.contains(SessionCriteria.COMPLETED);
		this.crashedSessions = criteria.contains(SessionCriteria.CRASHED);
		this.criticalSessions = criteria.contains(SessionCriteria.CRITICAL);
		this.errorSessions = criteria.contains(SessionCriteria.ERROR);
		this.warningSessions = criteria.contains(SessionCriteria.WARNING);
	}

	/**
	 * Gets the criteria.
	 *
	 * @return the criteria
	 */
	public final EnumSet<SessionCriteria> getCriteria() {
		return criteria;
	}

	/**
	 * Gets the application.
	 *
	 * @return the application
	 */
	public final String getApplication() {
		return this.applicationName;
	}

	/**
	 * Gets the product.
	 *
	 * @return the product
	 */
	public final String getProduct() {
		return this.productName;
	}

	/* (non-Javadoc)
	 * @see java.util.function.Predicate#test(java.lang.Object)
	 */
	@Override
	public boolean test(ISessionSummary sessionSummary) {
		if (!sessionSummary.getProduct().equalsIgnoreCase(this.productName)) {
			return false;
		}

		if (!TypeUtils.isBlank(this.applicationName)
				&& !sessionSummary.getApplication().equals(this.applicationName)) {
			return false;
		}

		// at this point if we get a qualifying match we can just return true.
		try {
			if (this.activeSession && (Log.getSessionSummary().getId().equals(sessionSummary.getId()))) {
				return true;
			}
		} catch (IOException e) {
			return false;
		}

		if (sessionSummary.getStatus() != SessionStatus.RUNNING) {
			if (this.completedSessions) {
				return true;
			}

			if (sessionSummary.isNew() && this.newSessions) {
				return true;
			}

			if (this.crashedSessions && (sessionSummary.getStatus() == SessionStatus.CRASHED)) {
				return true;
			}

			if (this.criticalSessions && (sessionSummary.getCriticalCount() > 0)) {
				return true;
			}

			if (this.errorSessions && (sessionSummary.getErrorCount() > 0)) {
				return true;
			}

			if (this.warningSessions && (sessionSummary.getWarningCount() > 0)) {
				return true;
			}
		}

		// if we didn't get there by now, we aren't going to match.
		return false;
	}
}