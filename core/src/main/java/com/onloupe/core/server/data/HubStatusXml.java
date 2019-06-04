package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
/**
 * The Enum HubStatusXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum HubStatusXml
public enum HubStatusXml {

	/** The available. */
	AVAILABLE,

	/** The maintenance. */
	MAINTENANCE,

	/** The expired. */
	EXPIRED;

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
	 * @return the hub status xml
	 */
	public static HubStatusXml forValue(int value) {
		return values()[value];
	}
}