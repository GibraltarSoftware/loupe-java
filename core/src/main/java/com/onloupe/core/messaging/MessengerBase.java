package com.onloupe.core.messaging;

import com.onloupe.configuration.IMessengerConfiguration;
import com.onloupe.core.logging.Log;
import com.onloupe.model.log.LogMessageSeverity;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * A baseline implementation of a messenger that provides common messenger
 * functionality.
 * 
 * This implementation is somewhat more elaborate than necessary because it
 * maintains a design that allows for multiple threads queuing requests, which
 * is why it needs an overflow queue instead of just blocking as soon as it
 * overflows. This can be simplified, but lets make sure we can test this out
 * first.
 */
public abstract class MessengerBase implements IMessenger, Closeable {
	
	/** The log category to use for log messages in this class. */
	public static final String LOG_CATEGORY = "Loupe.Messenger";

	/** The supports write through. */
	private boolean supportsWriteThrough;
	
	/** The message queue lock. */
	private final Object messageQueueLock = new Object();
	
	/** The message dispatch thread lock. */
	private final Object messageDispatchThreadLock = new Object();
	
	/** The message queue. */
	private ConcurrentLinkedQueue<PacketEnvelope> messageQueue;
	
	/** The message overflow queue. */
	private ConcurrentLinkedQueue<PacketEnvelope> messageOverflowQueue;

	/** The caption. */
	private String caption;
	
	/** The description. */
	private String description;
	
	/** The name. */
	private String name;

	/** The publisher. */
	private Publisher publisher;
	
	/** The configuration. */
	private IMessengerConfiguration configuration;
	
	/** The message dispatch thread. */
	private Thread messageDispatchThread; // LOCKED BY THREADLOCK
	
	/** The message dispatch thread failed. */
	volatile private boolean messageDispatchThreadFailed; // LOCKED BY THREADLOCK
	
	/** The message queue max length. */
	private int messageQueueMaxLength = 2000; // LOCKED BY QUEUELOCK
	
	/** The force write through. */
	private boolean forceWriteThrough;
	
	/** The initialized. */
	volatile private boolean initialized; // designed to enable us to do our initialization in the background. LOCKED
											
											/** The exiting. */
											// BY THREADLOCK
	volatile private boolean exiting; // set true once we have an exit-mode or close-messenger command pending LOCKED
										
										/** The exited. */
										// BY QUEUELOCK
	private boolean exited; // set true once we have processed the exit-mode command LOCKED BY QUEUELOCK
	
	/** The closed. */
	private boolean closed; // set true once we have been decommissioned (close-messenger) LOCKED BY
								
								/** The in overflow mode. */
								// QUEUELOCK
	private boolean inOverflowMode; // a flag to indicate whether we're in overflow mode or not. LOCKED BY QUEUELOCK
	
	/** The maintenance mode. */
	private boolean maintenanceMode; // a flag indicating when we're in maintenance mode. LOCKED BY QUEUELOCK
	
	/** The next flush due. */
	private LocalDateTime nextFlushDue = LocalDateTime.MIN;

	/**
	 * Create a new messenger.
	 *
	 * @param name                 A display name for this messenger to
	 *                             differentiate it from other messengers
	 */

	protected MessengerBase(String name) {
		this(name, true);
	}

	/**
	 * Create a new messenger.
	 *
	 * @param name                 A display name for this messenger to
	 *                             differentiate it from other messengers
	 * @param supportsWriteThrough True if the messenger supports synchronous (write
	 *                             through) processing
	 */

	protected MessengerBase(String name, boolean supportsWriteThrough) {
		this.name = name;
		this.supportsWriteThrough = supportsWriteThrough;

		// create our queue, cache, and messenger objects
		this.messageQueue = new ConcurrentLinkedQueue<>(); // a more or less arbitrary initial queue size.
		this.messageOverflowQueue = new ConcurrentLinkedQueue<>(); // a more or less arbitrary initial queue size.
	}

	/**
	 * Creates the message dispatch thread.
	 */
	private void createMessageDispatchThread() {
		synchronized (this.messageDispatchThreadLock) {
			// clear the dispatch thread failed flag so no one else tries to create our
			// thread
			this.messageDispatchThreadFailed = false;
			
			this.messageDispatchThread = new Thread() {
				@Override
				public void run() {
					messageDispatchMain();
				}
			};
			
			 // name our thread so we can isolate it out of metrics and such
			this.messageDispatchThread.setName("Loupe " + this.name + " Messenger");
			this.messageDispatchThread.start();

			this.messageDispatchThreadLock.notifyAll();
		}
	}

