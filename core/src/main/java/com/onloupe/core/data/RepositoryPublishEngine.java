package com.onloupe.core.data;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;

import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.core.FileSystemTools;
import com.onloupe.core.logging.Log;
import com.onloupe.core.messaging.Publisher;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.server.HubConnectionStatus;
import com.onloupe.core.server.HubStatus;
import com.onloupe.core.util.IOUtils;
import com.onloupe.core.util.TimeConversion;

// TODO: Auto-generated Javadoc
/**
 * Performs constant, background publishing of sessions from the repository.
 */
public class RepositoryPublishEngine {
	
	/** The Constant BASE_MULTIPROCESS_LOCK_NAME. */
	private static final String BASE_MULTIPROCESS_LOCK_NAME = "repositoryPublish";
	
	/** The Constant BACKGROUND_STARTUP_DELAY. */
	private static final int BACKGROUND_STARTUP_DELAY = 5000; // 5 seconds
	
	/** The Constant SHORT_CHECK_INTERVAL_SECONDS. */
	private static final int SHORT_CHECK_INTERVAL_SECONDS = 60;
	
	/** The Constant LONG_CHECK_INTERVAL_SECONDS. */
	private static final int LONG_CHECK_INTERVAL_SECONDS = 1800;
	
	/** The Constant EXPIRED_CHECK_INTERVAL_SECONDS. */
	private static final int EXPIRED_CHECK_INTERVAL_SECONDS = 3600;

	/** The session publish thread lock. */
	private final Object sessionPublishThreadLock = new Object();
	
	/** The publisher. */
	private Publisher publisher;
	
	/** The configuration. */
	private AgentConfiguration configuration;
	
	/** The repository folder. */
	private String repositoryFolder; // used to coordinate where to coordinate locks
	
	/** The multiprocess lock name. */
	private String multiprocessLockName; // used to establish our unique publisher lock.

	/** The initialized. */
	private volatile boolean initialized; // designed to enable us to do our initialization in the background.
											
											/** The stop requested. */
											// PROTECTED BY THREADLOCK
	private volatile boolean stopRequested;
	
	/** The session publish thread failed. */
	private volatile boolean sessionPublishThreadFailed; // PROTECTED BY THREADLOCK
	
	/** The session publish thread. */
	private Thread sessionPublishThread; // PROTECTED BY THREADLOCK
	
	/** The client. */
	private RepositoryPublishClient client; // PROTECTED BY THREADLOCK
	
	/** The force check. */
	private boolean forceCheck; // PROTECTED BY THREADLOCK
	
	/** The last check. */
	private OffsetDateTime lastCheck = TimeConversion.MIN; // PROTECTED BY THREADLOCK
	
	/** The check interval. */
	private Duration checkInterval = Duration.ofMinutes(1); // PROTECTED BY THREADLOCK
	
	/** The multiprocess lock check interval. */
	private Duration multiprocessLockCheckInterval = Duration.ofMinutes(5);
	
	/** The failed attempts. */
	private int failedAttempts; // PROTECTED BY THREADLOCK

	/**
	 * Instantiates a new repository publish engine.
	 *
	 * @param publisher the publisher
	 * @param configuration the configuration
	 */
	public RepositoryPublishEngine(Publisher publisher, AgentConfiguration configuration) {
		this.publisher = publisher;
		this.configuration = configuration;
		this.sessionPublishThreadFailed = true; // otherwise we won't start it when we need to.

		// find the repository path we're using. We use the same logic that the
		// FileMessenger users.
		this.repositoryFolder = LocalRepository.calculateRepositoryPath(
				this.publisher.getSessionSummary().getProduct(), configuration.getSessionFile().getFolder());

		// create the correct lock name for our scope.
		ServerConfiguration config = this.configuration.getServer();
		this.multiprocessLockName = BASE_MULTIPROCESS_LOCK_NAME + "~"
				+ this.publisher.getSessionSummary().getProduct()
				+ (config.getSendAllApplications() ? "" : "~" + this.publisher.getSessionSummary().getApplication());

		// we have to make sure the multiprocess lock doesn't have any unsafe
		// characters.
		this.multiprocessLockName = FileSystemTools.sanitizeFileName(this.multiprocessLockName);
	}

	/**
	 * Indicates if the publisher has a valid configuration and is running.
	 *
	 * @return true, if is active
	 */
	public final boolean isActive() {
		return (this.initialized && !this.sessionPublishThreadFailed); // protected by volatile
	}

