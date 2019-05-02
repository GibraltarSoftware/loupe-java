package com.onloupe.core.server.data;

/** <remarks/>
*/

/** <remarks/>
*/

/**
 * <remarks/>
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public enum FolderTypeXml
public enum FolderTypeXml {

	/**
	 * <remarks/>
	 */
	MANUAL,

	/**
	 * <remarks/>
	 */
	SEARCH,

	/**
	 * <remarks/>
	 */
	DYNAMIC;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue() {
		return this.ordinal();
	}

	public static FolderTypeXml forValue(int value) {
		return values()[value];
	}
}