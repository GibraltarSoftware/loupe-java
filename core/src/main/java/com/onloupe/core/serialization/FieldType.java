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
	
	/** The unknown. */
	UNKNOWN(0), 
 /** The bool. */
 BOOL(1), 
 /** The string. */
 STRING(3), 
 /** The string array. */
 STRING_ARRAY(4), 
 /** The int. */
 INT(5), 
 /** The long. */
 LONG(7), 
 /** The double. */
 DOUBLE(13), 
 /** The duration. */
 DURATION(15), 
 /** The date time. */
 DATE_TIME(17), 
 /** The guid. */
 GUID(19),
	
	/** The date time offset. */
	DATE_TIME_OFFSET(21);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, FieldType> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
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

	/**
	 * Instantiates a new field type.
	 *
	 * @param value the value
	 */
	private FieldType(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.intValue;
	}

	/**
	 * For value.
	 *
	 * @param value the value
	 * @return the field type
	 */
	public static FieldType forValue(int value) {
		return getMappings().get(value);
	}
}