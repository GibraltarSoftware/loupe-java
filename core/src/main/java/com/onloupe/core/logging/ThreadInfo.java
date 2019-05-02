package com.onloupe.core.logging;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.onloupe.core.monitor.PropertyChangedEventArgs;
import com.onloupe.core.serialization.monitor.IDisplayable;
import com.onloupe.core.serialization.monitor.ThreadInfoPacket;
import com.onloupe.core.util.TypeUtils;

public class ThreadInfo extends Observable implements IDisplayable {
	private ThreadInfoPacket packet;
	private volatile int threadInstance; // Used to distinguish threads with the same name.
	private volatile String caption;
	private volatile String description;

	public ThreadInfo() {
		this.packet = new ThreadInfoPacket();
		this.packet.setThreadIndex(ThreadToken.getCurrentThreadIndex()); // Each ThreadInfo we create gets a unique
																			// index
																			// value.

		Thread thread = Thread.currentThread();
		this.packet.setThreadId(thread.getId()); // These can get recycled, so they aren't domain-lifetime unique.
		this.packet.setThreadName((thread.getName() != null) ? thread.getName() : ""); // prevent null, but let empty
																						// name
		// pass through
	}

	public ThreadInfo(ThreadInfoPacket packet) {
		this.packet = packet;
	}

	public ThreadInfo(String threadName) {		
		this.packet = new ThreadInfoPacket();
		this.packet.setThreadIndex(ThreadToken.getCurrentThreadIndex());
		this.packet.setThreadName(threadName);

		// we were not given the thread id. Attempt to find it.
		try {
			Optional<Thread> thread = Thread.getAllStackTraces().keySet().stream()
					.filter(key -> key.getName().equalsIgnoreCase(threadName)).findFirst();

			this.packet.setThreadId(thread.isPresent() ? thread.get().getId() : null);
		} catch (Exception e) {
			// forget it.
		}
	}
	
	public ThreadInfo(long threadId, String threadName) {
		this.packet = new ThreadInfoPacket();
		this.packet.setThreadIndex(ThreadToken.getCurrentThreadIndex());
		this.packet.setThreadId(threadId);
		this.packet.setThreadName(threadName);
	}

	@Override
	public final String getCaption() {
		if (this.caption == null) {
			StringBuilder buffer = new StringBuilder();
			if (TypeUtils.isBlank(this.packet.getThreadName())) {
				buffer.append(String.format("Thread %1$s", this.packet.getThreadId()));
			} else {
				buffer.append(this.packet.getThreadName());
			}

			if (this.threadInstance > 0) {
				buffer.append(String.format(" #%1$s", this.threadInstance));
			}

			this.caption = buffer.toString();
		}

		return this.caption;
	}

	@Override
	public final String getDescription() {
		if (this.description == null) {
			StringBuilder buffer = new StringBuilder();
			// Threads are either foreground, background, or threadpool (which are a subset
			// of background)
			// TODO RKELLIHER look into this, since all java threads are background
			if (this.packet.isBackground()) {
				buffer.append(this.packet.isThreadPoolThread() ? "ThreadPool Thread " : "Background Thread ");
			} else {
				buffer.append("Foreground Thread ");
			}
			buffer.append(this.packet.getThreadId());

			if (!TypeUtils.isBlank(this.packet.getThreadName())) // Add specific name, if it had one
			{
				buffer.append(String.format(" %1$s", this.packet.getThreadName()));
			}

			if (this.threadInstance > 0) {
				buffer.append(String.format(" #%1$s", this.threadInstance));
			}

			this.description = buffer.toString();
		}

		return this.description;
	}

	public final UUID getId() {
		return this.packet.getID();
	}

	public final long getThreadId() {
		return this.packet.getThreadId();
	}

	public final long getThreadIndex() {
		return this.packet.getThreadIndex();
	}

	/**
	 * A uniquifier for display purposes (set by Analyst)
	 */
	public final int getThreadInstance() {
		return this.threadInstance;
	}

