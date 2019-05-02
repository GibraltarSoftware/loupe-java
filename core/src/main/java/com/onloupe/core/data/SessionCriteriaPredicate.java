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
 * Compares sessions to the supplied session criteria to determine if they match
 */
public class SessionCriteriaPredicate implements Predicate<ISessionSummary> {
	private String productName;
	private String applicationName;
	private EnumSet<SessionCriteria> criteria;

	private boolean activeSession;
	private boolean newSessions;
	private boolean completedSessions;
	private boolean crashedSessions;
	private boolean criticalSessions;
	private boolean errorSessions;
	private boolean warningSessions;

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

	public final EnumSet<SessionCriteria> getCriteria() {
		return criteria;
	}

	public final String getApplication() {
		return this.applicationName;
	}

	public final String getProduct() {
		return this.productName;
	}

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