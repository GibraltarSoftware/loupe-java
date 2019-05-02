package com.onloupe.core.server.data;

/**
 * <remarks/>
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum SessionStatusXml
public enum SessionStatusXml {

	/**
	 * <remarks/>
	 */
	UNKNOWN,

	/**
	 * <remarks/>
	 */
	RUNNING,

	/**
	 * <remarks/>
	 */
	NORMAL,

	/**
	 * <remarks/>
	 */
	CRASHED;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static SessionStatusXml forValue(int value) {
		return values()[value];
	}
}