	public final void setThreadInstance(int value) {
		if (this.threadInstance != value) {
			this.threadInstance = value;
			this.caption = null; // Clear the cache so it recomputes with the new instance number.
			this.description = null; // Clear the cache so it recomputes with the new instance number.
		}
	}

	public final String getThreadName() {
		return this.packet.getThreadName();
	}

	public final void setThreadName(String value) {
		if (!this.packet.getThreadName().equals(value)) {
			this.packet.setThreadName(value);

			// and signal that we changed a property we expose
			sendPropertyChanged("ThreadName");
		}
	}

	public final int getDomainId() {
		return this.packet.getDomainId();
	}

	public final String getDomainName() {
		return this.packet.getDomainName();
	}

	public final boolean isBackground() {
		return this.packet.isBackground();
	}

	public final boolean isThreadPoolThread() {
		return this.packet.isThreadPoolThread();
	}

	public final ThreadInfoPacket getPacket() {
		return this.packet;
	}

	/**
	 * Is the thread this instance is about still active in memory? Only legitimate
	 * within the session where the thread was running. Do not query this for
	 * playback outside the original running session.
	 */
	public final boolean isStillAlive() {
		return ThreadToken.isThreadStillAlive(getThreadIndex());
	}

	/**
	 * Is the thread with the specified threadIndex still active in memory? Only
	 * legitimate within the session where the thread was running. Do not query this
	 * for playback outside the original running session.
	 * 
	 * @param threadIndex The unique ThreadIndex value which the Agent assigned to
	 *                    the thread in question.
	 * @return Reports true if the managed thread which was assigned the specified
	 *         threadIndex still exists or has not yet garbage collected its
	 *         [ThreadStatic] variables. Reports false after garbage collection.
	 */
	public static boolean isThreadStillAlive(int threadIndex) {
		return ThreadToken.isThreadStillAlive(threadIndex);
	}

	/**
	 * Returns the unique threadIndex value assigned to the current thread.
	 * 
	 * @return The threadIndex value for the current thread which is unique across
	 *         the life of this log session.
	 */
	public static long getCurrentThreadIndex() {
		return ThreadToken.getCurrentThreadIndex();
	}

	private void sendPropertyChanged(String propertyName) {
		setChanged();
		notifyObservers(new PropertyChangedEventArgs(propertyName));
	}

	/**
	 * Compares this ThreadInfo object to another to determine sorting order.
	 * 
	 * ThreadInfo instances are sorted by their ThreadId property.
	 * 
	 * @param other The other ThreadInfo object to compare this object to.
	 * @return An int which is less than zero, equal to zero, or greater than zero
	 *         to reflect whether this ThreadInfo should sort as being less-than,
	 *         equal to, or greater-than the other ThreadInfo, respectively.
	 */
	public final int compareTo(ThreadInfo other) {
		if (other == null) {
			return 1; // We're not null, so we're greater than anything that is null.
		}

		if (this == other) {
			return 0; // Refers to the same instance, so obviously we're equal.
		}

		// But in general, we compare first based on ThreadId.
		int compare = (new Long(getThreadId())).compareTo(other.getThreadId());

		// Unfortunately, ThreadId isn't as unique as we thought, so do some follow-up
		// compares.
		if (compare == 0) {
			compare = (new Long(this.packet.getThreadIndex())).compareTo(other.getThreadIndex());
		}

		if (compare == 0) {
			compare = this.packet.getTimestamp().compareTo(other.getPacket().getTimestamp());
		}

		if (compare == 0) {
			compare = (new Long(this.packet.getSequence())).compareTo(other.getPacket().getSequence());
		}

		if (compare == 0) {
			compare = getId().compareTo(other.getId()); // Finally, compare by Guid if we have to.
		}

		return compare;
	}