	/**
	 * The main method of the message dispatch thread.
	 */
	private void messageDispatchMain() {
		try {
			// Before initialization... We must never allow this thread (which processes the
			// queue!) to block
			// when adding items to our queue, or we would deadlock. (Does not need the lock
			// to set this.)
			Publisher.threadMustNotBlock();

			// Now we need to make sure we're initialized.
			synchronized (this.messageDispatchThreadLock) {
				// are we initialized?
				ensureInitialized();

				this.messageDispatchThreadLock.notifyAll();
			}

			// Enter our main loop - dequeue packets and write them to all of the
			// messengers.
			while ((!this.closed) && (this.initialized)) // when we introduce state-full error handling,
																	// remove the
			// initialized check
			{
				PacketEnvelope currentPacket = null;
				synchronized (this.messageQueueLock) {
					// If the queue is empty, wait for an item to be added
					// This is a while loop, as we may be pulsed but not wake up before another
					// thread has come in and
					// consumed the newly added object or done something to modify the queue. In
					// that case,
					// we'll have to wait for another pulse.
					while (this.messageQueue.isEmpty() && !this.closed
							&& (!getAutoFlush() || (this.nextFlushDue.compareTo(LocalDateTime.now()) > 0))) {
						// This releases the message queue lock, only reacquiring it after being woken
						// up by a call to Pulse
						this.messageQueueLock.wait(1000); // we'll stop waiting after a second so we can check for an
															// auto-flush
					}

					if (!this.messageQueue.isEmpty()) {
						// if we got here then there was an item in the queue AND we have the lock.
						// Dequeue the item and then we want to release our lock.
						currentPacket = this.messageQueue.poll();

						// Odd interlock case: If we were in maintenance mode then we may have gone over
						// our limit. Can we re-establish the limit?
						if (this.messageQueue.size() < this.messageQueueMaxLength) {
							// we can just clear it: Since the same thread in the same loop does maintenance
							// mode, we won't get back here
							// unless the derived class has already completed its maintenance.
							this.maintenanceMode = false;
						}

						// and are we now below the maximum packet queue? if so we can release the
						// pending items.
						while ((!this.messageOverflowQueue.isEmpty())
								&& (this.messageQueue.size() < this.messageQueueMaxLength)) {
							transferOverflow();
						}
					}

					// now pulse the next waiting thread there are that we've dequeued the packet.
					this.messageQueueLock.notifyAll();
				}

				// We have a packet and have released the lock (so others can queue more packets
				// while we're dispatching items.
				if (currentPacket != null) {
					MaintenanceModeRequest maintenanceRequested = dispatchPacket(currentPacket);

					// Did they request maintenance mode? If so we really need to change our
					// behavior.
					// But ignore regular maintenance requests if we're closing. No sense in adding
					// unnecessary work.
					// Unless the client app explicitly requested a maintenance rollover, which
					// we'll do regardless.
					if (maintenanceRequested != MaintenanceModeRequest.NONE
							&& (!this.exiting || maintenanceRequested == MaintenanceModeRequest.EXPLICIT)) {
						enterMaintenanceMode();
					}
				}

				// Do we need to do an auto-flush before we do the next packet?
				if (getAutoFlush() && !LocalDateTime.now().isBefore(nextFlushDue)) {
					actionOnFlush();
				}
			}
		} catch (Exception e) {
			synchronized (this.messageDispatchThreadLock) {
				// we want to write out that we had a problem and mark that we're failed so
				// we'll get restarted.
				this.messageDispatchThreadFailed = true;

				this.messageDispatchThreadLock.notifyAll();
			}

			onThreadAbort();
		}
	}

	/**
	 * On thread abort.
	 */
	private void onThreadAbort() {
		synchronized (this.messageQueueLock) {
			if (!this.exiting) // If we aren't exiting yet then we want to allow a new thread to pick up and
			// work the queue.
			{
				return;
			}

			// We need to dump the queues and tell everyone to stop waiting, because we'll never process them.
			this.closed = true; // Consider us closed after this. The app is really exiting.
			PacketEnvelope envelope;
			while (!this.messageQueue.isEmpty()) {
				envelope = this.messageQueue.poll();
				envelope.setIsPending(false);
				envelope.setIsCommitted(true);
			}

			while (!this.messageOverflowQueue.isEmpty()) {
				envelope = this.messageOverflowQueue.poll();
				envelope.setIsPending(false);
				envelope.setIsCommitted(true);
			}
		}
	}

	/**
	 * Transfer overflow.
	 */
	private void transferOverflow() {
		// we still have an item in the overflow queue and we have room for it, so lets
		// add it.
		PacketEnvelope currentOverflowEnvelope = this.messageOverflowQueue.poll();

		this.messageQueue.offer(currentOverflowEnvelope);

		// and indicate that we've submitted this queue item. This does a thread pulse
		// under the covers,
		// and gets its own lock so we should NOT lock the envelope.
		currentOverflowEnvelope.setIsPending(false);
	}

