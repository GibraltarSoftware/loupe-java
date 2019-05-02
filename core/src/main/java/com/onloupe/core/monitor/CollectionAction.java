package com.onloupe.core.monitor;

/**
 * The different possible actions that were performed on a collection
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

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, CollectionAction> mappings;

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

	private CollectionAction(int value) {
		this.intValue = value;
		getMappings().put(value, this);
	}

	public int getValue() {
		return this.intValue;
	}

	public static CollectionAction forValue(int value) {
		return getMappings().get(value);
	}
}