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
 * The Class RepositoryMailboxTrackingXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class RepositoryMailboxTrackingXml: object, System.ComponentModel.INotifyPropertyChanged
public class RepositoryMailboxTrackingXml {

	/** The status message field. */
	private String statusMessageField;

	/** The last contact dt field. */
	private DateTimeOffsetXml lastContactDtField;

	/** The updated dt field. */
	private DateTimeOffsetXml updatedDtField;

	/** The in error field. */
	private boolean inErrorField;

	/**
	 * Gets the status message.
	 *
	 * @return the status message
	 */
	public final String getstatusMessage() {
		return this.statusMessageField;
	}

	/**
	 * Sets the status message.
	 *
	 * @param value the new status message
	 */
	public final void setstatusMessage(String value) {
		this.statusMessageField = value;
	}

	/**
	 * Gets the last contact dt.
	 *
	 * @return the last contact dt
	 */
	public final DateTimeOffsetXml getlastContactDt() {
		return this.lastContactDtField;
	}

	/**
	 * Sets the last contact dt.
	 *
	 * @param value the new last contact dt
	 */
	public final void setlastContactDt(DateTimeOffsetXml value) {
		this.lastContactDtField = value;
	}

	/**
	 * Gets the updated dt.
	 *
	 * @return the updated dt
	 */
	public final DateTimeOffsetXml getupdatedDt() {
		return this.updatedDtField;
	}

	/**
	 * Sets the updated dt.
	 *
	 * @param value the new updated dt
	 */
	public final void setupdatedDt(DateTimeOffsetXml value) {
		this.updatedDtField = value;
	}

	/**
	 * Gets the in error.
	 *
	 * @return the in error
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool inError
	public final boolean getinError() {
		return this.inErrorField;
	}

	/**
	 * Sets the in error.
	 *
	 * @param value the new in error
	 */
	public final void setinError(boolean value) {
		this.inErrorField = value;
	}

}