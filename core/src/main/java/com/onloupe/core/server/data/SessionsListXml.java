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
 * The Class SessionsListXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")][System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd", IsNullable=false)] public partial class SessionsListXml: object, System.ComponentModel.INotifyPropertyChanged
public class SessionsListXml {

	/** The sessions field. */
	private SessionXml[] sessionsField;

	/** The version field. */
	private long versionField;

	/**
	 * Instantiates a new sessions list xml.
	 */
	public SessionsListXml() {
		this.versionField = 0;
	}

	/**
	 * Gets the sessions.
	 *
	 * @return the sessions
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlArrayItemAttribute("session", IsNullable=false)] public SessionXml[] sessions
	public final SessionXml[] getsessions() {
		return this.sessionsField;
	}

	/**
	 * Sets the sessions.
	 *
	 * @param value the new sessions
	 */
	public final void setsessions(SessionXml[] value) {
		this.sessionsField = value;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()][System.ComponentModel.DefaultValueAttribute(typeof(long), "0")] public long version
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