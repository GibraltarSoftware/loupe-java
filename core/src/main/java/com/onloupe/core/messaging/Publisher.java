package com.onloupe.core.messaging;

import com.onloupe.agent.SessionSummary;
import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.IMessengerConfiguration;
import com.onloupe.configuration.PublisherConfiguration;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketCache;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.PacketDefinitionList;
import com.onloupe.core.serialization.monitor.GibraltarPacket;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.session.SessionStatus;

import java.io.Closeable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * The central publisher for messaging.
 */
public class Publisher implements Closeable {
	
	/** The session summary. */
	private SessionSummary sessionSummary;
	
	/** The message queue lock. */
	private final Object messageQueueLock = new Object();
	
	/** The message dispatch thread lock. */
	private final Object messageDispatchThreadLock = new Object();
	
	/** The header packets lock. */
	private final Object headerPacketsLock = new Object();
	
	/** The config lock. */
	private final Object configLock = new Object();
	
	/** The cached types. */
	private PacketDefinitionList cachedTypes;
	
	/** The packet cache. */
	private PacketCache packetCache;
	
	/** The header packets. */
	private final ArrayList<ICachedMessengerPacket> headerPackets = new ArrayList<ICachedMessengerPacket>(); // LOCKED
																												// BY
																												/** The message queue. */
																												// HEADERPACKETSLOCK
	private ConcurrentLinkedQueue<PacketEnvelope> messageQueue;
	
	/** The message overflow queue. */
	private ConcurrentLinkedQueue<PacketEnvelope> messageOverflowQueue;
	
	/** The messengers. */
	private List<IMessenger> messengers; // LOCKED BY CONFIGLOCK
	
	/** The session name. */
	private String sessionName;

	/** The configuration. */
	private AgentConfiguration configuration;
	
	/** The message dispatch thread. */
	private Thread messageDispatchThread; // LOCKED BY THREADLOCK
	
	/** The message dispatch thread failed. */
	private volatile boolean messageDispatchThreadFailed; // LOCKED BY THREADLOCK (and volatile to allow quick reading
															
															/** The message queue max length. */
															// outside the lock)
	private int messageQueueMaxLength = 2000; // LOCKED BY QUEUELOCK
	
	/** The force write through. */
	private boolean forceWriteThrough;
	
	/** The initialized. */
	private boolean initialized; // designed to enable us to do our initialization in the background. LOCKED BY
									
									/** The shutdown. */
									// THREADLOCK
	private volatile boolean shutdown; // locks us down when we shut down LOCKED BY QUEUELOCK (and volatile to allow
										
										/** The packet sequence. */
										// quick reading outside the lock)
	private long packetSequence; // a monotonically increasing sequence number for packets as they get queued.
									
									/** The closed. */
									// LOCKED BY QUEUELOCK
	private boolean closed;

	// A thread-specific static flag for each thread, so we can disable blocking for
	// Publisher and Messenger threads

