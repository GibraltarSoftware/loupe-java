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
 * <remarks/>
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class SessionFileXml: object, System.ComponentModel.INotifyPropertyChanged
public class SessionFileXml {

	private String idField;

	private long sequenceField;

	private long versionField;

	/**
	 * <remarks/>
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string id
	public final String getid() {
		return this.idField;
	}

	public final void setid(String value) {
		this.idField = value;
	}

	/**
	 * <remarks/>
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public long sequence
	public final long getsequence() {
		return this.sequenceField;
	}

	public final void setsequence(long value) {
		this.sequenceField = value;
	}

	/**
	 * <remarks/>
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public long version
	public final long getversion() {
		return this.versionField;
	}

	public final void setversion(long value) {
		this.versionField = value;
	}

}