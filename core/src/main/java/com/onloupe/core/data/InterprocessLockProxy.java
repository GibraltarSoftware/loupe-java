package com.onloupe.core.data;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.onloupe.core.CommonCentralLogic;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TimeConversion;

// TODO: Auto-generated Javadoc
/**
 * A class to hold a file lock for this process (app domain) and pass it fairly
 * to other waiting threads before release.
 */
public class InterprocessLockProxy extends Observable implements Observer, Closeable {
	
	/** The Constant LOCK_POLLING_DELAY. */
	private static final int LOCK_POLLING_DELAY = 16; // 16 ms wait between attempts to open a lock file.
	
	/** The Constant BACK_OFF_DELAY. */
	private static final int BACK_OFF_DELAY = LOCK_POLLING_DELAY * 3; // 48 ms wait when another process requests a
																		// turn.

	/** The wait queue. */
																		private final ConcurrentLinkedQueue<InterprocessLock> waitQueue = new ConcurrentLinkedQueue<InterprocessLock>();
	
	/** The queue lock. */
	private final Object queueLock = new Object();
	
	/** The index path. */
	private String indexPath;
	
	/** The lock name. */
	private String lockName;
	
	/** The lock full file name path. */
	private String lockFullFileNamePath;
	
	/** The delete on close. */
	private boolean deleteOnClose; // File persistence policy for this lock (should delete unless a high-traffic
									
									/** The current lock turn. */
									// lock).
	private InterprocessLock currentLockTurn;
	
	/** The file lock. */
	private FileLock fileLock;
	
	/** The lock request. */
	private FileLock lockRequest;
	
	/** The min time next turn. */
	private OffsetDateTime minTimeNextTurn = TimeConversion.MIN;
	
	/** The dispose on close. */
	private boolean disposeOnClose; // Object persistence policy for this instance (should delete if not a reused
									
									/** The closed. */
									// lock).
	private boolean closed;

	/**
	 * Instantiates a new interprocess lock proxy.
	 *
	 * @param indexPath the index path
	 * @param lockName the lock name
	 * @param deleteOnClose the delete on close
	 */
	public InterprocessLockProxy(String indexPath, String lockName, boolean deleteOnClose) {
		this.indexPath = indexPath;
		this.lockName = lockName;
		this.deleteOnClose = deleteOnClose;
		this.lockFullFileNamePath = getLockFileName(indexPath, lockName).toString();
	}

	/**
	 * The full (unique) name for the lock, combining the index path and lock name.
	 *
	 * @return the full name
	 */
	public final String getFullName() {
		return this.lockFullFileNamePath;
	}

	/**
	 * The name of the repository this lock controls access to.
	 *
	 * @return the index path
	 */
	public final String getIndexPath() {
		return this.indexPath;
	}

	/**
	 * The name of the lock within the repository.
	 *
	 * @return the lock name
	 */
	public final String getLockName() {
		return this.lockName;
	}

	/**
	 * Whether this lock instance has been disposed (and thus does not hold any
	 * locks).
	 *
	 * @return true, if is closed
	 */
	public final boolean isClosed() {
		return this.closed;
	}

	/**
	 * Reports how many threads are in the queue waiting on the lock (some may have
	 * timed out and given up already). (Reports -1 if the proxy is idle (no current
	 * turn).)
	 *
	 * @return the waiting count
	 */
	public final int getWaitingCount() {
		return (this.currentLockTurn == null) ? -1 : this.waitQueue.size();
	}

	/**
	 * The lock request with the current turn to hold or wait for the lock.
	 *
	 * @return the current lock turn
	 */
	public final InterprocessLock getCurrentLockTurn() {
		return this.currentLockTurn;
	}

	/**
	 * The requesting owner of the current turn for the lock.
	 *
	 * @return the current turn owner
	 */
	public final Object getCurrentTurnOwner() {
		return this.currentLockTurn == null ? null : this.currentLockTurn.getOwner();
	}

	/**
	 * The thread with the current turn for the lock.
	 *
	 * @return the current turn thread
	 */
	public final Thread getCurrentTurnThread() {
		return this.currentLockTurn == null ? null : this.currentLockTurn.getOwningThread();
	}

