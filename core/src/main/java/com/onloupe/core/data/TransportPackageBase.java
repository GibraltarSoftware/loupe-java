package com.onloupe.core.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.onloupe.agent.Packager;
import com.onloupe.core.logging.Log;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

public abstract class TransportPackageBase implements Closeable {
	private boolean closed;

	protected TransportPackageBase(String product, String application, SimplePackage package_Renamed) {
		if (package_Renamed == null) {
			throw new NullPointerException("@package");
		}

		if (TypeUtils.isBlank(product)) {
			throw new NullPointerException("product");
		}

		// application IS allowed to be null.

		setPackage(package_Renamed);
		setProduct(product);
		setApplication(application);
	}

	private SimplePackage simplePackage;

	public final SimplePackage getPackage() {
		return this.simplePackage;
	}

	private void setPackage(SimplePackage value) {
		this.simplePackage = value;
	}

	/**
	 * The product the package was restricted to.
	 */
	private String product;

	public final String getProduct() {
		return this.product;
	}

	private void setProduct(String value) {
		this.product = value;
	}

	/**
	 * The application the package was restricted to (optional, may be null)
	 */
	private String application;

	public final String getApplication() {
		return this.application;
	}

	private void setApplication(String value) {
		this.application = value;
	}

	private boolean hasProblemSessions;

	public final boolean getHasProblemSessions() {
		return this.hasProblemSessions;
	}

	public final void setHasProblemSessions(boolean value) {
		this.hasProblemSessions = value;
	}

	@Override
	public final void close() throws IOException {
		if (!this.closed) {
			try {
				if (getPackage() != null) {
					getPackage().close();
					setPackage(null);
				}
			} catch (Exception ex) {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, Packager.LOG_CATEGORY,
							"Unable to properly dispose working email package file",
							"Unable to properly dispose the transport package due to an exception (%s): %s",
							ex.getClass().getName(), ex.getMessage());
				}
			}
			
			this.closed = true;
		}

	}

	public void send() {
		onSend();
	}

	protected abstract void onSend();

	/**
	 * Mark all of the sessions contained in the source package as being read.
	 * 
	 * @throws IOException
	 */
	public final void markContentsAsRead(LocalRepository repository) {
		// we need to make sure we have the package lock at least long enough to keep
		// the working package and grab sessions.
		if (getPackage() == null) {
			throw new NullPointerException(
					"There is no working package available, indicating an internal Loupe programming error.");
		}

		try {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.VERBOSE, Packager.LOG_CATEGORY,
						"Marking all sessions in the package as read", null);
			}

			List<SessionHeader> allSessions = getPackage().getSessions();

			if (allSessions.isEmpty()) {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.VERBOSE, Packager.LOG_CATEGORY, "No Sessions to Mark Read",
							"There are unexpectedly no sessions in the working package, so none can be marked as read.  We shouldn't have gotten to this point if tehre are no sessions in the package.");
				}
			} else {
				if (!Log.getSilentMode()) {
					Log.write(LogMessageSeverity.VERBOSE, Packager.LOG_CATEGORY, "Found sessions to mark as read",
							"There are %d sessions to be marked as read (although some may have been marked as read before).",
							allSessions.size());
				}

				// assemble the array of sessions to change
				List<UUID> sessionIds = new ArrayList<UUID>(allSessions.size());
				for (SessionHeader session : allSessions) {
					sessionIds.add(session.getId());
				}

				try {
					repository.setSessionsNew(sessionIds, false);
				} catch (Exception ex) {
					if (!Log.getSilentMode()) {
						Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, Packager.LOG_CATEGORY,
								"Error marking an included session as read",
								"Unable to mark one or more of the sessions included in the package as read.  This won't prevent the package from being sent.  Exception:\r\n%s",
								ex.getMessage());
					}
				}
			}
		} catch (RuntimeException ex) {
			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.WARNING, LogWriteMode.QUEUED, ex, Packager.LOG_CATEGORY,
						"General error while marking included sessions as read",
						"A general error occurred while marking the source sessions included in the package as read.  This won't prevent the package from being sent.\r\nException (%s):\r\n%s",
						ex.getClass().getName(), ex.getMessage());
			}
			throw ex;
		}
	}

}