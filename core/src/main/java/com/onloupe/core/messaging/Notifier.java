package com.onloupe.core.messaging;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.onloupe.agent.logging.MessageSourceProvider;
import com.onloupe.core.serialization.monitor.LogMessagePacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.log.LogMessageSeverity;

/**
 * Generates notifications from scanning log messages
 */
public class Notifier {
	private static final String NOTIFIER_CATEGORY_BASE = "Gibraltar.Agent.Notifier";
	private static final String NOTIFIER_THREAD_BASE = "Gibraltar Notifier";
	private static final int BURST_MILLISECOND_LATENCY = 28;
	
	private final Object messageQueueLock = new Object();
	private final Object messageDispatchThreadLock = new Object();
	private LogMessageSeverity minimumSeverity;
	private boolean groupMessages;
	private String notifierName;
	private String notifierCategoryName;
	private ConcurrentLinkedQueue<LogMessagePacket> messageQueue; // LOCKED BY QUEUELOCK

	private Thread messageDispatchThread; // LOCKED BY THREADLOCK
	private volatile boolean messageDispatchThreadFailed; // LOCKED BY THREADLOCK (and volatile to allow quick reading
															// outside the lock)
	private int messageQueueMaxLength = 2000; // LOCKED BY QUEUELOCK
	private OffsetDateTime burstCollectionWait; // LOCKED BY QUEUELOCK

	private OffsetDateTime lastNotifyCompleted; // Not locked. Single-threaded use inside the dispatch loop only.
	private Duration minimumWaitNextNotify; // Not locked. Single-threaded use inside the dispatch
												// loop only.
	private OffsetDateTime nextNotifyAfter; // Not locked. Single-threaded modify inside the dispatch loop only.
	private MessageSourceProvider eventErrorSourceProvider; // Not locked. Single-threaded use inside the dispatch loop
																// only.
																// BY
																// QUEUELOCK
																// (subscribed
																// only
																// through
																// proper

	/**
	 * Create a Notifier looking for a given minimum LogMessageSeverity.
	 * 
	 * @param minimumSeverity The minimum LogMessageSeverity to look for.
	 * @param notifierName    A name for this notifier (may be null for generic).
	 */

	public Notifier(LogMessageSeverity minimumSeverity, String notifierName) {
		this(minimumSeverity, notifierName, true);
	}

	/**
	 * Create a Notifier looking for a given minimum LogMessageSeverity.
	 *
	 * @param minimumSeverity The minimum LogMessageSeverity to look for.
	 * @param notifierName    A name for this notifier (may be null for generic).
	 * @param groupMessages   True to delay and group messages together for more
	 *                        efficient processing
	 */
	public Notifier(LogMessageSeverity minimumSeverity, String notifierName, boolean groupMessages) {
		this.messageQueue = new ConcurrentLinkedQueue<LogMessagePacket>(); // a more or less arbitrary initial size.
		this.minimumSeverity = minimumSeverity;
		this.groupMessages = groupMessages;
		this.minimumWaitNextNotify = Duration.ZERO; // No delay by default.
		this.messageDispatchThreadFailed = true; // We'll need to start one if we get a packet we care about.

		if (TypeUtils.isBlank(notifierName)) {
			this.notifierName = "";
			this.notifierCategoryName = NOTIFIER_CATEGORY_BASE;
		} else {
			this.notifierName = notifierName;
			this.notifierCategoryName = String.format("%1$s.%2$s", NOTIFIER_CATEGORY_BASE, notifierName);
		}
	}

	/**
	 * Get the CategoryName for this Notifier instance, as determined from the
	 * provided notifier name.
	 */
	public final String getNotifierCategoryName() {
		return this.notifierCategoryName;
	}

	private void publisherLogMessageNotify(Object sender, LogMessageNotifyEventArgs e) {
		queuePacket(e.packet);
	}