	/**
	 * Send the packet via our messenger and add it to our packet cache, if
	 * necessary.
	 *
	 * @param packetEnvelope the packet envelope
	 * @return maintenanceRequested Specifies whether maintenance mode has been
	 *                             requested after this packet and the type (source)
	 *                             of that request.
	 */
	private MaintenanceModeRequest dispatchPacket(PacketEnvelope packetEnvelope) {
		synchronized (packetEnvelope) {
			try {
				MaintenanceModeRequest maintenanceModeRequest = MaintenanceModeRequest.NONE;
				// We process all commands...
				if (packetEnvelope.isCommand()) {
					// this is a command packet, we process it as a command instead of a data message
					CommandPacket commandPacket = (CommandPacket) packetEnvelope.getPacket();
					MessagingCommand command = commandPacket.getCommand();

					switch (command) {
					case SHUTDOWN:
						// mark that we're closed.
						this.closed = true;

						// and call our closed event
						actionOnClose();
						break;
					case FLUSH:
						actionOnFlush();
						break;
					default:
						// Allow special handling by inheritors
						maintenanceModeRequest = actionOnCommand(command, commandPacket.getState(),
								packetEnvelope.getWriteThrough(), maintenanceModeRequest);
						break;
					}
				} else {
					// it's a data packet - we send this to our overridden write method for our inheritor to process.

					// we really don't want to expose the envelope at this time if we don't have to
					maintenanceModeRequest = actionOnWrite(packetEnvelope.getPacket(), packetEnvelope.getWriteThrough(), maintenanceModeRequest);
				}

				return maintenanceModeRequest;
			} finally {
				// if this was a write through packet we need to let the caller know that it was committed (at least, best we can do..)
				packetEnvelope.setIsCommitted(true); // under the covers this does a pulse on the threads waiting on this envelope
			}
		}
	}

	/**
	 * Enter maintenance mode.
	 *
	 * @throws NoSuchMethodException the no such method exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void enterMaintenanceMode() throws NoSuchMethodException, IOException {
		// set our flag so we know we're in maintenance. This affects the queuing, so we
		// need a queuelock
		synchronized (this.messageQueueLock) {
			this.maintenanceMode = true;

			// Point of order: We may be overfilling the queue if we're in maintenance mode.
			// This is because we don't want synchronous
			// overflow behavior while we are in maintenance or when catching back up from maintenance.
			// so, we need to be fair: Since these packets are going into the main queue, if
			// we have anything in the overflow queue it needs to be rolled into the main queue too.
			while (!this.messageOverflowQueue.isEmpty()) {
				transferOverflow(); // common functionality
			}

			// for best MT response time we're going to pulse waiting threads
			this.messageQueueLock.notifyAll();
		}

		// and now we let our thread go in and do the maintenance
		actionOnMaintenance();

		// we do NOT exit maintenance mode here: that is done by the message dispatch
		// thread when it sees that we've gotten back below the maximum queue size (if we're above it).
	}

	/**
	 * Wraps calling the OnCommand() method that derived classes use to provide
	 * common exception handling.
	 *
	 * @param command              The MessagingCommand enum value of this command.
	 * @param state                Optional. Command arguments
	 * @param writeThrough         Whether write-through (synchronous) behavior was
	 *                             requested.
	 * @param maintenanceRequested the maintenance requested
	 * @return the maintenance mode request
	 */
	private MaintenanceModeRequest actionOnCommand(MessagingCommand command, Object state, boolean writeThrough, MaintenanceModeRequest maintenanceRequested) {
		try {
			maintenanceRequested = onCommand(command, state, writeThrough, maintenanceRequested);
		} catch (Exception ex) {
			//we do not want exceptions to propagate back.
		}

		return maintenanceRequested;
	}

	/**
	 * Wraps calling the OnExit() method that derived classes use to provide common
	 * exception handling.
	 */
	private void actionOnExit() {
		try {
			onExit();
		} catch (RuntimeException ex) {
		}
	}

	/**
	 * Wraps calling the OnClose() method that derived classes use to provide common
	 * exception handling.
	 */
	private void actionOnClose() {
		try {
			onClose();
		} catch (Exception ex) {
		}
	}

	/**
	 * Wraps calling the OnConfigurationUpdate() method that derived classes use to
	 * provide common exception handling.
	 *
	 * @param configuration the configuration
	 */
	private void actionOnConfigurationUpdate(IMessengerConfiguration configuration) {
		try {
			onConfigurationUpdate(configuration);
		} catch (RuntimeException ex) {
		}
	}

	/**
	 * Wraps calling the OnFlush() method that derived classes use to provide common
	 * exception handling.
	 */
	private void actionOnFlush() {
		// since we're starting the flush procedure, we'll assume that this is going to
		// complete and there's no reason to autoflush
		if (getAutoFlush()) {
			updateNextFlushDue();
		}

		try {
			onFlush();
		} catch (Exception ex) {
		}
	}

