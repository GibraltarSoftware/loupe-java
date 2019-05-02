package com.onloupe.core.messaging;

import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A command to be processed by the messaging system.
 */
public class CommandPacket implements IMessengerPacket {
	/**
	 * Create a new command packet for the provided command.
	 * 
	 * @param command
	 */
	public CommandPacket(MessagingCommand command) {
		setCommand(command);
		setState(null);
	}

	/**
	 * Create a new command packet for the provided command, with state.
	 * 
	 * @param command
	 * @param state
	 */
	public CommandPacket(MessagingCommand command, Object state) {
		setCommand(command);
		setState(state);
	}

	/**
	 * The command to execute
	 */
	private MessagingCommand command = MessagingCommand.values()[0];

	public final MessagingCommand getCommand() {
		return this.command;
	}

	private void setCommand(MessagingCommand value) {
		this.command = value;
	}

	/**
	 * Optional. State arguments for the command
	 */
	private Object state;

	public final Object getState() {
		return this.state;
	}

	private void setState(Object value) {
		this.state = value;
	}

	private OffsetDateTime timestamp;

	@Override
	public final OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	@Override
	public final void setTimestamp(OffsetDateTime value) {
		this.timestamp = value;
	}

	private long sequence;

	@Override
	public final long getSequence() {
		return this.sequence;
	}

	@Override
	public final void setSequence(long value) {
		this.sequence = value;
	}

	// While we have to implement all of this serialization stuff, command packets
	// should never
	// actually be serialized, so they are all about throwing exceptions.
	@Override
	public final List<IPacket> getRequiredPackets() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void writePacketDefinition(PacketDefinition definition) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException();
	}
}