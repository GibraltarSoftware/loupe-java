package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
/**
 * The Enum SessionSynchronizationModeXml.
 */

/** 
*/

/** 
*/

/** 
*/

/** 
*/

/** 
*/

/**
 * 
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum SessionSynchronizationModeXml
public enum SessionSynchronizationModeXml {

	/** The manual. */
	MANUAL,

	/** The summary only. */
	SUMMARY_ONLY,

	/** The automatic. */
	AUTOMATIC;

	/** The Constant SIZE. */
	public static final int SIZE = java.lang.Integer.SIZE;

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.ordinal();
	}

	/**
	 * For value.
	 *
	 * @param value the value
	 * @return the session synchronization mode xml
	 */
	public static SessionSynchronizationModeXml forValue(int value) {
		return values()[value];
	}
}
/**
 * 
 */

/**
 * 
 */

/**
 * 
 */
