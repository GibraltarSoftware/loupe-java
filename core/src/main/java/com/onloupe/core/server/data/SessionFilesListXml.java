package com.onloupe.core.server.data;

// TODO: Auto-generated Javadoc
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
 * The Class SessionFilesListXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")][System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd", IsNullable=false)] public partial class SessionFilesListXml: object, System.ComponentModel.INotifyPropertyChanged
public class SessionFilesListXml {

	/** The files field. */
	private SessionFileXml[] filesField;

	/** The id field. */
	private String idField;

	/** The version field. */
	private long versionField;

	/** The single stream only field. */
	private boolean singleStreamOnlyField;

	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlArrayItemAttribute("file", IsNullable=false)] public SessionFileXml[] files
	public final SessionFileXml[] getfiles() {
		return this.filesField;
	}

	/**
	 * Sets the files.
	 *
	 * @param value the new files
	 */
	public final void setfiles(SessionFileXml[] value) {
		this.filesField = value;
	}

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

	/**
	 * Gets the single stream only.
	 *
	 * @return the single stream only
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool singleStreamOnly
	public final boolean getsingleStreamOnly() {
		return this.singleStreamOnlyField;
	}

	/**
	 * Sets the single stream only.
	 *
	 * @param value the new single stream only
	 */
	public final void setsingleStreamOnly(boolean value) {
		this.singleStreamOnlyField = value;
	}

}