	/**
	 * The ManagedThreadId of the thread with the current turn for the lock, or -1
	 * if none. (For debug convenience only.)
	 *
	 * @return the current turn thread id
	 */
	public final long getCurrentTurnThreadId() {
		if (this.currentLockTurn == null) {
			return -1; // TODO or 0?
		}

		return this.currentLockTurn.getOwningThread().getId();
	}

	/**
	 * Object persistence policy for this instance: Whether to dispose this instance
	 * when file lock is released.
	 *
	 * @return the dispose on close
	 */
	public final boolean getDisposeOnClose() {
		return this.disposeOnClose;
	}

	/**
	 * Sets the dispose on close.
	 *
	 * @param value the new dispose on close
	 */
	public final void setDisposeOnClose(boolean value) {
		this.disposeOnClose = value;
	}

	/**
	 * Check the thread with the current turn for the lock and grant a secondary
	 * lock if applicable.
	 * 
	 * @param candidateLock An unexpired lock request on the current thread, or null
	 *                      to just check the turn thread.
	 * @return The Thread with the current turn for the lock, or null if there are
	 *         none holding or waiting.
	 */
	public final Thread checkCurrentTurnThread(InterprocessLock candidateLock) {
		if (candidateLock != null && candidateLock.getOwningThread() != Thread.currentThread()) {
			throw new IllegalStateException("A lock request may only be waited on by the thread which created it.");
		}

		synchronized (this.queueLock) {
			if (this.currentLockTurn != null) {
				Thread currentOwningThread = this.currentLockTurn.getOwningThread();
				if (candidateLock != null && Thread.currentThread() == currentOwningThread) {
					candidateLock.grantTheLock(this.currentLockTurn); // Set it as a secondary lock on that holder (same
																		// thread).
					if (candidateLock.getActualLock() == this.currentLockTurn) // Sanity-check that it was successful.
					{
						candidateLock.setOurLockProxy(this); // So its dispose-on-close setting pass-through can
																// function.
					}
				}

				return currentOwningThread; // Whether it's a match or some other thread.
			}

			return null; // No thread owns the lock.
		}
	}

	/**
	 * Queue a lock request (RepositoryLock instance). Must be followed by a call to
	 * AwaitOurTurnOrTimeout (which can block).
	 *
	 * @param lockRequest the lock request
	 */
	public final void queueRequest(InterprocessLock lockRequest) {
		if (!lockRequest.getFullName().equalsIgnoreCase(this.lockFullFileNamePath)) {
			throw new IllegalStateException("A lock request may not be queued to a proxy for a different full name.");
		}

		if (lockRequest.getOwningThread() != Thread.currentThread()) {
			throw new IllegalStateException("A lock request may only be queued by the thread which created it.");
		}

		synchronized (this.queueLock) {
			this.waitQueue.offer(lockRequest);
		}
	}

	/**
	 * Wait for our turn to have the lock (and wait for the lock) up to our time
	 * limit.
	 *
	 * @param lockRequest the lock request
	 * @return true, if successful
	 */
	public final boolean awaitOurTurnOrTimeout(InterprocessLock lockRequest) {
		if (lockRequest.isExpired()) {
			throw new IllegalStateException("Can't wait on an expired lock request.");
		}

		if (!lockRequest.getFullName().equalsIgnoreCase(this.lockFullFileNamePath)) {
			throw new IllegalStateException("A lock request may not be queued to a proxy for a different full name.");
		}

		if (lockRequest.getOwningThread() != Thread.currentThread()) {
			throw new IllegalStateException("A lock request may only be waited on by the thread which created it.");
		}

		lockRequest.setOurLockProxy(this); // Mark the request as pending with us.

		// Do NOT clear out current lock owner, this will allow DequeueNextRequest to
		// find one already there, if any.
		boolean ourTurn = startNextTurn(lockRequest); // Gets its own queue lock.
		if (!ourTurn) {
			// It's not our turn yet, we need to wait our turn. Are we willing to wait?
			if (lockRequest.getWaitForLock() && lockRequest.getWaitTimeout().isAfter(OffsetDateTime.now())) {
				try {
					ourTurn = lockRequest.awaitTurnOrTimeout();
				} catch (InterruptedException e) {
					// do nothing.
				}
			}

			// Still not our turn?
			if (!ourTurn) {
				if (!CommonCentralLogic.getSilentMode()) {
					// Who actually has the lock right now?
					if (this.currentLockTurn != null) {
						Thread currentOwningThread = this.currentLockTurn.getOwningThread();
						long currentOwningThreadId = -1;
						String currentOwningThreadName = "null";
						if (currentOwningThread != null) // To make sure we can't get a null-ref exception from logging
															// this...
						{
							currentOwningThreadId = currentOwningThread.getId();
							currentOwningThreadName = (currentOwningThread.getName() != null)
									? currentOwningThread.getName()
									: "";
						}
					}
				}

				IOUtils.closeQuietly(lockRequest);
				return false; // Failed to get the lock. Time to give up.
			}
		}

		// Yay, now it's our turn! Do we already hold the lock?

		boolean validLock;
		if (this.fileLock != null) {
			validLock = true; // It's our request's turn and this proxy already holds the lock!
		} else {
			validLock = tryGetLock(lockRequest); // Can we get the lock?
		}

		// Do we actually have the lock now?
		if (validLock) {
			lockRequest.grantTheLock(lockRequest); // It owns the actual lock itself now.
		} else {
			IOUtils.closeQuietly(lockRequest);
			; // Failed to get the lock. Expire the request and give up.
		}

		return validLock;
	}

