package com.onloupe.core.serialization;

/**
 * This is the set of data types that can be read/written using
 * FieldReader/FieldWriter.
 * 
 * 
 * The enum starts at 1 to allow 0 to be clearly understood as unknown (and
 * therefore fail)
 * 
 */
public enum FieldType {
	UNKNOWN(0), BOOL(1), STRING(3), STRING_ARRAY(4), INT(5), LONG(7), DOUBLE(13), DURATION(15), DATE_TIME(17), GUID(19),
	DATE_TIME_OFFSET(21);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, FieldType> mappings;

	private static java.util.HashMap<Integer, FieldType> getMappings() {
		if (mappings == null) {
			synchronized (FieldType.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, FieldType>();
				}
			}
		}
		return mappings;
	}

	private FieldType(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static FieldType forValue(int value) {
		return getMappings().get(value);
	}
}