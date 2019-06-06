package com.onloupe.core.data;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Observable;

import com.onloupe.core.util.TimeConversion;


/**
 * Represents an exclusive lock on a repository within a process and between
 * processes.
 * 
 * To be valid, the repository lock object must be obtained from the
 * RepositoryLockManager class. When you're done with a lock, dispose the
 * repository lock to release it.
 */
public final class InterprocessLock extends Observable implements Closeable {
	/**
	 * The file extension of lock files used to lock repositories..
	 */
	public static final String LOCK_FILE_EXTENSION = "lock";

	/** The index path. */
	private String indexPath;
	
	/** The lock name. */
	private String lockName;
	
	/** The lock full file name path. */
	private String lockFullFileNamePath;
	
	/** The wait for lock. */
	private boolean waitForLock;
	
	/** The lock. */
	private final Object lock = new Object(); // For locking inter-thread signals to this instance.

	/** The owning thread. */
	private Thread owningThread;
	
	/** The owning object. */
	private Object owningObject;
	
	/** The our lock proxy. */
	private InterprocessLockProxy ourLockProxy;
	
	/** The actual lock. */
	private InterprocessLock actualLock;
	
	/** The wait timeout. */
	private OffsetDateTime waitTimeout; // Might be locked by MyLock?
	
	/** The turn. */
	private boolean turn; // LOCKED by MyLock
	
	/** The closed. */
	private boolean closed; // LOCKED by MyLock

