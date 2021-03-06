package com.onloupe.core.data;

import java.io.Closeable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import com.onloupe.core.util.TypeUtils;


/**
 * Performs repository level maintenance such as purging for size. Should be
 * used with collection repositories only.
 */
public class RepositoryMaintenance implements Closeable {
	/**
	 * The name of the repository lock used to serialize repository maintenance.
	 */
	public static final String MUTIPROCESS_LOCK_NAME = "Maintenance";

	/** The lock. */
	private final Object lock = new Object();
	
	/** The repository path. */
	private String repositoryPath;

	/** The performing maintenance. */
	private volatile boolean performingMaintenance; // locked by _Lock.
	
	/** The closed. */
	private boolean closed;
	
	/** The logging enabled. */
	private boolean loggingEnabled;

	/**
	 * Create a repository maintenance object for the provided repository without
	 * the ability to perform pruning.
	 *
	 * @param repositoryPath the repository path
	 * @param loggingEnabled Indicates if the maintenance process should log its
	 *                       actions.
	 */
	public RepositoryMaintenance(String repositoryPath, boolean loggingEnabled) {
		if (TypeUtils.isBlank(repositoryPath)) {
			throw new NullPointerException("repositoryPath");
		}

		// in this mode we aren't able to do things that use product/application.
		setProductName(null);
		setApplicationName(null);
		this.repositoryPath = repositoryPath;
		this.loggingEnabled = loggingEnabled; // property does some propagation - safe for now, but don't risk it.
	}

	/**
	 * Create the repository maintenance object for the provided repository.
	 * 
	 * @param repositoryPath   The full path to the base of the repository (which
	 *                         must contain an index)
	 * @param productName      The product name of the application(s) to restrict
	 *                         pruning to.
	 * @param applicationName  Optional. The application within the product to
	 *                         restrict pruning to.
	 * @param maxAgeDays       The maximum allowed days since the session fragment
	 *                         was closed to keep the fragment around.
	 * @param maxSizeMegabytes The maximum number of megabytes of session fragments
	 *                         to keep
	 * @param loggingEnabled   Indicates if the maintenance process should log its
	 *                         actions.
	 */
	public RepositoryMaintenance(String repositoryPath, String productName, String applicationName, int maxAgeDays,
			int maxSizeMegabytes, boolean loggingEnabled) {
		if (TypeUtils.isBlank(repositoryPath)) {
			throw new NullPointerException("repositoryPath");
		}

		if (TypeUtils.isBlank(productName)) {
			throw new NullPointerException("productName");
		}

		setProductName(productName);
		setApplicationName(applicationName);
		setMaxAgeDays(maxAgeDays);
		setMaxSizeMegabytes(maxSizeMegabytes);
		this.repositoryPath = repositoryPath;
		this.loggingEnabled = loggingEnabled; // property does some propagation - safe for now, but don't risk it.
	}

	/**
	 * Performs application-defined tasks associated with freeing, releasing, or
	 * resetting unmanaged resources.
	 * 
	 * 
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public final void close() throws IOException {
		// Free managed resources here (normal Dispose() stuff, which should itself call
		// Dispose(true))
		// Other objects may be referenced in this case

		if (!closed) {
			// make sure that we aren't performing maintenance in the background... we want
			// to give it time to get its act together.
			synchronized (this.lock) {
				OffsetDateTime fileLockTimeout = OffsetDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(2000));

				while ((this.performingMaintenance) && (fileLockTimeout.isAfter(OffsetDateTime.now()))) {
					try {
						this.lock.wait(100);
					} catch (InterruptedException e) {
						// do nothing.
					} // wake back up 10 times a second to check if we're still in maintenance.
				}

				// now that we're done with the lock, make sure we notify every other waiting
				// thread.
				this.lock.notifyAll();
			}
			this.closed = true; // so we can catch it when we get multiply disposed.
		}
	}

	/**
	 * The product to restrict purge operations to.
	 */
	private String productName;

	/**
	 * Gets the product name.
	 *
	 * @return the product name
	 */
	public final String getProductName() {
		return this.productName;
	}

	/**
	 * Sets the product name.
	 *
	 * @param value the new product name
	 */
	private void setProductName(String value) {
		this.productName = value;
	}

	/**
	 * Optional. The application name to restrict purge operations to.
	 */
	private String applicationName;

	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
	public final String getApplicationName() {
		return this.applicationName;
	}

