package com.onloupe.core.monitor;

// TODO: Auto-generated Javadoc
/**
 * The different possible actions that were performed on a collection.
 */
public enum CollectionAction {
	/**
	 * No changes were made.
	 */
	NO_CHANGE(0),

	/**
	 * An item was added to the collection.
	 */
	ADDED(1),

	/**
	 * An item was removed from the collection.
	 */
	REMOVED(2),

	/**
	 * An item was updated in the collection.
	 */
	UPDATED(3),

	/**
	 * The entire collection was cleared.
	 */
	CLEARED(4);

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/** The int value. */
	private int intValue;
	
	/** The mappings. */
	private static java.util.HashMap<Integer, CollectionAction> mappings;

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	private static java.util.HashMap<Integer, CollectionAction> getMappings() {
		if (mappings == null) {
			synchronized (CollectionAction.class) {
				if (mappings == null) {
					mappings = new java.util.HashMap<Integer, CollectionAction>();
				}
			}
		}
		return mappings;
	}

	/**
	 * Instantiates a new collection action.
	 *
	 * @param value the value
	 */
	private CollectionAction(int value) {
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
	 * @return the collection action
	 */
	public static CollectionAction forValue(int value) {
		return getMappings().get(value);
	}
}