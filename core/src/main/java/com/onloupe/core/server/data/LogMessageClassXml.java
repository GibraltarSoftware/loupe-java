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
 * The Class LogMessageClassXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class LogMessageClassXml: object, System.ComponentModel.INotifyPropertyChanged
public class LogMessageClassXml {

	/** The id field. */
	private String idField;

	/** The severity field. */
	private LogMessageSeverityXml severityField = LogMessageSeverityXml.values()[0];

	/** The class field. */
	private String classField;

	/** The caption field. */
	private String captionField;

	/** The message count field. */
	private int messageCountField;

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
	 * Gets the severity.
	 *
	 * @return the severity
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public LogMessageSeverityXml severity
	public final LogMessageSeverityXml getseverity() {
		return this.severityField;
	}

	/**
	 * Sets the severity.
	 *
	 * @param value the new severity
	 */
	public final void setseverity(LogMessageSeverityXml value) {
		this.severityField = value;
	}

	/**
	 * Gets the class.
	 *
	 * @return the class
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string class
	public final String getclass() {
		return this.classField;
	}

	/**
	 * Sets the class.
	 *
	 * @param value the new class
	 */
	public final void setclass(String value) {
		this.classField = value;
	}

	/**
	 * Gets the caption.
	 *
	 * @return the caption
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string caption
	public final String getcaption() {
		return this.captionField;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setcaption(String value) {
		this.captionField = value;
	}

	/**
	 * Gets the message count.
	 *
	 * @return the message count
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int messageCount
	public final int getmessageCount() {
		return this.messageCountField;
	}

	/**
	 * Sets the message count.
	 *
	 * @param value the new message count
	 */
	public final void setmessageCount(int value) {
		this.messageCountField = value;
	}
}