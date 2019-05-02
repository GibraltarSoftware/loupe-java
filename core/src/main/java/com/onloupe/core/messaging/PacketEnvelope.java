package com.onloupe.core.messaging;

/**
 * Wraps a Gibraltar Packet for publishing
 * 
 * For thread safety, request a lock on this object directly. This is necessary
 * when accessing updateable properties.
 */
public class PacketEnvelope {
	private IMessengerPacket packet;
	private boolean command;
	private boolean header;
	private boolean writeThrough;
	private boolean committed;
	private boolean pending;

	// public event EventHandler PacketCommitted;

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
	 */
	public final boolean isCommand() {
		return this.command;
	}

	/**
	 * True if the packet is a header cached packet, false otherwise.
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
	 */
	public final boolean isCommitted() {
		return this.committed;
	}

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
	 */
	public final boolean isPending() {
		return this.pending;
	}

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
	 * The actual Gibraltar Packet
	 */
	public final IMessengerPacket getPacket() {
		return this.packet;
	}

	/**
	 * True if the client is waiting for the packet to be written before returning.
	 */
	public final boolean getWriteThrough() {
		return this.writeThrough;
	}

}