	/**
	 * Determines if the provided ThreadInfo object is identical to this object.
	 * 
	 * @param other The ThreadInfo object to compare this object to
	 * @return True if the objects represent the same data.
	 */
	public final boolean equals(ThreadInfo other) {
		if (compareTo(other) == 0) {
			return true;
		}

		return false;
	}

	/**
	 * Determines if the provided object is identical to this object.
	 * 
	 * @param obj The object to compare this object to
	 * @return True if the other object is also a ThreadInfo and represents the same
	 *         data.
	 */
	@Override
	public boolean equals(Object obj) {
		ThreadInfo otherThreadInfo = obj instanceof ThreadInfo ? (ThreadInfo) obj : null;

		return equals(otherThreadInfo); // Just have type-specific Equals do the check (it even handles null)
	}

	/**
	 * Provides a representative hash code for objects of this type to spread out
	 * distribution in hash tables.
	 * 
	 * Objects which consider themselves to be Equal (a.Equals(b) returns true) are
	 * expected to have the same hash code. Objects which are not Equal may have the
	 * same hash code, but minimizing such overlaps helps with efficient operation
	 * of hash tables.
	 * 
	 * @return An int representing the hash code calculated for the contents of this
	 *         object.
	 * 
	 */
	@Override
	public int hashCode() {
		// We can't use timestamp or sequence here because they might not be set
		// initially (or with older Agent versions)
		// and bad things could happen if the hash code changes while it's placed in a
		// hashed collection. So use Guid.
		int myHash = getId().hashCode(); // Guid is guaranteed to exist and remain constant.

		return myHash;
	}

	/**
	 * A class to help detect when a managed thread no longer exists.
	 */
	private static class ThreadToken {
		private static ThreadLocal<ThreadToken> tThreadToken = new ThreadLocal<>();

		private static AtomicInteger latestThreadIndex = new AtomicInteger();
		private static final Map<Long, WeakReference> threadTokenMap = new HashMap<Long, WeakReference>();
		private static final Object mapLock = new Object(); // Lock for ThreadTokenMap.

		private long _ThreadIndex;

		/**
		 * This class can not be instantiated elsewhere.
		 */
		private ThreadToken(long threadIndex) {
			this._ThreadIndex = threadIndex;
		}

		/**
		 * Get the unique-within-this-session ThreadIndex value.
		 */
		private long getThreadIndex() {
			return this._ThreadIndex;
		}

		/**
		 * Register the current thread so that we can detect when it no longer exists,
		 * and return its unique ThreadIndex.
		 * 
		 * @return The unique ThreadIndex value assigned to the current thread.
		 */
		public static long getCurrentThreadIndex() {
			long index;

			ThreadToken curToken = tThreadToken.get();

			if (curToken == null) {
				index = latestThreadIndex.incrementAndGet();
				curToken = new ThreadToken(index);
				synchronized (mapLock) {
					threadTokenMap.put(index, new WeakReference(curToken));
				}
				tThreadToken.set(curToken);
			} else {
				// This shouldn't normally happen, but if they call us again from the same
				// thread....
				index = tThreadToken.get().getThreadIndex();
			}

			return index;
		}

		/**
		 * Determine whether an identifed thread likely still exists or definitely no
		 * longer exists in this process.
		 * 
		 * @param threadIndex The unique ThreadIndex value which the Agent assigned to
		 *                    the thread in question.
		 * @return Reports true if the managed thread which was assigned the specified
		 *         threadIndex still exists or has not yet garbage collected its
		 *         [ThreadStatic] variables. Reports false after garbage collection.
		 */
		public static boolean isThreadStillAlive(long threadIndex) {
			WeakReference reference = null;
			boolean alive = false;
			synchronized (mapLock) {
				if (threadTokenMap.containsKey(threadIndex)) {
					alive = true;
					reference = threadTokenMap.get(threadIndex);
				}
			}
			if (alive) {
				// TODO RKELLIHER review isenqueued vs isalive
				alive = (reference != null && reference.isEnqueued());
			}

			return alive;
		}

	}
}