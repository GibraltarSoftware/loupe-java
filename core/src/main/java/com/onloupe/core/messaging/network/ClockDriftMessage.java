package com.onloupe.core.messaging.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.onloupe.core.data.BinarySerializer;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.model.system.Version;


/**
 * Used to determine the latency and relative clock drift of a connection.
 */
public class ClockDriftMessage extends NetworkMessage {
	
	/** The id. */
	private UUID id;
	
	/** The originator timestamp. */
	private Optional<OffsetDateTime> originatorTimestamp = Optional.empty();
	
	/** The destination timestamp. */
	private Optional<OffsetDateTime> destinationTimestamp = Optional.empty();
	
	/** The deserialization timestamp. */
	private OffsetDateTime deserializationTimestamp;

	/** The clock drift. */
	private Duration clockDrift;
	
	/** The latency. */
	private Duration latency;

	/**
	 * Instantiates a new clock drift message.
	 */
	public ClockDriftMessage() {
		setTypeCode(NetworkMessageTypeCode.CLOCK_DRIFT);
		setVersion(new Version(1, 0));
	}

	/**
	 * Create a new clock drift message for the specified agent.
	 *
	 * @param id the id
	 */
	public ClockDriftMessage(UUID id) {
		this();
		this.id = id;
	}

	/**
	 * The session Id of the endpoint we're identifying clock drift for.
	 *
	 * @return the id
	 */
	public final UUID getId() {
		return this.id;
	}

	/**
	 * Sets the id.
	 *
	 * @param value the new id
	 */
	public final void setId(UUID value) {
		this.id = value;
	}

	/**
	 * The timestamp the original request was created on the source end.
	 *
	 * @return the originator timestamp
	 */
	public final Optional<OffsetDateTime> getOriginatorTimestamp() {
		return this.originatorTimestamp;
	}

	/**
	 * Sets the originator timestamp.
	 *
	 * @param value the new originator timestamp
	 */
	public final void setOriginatorTimestamp(Optional<OffsetDateTime> value) {
		this.originatorTimestamp = value;
	}

	/**
	 * The timestamp of the destination when it received the message.
	 *
	 * @return the destination timestamp
	 */
	public final Optional<OffsetDateTime> getDestinationTimestamp() {
		return this.destinationTimestamp;
	}

	/**
	 * Sets the destination timestamp.
	 *
	 * @param value the new destination timestamp
	 */
	public final void setDestinationTimestamp(Optional<OffsetDateTime> value) {
		this.destinationTimestamp = value;
	}

	/**
	 * The clock drift between the agent and the server, discounting latency.
	 *
	 * @return the clock drift
	 */
	public final Duration getClockDrift() {
		return this.clockDrift;
	}

	/**
	 * The estimated latency in the connection (used to calculate true clock drift).
	 *
	 * @return the latency
	 */
	public final Duration getLatency() {
		return this.latency;
	}

	/**
	 * Locks in the latency and drift calculations when called by the originator
	 * after a round trip.
	 */
	public final void calculateValues() {
		long latencyTicks = TimeConversion
				.durationInTicks(Duration.between(this.originatorTimestamp.get(), this.deserializationTimestamp));
		if (latencyTicks < 0) {
			latencyTicks = 0;
		}

		this.latency = TimeConversion.durationOfTicks(latencyTicks / 2);

		if (this.destinationTimestamp.isPresent()) {
			this.clockDrift = Duration.between(this.destinationTimestamp.get(), this.originatorTimestamp.get())
					.minus(this.latency).minus(this.latency); // account for coming & going latency
		}
	}

	/**
	 * Write the packet to the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onWrite(OutputStream stream) throws IOException {
		if (!getOriginatorTimestamp().isPresent()) {
			setOriginatorTimestamp(Optional.ofNullable(OffsetDateTime.now()));
		}

		stream.write(BinarySerializer.serializeValue(getId()));
		stream.write(BinarySerializer.serializeValue(getOriginatorTimestamp().get()));
		OffsetDateTime tempVar = getDestinationTimestamp().get();
		stream.write(BinarySerializer.serializeValue((tempVar != null) ? tempVar : TimeConversion.MIN));
	}

	/**
	 * Read packet data from the stream.
	 *
	 * @param stream the stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void onRead(InputStream stream) throws IOException {
		this.deserializationTimestamp = OffsetDateTime.now();

		this.id = BinarySerializer.deserializeUUIDValue(stream);

		OffsetDateTime originator = BinarySerializer.deserializeOffsetDateTimeValue(stream);
		if (originator != TimeConversion.MIN) {
			this.originatorTimestamp = Optional.of(originator);
		}

		OffsetDateTime destination = BinarySerializer.deserializeOffsetDateTimeValue(stream);
		if (destination != TimeConversion.MIN) {
			this.destinationTimestamp = Optional.of(destination);
		}

	}

}