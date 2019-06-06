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
	 * @param command the command
	 */
	public CommandPacket(MessagingCommand command) {
		setCommand(command);
		setState(null);
	}

	/**
	 * Create a new command packet for the provided command, with state.
	 *
	 * @param command the command
	 * @param state the state
	 */
	public CommandPacket(MessagingCommand command, Object state) {
		setCommand(command);
		setState(state);
	}

	/** The command to execute. */
	private MessagingCommand command = MessagingCommand.values()[0];

	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public final MessagingCommand getCommand() {
		return this.command;
	}

	/**
	 * Sets the command.
	 *
	 * @param value the new command
	 */
	private void setCommand(MessagingCommand value) {
		this.command = value;
	}

	/**
	 * Optional. State arguments for the command
	 */
	private Object state;

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public final Object getState() {
		return this.state;
	}

	/**
	 * Sets the state.
	 *
	 * @param value the new state
	 */
	private void setState(Object value) {
		this.state = value;
	}

	/** The timestamp. */
	private OffsetDateTime timestamp;

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#getTimestamp()
	 */
	@Override
	public final OffsetDateTime getTimestamp() {
		return this.timestamp;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#setTimestamp(java.time.OffsetDateTime)
	 */
	@Override
	public final void setTimestamp(OffsetDateTime value) {
		this.timestamp = value;
	}

	/** The sequence. */
	private long sequence;

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#getSequence()
	 */
	@Override
	public final long getSequence() {
		return this.sequence;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.messaging.IMessengerPacket#setSequence(long)
	 */
	@Override
	public final void setSequence(long value) {
		this.sequence = value;
	}

	// While we have to implement all of this serialization stuff, command packets
	// should never
	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#getRequiredPackets()
	 */
	// actually be serialized, so they are all about throwing exceptions.
	@Override
	public final List<IPacket> getRequiredPackets() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public final void writePacketDefinition(PacketDefinition definition) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.IPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException();
	}
}