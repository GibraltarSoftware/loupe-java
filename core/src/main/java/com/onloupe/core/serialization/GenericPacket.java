package com.onloupe.core.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the class returned by PacketReader when an unknown packet type is
 * read from the input stream. This class is designed to allow the underlying
 * data to be serialized back out just as it was read. This handles the use case
 * of an old
 * 
 */
public final class GenericPacket implements IPacket {
	
	/** The definition. */
	private PacketDefinition definition;
	
	/** The field values. */
	private Object[] fieldValues;
	
	/** The base packet. */
	private GenericPacket basePacket;

	/**
	 * Read any packet based solely on its PacketDefinition.
	 *
	 * @param definition PacketDefinition describing the next packet in the stream
	 * @param reader     Data stream to be read
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public GenericPacket(PacketDefinition definition, IFieldReader reader) throws IOException {
		if (definition.getParentPacket() != null) {
			this.basePacket = new GenericPacket(definition.getParentPacket(), reader);
		}

		this.definition = definition;
		this.fieldValues = new Object[definition.getFields().size()];

		for (int index = 0; index < definition.getFields().size(); index++) {
			switch (definition.getFields().get(index).getFieldType()) {
			case BOOL:
				this.fieldValues[index] = reader.readBool();
				break;
			case STRING:
				this.fieldValues[index] = reader.readString();
				break;
			case STRING_ARRAY:
				this.fieldValues[index] = reader.readStringArray();
				break;
			case INT:
				this.fieldValues[index] = reader.readInt();
				break;
			case LONG:
				this.fieldValues[index] = reader.readLong();
				break;
			case DOUBLE:
				this.fieldValues[index] = reader.readDouble();
				break;
			case DURATION:
				this.fieldValues[index] = reader.readDuration();
				break;
			case DATE_TIME:
				this.fieldValues[index] = reader.readDateTime();
				break;
			case GUID:
				this.fieldValues[index] = reader.readGuid();
				break;
			case DATE_TIME_OFFSET:
				this.fieldValues[index] = reader.readDateTimeOffset();
				break;
			default:
				throw new IllegalStateException(
						String.format("The field type %1$s is unknown so we can't deserialize the packet ",
								definition.getFields().get(index).getFieldType()));
			}
		}
	}

	/**
	 * Gets the packet definition.
	 *
	 * @return the packet definition
	 */
	public PacketDefinition getPacketDefinition() { return this.definition; }

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public int getVersion() {
		return this.definition.getVersion();
	}

	/**
	 * Gets the field count.
	 *
	 * @return the field count
	 */
	public int getFieldCount() {
		return this.definition.getFields().size();
	}

	/**
	 * Index of.
	 *
	 * @param fieldName the field name
	 * @return the int
	 */
	public int indexOf(String fieldName) {
		return this.definition.getFields().indexOf(fieldName);
	}

	/**
	 * Gets the field name.
	 *
	 * @param index the index
	 * @return the field name
	 */
	public String getFieldName(int index) {
		return this.definition.getFields().get(index).getName();
	}

	/**
	 * Gets the field type.
	 *
	 * @param index the index
	 * @return the field type
	 */
	public FieldType getFieldType(int index) {
		return this.definition.getFields().get(index).getFieldType();
	}

	/**
	 * Gets the field value.
	 *
	 * @param index the index
	 * @return the field value
	 */
	public Object getFieldValue(int index) {
		return this.fieldValues[index];
	}

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public List<IPacket> getRequiredPackets() {
		// we're a base packet and depend on nothing.
		return new ArrayList<>();
	}

	/**
	 * The key idea of a GenericPacket is that it allows an unknown packet type to
	 * be read and rewritten such that it can subsequently be read properly when the
	 * appropriate IPacketFactory is registered.
	 *
	 * @param definition the definition
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		throw new UnsupportedOperationException("writing generic packet definitions is not supported at this time.");
	}

	/**
	 * Write out all of the fields for the current packet.
	 *
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to populate with data
	 */
	@Override
	public void writeFields(PacketDefinition definition, SerializedPacket packet) {
		if (this.basePacket != null) {
			((IPacket) this.basePacket).writeFields(definition, packet);
		}

		for (int index = 0; index < this.definition.getFields().size(); index++) {
			FieldDefinition fieldDefinition = this.definition.getFields().get(index);
			switch (fieldDefinition.getFieldType()) {
			case BOOL:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case STRING:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case STRING_ARRAY:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case INT:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case LONG:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case DOUBLE:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case DURATION:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case DATE_TIME:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case GUID:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			case DATE_TIME_OFFSET:
				packet.setField(fieldDefinition.getName(), this.fieldValues[index]);
				break;
			default:
				throw new IllegalStateException(
						String.format("The field type %1$s is unknown so we can't serialize the packet ",
								definition.getFields().get(index).getFieldType()));
			}
		}
	}

	/**
	 * Read back the field values for the current packet.
	 * 
	 * @param definition The definition that was used to persist the packet.
	 * @param packet     The serialized packet to read data from
	 */
	@Override
	public void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException();
	}
}