	/**
	 * Gets the lock file name.
	 *
	 * @param indexPath the index path
	 * @param lockName the lock name
	 * @return the lock file name
	 */
	public static Path getLockFileName(String indexPath, String lockName) {
		return Paths.get(indexPath).resolve(lockName + "." + InterprocessLock.LOCK_FILE_EXTENSION);
	}

	/**
	 * Try to get the actual file lock on behalf of the current request.
	 *
	 * @param currentRequest the current request
	 * @return true, if successful
	 */
	private boolean tryGetLock(InterprocessLock currentRequest) {
		boolean waitForLock = currentRequest.getWaitForLock();
		OffsetDateTime lockTimeout = currentRequest.getWaitTimeout();
		boolean validLock = false;

		while (!waitForLock || OffsetDateTime.now().isBefore(lockTimeout)) {
			if (!OffsetDateTime.now().isBefore(this.minTimeNextTurn)) // Make sure we aren't in a back-off delay.
			{
				this.fileLock = getFileLock(this.lockFullFileNamePath); // TODO: DeleteOnClose no longer supported in
																		// our file
				// opens.
				if (this.fileLock != null) {
					// We have the lock! Close our lock request if we have one so later we can
					// detect if anyone else does.
					if (this.lockRequest != null) {
						try {
							this.lockRequest.release();
						} catch (IOException e) {
							// do nothing
						}
						this.lockRequest = null;
					}

					validLock = true; // Report that we have the lock now.
				}
			}
			// Otherwise, just pretend we couldn't get the lock in this attempt.

			if (!validLock && waitForLock) {
				// We didn't get the lock and we want to wait for it, so try to open a lock
				// request.
				if (this.lockRequest == null) {
					this.lockRequest = getLockRequest(this.lockFullFileNamePath); // Tell the other process we'd like a
																					// turn.
				}

				// Then we should allow some real time to pass before trying again because file
				// opens aren't very fast.
				try {
					Thread.sleep(LOCK_POLLING_DELAY);
				} catch (InterruptedException e) {
					// do nothing.
				}
			} else {
				// We either got the lock or the user doesn't want to keep retrying, so exit the
				// loop.
				break;
			}
		}

		return validLock;
	}

	/**
	 * Find the next request still waiting and signal it to go. Or return true if
	 * the current caller may proceed.
	 *
	 * @param currentRequest The request the caller is waiting on, or null for none.
	 * @return True if the caller's supplied request is the next turn, false
	 *         otherwise.
	 */
	private boolean startNextTurn(InterprocessLock currentRequest) {
		synchronized (this.queueLock) {
			int dequeueCount = dequeueNextRequest(); // Find the next turn if there isn't one already underway.
			if (this.currentLockTurn != null) {
				// If we popped a new turn off the queue make sure it gets started.
				if (dequeueCount > 0) {
					this.currentLockTurn.signalMyTurn(); // Signal the thread waiting on that request to proceed.
				}

				if (this.currentLockTurn == currentRequest) // Is the current request the next turn?
				{
					return true; // Yes, so skip waiting and just tell our caller they can go ahead (and wait for
									// the lock).
				}
			} else {
				// Otherwise, nothing else is waiting on the lock! Time to shut it down.

				if (this.lockRequest != null) {
					// Release the lock request (an open read) since we're no longer waiting on it.
					try {
						this.lockRequest.release();
					} catch (IOException e) {
						// do nothing
					}
					this.lockRequest = null;
				}

				if (this.fileLock != null) {
					// Release the OS file lock.
					try {
						this.lockRequest.release();
					} catch (IOException e) {
						// do nothing
					}
					this.fileLock = null;
				}

				if (this.disposeOnClose) {
					try {
						close();
					} catch (IOException e) {

					}
				}
			}

			return false;
		}
	}

