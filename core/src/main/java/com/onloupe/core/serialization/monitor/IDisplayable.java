package com.onloupe.core.serialization.monitor;

// TODO: Auto-generated Javadoc
/**
 * A standard interface for ensuring an item can be displayed in user interfaces
 * by providing an end user short caption and long description
 * 
 * Captions should be as short as feasible, typically less than 80 characters.
 * Descriptions can be considerably longer, but neither should have embedded
 * formatting outside of normal carriage return and line feed.
 */
public interface IDisplayable {
	
	/**
	 * A short end-user display caption.
	 *
	 * @return the caption
	 */
	String getCaption();

	/**
	 * An extended description without formatting.
	 *
	 * @return the description
	 */
	String getDescription();
}