package com.onloupe.core.server.data;


//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by a tool.
//     Runtime Version:4.0.30319.42000
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

// 
// This source code was auto-generated by xsd, Version=4.0.30319.33440.
// 

/**
 * The Class SessionFileXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class SessionFileXml: object, System.ComponentModel.INotifyPropertyChanged
public class SessionFileXml {

	/** The id field. */
	private String idField;

	/** The sequence field. */
	private long sequenceField;

	/** The version field. */
	private long versionField;

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string id
	public final String getid() {
		return this.idField;
	}

	/**
	 * Sets the id.
	 *
	 * @param value the new id
	 */
	public final void setid(String value) {
		this.idField = value;
	}

	/**
	 * Gets the sequence.
	 *
	 * @return the sequence
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public long sequence
	public final long getsequence() {
		return this.sequenceField;
	}

	/**
	 * Sets the sequence.
	 *
	 * @param value the new sequence
	 */
	public final void setsequence(long value) {
		this.sequenceField = value;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public long version
	public final long getversion() {
		return this.versionField;
	}

	/**
	 * Sets the version.
	 *
	 * @param value the new version
	 */
	public final void setversion(long value) {
		this.versionField = value;
	}

}