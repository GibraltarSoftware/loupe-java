package com.onloupe.core.monitor;

import java.io.Closeable;
import java.io.IOException;
import java.time.OffsetDateTime;

import com.onloupe.core.messaging.IMessengerPacket;
import com.onloupe.core.messaging.PacketEventArgs;
import com.onloupe.core.serialization.monitor.LogMessagePacket;
import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * Monitors packets going through the publisher to add user information as
 * needed.
 */
public class UserResolutionNotifier implements Closeable {
	
	/** The users. */
	private static ApplicationUserCollection users = new ApplicationUserCollection();

	/**
	 * Handler for the ResolveUser event.
	 */
	@FunctionalInterface
	public interface ResolveUserHandler {
		
		/**
		 * Invoke.
		 *
		 * @param sender the sender
		 * @param e the e
		 */
		void invoke(Object sender, ResolveUserEventArgs e);
	}

	/**
	 * Create a new instance of the user resolution notifier.
	 *
	 * @param anonymousMode the anonymous mode
	 */
	public UserResolutionNotifier(boolean anonymousMode) {
		if (!anonymousMode) {

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

	}

	/**
	 * Publisher on message dispatching.
	 *
	 * @param sender the sender
	 * @param e the e
	 */
	private void publisherOnMessageDispatching(Object sender, PacketEventArgs e) {
		IMessengerPacket tempVar = e.getPacket();
		LogMessagePacket logPacket = tempVar instanceof LogMessagePacket ? (LogMessagePacket) tempVar : null;
		if (logPacket == null) {
			return; // that's the only type of packet we care about, time to fast bail.
		}

		// if we don't have a user name at all then there's nothing to do
		if (TypeUtils.isBlank(logPacket.getUserName())) {
			return;
		}

		// now lookup the ApplicationUser to see if we have it already mapped..
		ApplicationUser user = getCurrentApplicationUser(logPacket.getUserName(), logPacket.getTimestamp(),
				logPacket.getSequence());
		if (user != null) {
			logPacket.setUserPacket(user.getPacket());
		}
	}

	/** The t in resolve user event. */
	private static ThreadLocal<Boolean> tInResolveUserEvent = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	/**
	 * Gets the current application user.
	 *
	 * @param userName the user name
	 * @param timestamp the timestamp
	 * @param sequence the sequence
	 * @return the current application user
	 */
	private ApplicationUser getCurrentApplicationUser(String userName, OffsetDateTime timestamp, long sequence) {
		// prevent infinite recursion
		if (tInResolveUserEvent.get()) {
			return null;
		}

		if (users.size() == 0) {
			return null; // we have nothing we can resolve...
		}

		if (TypeUtils.isBlank(userName)) {
			return null; // should never happen, we always have a user name BUT...
		}

		ApplicationUser applicationUser;
		if ((applicationUser = users.tryFindUserName(userName)) == null) {
			// since we have a miss we want to give our event subscribers a shot..
			ResolveUserEventArgs resolveEventArgs = new ResolveUserEventArgs(userName, timestamp, sequence);
			ThreadLocal<Boolean> threadLocalBoolean = new ThreadLocal<Boolean>();
			try {
				threadLocalBoolean.set(true);
			} catch (Exception ex) {
				// we can't log this because that would create an infinite loop (ignoring our
				// protection for same)
				threadLocalBoolean.set(false);
			} finally {
				tInResolveUserEvent = threadLocalBoolean;
			}

			applicationUser = resolveEventArgs.getUser();
			if (applicationUser != null) {
				// cache this so we don't keep going after it.
				applicationUser = users.trySetValue(applicationUser);
			}
		}

		return applicationUser;
	}
	
	/**
	 * Reset.
	 */
	public static void reset() {
		users = new ApplicationUserCollection();
		tInResolveUserEvent = new ThreadLocal<Boolean>() {
			@Override
			protected Boolean initialValue() {
				return Boolean.FALSE;
			}
		};
	}

}