	/**
	 * Start the engine's background processing thread. If it is currently running
	 * the call has no effect.
	 */
	public final void start() {
		ensureSessionPublishThreadIsValid(); // all we have to do is fire up the background thread.
	}

	/**
	 * Stop publishing sessions.
	 *
	 * @param waitForStop Indicates if the caller wants to wait for the engine to
	 *                    stop before returning
	 */
	public final void stop(boolean waitForStop) {
		if (isActive()) {
			// request the background thread stop
			this.stopRequested = true; // protected by volatile

			// and now wait for it.
			if (waitForStop) {
				synchronized (this.sessionPublishThreadLock) {
					while (isActive()) {
						try {
							this.sessionPublishThreadLock.wait(16);
						} catch (InterruptedException e) {
							// do nothing
						}
					}

					this.sessionPublishThreadLock.notifyAll();
				}
			}
		}
	}

	/**
	 * Creates the message dispatch thread.
	 */
	private void createMessageDispatchThread() {
		synchronized (this.sessionPublishThreadLock) {
			// clear the dispatch thread failed flag so no one else tries to create our
			// thread
			this.sessionPublishThreadFailed = false;

			this.sessionPublishThread = new Thread() {
				@Override
				public void run() {
					repositoryPublishMain();
				}
			};
			this.sessionPublishThread.setName("Loupe Session Publisher"); // name our thread so we can isolate it out
																			// of
																			// metrics and such
			this.sessionPublishThread.start();

			// and prep ourselves for checking
			this.forceCheck = true;

			this.sessionPublishThreadLock.notifyAll();
		}
	}

	/**
	 * Repository publish main.
	 */
	private void repositoryPublishMain() {
		// before we get going, lets stall for a few seconds. We aren't a critical
		// operation, and I don't
		// want to get in the way of the application starting up.
		try {
			Thread.sleep(BACKGROUND_STARTUP_DELAY);
		} catch (InterruptedException e) {
			// do nothing, throwing would "get in the way"
		}

		InterprocessLock backgroundLock = null; // we can't do our normal using trick in this situation.
		try {
			// we have two totally different modes: Either WE'RE the background processor or
			// someone else is.
			// if we are then we move on to start publishing. If someone else is then we
			// just poll
			// the lock to see if whoever owned it has exited.
			backgroundLock = getLock(0);
			while ((backgroundLock == null) && !this.stopRequested) {
				// we didn't get the lock - so someone else is currently the main background
				// thread.
				sleepUntilNextCheck(this.multiprocessLockCheckInterval);
				backgroundLock = getLock(0);
			}

			// if we got the lock then we want to go ahead and perform background
			// processing.
			if (backgroundLock != null) {
				repositoryPublishLoop();

				// release the lock; we'll get it on the next round.
				backgroundLock.close();

				backgroundLock = null;
			}
		} catch (Exception ex) {

		} finally {
			IOUtils.closeQuietly(backgroundLock);

			synchronized (this.sessionPublishThreadLock) {
				// clear the dispatch thread variable since we're about to exit.
				this.sessionPublishThread = null;

				// we want to write out that we had a problem and mark that we're failed so
				// we'll get restarted.
				this.sessionPublishThreadFailed = true;

				this.sessionPublishThreadLock.notifyAll();
			}
		}
	}