	/**
	 * Wraps calling the OnInitialize() method that derived classes use to provide
	 * common exception handling.
	 *
	 * @param configuration the configuration
	 * @throws Exception the exception
	 */
	private void actionOnInitialize(IMessengerConfiguration configuration) throws Exception {
		try {
			onInitialize(configuration);

			if (!Log.getSilentMode()) {
				Log.write(LogMessageSeverity.VERBOSE, LOG_CATEGORY, "Initialized " + getName() + " Messenger",
						"%s\r\n%s\r\n\r\nConfiguration:\r\n%s", getCaption(), getDescription(), configuration);
			}

			// and since we've never written anything, act like we just flushed.
			if (getAutoFlush()) {
				updateNextFlushDue();
			}
		} catch (Exception ex) {

			// But we do want init failures to propagate up and cause it to stop trying, so
			// re-throw in production.
			throw ex;
		}
	}

	/**
	 * Wraps calling the OnMaintenance() method that derived classes use to provide
	 * common exception handling.
	 * 
	 */
	private void actionOnMaintenance() {
		try {
			onMaintenance();
		} catch (Exception ex) {
		}
	}

	/**
	 * Wraps calling the OnOverflow() method that derived classes use to provide
	 * common exception handling.
	 */
	private void actionOnOverflow() {
		try {
			onOverflow();
		} catch (RuntimeException ex) {
		}
	}

	/**
	 * Wraps calling the OnOverflowRestore() method that derived classes use to
	 * provide common exception handling.
	 */
	private void actionOnOverflowRestore() {
		try {
			onOverflowRestore();
		} catch (RuntimeException ex) {
		}
	}

	/**
	 * Wraps calling the OnWrite() method that derived classes use to provide common
	 * exception handling.
	 *
	 * @param packet the packet
	 * @param writeThrough the write through
	 * @param modeRequest the mode request
	 * @return the maintenance mode request
	 */
	private MaintenanceModeRequest actionOnWrite(IMessengerPacket packet, boolean writeThrough, MaintenanceModeRequest modeRequest) {
		try {
			modeRequest = onWrite(packet, writeThrough, modeRequest);
		} catch (Exception ex) {
			//don't let this exception propagate back to the caller.
		}
		return modeRequest;
	}

	/**
	 * Sets the next flush due date &amp; time based on the current time and the
	 * auto flush interval.
	 */
	private void updateNextFlushDue() {
		this.nextFlushDue = LocalDateTime.now().plusSeconds(getAutoFlushInterval());
	}

	/**
	 * Specifies whether maintenance mode has been requested and the type (source)
	 * of that request.
	 */
	protected enum MaintenanceModeRequest {
		/**
		 * No maintenance is being requested.
		 */
		NONE,

		/**
		 * Maintenance has been triggered by size or time thresholds.
		 */
		REGULAR,

		/**
		 * Maintenance has been explicitly requested by the client.
		 */
		EXPLICIT;

		/** The Constant SIZE. */
		public static final int SIZE = java.lang.Integer.SIZE;

		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public int getValue() {
			return this.ordinal();
		}

		/**
		 * For value.
		 *
		 * @param value the value
		 * @return the maintenance mode request
		 */
		public static MaintenanceModeRequest forValue(int value) {
			return values()[value];
		}
	}

	/**
	 * Indicates whether the messenger base should automatically flush the derived
	 * messenger.
	 * 
	 * When true the derived messenger will automatically be asked to flush based on
	 * the auto flush interval. If the messenger is manually flushed due to an
	 * external flush request the auto-flush will take that into account and keep
	 * waiting.
	 */
	private boolean autoFlush;

	/**
	 * Gets the auto flush.
	 *
	 * @return the auto flush
	 */
	protected final boolean getAutoFlush() {
		return this.autoFlush;
	}

	/**
	 * Sets the auto flush.
	 *
	 * @param value the new auto flush
	 */
	protected final void setAutoFlush(boolean value) {
		this.autoFlush = value;
	}

	/**
	 * The number of seconds since the last flush to trigger an automatic flush
	 * 
	 * This only applies when AutoFlush is true.
	 */
	private int autoFlushInterval;

	/**
	 * Gets the auto flush interval.
	 *
	 * @return the auto flush interval
	 */
	protected final int getAutoFlushInterval() {
		return this.autoFlushInterval;
	}

	/**
	 * Sets the auto flush interval.
	 *
	 * @param value the new auto flush interval
	 */
	protected final void setAutoFlushInterval(int value) {
		this.autoFlushInterval = value;
	}