	/**
	 * Sets the application name.
	 *
	 * @param value the new application name
	 */
	private void setApplicationName(String value) {
		this.applicationName = value;
	}

	/**
	 * The full path to the base of the repository that is being maintained.
	 *
	 * @return the repository path
	 */
	public final String getRepositoryPath() {
		return this.repositoryPath;
	}

	/**
	 * The maximum allowed days since the session fragment was closed to keep the
	 * fragment around.
	 */
	private int maxAgeDays;

	/**
	 * Gets the max age days.
	 *
	 * @return the max age days
	 */
	public final int getMaxAgeDays() {
		return this.maxAgeDays;
	}

	/**
	 * Sets the max age days.
	 *
	 * @param value the new max age days
	 */
	public final void setMaxAgeDays(int value) {
		this.maxAgeDays = value;
	}

	/** The maximum number of megabytes of session fragments to keep. */
	private int maxSizeMegabytes;

	/**
	 * Gets the max size megabytes.
	 *
	 * @return the max size megabytes
	 */
	public final int getMaxSizeMegabytes() {
		return this.maxSizeMegabytes;
	}

	/**
	 * Sets the max size megabytes.
	 *
	 * @param value the new max size megabytes
	 */
	public final void setMaxSizeMegabytes(int value) {
		this.maxSizeMegabytes = value;
	}

	/**
	 * The last time a maintenance run was started.
	 */
	private OffsetDateTime lastMaintenanceRunDateTime;

	/**
	 * Gets the last maintenance run date time.
	 *
	 * @return the last maintenance run date time
	 */
	public final OffsetDateTime getLastMaintenanceRunDateTime() {
		return this.lastMaintenanceRunDateTime;
	}

	/**
	 * Indicates if the database should log operations to Gibraltar or not.
	 *
	 * @return true, if is logging enabled
	 */
	public final boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	/**
	 * Sets the checks if is logging enabled.
	 *
	 * @param value the new checks if is logging enabled
	 */
	public final void setIsLoggingEnabled(boolean value) {
		this.loggingEnabled = value;
	}

	/**
	 * Indicates whether maintenance is currently being performed on the repository.
	 *
	 * @return the performing maintenance
	 */
	public final boolean getPerformingMaintenance() {
		// marked as volatile so we'll have a very current value, but we're just
		// reading.
		return this.performingMaintenance;
	}

	/**
	 * Run the maintenance cycle.
	 */
	public final void performMaintenance() {
		// if we're currently performing maintenance, return immediately.
		synchronized (this.lock) {
			if (!performingMaintenance) {
				// otherwise, queue a maintenance action. We always use the background thread
				// for consistency,
				// the question is just whether or not we wait to return.
				this.performingMaintenance = true;
				this.lastMaintenanceRunDateTime = OffsetDateTime.now();
			}

			this.lock.notifyAll();
		}

	}

	/**
	 * Performs the actual releasing of managed and unmanaged resources.
	 * 
	 * @param releaseManaged Indicates whether to release managed resources. This
	 *                       should only be called with true, except from the
	 *                       finalizer which should call Dispose(false). Most usage
	 *                       should instead call Dispose(), which will call
	 *                       Dispose(true) for you and will suppress redundant
	 *                       finalization.
	 */
	protected void dispose(boolean releaseManaged) {
		this.closed = true; // so we can catch it when we get multiply disposed.

		if (releaseManaged) {
			// Free managed resources here (normal Dispose() stuff, which should itself call
			// Dispose(true))
			// Other objects may be referenced in this case

			// make sure that we aren't performing maintenance in the background... we want
			// to give it time to get its act together.
			synchronized (this.lock) {
				OffsetDateTime fileLockTimeout = OffsetDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(2000));

				while ((this.performingMaintenance) && (fileLockTimeout.isAfter(OffsetDateTime.now()))) {
					try {
						this.lock.wait(100);
					} catch (InterruptedException e) {
						// do nothing.
					} // wake back up 10 times a second to check if we're still in maintenance.
				}

				// now that we're done with the lock, make sure we notify every other waiting
				// thread.
				this.lock.notifyAll();
			}
		}
		// Free native resources here (alloc's, etc)
		// May be called from within the finalizer, so don't reference other objects
		// here
	}

}