	private void queuePacket(IMessengerPacket messengerPacket) {
		LogMessagePacket packet = messengerPacket instanceof LogMessagePacket ? (LogMessagePacket) messengerPacket
				: null;
		if (packet == null || packet.getSuppressNotification()) {
			return;
		}

		if (packet.getSeverity().getSeverity() > this.minimumSeverity.getSeverity()) // Severity compares in reverse.
																						// Critical = 1, Verbose = 16.
		{
			return; // Bail if this packet doesn't meet the minimum severity we care about.
		}
		return; // TODO RKELLIHER KMILLER bypassing until we can fix it
//		synchronized (_MessageQueueLock) {
////			if (notificationEvent == null) // Check for unsubscribe race condition.
////			{
////				return; // Don't add it to the queue if there are no subscribers.
////			}
//
//			int messageQueueLength = _MessageQueue.size();
//			if (messageQueueLength < _MessageQueueMaxLength) {
//				if (messageQueueLength <= 0) // First new one: Wait for a burst to collect.
//				{
//					_BurstCollectionWait = TimeConversion.MIN; // Clear it so we'll reset the wait clock.
//				}
//
//				_MessageQueue.offer(packet);
//
//				// If there were already messages in our queue, it's waiting on a timeout, so
//				// don't bother pulsing it.
//				// But if there were no messages in the queue, we need to make sure it's not
//				// waiting forever!
//				if (messageQueueLength <= 0 || !OffsetDateTime.now().isBefore(_NextNotifyAfter)) {
//					_MessageQueueLock.notifyAll();
//				}
//			}
//		}
//
//		ensureNotificationThreadIsValid();
	}

	private void ensureNotificationThreadIsValid() {
		// See if for some mystical reason our notification dispatch thread failed.
		if (this.messageDispatchThreadFailed) // Check it outside the lock for efficiency. Valid because it's volatile.
		{
			// OK, now - even though the thread was failed in our previous line, we now need
			// to get the thread lock and
			// check it again to make double-sure it didn't get changed on another thread.
			synchronized (this.messageDispatchThreadLock) {
				if (this.messageDispatchThreadFailed) {
					// We need to (re)create the notification thread.
					createNotificationDispatchThread();
				}

				this.messageDispatchThreadLock.notifyAll();
			}
		}
	}

	private void createNotificationDispatchThread() {
		synchronized (this.messageDispatchThreadLock) {
			// Clear the dispatch thread failed flag so no one else tries to create our
			// thread.
			this.messageDispatchThreadFailed = false;

			// Name our thread so we can isolate it out of metrics and such.
			String threadName = (TypeUtils.isBlank(this.notifierName)) ? NOTIFIER_THREAD_BASE
					: String.format("%1$s %2$s", NOTIFIER_THREAD_BASE, this.notifierName);

			this.messageDispatchThread = new Thread() {
				@Override
				public void run() {
					notificationDispatchMain();
				}
			};
			this.messageDispatchThread.setName(threadName);
			this.messageDispatchThread.start();

			this.messageDispatchThreadLock.notifyAll();
		}
	}

