package com.onloupe.core.messaging;


/**
 * The Class PacketEventArgs.
 */
public class PacketEventArgs {
	
	/** The packet. */
	private IMessengerPacket packet;

	/**
	 * Instantiates a new packet event args.
	 *
	 * @param packet the packet
	 */
	public PacketEventArgs(IMessengerPacket packet) {
		this.packet = packet;
	}

	/**
	 * Gets the packet.
	 *
	 * @return the packet
	 */
	public final IMessengerPacket getPacket() {
		return this.packet;
	}
}