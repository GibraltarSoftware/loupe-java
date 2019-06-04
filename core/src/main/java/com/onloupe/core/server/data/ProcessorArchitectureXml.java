package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
/**
 * The Enum ProcessorArchitectureXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum ProcessorArchitectureXml
public enum ProcessorArchitectureXml {

	/** The unknown. */
	UNKNOWN,

	/** The x86. */
	X86,

	/** The amd64. */
	AMD64,

	/** The ia64. */
	IA64;

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
	 * @return the processor architecture xml
	 */
	public static ProcessorArchitectureXml forValue(int value) {
		return values()[value];
	}
}