	/**
	 * Instantiates a new interprocess lock.
	 *
	 * @param requester the requester
	 * @param indexPath the index path
	 * @param lockName the lock name
	 * @param timeoutSeconds the timeout seconds
	 */
	public InterprocessLock(Object requester, String indexPath, String lockName, int timeoutSeconds) {
		this.owningObject = requester;
		this.owningThread = Thread.currentThread();
		this.indexPath = indexPath;
		this.lockName = lockName;
		this.actualLock = null;
		this.turn = false;
		this.waitForLock = (timeoutSeconds > 0);
		this.waitTimeout = this.waitForLock ? OffsetDateTime.now().plusSeconds(timeoutSeconds) : OffsetDateTime.now();
		this.lockFullFileNamePath = getLockFileName(indexPath, lockName);
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
	public void close() throws IOException {
		// Free managed resources here (normal Dispose() stuff, which should itself call
		// Dispose(true))
		// Other objects may be referenced in this case

		synchronized (this.lock) {
			if (!this.closed) {
				this.closed = true; // Make sure we don't do it more than once.
				this.waitTimeout = TimeConversion.MIN;
				this.owningThread = null;
				this.owningObject = null;
			}

			this.lock.notifyAll(); // No one should be waiting, but we did change state, so...
		}

		// marks this observable as having changed.
		setChanged();
		// instructs observers to act on the change.
		notifyObservers();

		// Free native resources here (alloc's, etc)
		// May be called from within the finalizer, so don't reference other objects
		// here

	}

	/**
	 * The full (unique) name for the lock, combining the index path and lock name.
	 *
	 * @return the full name
	 */
	public String getFullName() {
		return this.lockFullFileNamePath;
	}

	/**
	 * The name of the repository this lock controls access to.
	 *
	 * @return the index path
	 */
	public String getIndexPath() {
		return this.indexPath;
	}

	/**
	 * The name of the lock within the repository.
	 *
	 * @return the lock name
	 */
	public String getLockName() {
		return this.lockName;
	}

	/**
	 * The object that is currently holding the lock.
	 *
	 * @return the owner
	 */
	public Object getOwner() {
		return this.owningObject;
	}

	/**
	 * The thread that created and waits on this request and owns the lock when this
	 * request is granted.
	 *
	 * @return the owning thread
	 */
	public Thread getOwningThread() {
		return this.owningThread;
	}

	/**
	 * The ManagedThreadId of the thread that owns this lock instance.
	 *
	 * @return the owning thread id
	 */
	public long getOwningThreadId() {
		return this.owningThread.getId();
	}

	/**
	 * Whether this lock request is willing to wait (finite) for the lock or return
	 * immediately if not available.
	 *
	 * @return the wait for lock
	 */
	public boolean getWaitForLock() {
		return this.waitForLock;
	}

	/**
	 * The clock time at which this lock request wants to stop waiting for the lock
	 * and give up. (MaxValue once the lock is granted, MinValue if the lock was
	 * denied.)
	 *
	 * @return the wait timeout
	 */
	public OffsetDateTime getWaitTimeout() {
		return this.waitTimeout;
	}
	// TODO: Above needs lock wrapper?

	/**
	 * The actual holder of the lock if we are a secondary lock on the same thread,
	 * or ourselves if we hold the file lock.
	 *
	 * @return the actual lock
	 */
	public InterprocessLock getActualLock() {
		return this.actualLock;
	}

	/**
	 * Reports if this lock object holds a secondary lock rather than the actual
	 * lock (or no lock).
	 *
	 * @return true, if is secondary lock
	 */
	public boolean isSecondaryLock() {
		return this.actualLock != this;
	}

	/**
	 * Reports if this request instance has expired and should be skipped over
	 * because no thread is still waiting on it.
	 *
	 * @return true, if is expired
	 */
	public boolean isExpired() {
		synchronized (this.lock) {
			return this.closed || this.waitTimeout.equals(TimeConversion.MIN);
		}
	}

	/**
	 * Whether this lock instance has been disposed (and thus does not hold any
	 * locks).
	 *
	 * @return true, if is disposed
	 */
	public boolean isDisposed() {
		return this.closed;
	}

	/**
	 * Gets or sets the dispose-on-close policy for the lock proxy associated with
	 * this lock instance.
	 *
	 * @return the dispose proxy on close
	 */
	public boolean getDisposeProxyOnClose() {
		return (this.ourLockProxy == null) ? false : this.ourLockProxy.getDisposeOnClose();
	}

	/**
	 * Sets the dispose proxy on close.
	 *
	 * @param value the new dispose proxy on close
	 */
	public void setDisposeProxyOnClose(boolean value) {
		if (this.ourLockProxy != null) {
			this.ourLockProxy.setDisposeOnClose(value);
		}
	}

	/**
	 * The proxy who will actually hold the file lock on our behalf.
	 *
	 * @return the our lock proxy
	 */
	public InterprocessLockProxy getOurLockProxy() {
		return this.ourLockProxy;
	}

	/**
	 * Sets the our lock proxy.
	 *
	 * @param value the new our lock proxy
	 */
	public void setOurLockProxy(InterprocessLockProxy value) {
		this.ourLockProxy = value;
	}

	/**
	 * Grant the lock.
	 *
	 * @param actualLock the actual lock
	 */
	public void grantTheLock(InterprocessLock actualLock) {
		if (actualLock != null && !actualLock.isDisposed()
				&& actualLock.getOwningThread() == this.owningThread
				&& actualLock.getFullName().equalsIgnoreCase(this.lockFullFileNamePath)) {
			// We don't need to lock around this because we're bypassing the proxy's queue
			// and staying only on our own thread.
			this.actualLock = actualLock;
			this.waitTimeout = OffsetDateTime.MAX; // We have a lock (sort of), so reset our timeout to forever.
		} else {
			// It's an invalid call, so make sure our setting is cleared out.
			this.actualLock = null;
		}
	}

	/**
	 * Signal my turn.
	 */
	public void signalMyTurn() {
		synchronized (this.lock) {
			this.turn = true; // Flag it as being our turn.

			this.lock.notifyAll(); // And signal Monitor.Wait that we changed the state.
		}
	}

	/**
	 * Await turn or timeout.
	 *
	 * @return true, if successful
	 * @throws InterruptedException the interrupted exception
	 */
	public boolean awaitTurnOrTimeout() throws InterruptedException {
		synchronized (this.lock) {
			if (this.waitForLock) // Never changes, so check it first.
			{
				while (!this.turn && !this.closed) // Either flag and we're done waiting.
				{
					Duration howLong = Duration.between(OffsetDateTime.now(), this.waitTimeout);
					if (howLong.isZero() || howLong.isNegative()) {
						this.waitTimeout = TimeConversion.MIN; // Mark timeout as expired.
						return false; // Our time is up!
					}

					// We don't need to do a pulse here, we're the only ones waiting, and we didn't
					// change any state.
					this.lock.wait(howLong.toMillis());
				}
			}

			// Now we've done any allowed waiting as needed, check what our status is.

			if (this.closed || !this.turn) {
				return false; // We're expired!
			} else {
				return true; // Otherwise, we're not disposed and it's our turn!
			}

			// We don't need to do a pulse here, we're the only ones waiting, and we didn't
			// change any state.
		}
	}

	/**
	 * Gets the lock file name.
	 *
	 * @param indexPath the index path
	 * @param lockName the lock name
	 * @return the lock file name
	 */
	public static String getLockFileName(String indexPath, String lockName) {
		return Paths.get(indexPath).resolve(lockName + "." + LOCK_FILE_EXTENSION).toString();
	}

}