	/**
	 * Called when we're the one true publisher for our data to have us poll for
	 * data to push and push as soon as available.
	 *
	 * @throws Exception the exception
	 */
	private void repositoryPublishLoop() throws Exception {
		// Now we need to make sure we're initialized.
		synchronized (this.sessionPublishThreadLock) {
			// are we initialized?
			ensureInitialized();

			this.sessionPublishThreadLock.notifyAll();
		}

		// if we managed to initialize completely then lets get rockin'
		if (this.initialized) {
			while (!this.stopRequested) {
				// make sure the server is available. if not there's no point in proceeding.
				HubConnectionStatus serverStatus = this.client.canConnect();
				if (serverStatus.isValid()) {
					// make sure SendSessionsOnExit is set (since we were active)
					String ignoreMessage = "";
					String tempRefIgnoreMessage = new String(ignoreMessage);
					if (!Log.getSendSessionsOnExit() && (Log.canSendSessionsOnExit(tempRefIgnoreMessage))) {
						ignoreMessage = tempRefIgnoreMessage;
						Log.setSendSessionsOnExit(true);
					} else {
						ignoreMessage = tempRefIgnoreMessage;
					}

					// make sure we're set to the shorter check interval.
					this.checkInterval = Duration.ofSeconds(SHORT_CHECK_INTERVAL_SECONDS);
					this.failedAttempts = 0;

					// perform one process cycle. This is isolated so that whenever it needs to end
					// it can just return and we'll sleep.
					this.client.publishSessions(this.configuration.getServer().getPurgeSentSessions());
				} else {
					if (serverStatus.getStatus() == HubStatus.EXPIRED) {
						// use the extra long delay since it's very unlikely it'll get fixed.
						this.checkInterval = Duration.ofSeconds(EXPIRED_CHECK_INTERVAL_SECONDS);
					} else if (this.failedAttempts > 5) {
						this.checkInterval = Duration.ofSeconds(LONG_CHECK_INTERVAL_SECONDS);
						this.failedAttempts++;
					} else {
						this.checkInterval = Duration.ofSeconds(SHORT_CHECK_INTERVAL_SECONDS);
						this.failedAttempts++;
					}
				}

				// now it's time to rest our sleep interval unless there is a force request.
				sleepUntilNextCheck(this.checkInterval);
			}
		}
	}

	/**
	 * Ensure initialized.
	 */
	private void ensureInitialized() {
		if (!this.initialized) {
			synchronized (this.sessionPublishThreadLock) {
				try {
					// set up the repository client to work with the collection repository
					LocalRepository collection = Log.getRepository();

					ServerConfiguration serverConfiguration = this.configuration.getServer();
					if ((serverConfiguration != null) && (serverConfiguration.getEnabled())) {
						// We read the configuration to determine whether to pass in the application
						// name or not.
						this.client = new RepositoryPublishClient(collection,
								this.publisher.getSessionSummary().getProduct(),
								(serverConfiguration.getSendAllApplications() ? null
										: this.publisher.getSessionSummary().getApplication()),
								serverConfiguration);
					}

					// finally! We're good to go!
					this.initialized = true;
				} catch (RuntimeException ex) {

				}

				this.sessionPublishThreadLock.notifyAll();
			}
		}
	}

	/**
	 * Makes sure that there is an active, valid session publishing thread
	 * 
	 * This is a thread-safe method that acquires the session publishing thread lock
	 * on its own, so the caller need not have that lock prior to calling this
	 * method. If the session publishing thread has failed a new one will be
	 * started.
	 */
	private void ensureSessionPublishThreadIsValid() {
		// see if for some mystical reason our message dispatch thread failed.
		if (this.sessionPublishThreadFailed) {
			// OK, now - even though the thread was failed in our previous line, we now need
			// to get the thread lock and check it again
			// to make sure it didn't get changed on another thread.
			synchronized (this.sessionPublishThreadLock) {
				if (this.sessionPublishThreadFailed) {
					// we need to recreate the message thread
					createMessageDispatchThread();
				}

				this.sessionPublishThreadLock.notifyAll();
			}
		}
	}

	/**
	 * Get a multiprocess lock for the subscription engine.
	 *
	 * @param timeout the timeout
	 * @return the lock
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private InterprocessLock getLock(int timeout) throws IOException {
		return InterprocessLockManager.getInstance().lock(this, this.repositoryFolder, this.multiprocessLockName,
				timeout);
	}

	/**
	 * Sleep until next check.
	 *
	 * @param checkInterval the check interval
	 */
	private void sleepUntilNextCheck(Duration checkInterval) {
		synchronized (this.sessionPublishThreadLock) {
			// time to sleep.
			OffsetDateTime nextCheckTime = this.lastCheck.plus(checkInterval);
			while (!this.forceCheck && (nextCheckTime.isAfter(OffsetDateTime.now()))) {
				try {
					this.sessionPublishThreadLock.wait(1000);
				} catch (InterruptedException e) {
					// do nothing
				}
			}

			// to make sure we don't get into a fast loop outside, assume that if we're not
			// sleeping
			// then we're going to do a subscription check, or whatever we're sleeping
			// between checks.
			this.lastCheck = OffsetDateTime.now();
			this.forceCheck = false;

			this.sessionPublishThreadLock.notifyAll();
		}
	}
}