	/**
	 * The publisher that created this messenger.
	 * 
	 * This property does not require any locks.
	 *
	 * @return the publisher
	 */
	protected final Publisher getPublisher() {
		return this.publisher;
	}

	/**
	 * True once the messenger is being closed or application is exiting, false
	 * otherwise
	 * 
	 * This property is not thread-safe; to guarantee thread safety callers should
	 * have the Queue Lock. This property indicates a normal application exit
	 * condition.
	 *
	 * @return the exiting
	 */
	protected final boolean getExiting() {
		return this.exiting;
	}

	/**
	 * True once the messenger is ready for the application to exit, false otherwise
	 * 
	 * This property is not thread-safe; to guarantee thread safety callers should
	 * have the Queue Lock.
	 *
	 * @return the exited
	 */
	protected final boolean getExited() {
		return this.exited;
	}

	/**
	 * True once the messenger has been closed, false otherwise
	 * 
	 * This property is not thread-safe; to guarantee thread safety callers should
	 * have the Queue Lock.
	 *
	 * @return the closed
	 */
	protected final boolean getClosed() {
		return this.closed;
	}

	/**
	 * True if the messenger has been initialized.
	 * 
	 * This property is not thread-safe; to guarantee thread safety callers should
	 * have the Thread Lock.
	 *
	 * @return the initialized
	 */
	protected final boolean getInitialized() {
		return this.initialized;
	}

	/**
	 * True if the current configuration forces write through mode.
	 * 
	 * This property is not thread-safe; to guarantee thread safety callers should
	 * have the Thread Lock.
	 *
	 * @return the force write through
	 */
	protected final boolean getForceWriteThrough() {
		return this.forceWriteThrough;
	}

	/**
	 * Synchronization object for the message queue.
	 * 
	 * In general it should not be necessary to do your own locking provided that
	 * you work within the locks provided by overrideable methods. If you get an
	 * object lock, you must use the Monitor.Pulse command to notify other threads
	 * that you are done with the lock. Failure to do so may cause your messenger to
	 * be unresponsive.
	 *
	 * @return the queue lock
	 */
	protected final Object getQueueLock() {
		return this.messageQueueLock;
	}

	/**
	 * Synchronization object for the dispatch thread.
	 * 
	 * In general it should not be necessary to do your own locking provided that
	 * you work within the locks provided by overrideable methods. If you get an
	 * object lock, you must use the Monitor.Pulse command to notify other threads
	 * that you are done with the lock. Failure to do so may cause your messenger to
	 * be unresponsive.
	 *
	 * @return the thread lock
	 */
	protected final Object getThreadLock() {
		return this.messageDispatchThreadLock;
	}

	/**
	 * The behavior of the messenger when there are too many messages in the queue
	 * 
	 * 
	 * Changes take effect the next time a message is published to the messenger.
	 * 
	 */
	private OverflowMode overflowMode;

	/**
	 * Gets the overflow mode.
	 *
	 * @return the overflow mode
	 */
	protected final OverflowMode getOverflowMode() {
		return this.overflowMode;
	}

	/**
	 * Sets the overflow mode.
	 *
	 * @param value the new overflow mode
	 */
	protected final void setOverflowMode(OverflowMode value) {
		this.overflowMode = value;
	}

	/**
	 * Perform first-time initialization. Requires the caller have the Thread Lock.
	 *
	 * @throws Exception the exception
	 */
	protected final void ensureInitialized() throws Exception {
		if (!this.initialized) {
			// set the values that are from our configuration
			this.caption = this.name;
			this.forceWriteThrough = this.configuration.getForceSynchronous();
			this.messageQueueMaxLength = this.configuration.getMaxQueueLength();

			// then let our inheritors know so they can have their way with things.
			actionOnInitialize(this.configuration);

			// and now we're initialized
			this.initialized = true;
		}
	}

	/**
	 * Makes sure that there is an active, valid queue dispatching thread
	 * 
	 * This is a thread-safe method that acquires the message dispatch thread lock
	 * on its own, so the caller need not have that lock prior to calling this
	 * method. If the message dispatch thread has failed a new one will be started.
	 */
	protected final void ensureMessageDispatchThreadIsValid() {
		// see if for some mystical reason our message dispatch thread failed.
		if (this.messageDispatchThreadFailed) {
			// OK, now - even though the thread was failed in our previous line, we now need
			// to get the thread lock and check it again to make sure it didn't get changed on another thread.
			synchronized (this.messageDispatchThreadLock) {
				if (this.messageDispatchThreadFailed) {
					// we need to recreate the message thread
					createMessageDispatchThread();
				}

				this.messageDispatchThreadLock.notifyAll();
			}
		}
	}

