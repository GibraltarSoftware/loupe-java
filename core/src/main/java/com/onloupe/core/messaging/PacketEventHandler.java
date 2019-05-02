package com.onloupe.core.messaging;

@FunctionalInterface
public interface PacketEventHandler {
	void invoke(Object sender, PacketEventArgs e);
}