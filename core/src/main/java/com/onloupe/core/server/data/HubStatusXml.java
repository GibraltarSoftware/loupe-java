package com.onloupe.core.server.data;

/**
 * <remarks/>
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum HubStatusXml
public enum HubStatusXml {

	/**
	 * <remarks/>
	 */
	AVAILABLE,

	/**
	 * <remarks/>
	 */
	MAINTENANCE,

	/**
	 * <remarks/>
	 */
	EXPIRED;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static HubStatusXml forValue(int value) {
		return values()[value];
	}
}