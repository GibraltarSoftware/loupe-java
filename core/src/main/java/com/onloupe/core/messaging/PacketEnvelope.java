package com.onloupe.core.messaging;

// TODO: Auto-generated Javadoc
/**
 * Wraps a Gibraltar Packet for publishing
 * 
 * For thread safety, request a lock on this object directly. This is necessary
 * when accessing updateable properties.
 */
public class PacketEnvelope {
	
	/** The packet. */
	private IMessengerPacket packet;
	
	/** The command. */
	private boolean command;
	
	/** The header. */
	private boolean header;
	
	/** The write through. */
	private boolean writeThrough;
	
	/** The committed. */
	private boolean committed;
	
	/** The pending. */
	private boolean pending;

	// public event EventHandler PacketCommitted;

	/**
	 * Instantiates a new packet envelope.
	 *
	 * @param packet the packet
	 * @param writeThrough the write through
	 */
	public PacketEnvelope(IMessengerPacket packet, boolean writeThrough) {
		this.packet = packet;
		this.writeThrough = writeThrough;

		if (packet instanceof CommandPacket) {
			this.command = true;
		} else {
			this.command = false;
		}

		ICachedMessengerPacket cachedPacket = packet instanceof ICachedMessengerPacket ? (ICachedMessengerPacket) packet
				: null;
		if (cachedPacket != null) {
			this.header = cachedPacket.isHeader();
		}
	}

	/**
	 * True if the packet is a command packet, false otherwise.
	 *
	 * @return true, if is command
	 */
	public final boolean isCommand() {
		return this.command;
	}

	/**
	 * True if the packet is a header cached packet, false otherwise.
	 *
	 * @return true, if is header
	 */
	public final boolean isHeader() {
		return this.header;
	}

	/**
	 * True if the packet has been commited, false otherwise
	 * 
	 * This property is thread safe and will pulse waiting threads when it is set to
	 * true. This property functions as a latch and can't be set false once it has
	 * been set to true.
	 *
	 * @return true, if is committed
	 */
	public final boolean isCommitted() {
		return this.committed;
	}

	/**
	 * Sets the checks if is committed.
	 *
	 * @param value the new checks if is committed
	 */
	public final void setIsCommitted(boolean value) {
		synchronized (this) {
			// we can't set committed to false, only true.
			if ((value) && !this.committed) {
				this.committed = true;
			}

			this.notifyAll();
			;
		}
	}

	/**
	 * True if the packet is pending submission to the queue, false otherwise
	 * 
	 * This property is thread safe and will pulse waiting threads when changed.
	 *
	 * @return true, if is pending
	 */
	public final boolean isPending() {
		return this.pending;
	}

	/**
	 * Sets the checks if is pending.
	 *
	 * @param value the new checks if is pending
	 */
	public final void setIsPending(boolean value) {
		synchronized (this) {
			// are they changing the value?
			if (value != this.pending) {
				this.pending = value;
			}

			this.notifyAll();
			;
		}
	}

	/**
	 * The actual Gibraltar Packet.
	 *
	 * @return the packet
	 */
	public final IMessengerPacket getPacket() {
		return this.packet;
	}

	/**
	 * True if the client is waiting for the packet to be written before returning.
	 *
	 * @return the write through
	 */
	public final boolean getWriteThrough() {
		return this.writeThrough;
	}

}