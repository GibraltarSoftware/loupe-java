package com.onloupe.core.messaging;

import java.time.Duration;

import com.onloupe.core.logging.Log;
import com.onloupe.model.log.ILogMessage;
import com.onloupe.model.log.LogMessageSeverity;


/**
 * EventArgs for Notification events.
 */
public class NotificationEventArgs {
	
	/** The Send session. */
	private boolean _SendSession;

	/**
	 * The set of one or more log messages for this notification event.
	 */
	public ILogMessage[] messages;

	/**
	 * The strongest log message severity included in this notification event.
	 */
	public LogMessageSeverity topSeverity;

	/**
	 * The total number of log messages included in this notification event.
	 */
	public int totalCount;

	/**
	 * The number of Critical log messages included in this notification event.
	 */
	public int criticalCount;

	/**
	 * The number of Error log messages included in this notification event.
	 */
	public int errorCount;

	/**
	 * The number of Warning log messages included in this notification event.
	 */
	public int warningCount;

	/**
	 * The number of log messages which have an attached Exception included in this
	 * notification event.
	 */
	public int exceptionCount;

	/**
	 * A minimum length of time to wait until another notification may be issued,
	 * requested by the client upon return.
	 */
	public Duration minimumNotificationDelay;

	/**
	 * Set to automatically send the latest information on the current session when
	 * the event returns.
	 * 
	 * If there is insufficient configuration information to automatically send
	 * sessions this property will revert to false when set true. To verify if there
	 * is sufficient configuration information, use CanSendSession
	 *
	 * @return the send session
	 */
	public final boolean getSendSession() {
		return this._SendSession;
	}

	/**
	 * Sets the send session.
	 *
	 * @param value the new send session
	 */
	public final void setSendSession(boolean value) {
		if (this._SendSession == value) {
			return;
		}

		if (!value) {
			// just do it - doesn't matter if we're valid or if we are already false.
			this._SendSession = false;
		} else {
			// we must be setting to true.
			String message = null;
			String tempRefMessage = new String(message);
			if (Log.canSendSessions(tempRefMessage)) {
				message = tempRefMessage;
				this._SendSession = true;
			} else {
				message = tempRefMessage;
			}
		}
	}

	/**
	 * Instantiates a new notification event args.
	 *
	 * @param messages the messages
	 * @param defaultMinWait the default min wait
	 */
	public NotificationEventArgs(ILogMessage[] messages, Duration defaultMinWait) {
		this.minimumNotificationDelay = defaultMinWait;
		this.messages = messages;

		this.topSeverity = LogMessageSeverity.VERBOSE;
		for (ILogMessage message : messages) {
			if (message == null) {
				continue;
			}

			this.totalCount++;
			LogMessageSeverity severity = message.getSeverity();
			if (severity.getSeverity() < this.topSeverity.getSeverity()) // Severity compares in reverse, Critical = 1,
			// Verbose = 16.
			{
				this.topSeverity = severity; // Remember the new top severity.
			}

			switch (severity) {
			case CRITICAL:
				this.criticalCount++;
				break;
			case ERROR:
				this.errorCount++;
				break;
			case WARNING:
				this.warningCount++;
				break;
			default:
				break;
			}

			if (message.getHasException()) {
				this.exceptionCount++;
			}
		}
	}
}