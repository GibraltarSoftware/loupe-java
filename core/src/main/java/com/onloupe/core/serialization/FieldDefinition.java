package com.onloupe.core.serialization;

/**
 * FieldDefinition is only used internally by PacketDefinition to hold the name
 * and type of a field
 */
public class FieldDefinition {
	private FieldType fieldType;
	private String fieldName;

	/**
	 * Create a new field definition.
	 * 
	 * @param fieldName
	 * @param fieldType
	 */
	public FieldDefinition(String fieldName, FieldType fieldType) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}

	/**
	 * The exact serializable field type of the field
	 */
	public final FieldType getFieldType() {
		return this.fieldType;
	}

	/**
	 * The unique name of this field within the packet
	 */
	public final String getName() {
		return this.fieldName;
	}

	/**
	 * Indicates if this field definition can store data of the provided type
	 * losslessly.
	 * 
	 * @param type The prospective value type to be serialized
	 * @return True if the provided type can be converted into this field type
	 *         without losing precision. This method will indicate if a provided
	 *         value type is sufficiently compatible with the exact type of this
	 *         field to be converted without losing data. For example, a signed
	 *         integer can be stored in an unsigned integer field. A short can be
	 *         stored as a long, etc.
	 */
	public final boolean isCompatible(FieldType type) {
		// exact matches are always good.
		if (type == this.fieldType) {
			return true;
		}

		// now handle odd overrides.
		switch (type) {
		case INT:
			return ((this.fieldType == FieldType.LONG) || (this.fieldType == FieldType.DOUBLE));
		case DATE_TIME_OFFSET:
			return (this.fieldType == FieldType.DATE_TIME);
		default:
			break;
		}

		// if it isn't one of our specific overrides, no dice
		return false;
	}
}