package com.onloupe.core.messaging;

public class PacketEventArgs {
	private IMessengerPacket packet;

	public PacketEventArgs(IMessengerPacket packet) {
		this.packet = packet;
	}

	public final IMessengerPacket getPacket() {
		return this.packet;
	}
}