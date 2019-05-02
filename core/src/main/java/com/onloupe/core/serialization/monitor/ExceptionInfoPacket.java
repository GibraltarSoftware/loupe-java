package com.onloupe.core.serialization.monitor;

import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.IExceptionInfo;

import java.util.List;

public class ExceptionInfoPacket extends GibraltarPacket implements IPacket, IExceptionInfo {
	private String typeName;
	private String message;
	private String source;
	private String stackTrace;
	private IExceptionInfo innerException; // not serialized as such.

	public ExceptionInfoPacket() {
	}

	public ExceptionInfoPacket(Throwable throwable) {
		this.typeName = throwable.getClass().getName();
		this.message = throwable.getMessage();
		this.source = TypeUtils.getRootCauseMessage(throwable);
		this.stackTrace = TypeUtils.getStackTrace(throwable);
	}

	@Override
	public final String getTypeName() {
		return this.typeName;
	}

	public final void setTypeName(String value) {
		this.typeName = value;
	}

	@Override
	public final String getMessage() {
		return this.message;
	}

	public final void setMessage(String value) {
		this.message = value;
	}

	@Override
	public final String getSource() {
		return this.source;
	}

	public final void setSource(String value) {
		this.source = value;
	}

	@Override
	public final String getStackTrace() {
		return this.stackTrace;
	}

	public final void setStackTrace(String value) {
		this.stackTrace = value;
	}

	@Override
	public final IExceptionInfo getInnerException() {
		return this.innerException;
	}

	public final void setInnerException(IExceptionInfo value) {
		this.innerException = value;
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof ExceptionInfoPacket ? (ExceptionInfoPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
	 */
	public final boolean equals(ExceptionInfoPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		return ((getTypeName().equals(other.getTypeName())) && (getMessage().equals(other.getMessage()))
				&& (getSource().equals(other.getSource())) && (getStackTrace().equals(other.getStackTrace()))
				&& super.equals(other));
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

		if (this.typeName != null) {
			myHash ^= this.typeName.hashCode(); // Fold in hash code for string TypeName
		}
		if (this.message != null) {
			myHash ^= this.message.hashCode(); // Fold in hash code for string Message
		}
		if (this.source != null) {
			myHash ^= this.source.hashCode(); // Fold in hash code for string Source
		}
		if (this.stackTrace != null) {
			myHash ^= this.stackTrace.hashCode(); // Fold in hash code for string StackTrace
		}

		return myHash;
	}

	private static final int SERIALIZATION_VERSION = 1;

	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		definition.getFields().add("TypeName", FieldType.STRING);
		definition.getFields().add("Message", FieldType.STRING);
		definition.getFields().add("Source", FieldType.STRING);
		definition.getFields().add("StackTrace", FieldType.STRING);
	}

	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		packet.setField("TypeName", getTypeName());
		packet.setField("Message", getMessage());
		packet.setField("Source", getSource());
		packet.setField("StackTrace", getStackTrace());
	}

	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	@Override
	public final List<IPacket> getRequiredPackets() {
		// we depend on nothing
		return super.getRequiredPackets();
	}
}