	/**
	 * Dequeue next request.
	 *
	 * @return the int
	 */
	private int dequeueNextRequest() {
		synchronized (this.queueLock) {
			int dequeueCount = 0;

			// Make sure we don't thread-abort in the middle of this logic.
			try {
			} finally {
				while (this.currentLockTurn == null && !this.waitQueue.isEmpty()) {
					this.currentLockTurn = this.waitQueue.poll();
					dequeueCount++;

					if (this.currentLockTurn.isExpired()) {
						IOUtils.closeQuietly(this.currentLockTurn);
						// There's no one waiting on that request, so just discard it.
						this.currentLockTurn = null; // Get the next one (if any) on next loop.
					} else {
						this.currentLockTurn.addObserver(this);
					}
				}
			}

			return dequeueCount;
		}
	}

	/**
	 * Attempts to get an exclusive lock on a specified file.
	 * 
	 * @param lockFullFileNamePath The full-path file name for the lock file.
	 * @return A file stream to the maintenance file if locked, null otherwise
	 *         Callers should check the provided handle for null to ensure they got
	 *         the lock on the file. If it is not null, it must be disposed to
	 *         release the lock in a timely manner.
	 */
	private FileLock getFileLock(String lockFullFileNamePath) {
		FileLock fileLock = null;

		try {
			(new File((new File(lockFullFileNamePath)).getParent())).mkdirs();
		} catch (Exception ex) {
			return null; // we aren't going to try to spinlock on this.. we failed. ex; // we aren't
						// going to try to spinlock on this.. we failed.
		}

		try {
			// We share Read so that other processes who desire the lock can open for read
			// to signal that to us.
			fileLock = openFileAccess(lockFullFileNamePath, "rw", true, this.deleteOnClose);
		} catch (java.lang.Exception e) {
			// don't care why we failed, we just did - so no lock for you!
			fileLock = null;
		}

		return fileLock;
	}

	/**
	 * Attempts to request a turn at an exclusive lock on a specified file.
	 * 
	 * @param lockFullFileNamePath The full-path file name for the lock file.
	 * @return A LockFile holding a lock request if available, null otherwise
	 *         Callers should check the provided handle for null to ensure they got
	 *         a valid lock request on the file. If it is not null, it must be
	 *         disposed to release the request when expired or full lock is
	 *         acquired.
	 */
	private FileLock getLockRequest(String lockFullFileNamePath) {
		FileLock fileLock;

		try {
			(new File((new File(lockFullFileNamePath)).getParent())).mkdirs();
		} catch (Exception ex) {
			return null;
		}

		// We share ReadWrite so that we overlap with an open lock (unshared write) and
		// other requests (open reads).

		try {
			// This is meant to overlap with other requestors, so it should never delete on
			// close; others may still have it open.
			fileLock = openFileAccess(lockFullFileNamePath, "r", true, false);
		} catch (java.lang.Exception e) {
			// We don't care why we failed, we just did - so no lock for you!
			fileLock = null;
		}

		return fileLock;
	}

	/**
	 * Check if a lock request is pending (without blocking).
	 * 
	 * @param lockFullFileNamePath The full-path file name to request a lock on.
	 * @return True if a lock request is pending (an open read), false if no reads
	 *         are open on the file.
	 */
	private boolean checkLockRequest(String lockFullFileNamePath) {
		try {
			(new File((new File(lockFullFileNamePath)).getParent())).mkdirs();
		} catch (RuntimeException ex) {
		}

		// We share Write because we'll check this while we already have an unshared
		// write open!
		boolean deleteOnClose = false; // This overlaps with holding a write lock, so don't delete the file when
										// successful.

		try {
			try (FileLock fileLockRequest = openFileAccess(lockFullFileNamePath, "r", true, deleteOnClose)) {
				return (fileLockRequest == null); // There's an open read on it if we could NOT open an unshared read.
			}
		} catch (java.lang.Exception e) {
			// We don't care why we failed, we just did - so assume there IS a request
			// pending.
			return true;
		}
	}

