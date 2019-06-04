package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
/**
 * The Enum SessionStatusXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum SessionStatusXml
public enum SessionStatusXml {

	/** The unknown. */
	UNKNOWN,

	/** The running. */
	RUNNING,

	/** The normal. */
	NORMAL,

	/** The crashed. */
	CRASHED;

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
	 * @return the session status xml
	 */
	public static SessionStatusXml forValue(int value) {
		return values()[value];
	}
}