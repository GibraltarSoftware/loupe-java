package com.onloupe.core.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.onloupe.core.util.IOUtils;


/**
 * A multiprocess lock manager for repositories. This class is a singleton.
 * 
 * Manages locking first within the process and then extends the process lock to
 * multiple processes by locking a file on disk. Designed for use with the Using
 * statement as opposed to the Lock statement.
 */
public final class InterprocessLockManager implements Observer {

	/** The g lock. */
	private final Object gLock = new Object();
	
	/** The g proxies. */
	private final Map<String, InterprocessLockProxy> gProxies = new HashMap<String, InterprocessLockProxy>();

	/** The interprocess lock manager. */
	private static InterprocessLockManager interprocessLockManager;

	/**
	 * Instantiates a new interprocess lock manager.
	 */
	private InterprocessLockManager() {

	}

	/**
	 * Gets the single instance of InterprocessLockManager.
	 *
	 * @return single instance of InterprocessLockManager
	 */
	public static InterprocessLockManager getInstance() {
		if (interprocessLockManager == null) {
			interprocessLockManager = new InterprocessLockManager();
		}
		return interprocessLockManager;
	}

	/**
	 * Attempt to lock the repository with the provided index path.
	 *
	 * @param requester      The object that is requesting the lock (useful for
	 *                       debugging purposes)
	 * @param indexPath      The fully qualified path to the directory containing
	 *                       the index file of the repository
	 * @param lockName       The name of the lock to get (locks are a combination of
	 *                       index and this name)
	 * @param timeoutSeconds The maximum number of seconds to wait on the lock
	 *                       before giving up.
	 * @return A Repository Lock object if the lock could be obtained or Null if the
	 *         lock timed out.
	 */
	public InterprocessLock lock(Object requester, String indexPath, String lockName, int timeoutSeconds) {
		return lock(requester, indexPath, lockName, timeoutSeconds, false);
	}

	/**
	 * Attempt to lock the repository with the provided index path.
	 *
	 * @param requester      The object that is requesting the lock (useful for
	 *                       debugging purposes)
	 * @param indexPath      The fully qualified path to the directory containing
	 *                       the index file of the repository
	 * @param lockName       The name of the lock to get (locks are a combination of
	 *                       index and this name)
	 * @param timeoutSeconds The maximum number of seconds to wait on the lock
	 *                       before giving up.
	 * @param deleteOnClose  Whether the lock file should be deleted on close or
	 *                       left around for reuse.
	 * @return A Repository Lock object if the lock could be obtained or Null if the
	 *         lock timed out.
	 */
	public InterprocessLock lock(Object requester, String indexPath, String lockName, int timeoutSeconds,
			boolean deleteOnClose) {
		if (requester == null) {
			throw new NullPointerException("requester");
		}

		if (indexPath == null) {
			throw new NullPointerException("indexPath");
		}

		if (lockName == null) {
			throw new NullPointerException("lockName");
		}

		InterprocessLock candidateLock = new InterprocessLock(requester, indexPath, lockName, timeoutSeconds);

		// Lookup or create the proxy for the requested lock.
		InterprocessLockProxy lockProxy;
		synchronized (this.gLock) {
			if (!this.gProxies.containsKey(candidateLock.getFullName())) {
				// Didn't exist, need to make one.
				lockProxy = new InterprocessLockProxy(indexPath, lockName, deleteOnClose);

				lockProxy.addObserver(this);
				this.gProxies.put(lockProxy.getFullName(), lockProxy);
			} else {
				lockProxy = this.gProxies.get(candidateLock.getFullName());
			}

			// Does the current thread already hold the lock? (If it was still waiting on
			// it, we couldn't get here.)
			Thread currentTurnThread = lockProxy.checkCurrentTurnThread(candidateLock);
			if (Thread.currentThread() == currentTurnThread && candidateLock.getActualLock() != null) {
				return candidateLock; // It's a secondary lock, so we don't need to queue it or wait.
			}
			// Or is the lock currently held by another thread that we don't want to wait
			// for?
			if (currentTurnThread != null && !candidateLock.getWaitForLock()) {
				IOUtils.closeQuietly(candidateLock);
				// We don't want to wait for it, so don't bother queuing an expired request.
				return null; // Just fail out.
			}

			lockProxy.queueRequest(candidateLock); // Otherwise, queue the request inside the lock to keep the proxy
													// around.
		}

		// Now we have the proxy and our request is queued. Make sure some thread is
		// trying to get the file lock.
		boolean ourTurn = false; // Assume false.
		try {
			ourTurn = lockProxy.awaitOurTurnOrTimeout(candidateLock);
		} finally {
			if (!ourTurn) {
				// We have to make sure this gets disposed if we didn't get the lock, even if a
				// ThreadAbortException occurs.
				IOUtils.closeQuietly(candidateLock);
				// Bummer, we didn't get it. Probably already disposed, but safe to do again.
				candidateLock = null; // Clear it out to report the failure.
			}
		}
		// Otherwise... yay, we got it!

		return candidateLock;
	}

	/**
	 * Query whether a particular lock is available without holding on to it.
	 *
	 * @param requester The object that is querying the lock (useful for debugging
	 *                  purposes)
	 * @param indexPath The fully qualified path to the directory containing the
	 *                  index file of the repository
	 * @param lockName  The name of the lock to query (locks are a combination of
	 *                  index and this name)
	 * @return True if the lock could have been obtained. False if the lock could
	 *         not be obtained without waiting.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean queryLockAvailable(Object requester, String indexPath, String lockName) throws IOException {
		Path file = InterprocessLockProxy.getLockFileName(indexPath, lockName);
		if (!file.toFile().isFile()) {
			return true; // Lock file didn't exist, so we could have obtained it.
		}

		boolean lockAvailable = false;
		try (InterprocessLock attemptedLock = lock(requester, indexPath, lockName, 0, true)) {
			lockAvailable = (attemptedLock != null);
		} catch (IOException e) {
			throw e;
		}

		return lockAvailable;
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		InterprocessLockProxy lockProxy = (InterprocessLockProxy) o;
		lockProxy.deleteObserver(this);

		if (lockProxy.isClosed()) {
			lockProxyDisposed(lockProxy);
		}
	}

	/**
	 * Lock proxy disposed.
	 *
	 * @param disposingProxy the disposing proxy
	 */
	private void lockProxyDisposed(InterprocessLockProxy disposingProxy) {
		synchronized (this.gLock) {
			String lockKey = disposingProxy.getFullName();
			// Only remove the proxy if the one we're disposing is the one in our collection
			// for that key.
			InterprocessLockProxy actualProxy = this.gProxies.get(lockKey);
			if (actualProxy == disposingProxy) {
				this.gProxies.remove(lockKey);
			}
			this.gLock.notifyAll();
		}
	}
}