	/**
	 * Open a file for the specified fileAccess and fileShare, or return null if
	 * open fails (avoids exceptions).
	 *
	 * @param fullFileNamePath    The full-path file name to open for the specified
	 *                            access.
	 * @param mode the mode
	 * @param shared the shared
	 * @param manualDeleteOnClose Whether the (successfully-opened) FileLock
	 *                            returned should delete the file upon dispose.
	 * @return A disposable FileLock opened with the specified access and sharing),
	 *         or null if the attempt failed.
	 */
	private FileLock openFileAccess(String fullFileNamePath, String mode, boolean shared, boolean manualDeleteOnClose) {
		File file = new File(fullFileNamePath);
		try {
			file.createNewFile();
			
			if (manualDeleteOnClose)
				file.deleteOnExit();
			
			List<OpenOption> openOptions = new ArrayList<OpenOption>();
			openOptions.add(StandardOpenOption.WRITE);
			
			if (shared)
				openOptions.add(StandardOpenOption.READ);
			
			FileChannel channel = FileChannel.open(file.toPath(), openOptions.toArray(new OpenOption[openOptions.size()]));
			return channel.lock(0, Long.MAX_VALUE, shared);
		} catch (java.lang.Exception e) {
			return null;
		}
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
		synchronized (this.queueLock) {
			if (!this.closed) {
				this.closed = true; // Make sure we don't do it more than once.

				// Empty our queue (although it should already be empty!).
				while (!this.waitQueue.isEmpty()) {
					InterprocessLock lockInstance = this.waitQueue.poll();
					// lockInstance.Disposed -= Lock_Disposed; // Suppress the events, don't start
					// new turns!
					IOUtils.closeQuietly(lockInstance);
					; // Tell any threads still waiting that their request has expired.
				}

				if (this.currentLockTurn == null) {
					// No thread is currently prepared to do this, so clear them here.
					if (this.lockRequest != null) {
						this.lockRequest.release();
						this.lockRequest = null;
					}

					if (this.fileLock != null) {
						this.fileLock.release();
						this.fileLock = null;
					}
				}

				// We're not fully disposed until the current lock owner gets disposed so we can
				// release the lock.
				// But fire the event to tell the RepositoryLockManager that we are no longer a
				// valid proxy.
				setChanged();
				notifyObservers();
			}
		}
	}


	/**
	 * Lock disposed.
	 *
	 * @param disposingLock the disposing lock
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void lockDisposed(InterprocessLock disposingLock) throws IOException {
		// we need to remove this object from the lock collection
		synchronized (this.queueLock) {
			// Only remove the lock if the one we're disposing is the original top-level
			// lock for that key.
			if (this.currentLockTurn == null || this.currentLockTurn != disposingLock) {
				return; // Wasn't our current holder, so we don't care about it.
			}

			this.currentLockTurn = null; // It's disposed, no longer current owner.

			if (!this.closed) {
				// We're releasing the lock for this thread. We need to check if any other
				// process has a request pending.
				// And if so, we need to force this process to wait a minimum delay, even if we
				// don't have one waiting now.
				if (this.fileLock != null && checkLockRequest(this.lockFullFileNamePath)) {
					this.minTimeNextTurn = OffsetDateTime.now()
							.plusNanos(TimeUnit.MILLISECONDS.toNanos(BACK_OFF_DELAY)); // Back
																						// off
																						// for
																						// a
																						// bit.
					this.fileLock.close(); // We have to give up the OS lock because other processes need a chance.
					this.fileLock = null;
				}

				startNextTurn(null); // Find and signal the next turn to go ahead (also handles all-done).
			} else {
				// We're already disposed, so we'd better release the lock and request now if we
				// still have them!
				if (this.lockRequest != null) {
					this.lockRequest.close();
					this.lockRequest = null;
				}

				if (this.fileLock != null) {
					this.fileLock.close();
					this.fileLock = null;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object arg) {
		InterprocessLock lock = (InterprocessLock) observable;
		lock.deleteObserver(this); // Unsubscribe.

		if (lock.isDisposed()) {
			try {
				lockDisposed(lock);
			} catch (IOException e) {
				// do nothing.
			}
		}
	}
}