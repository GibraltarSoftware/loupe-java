package com.onloupe.core.monitor;


/**
 * The Class PropertyChangedEventArgs.
 */
public class PropertyChangedEventArgs {

	/** The property name. */
	private String propertyName;

	/**
	 * Instantiates a new property changed event args.
	 *
	 * @param propertyName the property name
	 */
	public PropertyChangedEventArgs(String propertyName) {
		super();
		this.propertyName = propertyName;
	}

	/**
	 * Gets the property name.
	 *
	 * @return the property name
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

	/**
	 * Sets the property name.
	 *
	 * @param propertyName the new property name
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

}
