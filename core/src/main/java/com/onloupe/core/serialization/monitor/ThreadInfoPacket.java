package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.model.IThreadInfo;

import java.util.List;


/**
 * The Class ThreadInfoPacket.
 */
public class ThreadInfoPacket extends GibraltarCachedPacket implements IPacket, IThreadInfo {
	
	/** The thread index. */
	private long threadIndex;
	
	/** The thread id. */
	private long threadId;
	
	/** The thread name. */
	private String threadName;
	
	/** The domain id. */
	private int domainId;
	
	/** The domain name. */
	private String domainName;
	
	/** The thread pool thread. */
	private boolean threadPoolThread;

	/**
	 * Instantiates a new thread info packet.
	 */
	public ThreadInfoPacket() {
		super(false);
	}

	/**
	 * Instantiates a new thread info packet.
	 *
	 * @param threadIndex the thread index
	 * @param threadId the thread id
	 * @param threadName the thread name
	 * @param domainId the domain id
	 * @param domainName the domain name
	 * @param threadPoolThread the thread pool thread
	 */
	public ThreadInfoPacket(long threadIndex, long threadId, String threadName,
			int domainId, String domainName, boolean threadPoolThread) {
		super(false);
		this.threadIndex = threadIndex;
		this.threadId = threadId;
		this.threadName = threadName;
		this.domainId = domainId;
		this.domainName = domainName;
		this.threadPoolThread = threadPoolThread;
	}



	/**
	 * Gets the thread index.
	 *
	 * @return the thread index
	 */
	public final long getThreadIndex() {
		return this.threadIndex;
	}

	/**
	 * Sets the thread index.
	 *
	 * @param value the new thread index
	 */
	public final void setThreadIndex(long value) {
		this.threadIndex = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.IThreadInfo#getThreadId()
	 */
	@Override
	public final long getThreadId() {
		return this.threadId;
	}

	/**
	 * Sets the thread id.
	 *
	 * @param value the new thread id
	 */
	public final void setThreadId(long value) {
		this.threadId = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.IThreadInfo#getThreadName()
	 */
	@Override
	public final String getThreadName() {
		return this.threadName;
	}

	/**
	 * Sets the thread name.
	 *
	 * @param value the new thread name
	 */
	public final void setThreadName(String value) {
		this.threadName = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.IThreadInfo#getDomainId()
	 */
	@Override
	public final int getDomainId() {
		return this.domainId;
	}

	/**
	 * Sets the domain id.
	 *
	 * @param value the new domain id
	 */
	public final void setDomainId(int value) {
		this.domainId = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.IThreadInfo#getDomainName()
	 */
	@Override
	public final String getDomainName() {
		return this.domainName;
	}

	/**
	 * Sets the domain name.
	 *
	 * @param value the new domain name
	 */
	public final void setDomainName(String value) {
		this.domainName = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.IThreadInfo#isThreadPoolThread()
	 */
	@Override
	public final boolean isThreadPoolThread() {
		return this.threadPoolThread;
	}

	/**
	 * Sets the checks if is thread pool thread.
	 *
	 * @param value the new checks if is thread pool thread
	 */
	public final void setIsThreadPoolThread(boolean value) {
		this.threadPoolThread = value;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof ThreadInfoPacket ? (ThreadInfoPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the 
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(ThreadInfoPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getThreadIndex() == other.getThreadIndex()) && (getThreadId() == other.getThreadId())
				&& (getThreadName().equals(other.getThreadName())) && (getDomainId() == other.getDomainId())
				&& (getDomainName().equals(other.getDomainName()))
				&& (isThreadPoolThread() == other.isThreadPoolThread()) && super.equals(other));
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
	 * @return an int representing the hash code calculated for the contents of this
	 *         object
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = super.hashCode(); // Fold in hash code for inherited base type

		myHash ^= this.threadId ^ this.domainId; // Fold in ThreadId and DomainId as hash code for themselves
		myHash ^= this.threadIndex << 16; // Fold in the ThreadIndex (in a different position).
		if (this.threadName != null) {
			myHash ^= this.threadName.hashCode(); // Fold in hash code for string ThreadName
		}
		if (this.domainName != null) {
			myHash ^= this.domainName.hashCode(); // Fold in hash code for string DomainName
		}

		// Not bothering with bool members

		return myHash;
	}

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 1;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("ThreadIndex", FieldType.INT);
		definition.getFields().add("ThreadId", FieldType.INT);
		definition.getFields().add("ThreadName", FieldType.STRING);
		definition.getFields().add("DomainId", FieldType.INT);
		definition.getFields().add("DomainName", FieldType.STRING);
		definition.getFields().add("IsBackground", FieldType.BOOL);
		definition.getFields().add("IsThreadPoolThread", FieldType.BOOL);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("ThreadIndex", Math.toIntExact(this.threadIndex));
		packet.setField("ThreadId", Math.toIntExact(this.threadId));
		packet.setField("ThreadName", this.threadName);
		packet.setField("DomainId", this.domainId);
		packet.setField("DomainName", this.domainName);
		packet.setField("IsBackground", true);
		packet.setField("IsThreadPoolThread", this.threadPoolThread);
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.IThreadInfo#isBackground()
	 */
	@Override
	public boolean isBackground() {
		//there is no equivalent for this in Java.
		return false;
	}
}