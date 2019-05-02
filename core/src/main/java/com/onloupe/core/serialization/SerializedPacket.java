package com.onloupe.core.serialization;

import java.util.HashMap;
import java.util.Map;

import com.onloupe.core.util.TypeUtils;

public class SerializedPacket {
	private PacketDefinition definition;
	private SerializedPacket parentPacket;
	private Map<Integer, Object> values = new HashMap<Integer, Object>();
	private boolean readOnly;

	/**
	 * Create a new serialized packet for serialization
	 * 
	 * @param definition
	 */
	public SerializedPacket(PacketDefinition definition) {
		this.definition = definition;
		this.readOnly = false;
	}

	/**
	 * Create a new serailized packet for deserialization
	 * 
	 * @param definition
	 * @param values
	 */
	public SerializedPacket(PacketDefinition definition, Map<Integer, Object> values, SerializedPacket parentPacket) {
		this.definition = definition;
		this.parentPacket = parentPacket;
		this.readOnly = true;
		this.values = values;
	}

	/**
	 * The serialized packet for our definition's super type definition. (may be null)
	 * @return
	 */
	public SerializedPacket getParentPacket() {
		//quick exit for best perf
		if (parentPacket != null) return parentPacket;

		//if we don't have a base packet - should we?
		if (!readOnly) {
			PacketDefinition baseDefinition = definition.getParentPacket();
			if (baseDefinition != null) {
				parentPacket = new SerializedPacket(baseDefinition);
			}
		}

		return parentPacket;
	}

	/**
	 * The serialized packet for our definition's parent (super type) IPacket definition (may be null)
	 * @return
	 */
	public SerializedPacket getParentIPacket() {
		SerializedPacket parentIPacket = getParentPacket();;
		while (parentIPacket != null && !parentIPacket.getPacketDefinition().getImplementsIPacket()) {
			parentIPacket = parentIPacket.getParentPacket();
		}

		return parentIPacket;
	}

	public PacketDefinition getPacketDefinition() { return definition; }

	@SuppressWarnings("unchecked")
	public final <T> T getField(String fieldName, Class<T> clazz) {
		return (T) getFieldValue(fieldName);
	}

	@SuppressWarnings("unchecked")
	public final <T> T getField(int fieldIndex, Class<T> clazz) {
		return (T) this.values.get(fieldIndex);
	}

	public final void setField(String fieldName, Object value) {
		if (TypeUtils.isBlank(fieldName)) {
			throw new NullPointerException("fieldName");
		}

		if (this.readOnly) {
			throw new IllegalStateException("The packet has been deserialized and is read-only");
		}

		// find the field index for the field name
		FieldDefinition fieldDefinition = this.definition.getFields().get(fieldName);
		if (fieldDefinition == null) {
			throw new IllegalArgumentException("There is no field in the definition named " + fieldName);
		}

		int fieldIndex = this.definition.getFields().indexOf(fieldDefinition);

		// now we need to check the value for sanity.
		if (value != null) {
			FieldType valueType = PacketDefinition.getSerializableType(value.getClass());
			if (!fieldDefinition.isCompatible(valueType)) {
				throw new IllegalArgumentException(
						"The provided value's type doesn't match the definition for the field.");
			}
		}

		// now that we know we're of the right type, store it away.
		this.values.put(fieldIndex, value);
	}

	public final Map<Integer, Object> getValues() {
		return this.values;
	}

	public final Object getFieldValue(String fieldName) {
		if (TypeUtils.isBlank(fieldName)) {
			throw new NullPointerException("fieldName");
		}

		// find the field index from the field name.
		int fieldIndex = this.definition.getFields().indexOf(fieldName);

		if (fieldIndex < 0)
			throw new IndexOutOfBoundsException("There is no field named " + fieldName);

		return this.values.get(fieldIndex);
	}
}