	/**
	 * Throws an exception if the messenger is closed. Useful on guarding public
	 * methods
	 * 
	 * This method does not check that the messenger is initialized (since that
	 * happens on the messenger's internal thread)
	 * 
	 * @param caller The string name of the calling method to attribute the
	 *               exception to.
	 */
	protected final void ensureOpen(String caller) {
		if (this.closed) {
			throw new IllegalArgumentException(
					"The messenger has been closed, so " + caller + " can no longer be called.");
		}
	}

	/**
	 * Perform the actual package queuing and wait for it to be committed.
	 * 
	 * This will be done within a Queue Lock.
	 * 
	 * @param packet       The packet to be queued
	 * @param writeThrough True if the call should block the current thread until
	 *                     the packet has been committed, false otherwise.
	 * @return The packet envelope for the packet that was queued.
	 */
	protected final PacketEnvelope queuePacket(IMessengerPacket packet, boolean writeThrough) {
		synchronized (this.messageQueueLock) {
			// wrap it in a packet envelope and indicate if we're in write through mode.
			PacketEnvelope packetEnvelope = new PacketEnvelope(packet, writeThrough);

			// First, a little hack. If we're queuing a flush command, reset our auto-flush time (if applicable).
			// Since PacketEnvelope already checks for a CommandPacket, use that to shortcut around this check normally.
			// Also check for an ExitMode or CloseMessenger command, to mark that one is pending (suppress maintenance).
			if (packetEnvelope.isCommand()) {
				CommandPacket commandPacket = (CommandPacket) packet; // safe if IsCommand
				MessagingCommand command = commandPacket.getCommand();
				if (command == MessagingCommand.FLUSH) {
					// Aha! We have a triggered flush coming down the pipe,
					// so we don't really need to do an autoflush in the mean time, if it's enabled.
					if (getAutoFlush()) {
						updateNextFlushDue(); // Put off the next auto-flush
					}
				} else if (command == MessagingCommand.SHUTDOWN) {
					// Once we receive an exit or close-messenger command packet, we want to
					// suppress maintenance operations.
					this.exiting = true; // An operation is pending which makes deferrable maintenance unimportant.
				}
			}

			// Now, which queue do we put the packet in?

			// We're in overflow if we are NOT in maintenance mode and either there are
			// items in the overflow queue or we reached our max length.
			// Maintenance mode trumps
			if (!this.maintenanceMode && ((!this.messageOverflowQueue.isEmpty())
					|| (this.messageQueue.size() > this.messageQueueMaxLength))) {
				// we are in an overflow scenario - what should we do?
				if (getOverflowMode() == OverflowMode.DROP) {
					// bah, we aren't supposed to record it.
					packetEnvelope.setIsCommitted(true); // because it never will be written. Just in case anyone else is checking that.
				} else {
					// we are currently using the overflow queue, put it there.
					this.messageOverflowQueue.offer(packetEnvelope);

					// and set that it's pending so our caller knows they need to wait for it.
					packetEnvelope.setIsPending(true);

					// do we need to call our On Overflow override?
					if (!this.inOverflowMode) {
						this.inOverflowMode = true;

						// yep, that was the first.
						actionOnOverflow();
					}
				}
			} else {
				// just queue the packet, we don't want to wait.
				this.messageQueue.offer(packetEnvelope);

				if (this.inOverflowMode) {
					this.inOverflowMode = false;

					// we need to let them know that we're no longer in overflow.
					actionOnOverflowRestore();
				}
			}

			return packetEnvelope;
		}
	}

	/**
	 * Suspends the calling thread until the provided packet is committed.
	 * 
	 * Even if the envelope is not set to write through the method will not return
	 * until the packet has been committed. This method performs its own
	 * synchronization and should not be done within a lock.
	 *
	 * @param packetEnvelope The packet that must be committed
	 * @throws InterruptedException the interrupted exception
	 */
	protected static void waitOnPacket(PacketEnvelope packetEnvelope) throws InterruptedException {
		// we are monitoring for write through by using object locking, so get the
		// lock...
		synchronized (packetEnvelope) {
			// and now we wait for it to be completed...
			while (!packetEnvelope.isCommitted()) {
				// This releases the envelope lock, only reacquiring it after being woken up by a call to Pulse
				packetEnvelope.wait();
			}

			// as we exit, we need to pulse the packet envelope in case there is another
			// thread waiting on it as well.
			packetEnvelope.notifyAll();
		}
	}

	/**
	 * Suspends the calling thread until the provided packet is no longer pending.
	 * 
	 * This method performs its own synchronization and should not be done within a
	 * lock.
	 *
	 * @param packetEnvelope The packet that must be submitted
	 * @throws InterruptedException the interrupted exception
	 */
	protected static void waitOnPending(PacketEnvelope packetEnvelope) throws InterruptedException {
		// we are monitoring for pending by using object locking, so get the lock...
		synchronized (packetEnvelope) {
			// and now we wait for it to be submitted...
			while (packetEnvelope.isPending()) {
				// This releases the envelope lock, only reacquiring it after being woken up by
				// a call to Pulse
				packetEnvelope.wait();
			}

			// as we exit, we need to pulse the packet envelope in case there is another
			// thread waiting on it as well.
			packetEnvelope.notifyAll();
		}
	}

