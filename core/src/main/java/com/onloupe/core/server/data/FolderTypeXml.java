package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
/**
 * The Enum FolderTypeXml.
 */

/** 
*/

/**
 * 
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum FolderTypeXml
public enum FolderTypeXml {

	/** The manual. */
	MANUAL,

	/** The search. */
	SEARCH,

	/** The dynamic. */
	DYNAMIC;

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
	 * @return the folder type xml
	 */
	public static FolderTypeXml forValue(int value) {
		return values()[value];
	}
}