	/** The t thread must not block. */
	private static ThreadLocal<Boolean> tThreadMustNotBlock = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			// TODO Auto-generated method stub
			return Boolean.FALSE;
		}
	};

	// A thread-specific static flag for each thread, so we can disable notification
	/** The t thread must not notify. */
	// loops for Notifier threads
	private static ThreadLocal<Boolean> tThreadMustNotNotify = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			// TODO Auto-generated method stub
			return Boolean.FALSE;
		}
	};

	/**
	 * Create a new publisher
	 * 
	 * The publisher is a very central class; generally there should be only one per
	 * process. More specifically, there should be a one to one relationship between
	 * publisher, packet cache, and messengers to ensure integrity of the message
	 * output.
	 *
	 * @param sessionName the session name
	 * @param configuration the configuration
	 * @param sessionSummary the session summary
	 */
	public Publisher(String sessionName, AgentConfiguration configuration, SessionSummary sessionSummary) {
		if (TypeUtils.isBlank(sessionName)) {
			throw new NullPointerException("sessionName");
		}

		if (configuration == null) {
			throw new NullPointerException("configuration");
		}

		if (sessionSummary == null) {
			throw new NullPointerException("sessionSummary");
		}

		// store off all our input
		this.sessionName = sessionName;
		this.sessionSummary = sessionSummary;
		this.configuration = configuration;

		// create our queue, cache, and messenger objects
		this.messageQueue = new ConcurrentLinkedQueue<PacketEnvelope>(); // a more or less arbitrary initial queue
																			// size.
		this.messageOverflowQueue = new ConcurrentLinkedQueue<PacketEnvelope>(); // a more or less arbitrary initial
																					// queue
																					// size.
		this.cachedTypes = new PacketDefinitionList();
		this.packetCache = new PacketCache();
		this.messengers = new ArrayList<IMessenger>();

		this.messageQueueMaxLength = Math.max(configuration.getPublisher().getMaxQueueLength(), 1); // make sure
																										// there's no
		// way to get
		// it below 1.

		// create the thread we use for dispatching messages
		createMessageDispatchThread();
	}

	/**
	 * Permanently disable blocking when queuing messages from this thread.
	 * 
	 * This allows threads to switch on their thread-specific blocking-disabled flag
	 * for our queue, to guard against deadlocks in threads which are responsible
	 * for consuming and processing items from our queue. WARNING: This setting can
	 * not be reversed.
	 */
	public static void threadMustNotBlock() {
		tThreadMustNotBlock.set(true);
	}

	/**
	 * Query whether waiting on our queue items has been permanently disabled for
	 * the current thread.
	 * 
	 * @return This returns the thread-specific blocking-disabled flag. This flag is
	 *         false by default for each thread, unless Log.ThisThreadCannotLog() is
	 *         called to set it to true.
	 */
	public static boolean queryThreadMustNotBlock() {
		return tThreadMustNotBlock.get();
	}

	/**
	 * Permanently disable notification for messages issued from this thread.
	 * 
	 * This allows threads to switch on their thread-specific notification-disabled
	 * flag for our queue, to guard against indefinite loops in threads which are
	 * responsible for issuing notification events. WARNING: This setting can not be
	 * reversed.
	 */
	public static void threadMustNotNotify() {
		tThreadMustNotNotify.set(true);
	}

	/**
	 * Query whether notification alerts have been permanently disabled for messages
	 * issued by the current thread.
	 * 
	 * @return This returns the thread-specific notification-disabled flag. This
	 *         flag is false by default for each thread, unless
	 *         Log.ThisThreadCannotNotify() is called to set it to true.
	 */
	public static boolean queryThreadMustNotNotify() {
		return tThreadMustNotNotify.get();
	}

	/**
	 * Creates the message dispatch thread.
	 */
	private void createMessageDispatchThread() {
		synchronized (this.messageDispatchThreadLock) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						close();
					} catch (IOException e) {

					}
				}
			});
			
			// clear the dispatch thread failed flag so no one else tries to create our
			// thread
			this.messageDispatchThreadFailed = false;

			this.messageDispatchThread = new Thread() {
				@Override
				public void run() {
					messageDispatchMain();
				}
				
			};
			this.messageDispatchThread.setName("Loupe Publisher"); // name our thread so we can isolate it out of
																	// metrics
			// and
			// such
			// We generally WANT to keep the app alive as a foreground thread so we make
			// sure logs get flushed.
			// But once we have processed the exit command, we want to switch to a
			// background thread
			// to process anything left (which will be forced to use writeThrough blocking),
			// letting the
			// application be kept alive by its own foreground threads (while we continue to
			// process any
			// new log messages they send), but not hold up the application further once
			// they exit.
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
			threadMustNotBlock();

			// Now we need to make sure we're initialized.
			synchronized (this.messageDispatchThreadLock) {
				// are we initialized?
				ensureInitialized();
				this.messageDispatchThreadLock.notifyAll();
			}

			// Enter our main loop - dequeue packets and write them to all of the
			// messengers.
			// Foreground thread should exit when we process exit, but a background thread
			// should continue.
			while (!this.shutdown) {
				PacketEnvelope currentPacket = null;
				synchronized (this.messageQueueLock) {
					// If the queue is empty, wait for an item to be added
					// This is a while loop, as we may be pulsed but not wake up before another
					// thread has come in and
					// consumed the newly added object or done something to modify the queue. In
					// that case, we'll have to wait for another pulse.
					while ((this.messageQueue.isEmpty()) && (!this.shutdown)) {
						// This releases the message queue lock, only reacquiring it after being woken
						// up by a call to Pulse
						this.messageQueueLock.wait();
					}
					
					// if we got here then there was an item in the queue AND we have the lock.
					// Dequeue the item and then we want to release our lock.
					currentPacket = this.messageQueue.poll();
					if (currentPacket != null) {
						// and are we now below the maximum packet queue? if so we can release the
						// pending items.
						while ((!this.messageOverflowQueue.isEmpty())
								&& (this.messageQueue.size() < this.messageQueueMaxLength)) {
							// we still have an item in the overflow queue and we have room for it, so lets
							// add it.
							PacketEnvelope currentOverflowEnvelope = this.messageOverflowQueue.poll();

							this.messageQueue.offer(currentOverflowEnvelope);

							// and indicate that we've submitted this queue item. This does a thread pulse
							// under the covers,
							// and gets its own lock so we should NOT lock the envelope.
							currentOverflowEnvelope.setIsPending(false);
						}
					}

					// now pulse the next waiting thread there are that we've dequeued the packet.
					this.messageQueueLock.notifyAll();
				}

				// We have a packet and have released the lock (so others can queue more packets
				// while we're dispatching items.
				if (currentPacket != null) {
					synchronized (this.configLock) {
						dispatchPacket(currentPacket);
					}
				}
			}

			// We only get here if we exited the loop because a foreground thread sees we
			// are in ExitMode,
			// or if we are completely shut down.

			// Clear the dispatch thread variable since we're about to exit it and...
			//this.messageDispatchThread.interrupt();
		} catch (Exception e) {
			synchronized (this.messageDispatchThreadLock) {				
				// clear the dispatch thread variable since we're about to exit.
				this.messageDispatchThread = null;

				// we want to write out that we had a problem and mark that we're failed so
				// we'll get restarted.
				this.messageDispatchThreadFailed = true;

				this.messageDispatchThreadLock.notifyAll();
				
				if (SystemUtils.isInDebugMode()) {
					e.printStackTrace();
				}
			}

			onThreadAbort();
		}
	}

	/**
	 * On thread abort.
	 */
	private void onThreadAbort() {
		synchronized (this.messageQueueLock) {
			// We need to dump the queues and tell everyone to stop waiting, because we'll
			// never process them.
			this.shutdown = true; // Consider us shut down after this. The app is really exiting.
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

			this.messageQueueLock.notifyAll();
		}
	}

	/**
	 * Send the packet to every current messenger and add it to the packet cache if
	 * it's cachable.
	 *
	 * @param envelope the envelope
	 * @throws Exception the exception
	 */
	private void dispatchPacket(PacketEnvelope envelope) throws Exception {
		IMessengerPacket packet;
		synchronized (envelope) {
			packet = envelope.getPacket(); // rather than dig it out each time
			boolean writeThrough = envelope.getWriteThrough();

			// Any special handling for this packet?
			if (envelope.isCommand()) {
				// this is a command packet, we process it as a command instead of just a data
				// message
				CommandPacket commandPacket = (CommandPacket) packet;

				// Is this our exit or shutdown packet? We need to handle those here.
				if (commandPacket.getCommand() == MessagingCommand.SHUTDOWN) {
					this.shutdown = true; // Mark us as shut down. We will be by the time this method returns.
					// Make sure we block until each messenger closes, even if we weren't already in
					// writeThrough mode.
					writeThrough = true;
				}
			} else {
				// Not a command, so it must be a Gibraltar data packet of some type.

				// stamp the packet, and all of its dependent packets (this sets the sequence
				// number)
				stampPacket(packet, packet.getTimestamp());
			}
			// If this is a header packet we want to put it in the header list now - that
			// way
			// if any messenger recycles while we are writing to the messengers it will be
			// there.
			// (Better to pull the packet forward than to risk having it in an older stream
			// but not a newer stream)
			if (envelope.isHeader()) {
				synchronized (this.headerPacketsLock) // MS doc inconclusive on thread safety of ToArray, so we
														// guarantee
														// add/ToArray safety.
				{
					this.headerPackets.add((ICachedMessengerPacket) packet);
					this.headerPacketsLock.notifyAll();
				}
			}

			// Data message or Command packet - either way, send it on to each messenger.
			for (IMessenger messenger : this.messengers) {
				// we don't want an exception with one messenger to cause us a problem, so each
				// gets its own try/catch
				try {
					messenger.write(packet, writeThrough);
				} catch (Exception e) {
					if (SystemUtils.isInDebugMode()) {
						e.printStackTrace();
					}
				}
			}

			// if this was a write through packet we need to let the caller know that it was
			// committed.
			envelope.setIsCommitted(true); // under the covers this does a pulse on the threads waiting on this
											// envelope.
		}

		// Now that it's committed, finally send it to any Notifiers that may be
		// subscribed.
		queueToNotifier(packet);
	}

	/**
	 * Perform first-time initialization. We presume we're in a thread-safe lock.
	 */
	private void ensureInitialized() {
		if (!this.initialized) {
			this.forceWriteThrough = this.configuration.getPublisher().getForceSynchronous();

			// We need to load up the messengers in the configuration object.
			if (this.configuration.getSessionFile().getEnabled()) {
				addMessenger(this.configuration.getSessionFile(), FileMessenger.class);
			}

			if (this.configuration.getNetworkViewer().getEnabled()) {
				addMessenger(this.configuration.getNetworkViewer(), NetworkMessenger.class);
			}

			// and now we're initialized
			this.initialized = true;
		}
	}

	/**
	 * Adds the messenger.
	 *
	 * @param configuration the configuration
	 * @param messengerType the messenger type
	 */
	@SuppressWarnings("rawtypes")
	private void addMessenger(IMessengerConfiguration configuration, java.lang.Class messengerType) {
		IMessenger newMessenger = null;
		try {
			newMessenger = (IMessenger) messengerType.newInstance();
		} catch (Exception e) {
			if (SystemUtils.isInDebugMode()) {
				e.printStackTrace();
			}
		}

		// next step: initialize it
		if (newMessenger != null) {
			try {
				newMessenger.initialize(this, configuration);

				// now add it to our collection
				synchronized (this.configLock) {
					this.messengers.add(newMessenger);
				}
			} catch (Exception e) {
				if (SystemUtils.isInDebugMode()) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Ensure message dispatch thread is valid.
	 */
	private void ensureMessageDispatchThreadIsValid() {
		// see if for some mystical reason our message dispatch thread failed. But if
		// we're shut down then we don't care.
		if (this.messageDispatchThreadFailed && !this.shutdown) // Check outside the lock for efficiency.
																			// Valid because
		// they're volatile.
		{
			// OK, now - even though the thread was failed in our previous line, we now need
			// to get the thread lock and check it again
			// to make sure it didn't get changed on another thread.
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
	 * Queue to notifier.
	 *
	 * @param packet the packet
	 */
	private void queueToNotifier(IMessengerPacket packet) {

	}

	/**
	 * Perform the actual package queuing and wait for it to be committed.
	 * 
	 * This must be done within the message queue lock. This method may return a
	 * null envelope if called on a thread which must not block and the packet had
	 * to be discarded due to an overflow condition.
	 * 
	 * @param packet       The packet to be queued
	 * @param writeThrough True if the call should block the current thread until
	 *                     the packet has been committed, false otherwise.
	 * @return The packet envelope for the packet that was queued, or null if the
	 *         packet was discarded.
	 */
	private PacketEnvelope queuePacket(IMessengerPacket packet, boolean writeThrough) {
		// even though the packet might already have a timestamp that's preferable to
		// ours, we're deciding we're the judge of order to ensure it aligns with
		// sequence.
		packet.setTimestamp(OffsetDateTime.now()); // we convert to UTC during serialization, we want local time.

		// wrap it in a packet envelope and indicate we're in write through mode.
		PacketEnvelope packetEnvelope = new PacketEnvelope(packet, writeThrough);

		// But what queue do we put the packet in?
		if ((!this.messageOverflowQueue.isEmpty()) || (this.messageQueue.size() > this.messageQueueMaxLength)) {
			// We are currently using the overflow queue, so we'll put it there.
			// However, if we were called by a must-not-block thread, we want to discard
			// overflow packets...
			// unless it's a command packet, which is too important to discard (it just
			// won't wait on pending).
			if (tThreadMustNotBlock.get() && !packetEnvelope.isCommand()) {
				packetEnvelope = null; // We won't queue this packet, so there's no envelope to hang onto.
			} else {
				this.messageOverflowQueue.offer(packetEnvelope);

				// and set that it's pending so our caller knows they need to wait for it.
				packetEnvelope.setIsPending(true);
			}
		} else {
			// just queue the packet, we don't want to wait.
			this.messageQueue.offer(packetEnvelope);
		}

		return packetEnvelope;
	}

	/**
	 * Stamp packet.
	 *
	 * @param packet the packet
	 * @param defaultTimeStamp the default time stamp
	 * @throws Exception the exception
	 */
	private void stampPacket(IMessengerPacket packet, OffsetDateTime defaultTimeStamp) throws Exception {
		assert TimeConversion.epochTicks(defaultTimeStamp) > 0;

		// we don't check dependencies on command packets, it'll fail (and they aren't
		// written out)
		if (!(packet instanceof CommandPacket)) {
			// check our dependent packets to see if they've been stamped.
			List<IPacket> dependentPackets = getRequiredPackets(packet);

			if ((dependentPackets != null) && (!dependentPackets.isEmpty())) {
				// we only have to check these packets, not their children because if they've
				// been stamped, their children have.
				for (IPacket dependentPacket : dependentPackets) {
					IMessengerPacket dependentMessengerPacket = dependentPacket instanceof IMessengerPacket
							? (IMessengerPacket) dependentPacket
							: null;
					if ((dependentMessengerPacket != null) && (dependentMessengerPacket.getSequence() == 0)
							&& dependentMessengerPacket.getTimestamp() == null) // our quickest bail check - if it has a nonzero sequence it's definitely been stamped.
					{
						// stamp this guy first, we depend on him and he's not been stamped.
						stampPacket(dependentMessengerPacket, defaultTimeStamp);
					}
				}
			}
		}

		packet.setSequence(this.packetSequence);
		this.packetSequence++; // yeah, this could have been on the previous line. but hey, this is really clear on order.

		// make sure we have a timestamp - if there isn't one use the default (which is
		// the timestamp of the packet that depended on us or earlier)
		if (packet.getTimestamp() == null) {
			packet.setTimestamp(defaultTimeStamp);
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
	private void waitOnPacket(PacketEnvelope packetEnvelope) throws InterruptedException {
		// we are monitoring for write through by using object locking, so get the
		// lock...
		synchronized (packetEnvelope) {
			// and now we wait for it to be completed...
			while (!packetEnvelope.isCommitted()) {
				// This releases the envelope lock, only reacquiring it after being woken up by
				// a call to Pulse
				packetEnvelope.wait();
			}

			// as we exit, we need to pulse the packet envelope in case there is another
			// thread waiting
			// on it as well.
			packetEnvelope.notifyAll();
			;
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
	private static void waitOnPending(PacketEnvelope packetEnvelope) throws InterruptedException {
		// we are monitoring for pending by using object locking, so get the lock...
		synchronized (packetEnvelope) {
			// and now we wait for it to be submitted...
			while (packetEnvelope.isPending()) {
				// This releases the envelope lock, only reacquiring it after being woken up by
				// a call to Pulse
				packetEnvelope.wait();
				;
			}

			// as we exit, we need to pulse the packet envelope in case there is another
			// thread waiting
			// on it as well.
			packetEnvelope.notifyAll();
		}
	}

	/**
	 * Gets the required packets.
	 *
	 * @param packet the packet
	 * @return the required packets
	 * @throws Exception the exception
	 */
	private List<IPacket> getRequiredPackets(IPacket packet) throws Exception {
		PacketDefinition previewDefinition;
		int previewTypeIndex = this.cachedTypes.indexOf(packet);
		if (previewTypeIndex < 0) {
			previewDefinition = PacketDefinition.createPacketDefinition(packet);
			this.cachedTypes.add(previewDefinition);
		} else {
			previewDefinition = this.cachedTypes.get(previewTypeIndex);
		}

		return previewDefinition.getRequiredPackets(packet);
	}

	/**
	 * The central configuration of the publisher.
	 *
	 * @return the configuration
	 */
	public final PublisherConfiguration getConfiguration() {
		// before we return the configuration, we need to have been initialized.
		return this.configuration.getPublisher();
	}

	/**
	 * The cache of packets that have already been published.
	 *
	 * @return the packet cache
	 */
	public final PacketCache getPacketCache() {
		return this.packetCache;
	}

	/**
	 * Publish the provided batch of packets.
	 *
	 * @param packetArray  An array of packets to publish as a batch.
	 * @param writeThrough True if the information contained in packet should be
	 *                     committed synchronously, false if the publisher can use
	 *                     write caching (when available).
	 * @throws InterruptedException the interrupted exception
	 */
	public final void publish(IMessengerPacket[] packetArray, boolean writeThrough) throws InterruptedException {
		// Sanity-check the most likely no-op cases before we bother with the lock
		if (packetArray == null) {
			return;
		}

		// Check for nulls from the end to find the last valid packet.
		int count = packetArray.length;
		int lastIndex = count - 1;
		while (lastIndex >= 0 && packetArray[lastIndex] == null) {
			lastIndex--;
		}

		if (lastIndex < 0) {
			return; // An array of only null packets (or empty), just quick bail. Don't bother with
					// the lock.
		}

		PacketEnvelope lastPacketEnvelope = null;

		boolean effectiveWriteThrough;
		boolean isPending;
		int queuedCount = 0;

		// Get the queue lock.
		synchronized (this.messageQueueLock) {
			if (this.shutdown) // If we're already shut down, just bail. We'll never process it anyway.
			{
				return;
			}

			// Check to see if either the overall force write through or the local write
			// through are set...
			// or if we are in ExitingMode. In those cases, we'll want to block until the
			// packet is committed.
			effectiveWriteThrough = (this.forceWriteThrough || writeThrough);
			for (int i = 0; i < count; i++) {
				IMessengerPacket packet = packetArray[i];

				// We have to double-check each element for null, or QueuePacket() would barf on
				// it.
				if (packet != null) {
					// We have a real packet, so queue it. Only WriteThrough for the last packet, to
					// flush the rest.
					PacketEnvelope packetEnvelope = queuePacket(packet, effectiveWriteThrough && i >= lastIndex);

					// If a null is returned, the packet wasn't queued, so don't overwrite
					// lastPacketEnvelope.
					if (packetEnvelope != null) {
						queuedCount++;
						lastPacketEnvelope = packetEnvelope; // Keep track of the last one queued.

						if (!this.shutdown && packetEnvelope.isCommand()) {
							CommandPacket commandPacket = (CommandPacket) packet;
							if (commandPacket.getCommand() == MessagingCommand.SHUTDOWN) {
								// Once we *receive* an ExitMode command, all subsequent messages queued
								// need to block, to make sure the process stays alive for any final logging
								// foreground threads might have. We will be switching the Publisher to a
								// background thread when we process the ExitMode command so we don't hold
								// up the process beyond its own foreground threads.

								// Set the ending status, if it needs to be (probably won't).
								SessionStatus endingStatus = (SessionStatus) commandPacket.getState();
								if (endingStatus != null
										&& this.sessionSummary.getStatus().getValue() < endingStatus.getValue()) {
									this.sessionSummary.setStatus(endingStatus);
								}
							}
						}
					}
				}
			}

			if (effectiveWriteThrough && !tThreadMustNotBlock.get() && queuedCount > 0
					&& (lastPacketEnvelope == null || (lastPacketEnvelope.getPacket() != packetArray[lastIndex]))) {
				// The expected WriteThrough packet got dropped because of overflow? But we
				// still need to block until
				// those queued have completed, so issue a specific Flush command packet, which
				// should not get dropped.
				CommandPacket flushPacket = new CommandPacket(MessagingCommand.FLUSH);
				PacketEnvelope flushEnvelope = queuePacket(flushPacket, true);
				if (flushEnvelope != null) {
					lastPacketEnvelope = flushEnvelope;
				}
			}

			// Grab the pending flag before we release the lock so we know we have a
			// consistent view.
			// If we didn't queue any packets then lastPacketEnvelope will be null and
			// there's nothing to be pending.
			isPending = (lastPacketEnvelope == null) ? false : lastPacketEnvelope.isPending();

			// Now signal our next thread that might be waiting that the lock will be
			// released.
			this.messageQueueLock.notifyAll();
		}

		// Make sure our dispatch thread is still going. This has its own independent
		// locking (when necessary),
		// so we don't need to hold up other threads that are publishing.
		ensureMessageDispatchThreadIsValid();

		if (lastPacketEnvelope == null || tThreadMustNotBlock.get()) {
			// If we had no actual packets queued (e.g. shutdown, or no packets to queue),
			// there's nothing to wait on.
			// Also, special case for must-not-block threads. Once it's on the queue (or
			// not), don't wait further.
			// We need the thread to get back to processing stuff off the queue or we're
			// deadlocked!
			return;
		}

		// See if we need to wait because we've degraded to synchronous message handling
		// due to a backlog of messages
		if (isPending) {
			// This routine does its own locking so we don't need to interfere with the
			// nominal case of
			// not needing to pend.
			waitOnPending(lastPacketEnvelope);
		}

		// Finally, if we need to wait on the write to complete now we want to stall. We
		// had to do this outside of
		// the message queue lock to ensure we don't block other threads.
		if (effectiveWriteThrough) {
			waitOnPacket(lastPacketEnvelope);
		}
	}

	/**
	 * A generally unique name for this session
	 * 
	 * The session name consists of the application name and version and the session
	 * start date. It will generally be unique except in the case where a user
	 * starts two instances of the same application in the same second.
	 *
	 * @return the session name
	 */
	public final String getSessionName() {
		return this.sessionName;
	}

	/**
	 * The session summary for the session being published.
	 *
	 * @return the session summary
	 */
	public final SessionSummary getSessionSummary() {
		return this.sessionSummary;
	}

	/**
	 * The list of cached packets that should be in every stream before any other
	 * packet.
	 *
	 * @return the header packets
	 */
	public final ICachedMessengerPacket[] getHeaderPackets() {
		ICachedMessengerPacket[] returnVal;

		synchronized (this.headerPacketsLock) // MS doc inconclusive on thread safety of ToArray, so we guarantee
												// add/ToArray safety.
		{
			returnVal = this.headerPackets.toArray(new ICachedMessengerPacket[0]);
			this.headerPacketsLock.notifyAll();
		}

		return returnVal;
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if (!this.closed) {
			// Free managed resources here (normal Dispose() stuff, which should itself call
			// Dispose(true)).
			// Other objects may be referenced in this case.

			// We need to stall until we are shut down.
			if (!this.shutdown) {
				// We need to create and queue a close-messenger packet, and wait until it's
				// processed.
				try {
					publish(new CommandPacket[] { new CommandPacket(MessagingCommand.SHUTDOWN) }, true);
				} catch (InterruptedException e) {
					if (SystemUtils.isInDebugMode()) {
						e.printStackTrace();
					}
				}
			}
			
			// Free native resources here (alloc's, etc).
			// May be called from within the finalizer, so don't reference other objects
			// here.

			this.closed = true; // Make sure we only do this once
		}
	}
	
	/**
	 * Reset.
	 */
	public static void reset() {
		tThreadMustNotBlock = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				// TODO Auto-generated method stub
				return Boolean.FALSE;
			}
		};
		
		tThreadMustNotNotify = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				// TODO Auto-generated method stub
				return Boolean.FALSE;
			}
		};
	}
}