	/**
	 * Inheritors should override this method to implement custom Command handling
	 * functionality
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively. Some commands (Shutdown, Flush) are
	 * handled by MessengerBase and redirected into specific method calls.
	 *
	 * @param command              The MessagingCommand enum value of this command.
	 * @param state                Optional. Command arguments
	 * @param writeThrough         Whether write-through (synchronous) behavior was
	 *                             requested.
	 * @param maintenanceRequested Specifies whether maintenance mode has been
	 *                             requested and the type (source) of that request.
	 * @return the maintenance mode request
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected MaintenanceModeRequest onCommand(MessagingCommand command, Object state, boolean writeThrough, MaintenanceModeRequest maintenanceRequested) throws IOException {
		// we do nothing by default
		return MaintenanceModeRequest.NONE;
	}

	/**
	 * Inheritors should override this method to implement custom Exit functionality
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 */
	protected void onExit() {
		// we do nothing by default
	}

	/**
	 * Inheritors should override this method to implement custom Close
	 * functionality
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void onClose() throws IOException {
		// we do nothing by default
	}

	/**
	 * Inheritors should override this method to implement custom Configuration
	 * Update functionality.
	 * 
	 * Code in this method is protected by a Thread Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @param configuration the configuration
	 */
	protected void onConfigurationUpdate(IMessengerConfiguration configuration) {
		// we do nothing by default
	}

	/**
	 * Inheritors should override this method to implement custom flush
	 * functionality.
	 * 
	 * Code in this method is protected by a Queue Lock. This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void onFlush() throws IOException {
		// we do nothing by default
	}

	/**
	 * Inheritors should override this method to implement custom initialize
	 * functionality.
	 * 
	 * This method will be called exactly once before any call to OnFlush or OnWrite
	 * is made. Code in this method is protected by a Thread Lock. This method is
	 * called with the Message Dispatch thread exclusively.
	 *
	 * @param configuration the configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void onInitialize(IMessengerConfiguration configuration) throws IOException {
		// we do nothing by default
	}

	/**
	 * Inheritors should override this to implement a periodic maintenance
	 * capability
	 * 
	 * Maintenance is invoked by a return value from the OnWrite method. When
	 * invoked, this method is called and all log messages are buffered for the
	 * duration of the maintenance period. Once this method completes, normal log
	 * writing will resume. During maintenance, any queue size limit is ignored.
	 * This method is not called with any active locks to allow messages to continue
	 * to queue during maintenance. This method is called with the Message Dispatch
	 * thread exclusively.
	 *
	 * @throws Exception the exception
	 * @throws SecurityException the security exception
	 */
	protected void onMaintenance() throws Exception {
		// we do nothing by default
	}

	/**
	 * Inheritors can override this method to implement custom functionality when
	 * the main queue over flows.
	 * 
	 * This method is called when the first packet is placed in the overflow queue.
	 * It will not be called again unless there is a call to OnOverflowRestore,
	 * indicating that the overflow has been resolved. Code in this method is
	 * protected by a Queue Lock. This method is called with the Message Dispatch
	 * thread exclusively.
	 */
	protected void onOverflow() {
		// we do nothing by default
	}

	/**
	 * Inheritors can override this method to implement custom functionality when
	 * the main queue is no longer in overflow.
	 * 
	 * This method is called when there are no more packets in the overflow queue It
	 * will not be called again unless there is a call to OnOverflowRestore,
	 * indicating that the overflow has been resolved. Code in this method is
	 * protected by a Queue Lock. This method is called with the Message Dispatch
	 * thread exclusively.
	 */
	protected void onOverflowRestore() {
		// we do nothing by default
	}

	/**
	 * Inheritors must override this method to implement their custom message
	 * writing functionality.
	 * 
	 * Code in this method is protected by a Queue Lock This method is called with
	 * the Message Dispatch thread exclusively.
	 *
	 * @param packet the packet
	 * @param writeThrough the write through
	 * @param maintenanceModeRequested the maintenance mode requested
	 * @return the maintenance mode request
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	protected abstract MaintenanceModeRequest onWrite(IMessengerPacket packet, boolean writeThrough, MaintenanceModeRequest maintenanceModeRequested)
			throws NoSuchMethodException, SecurityException, IOException, Exception;

	/**
	 * A display caption for this messenger
	 * 
	 * End-user display caption for this messenger. Captions are typically not
	 * unique to a given instance of a messenger.
	 *
	 * @return the caption
	 */
	@Override
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	protected final void setCaption(String value) {
		this.caption = value;
	}

