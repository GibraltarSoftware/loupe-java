package com.onloupe.core.server.data;

/**
 * <remarks/>
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum ProcessorArchitectureXml
public enum ProcessorArchitectureXml {

	/**
	 * <remarks/>
	 */
	UNKNOWN,

	/**
	 * <remarks/>
	 */
	X86,

	/**
	 * <remarks/>
	 */
	AMD64,

	/**
	 * <remarks/>
	 */
	IA64;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static ProcessorArchitectureXml forValue(int value) {
		return values()[value];
	}
}