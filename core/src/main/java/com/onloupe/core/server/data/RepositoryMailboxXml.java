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
 * The Class RepositoryMailboxXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")][System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd", IsNullable=false)] public partial class RepositoryMailboxXml: object, System.ComponentModel.INotifyPropertyChanged
public class RepositoryMailboxXml {

	/** The updated dt field. */
	private DateTimeOffsetXml updatedDtField;

	/** The tracking field. */
	private RepositoryMailboxTrackingXml trackingField;

	/** The id field. */
	private String idField;

	/** The server field. */
	private String serverField;

	/** The port field. */
	private int portField;

	/** The use ssl field. */
	private boolean useSslField;

	/** The user name field. */
	private String userNameField;

	/** The password field. */
	private String passwordField;

	/**
	 * Instantiates a new repository mailbox xml.
	 */
	public RepositoryMailboxXml() {
		this.portField = 0;
		this.useSslField = false;
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
	 * Gets the tracking.
	 *
	 * @return the tracking
	 */
	public final RepositoryMailboxTrackingXml gettracking() {
		return this.trackingField;
	}

	/**
	 * Sets the tracking.
	 *
	 * @param value the new tracking
	 */
	public final void settracking(RepositoryMailboxTrackingXml value) {
		this.trackingField = value;
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
	 * Gets the server.
	 *
	 * @return the server
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string server
	public final String getserver() {
		return this.serverField;
	}

	/**
	 * Sets the server.
	 *
	 * @param value the new server
	 */
	public final void setserver(String value) {
		this.serverField = value;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()][System.ComponentModel.DefaultValueAttribute(0)] public int port
	public final int getport() {
		return this.portField;
	}

	/**
	 * Sets the port.
	 *
	 * @param value the new port
	 */
	public final void setport(int value) {
		this.portField = value;
	}

	/**
	 * Gets the use ssl.
	 *
	 * @return the use ssl
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()][System.ComponentModel.DefaultValueAttribute(false)] public bool useSsl
	public final boolean getuseSsl() {
		return this.useSslField;
	}

	/**
	 * Sets the use ssl.
	 *
	 * @param value the new use ssl
	 */
	public final void setuseSsl(boolean value) {
		this.useSslField = value;
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string userName
	public final String getuserName() {
		return this.userNameField;
	}

	/**
	 * Sets the user name.
	 *
	 * @param value the new user name
	 */
	public final void setuserName(String value) {
		this.userNameField = value;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string password
	public final String getpassword() {
		return this.passwordField;
	}

	/**
	 * Sets the password.
	 *
	 * @param value the new password
	 */
	public final void setpassword(String value) {
		this.passwordField = value;
	}
}