	/**
	 * A display description for this messenger.
	 *
	 * @return the description
	 */
	@Override
	public final String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description.
	 *
	 * @param value the new description
	 */
	protected final void setDescription(String value) {
		this.description = value;
	}

	/**
	 * Called by the publisher every time the configuration has been updated.
	 * 
	 * @param configuration The unique name for this messenger.
	 */
	@Override
	public final void configurationUpdated(IMessengerConfiguration configuration) {
		ensureOpen("ConfigurationUpdated");

		// we only process this event if we've already initialized.
		synchronized (this.messageDispatchThreadLock) {
			// we need to go get a new configuration object for ourself.
			this.configuration = configuration;

			if (this.initialized) {
				actionOnConfigurationUpdate(this.configuration);
			}

			this.messageDispatchThreadLock.notifyAll();
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
	public final void close() throws IOException {
		if (!this.closed) {
			// Free managed resources here (normal Dispose() stuff, which should itself call
			// Dispose(true))
			// Other objects may be referenced in this case

			// We need to stall until we are shut down.
			if (!this.closed) {
				// We need to create and queue a CloseMessenger command, and block until its
				// done.
				try {
					write(new CommandPacket(MessagingCommand.SHUTDOWN), true);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
			// Free native resources here (alloc's, etc)
			// May be called from within the finalizer, so don't reference other objects
			// here

			this.closed = true; // Make sure we only do this once
		}
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the other parameter;
	 *         otherwise, false.
	 */
	public final boolean equals(IMessenger other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		// compare based on name
		return this.name.equals(other.getName());
	}

	/**
	 * Initialize the messenger so it is ready to accept packets.
	 * 
	 * @param publisher     The publisher that owns the messenger
	 * @param configuration The unique name for this messenger.
	 */
	@Override
	public final void initialize(Publisher publisher, IMessengerConfiguration configuration) {
		ensureOpen("Initialize");

		this.publisher = publisher;
		this.configuration = configuration;

		// create the thread we use for dispatching messages
		createMessageDispatchThread();
	}

	/**
	 * A name for this messenger
	 * 
	 * The name is unique and specified by the publisher during initialization.
	 *
	 * @return the name
	 */
	@Override
	public final String getName() {
		return this.name;
	}

	/**
	 * Write the provided packet to this messenger.
	 * 
	 * The packet may depend on other packets. If the messenger needs those packets
	 * they are available from the publisher's packet cache.
	 *
	 * @param packet       The packet to write through the messenger.
	 * @param writeThrough True if the information contained in packet should be
	 *                     committed synchronously, false if the messenger should
	 *                     use write caching (if available).
	 * @throws InterruptedException the interrupted exception
	 */
	@Override
	public final void write(IMessengerPacket packet, boolean writeThrough) throws InterruptedException {
		if (packet == null) {
			return; // just rapid bail. don't bother with the lock.
		}

		// EnsureOpen("Write"); // Don't throw an exception; if we're closed just return
		// (below).

		PacketEnvelope packetEnvelope;

		boolean effectiveWriteThrough;
		boolean isPending;

		// get the queue lock
		synchronized (this.messageQueueLock) {
			// now that we're in the exclusive lock, check to see if we're actually closed.
			if (this.closed) {
				return; // it wouldn't get logged anyway.
			}

			// Check whether this should writeThrough, either by request, by configuration,
			// or because we have
			// received an ExitMode or CloseMessenger command (pending) and need to flush
			// after each packet.
			effectiveWriteThrough = this.supportsWriteThrough
					&& (this.forceWriteThrough || writeThrough || this.exiting);

			// and queue the packet.
			packetEnvelope = queuePacket(packet, effectiveWriteThrough);

			// grab the pending flag before we release the lock so we know we have a
			// consistent view.
			isPending = packetEnvelope.isPending();

			// now signal our next thread that might be waiting that the lock will be
			// released.
			this.messageQueueLock.notifyAll();
		}

		// make sure our dispatch thread is still going. This has its own independent
		// locking (when necessary),
		// so we don't need to hold up other threads that are publishing.
		ensureMessageDispatchThreadIsValid();

		// See if we need to wait because we've degraded to synchronous message handling
		// due to a backlog of messages
		if (isPending) {
			// this routine does its own locking so we don't need to interfere with the
			// nominal case of
			// not needing to pend.
			waitOnPending(packetEnvelope);
		}

		// Finally, if we need to wait on the write to complete now we want to stall. We
		// had to do this outside of
		// the message queue lock to ensure we don't block other threads.
		if (effectiveWriteThrough) {
			waitOnPacket(packetEnvelope);
		}
	}
}