	private void notificationDispatchMain() {
//		try {
//			Publisher.threadMustNotNotify(); // Suppress notification about any messages issued on this thread so we
//												// don't loop!
//
//			while (true) {
//				ILogMessage[] messages = null;
//				NotificationEventHandler notificationEvent;
//
//				// Wait until it is time to fire a notification event.
//				synchronized (_MessageQueueLock) {
//					while (_MessageQueue.size() <= 0) {
//						try {
//							_MessageQueueLock.wait();
//						} catch (InterruptedException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						} // Wait indefinitely until we get a message.
//
//						if (notificationEvent == null) {
//							_MinimumWaitNextNotify = Duration.ZERO; // Reset default wait time.
//							_NextNotifyAfter = OffsetDateTime.now(); // Reset any forced wait pending.
//							_MessageQueue.clear(); // And dump the queue since we have no subscribers. Loop again to
//													// wait.
//						}
//					}
//
//					if (_GroupMessages) {
//						OffsetDateTime now = OffsetDateTime.now();
//						if (_BurstCollectionWait.equals(TimeConversion.MIN)) {
//							// We know there must be a positive Count to exit the wait loop above, so Peek()
//							// is safe.
//							OffsetDateTime firstTime = _MessageQueue.peek().getTimestamp();
//							_BurstCollectionWait = firstTime.plusNanos(TimeUnit.MILLISECONDS.toNanos(BURST_MILLISECOND_LATENCY));
//							if (_BurstCollectionWait.isBefore(OffsetDateTime.now())) // Are we somehow already past this burst wait period?
//							{
//								_BurstCollectionWait = now.plusNanos(TimeUnit.MILLISECONDS.toNanos(10)); // Then allow a minimum wait in case of
//																				// lag.
//							}
//						}
//
//						if (_NextNotifyAfter.isBefore(_BurstCollectionWait) && _BurstCollectionWait.isAfter(OffsetDateTime.now())) {
//							_NextNotifyAfter = _BurstCollectionWait; // Wait for a burst to collect.
//						}
//
//						while (_NextNotifyAfter.isAfter(now) && !_MessageQueue.isEmpty()) {
//							Duration waitTime = Duration.between(now, _NextNotifyAfter); // How long must we wait to notify again?
//							try {
//								_MessageQueueLock.wait(waitTime.toMillis());
//							} catch (InterruptedException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							} // Wait the timeout.
//							now = OffsetDateTime.now();
//						}
//					}
//
//					// The wait has ended. Get our subscriber(s) and messages, if any.
//					notificationEvent = (Object sender, NotificationEventArgs e) -> notificationEvent.invoke(sender,
//							e);
//					if (notificationEvent == null) // Have we lost all of our subscribers while waiting?
//					{
//						_MinimumWaitNextNotify = Duration.ZERO; // Reset default wait time.
//						_NextNotifyAfter = OffsetDateTime.now(); // Reset any forced wait pending.
//					} else if (!_MessageQueue.isEmpty()) // Just to double-check; usually true here.
//					{
//						messages = _MessageQueue.toArray(new LogMessagePacket[0]);
//					}
//
//					_MessageQueue.clear(); // If no subscribers, we can clear it anyway.
//				}
//
//				// Now it's time to fire a notification event.
//				if (messages != null) {
//					// Fire the event from outside the lock.
//					NotificationEventArgs eventArgs = new NotificationEventArgs(messages, _MinimumWaitNextNotify);
//
//					// see if we should default to automatically sending data using the rules we
//					// typically recommend.
//					ServerConfiguration serverConfig = Log.getConfiguration().getServer();
//					if ((eventArgs.topSeverity.getSeverity() <= LogMessageSeverity.ERROR.getSeverity()) && (serverConfig.getAutoSendOnError()
//							&& serverConfig.getAutoSendSessions() && serverConfig.getEnabled())) {
//						eventArgs.setSendSession(true);
//						eventArgs.minimumNotificationDelay = Duration.ofMinutes(5);
//					}
//
//					try {
//						notificationEvent.invoke(this, eventArgs); // Call our subscriber(s) (should just be the Agent
//																	// layer).
//					} catch (RuntimeException ex) {
//						Log.recordException(getEventErrorSourceProvider(), ex, null, _NotifierCategoryName, true);
//					} finally {
//						_MinimumWaitNextNotify = eventArgs.minimumNotificationDelay;
//						if (_MinimumWaitNextNotify.isNegative()) // Sanity-check that the wait value is non-negative.
//						{
//							_MinimumWaitNextNotify = Duration.ZERO;
//						}
//
//						if (eventArgs.getSendSession()) // Did they signal us to send the current session now?
//						{
//
/////#pragma warning disable 4014
//							Log.sendSessions(Optional.of(SessionCriteria.ACTIVE), null, true); // Then let's
//																										// send it
//																										// before we
//																										// start the
//																										// wait time.
//						}
//
/////#pragma warning restore 4014
//
//						_LastNotifyCompleted = OffsetDateTime.now();
//						_NextNotifyAfter = _LastNotifyCompleted.plus(_MinimumWaitNextNotify);
//					}
//				}
//			}
//		} catch (RuntimeException ex) {
//			
//			synchronized (_MessageDispatchThreadLock) {
//				// clear the dispatch thread variable since we're about to exit.
//				_MessageDispatchThread = null;
//
//				// we want to write out that we had a problem and mark that we're failed so
//				// we'll get restarted.
//				_MessageDispatchThreadFailed = true;
//
//				_MessageDispatchThreadLock.notifyAll();
//			}
//		}
	}

	private MessageSourceProvider getEventErrorSourceProvider() {
		if (this.eventErrorSourceProvider == null) {
			this.eventErrorSourceProvider = new MessageSourceProvider("Gibraltar.Messaging.Notifier",
					"NotificationDispatchMain");
		}

		return this.eventErrorSourceProvider;
	}
}