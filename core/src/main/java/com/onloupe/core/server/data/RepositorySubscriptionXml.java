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
 * The Class RepositorySubscriptionXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")][System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd", IsNullable=false)] public partial class RepositorySubscriptionXml: object, System.ComponentModel.INotifyPropertyChanged
public class RepositorySubscriptionXml {

	/** The added dt field. */
	private DateTimeOffsetXml addedDtField;

	/** The id field. */
	private String idField;

	/** The server repository id field. */
	private String serverRepositoryIdField;

	/** The computer key field. */
	private String computerKeyField;

	/** The use gibraltar sds field. */
	private boolean useGibraltarSdsField;

	/** The customer name field. */
	private String customerNameField;

	/** The host name field. */
	private String hostNameField;

	/** The port field. */
	private int portField;

	/** The port field specified. */
	private boolean portFieldSpecified;

	/** The use ssl field. */
	private boolean useSslField;

	/** The use ssl field specified. */
	private boolean useSslFieldSpecified;

	/** The application base directory field. */
	private String applicationBaseDirectoryField;

	/** The repository field. */
	private String repositoryField;

	/** The sync mode field. */
	private SessionSynchronizationModeXml syncModeField;

	/** The use api key field. */
	private boolean useApiKeyField;

	/** The use api key field specified. */
	private boolean useApiKeyFieldSpecified;

	/** The user name field. */
	private String userNameField;

	/**
	 * Gets the added dt.
	 *
	 * @return the added dt
	 */
	public final DateTimeOffsetXml getaddedDt() {
		return this.addedDtField;
	}

	/**
	 * Sets the added dt.
	 *
	 * @param value the new added dt
	 */
	public final void setaddedDt(DateTimeOffsetXml value) {
		this.addedDtField = value;
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
	 * Gets the server repository id.
	 *
	 * @return the server repository id
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string serverRepositoryId
	public final String getserverRepositoryId() {
		return this.serverRepositoryIdField;
	}

	/**
	 * Sets the server repository id.
	 *
	 * @param value the new server repository id
	 */
	public final void setserverRepositoryId(String value) {
		this.serverRepositoryIdField = value;
	}

	/**
	 * Gets the computer key.
	 *
	 * @return the computer key
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string computerKey
	public final String getcomputerKey() {
		return this.computerKeyField;
	}

	/**
	 * Sets the computer key.
	 *
	 * @param value the new computer key
	 */
	public final void setcomputerKey(String value) {
		this.computerKeyField = value;
	}

	/**
	 * Gets the use gibraltar sds.
	 *
	 * @return the use gibraltar sds
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool useGibraltarSds
	public final boolean getuseGibraltarSds() {
		return this.useGibraltarSdsField;
	}

	/**
	 * Sets the use gibraltar sds.
	 *
	 * @param value the new use gibraltar sds
	 */
	public final void setuseGibraltarSds(boolean value) {
		this.useGibraltarSdsField = value;
	}

	/**
	 * Gets the customer name.
	 *
	 * @return the customer name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string customerName
	public final String getcustomerName() {
		return this.customerNameField;
	}

	/**
	 * Sets the customer name.
	 *
	 * @param value the new customer name
	 */
	public final void setcustomerName(String value) {
		this.customerNameField = value;
	}

	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string hostName
	public final String gethostName() {
		return this.hostNameField;
	}

	/**
	 * Sets the host name.
	 *
	 * @param value the new host name
	 */
	public final void sethostName(String value) {
		this.hostNameField = value;
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int port
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
	 * Gets the port specified.
	 *
	 * @return the port specified
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlIgnoreAttribute()] public bool portSpecified
	public final boolean getportSpecified() {
		return this.portFieldSpecified;
	}

	/**
	 * Sets the port specified.
	 *
	 * @param value the new port specified
	 */
	public final void setportSpecified(boolean value) {
		this.portFieldSpecified = value;
	}

	/**
	 * Gets the use ssl.
	 *
	 * @return the use ssl
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool useSsl
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
	 * Gets the use ssl specified.
	 *
	 * @return the use ssl specified
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlIgnoreAttribute()] public bool useSslSpecified
	public final boolean getuseSslSpecified() {
		return this.useSslFieldSpecified;
	}

	/**
	 * Sets the use ssl specified.
	 *
	 * @param value the new use ssl specified
	 */
	public final void setuseSslSpecified(boolean value) {
		this.useSslFieldSpecified = value;
	}

	/**
	 * Gets the application base directory.
	 *
	 * @return the application base directory
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string applicationBaseDirectory
	public final String getapplicationBaseDirectory() {
		return this.applicationBaseDirectoryField;
	}

	/**
	 * Sets the application base directory.
	 *
	 * @param value the new application base directory
	 */
	public final void setapplicationBaseDirectory(String value) {
		this.applicationBaseDirectoryField = value;
	}

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string repository
	public final String getrepository() {
		return this.repositoryField;
	}

	/**
	 * Sets the repository.
	 *
	 * @param value the new repository
	 */
	public final void setrepository(String value) {
		this.repositoryField = value;
	}

	/**
	 * Gets the sync mode.
	 *
	 * @return the sync mode
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public SessionSynchronizationModeXml syncMode
	public final SessionSynchronizationModeXml getsyncMode() {
		return this.syncModeField;
	}

	/**
	 * Sets the sync mode.
	 *
	 * @param value the new sync mode
	 */
	public final void setsyncMode(SessionSynchronizationModeXml value) {
		this.syncModeField = value;
	}

	/**
	 * Gets the use api key.
	 *
	 * @return the use api key
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool useApiKey
	public final boolean getuseApiKey() {
		return this.useApiKeyField;
	}

	/**
	 * Sets the use api key.
	 *
	 * @param value the new use api key
	 */
	public final void setuseApiKey(boolean value) {
		this.useApiKeyField = value;
	}

	/**
	 * Gets the use api key specified.
	 *
	 * @return the use api key specified
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlIgnoreAttribute()] public bool useApiKeySpecified
	public final boolean getuseApiKeySpecified() {
		return this.useApiKeyFieldSpecified;
	}

	/**
	 * Sets the use api key specified.
	 *
	 * @param value the new use api key specified
	 */
	public final void setuseApiKeySpecified(boolean value) {
		this.useApiKeyFieldSpecified = value;
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

}