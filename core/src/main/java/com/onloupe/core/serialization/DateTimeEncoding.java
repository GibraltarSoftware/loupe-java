package com.onloupe.core.serialization;

// TODO: Auto-generated Javadoc
/**
 * This is the set of encoding options for DateTime compression by
 * FieldReader/FieldWriter (for FieldType.DateTime and elements of
 * FieldType.DateTimeArray).
 * 
 * <p>
 * Once these options are set, they should not be changed. Extending with
 * additional options should not be done without serious thought as to handling
 * compatibility with prior versions of FieldReader! Enum values should never
 * exceed 63 (encodes as single byte) without a a good reason.
 * </p>
 * <p>
 * After the first four special cases, they must be added as Later/Earlier pairs
 * (indicates sign bit for direction of offset so that unsigned encoding can be
 * used for longer range before needing another byte to represent the value).
 * These encoding options take advantage of the opportunity to evenly divide the
 * .NET Ticks by a larger clock resolution (eg. divide by 160,000 for 16ms
 * resolution) used on typical platforms, to encode a smaller value and thus
 * save bytes.
 * </p>
 * <p>
 * The generic factor support is provided to allow for cases not anticipated at
 * rollout, since new enum options can not be added without breaking older code.
 * Both SetFactor and SetReference cases expect another DateTime encoding to
 * follow the value of Factor or Reference given (which could include the other
 * Set... case and yet another DateTime encoding after that).
 * </p>
 * 
 */
public enum DateTimeEncoding {
	
	/** The raw ticks. */
	RAW_TICKS(0), 
 /** The new reference. */
 // Timestamp given by absolute Ticks (.NET, which are 100-nanosecond clicks)
	NEW_REFERENCE(1), // Set ReferenceTime to this timestamp, by absolute Ticks (.NET)

	// Read as if the names are in reverse... eg. as how many "16-ms ticks later"
	/** The later ticks net. */
 // than ReferenceTime
	LATER_TICKS_NET(4), 
 /** The earlier ticks net. */
 // Timestamp is this many .NET Ticks later than ReferenceTime
	EARLIER_TICKS_NET(5); // ...earlier than ReferenceTime (equivalent to factor=1)

	/** The Constant SIZE. */
 public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, DateTimeEncoding> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, DateTimeEncoding> getMappings() {
		if (mappings == null) {
			synchronized (DateTimeEncoding.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, DateTimeEncoding>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new date time encoding.
	 *
	 * @param value the value
	 */
	private DateTimeEncoding(int value) {
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
	 * @return the date time encoding
	 */
	public static DateTimeEncoding forValue(int value) {
		return getMappings().get(value);
	}
}