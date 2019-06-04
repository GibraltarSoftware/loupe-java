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
 * The Class SessionDetailXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class SessionDetailXml: object, System.ComponentModel.INotifyPropertyChanged
public class SessionDetailXml {

	/** The start dt field. */
	private DateTimeOffsetXml startDtField;

	/** The end dt field. */
	private DateTimeOffsetXml endDtField;

	/** The added dt field. */
	private DateTimeOffsetXml addedDtField;

	/** The updated dt field. */
	private DateTimeOffsetXml updatedDtField;

	/** The properties field. */
	private SessionPropertyXml[] propertiesField;

	/** The log message categories field. */
	private LogMessageCategoryXml[] logMessageCategoriesField;

	/** The log message classes field. */
	private LogMessageClassXml[] logMessageClassesField;

	/** The product name field. */
	private String productNameField;

	/** The application name field. */
	private String applicationNameField;

	/** The environment name field. */
	private String environmentNameField;

	/** The promotion level name field. */
	private String promotionLevelNameField;

	/** The application version field. */
	private String applicationVersionField;

	/** The application type field. */
	private ApplicationTypeXml applicationTypeField = ApplicationTypeXml.values()[0];

	/** The application description field. */
	private String applicationDescriptionField;

	/** The caption field. */
	private String captionField;

	/** The status field. */
	private SessionStatusXml statusField = SessionStatusXml.values()[0];

	/** The time zone caption field. */
	private String timeZoneCaptionField;

	/** The duration sec field. */
	private long durationSecField;

	/** The agent version field. */
	private String agentVersionField;

	/** The user name field. */
	private String userNameField;

	/** The user domain name field. */
	private String userDomainNameField;

	/** The host name field. */
	private String hostNameField;

	/** The dns domain name field. */
	private String dnsDomainNameField;

	/** The is new field. */
	private boolean isNewField;

	/** The is complete field. */
	private boolean isCompleteField;

	/** The message count field. */
	private int messageCountField;

	/** The critical message count field. */
	private int criticalMessageCountField;

	/** The error message count field. */
	private int errorMessageCountField;

	/** The warning message count field. */
	private int warningMessageCountField;

	/** The update user field. */
	private String updateUserField;

	/** The os platform code field. */
	private int osPlatformCodeField;

	/** The os version field. */
	private String osVersionField;

	/** The os service pack field. */
	private String osServicePackField;

	/** The os culture name field. */
	private String osCultureNameField;

	/** The os architecture field. */
	private ProcessorArchitectureXml osArchitectureField = ProcessorArchitectureXml.values()[0];

	/** The os boot mode field. */
	private BootModeXml osBootModeField = BootModeXml.values()[0];

	/** The os suite mask code field. */
	private int osSuiteMaskCodeField;

	/** The os product type code field. */
	private int osProductTypeCodeField;

	/** The runtime version field. */
	private String runtimeVersionField;

	/** The runtime architecture field. */
	private ProcessorArchitectureXml runtimeArchitectureField = ProcessorArchitectureXml.values()[0];

	/** The current culture name field. */
	private String currentCultureNameField;

	/** The current ui culture name field. */
	private String currentUiCultureNameField;

	/** The memory mb field. */
	private int memoryMbField;

	/** The processors field. */
	private int processorsField;

	/** The processor cores field. */
	private int processorCoresField;

	/** The user interactive field. */
	private boolean userInteractiveField;

	/** The terminal server field. */
	private boolean terminalServerField;

	/** The screen width field. */
	private int screenWidthField;

	/** The screen height field. */
	private int screenHeightField;

	/** The color depth field. */
	private int colorDepthField;

	/** The command line field. */
	private String commandLineField;

	/** The file size field. */
	private long fileSizeField;

	/** The file available field. */
	private boolean fileAvailableField;

	/** The computer id field. */
	private String computerIdField;

	/**
	 * Gets the start dt.
	 *
	 * @return the start dt
	 */
	public final DateTimeOffsetXml getstartDt() {
		return this.startDtField;
	}

	/**
	 * Sets the start dt.
	 *
	 * @param value the new start dt
	 */
	public final void setstartDt(DateTimeOffsetXml value) {
		this.startDtField = value;
	}

	/**
	 * Gets the end dt.
	 *
	 * @return the end dt
	 */
	public final DateTimeOffsetXml getendDt() {
		return this.endDtField;
	}

	/**
	 * Sets the end dt.
	 *
	 * @param value the new end dt
	 */
	public final void setendDt(DateTimeOffsetXml value) {
		this.endDtField = value;
	}

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
	 * Gets the properties.
	 *
	 * @return the properties
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlArrayItemAttribute("property", IsNullable=false)] public SessionPropertyXml[] properties
	public final SessionPropertyXml[] getproperties() {
		return this.propertiesField;
	}

	/**
	 * Sets the properties.
	 *
	 * @param value the new properties
	 */
	public final void setproperties(SessionPropertyXml[] value) {
		this.propertiesField = value;
	}

	/**
	 * Gets the log message categories.
	 *
	 * @return the log message categories
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlArrayItemAttribute("logMessageCategory", IsNullable=false)] public LogMessageCategoryXml[] logMessageCategories
	public final LogMessageCategoryXml[] getlogMessageCategories() {
		return this.logMessageCategoriesField;
	}

	/**
	 * Sets the log message categories.
	 *
	 * @param value the new log message categories
	 */
	public final void setlogMessageCategories(LogMessageCategoryXml[] value) {
		this.logMessageCategoriesField = value;
	}

	/**
	 * Gets the log message classes.
	 *
	 * @return the log message classes
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlArrayItemAttribute("logMessageClass", IsNullable=false)] public LogMessageClassXml[] logMessageClasses
	public final LogMessageClassXml[] getlogMessageClasses() {
		return this.logMessageClassesField;
	}

	/**
	 * Sets the log message classes.
	 *
	 * @param value the new log message classes
	 */
	public final void setlogMessageClasses(LogMessageClassXml[] value) {
		this.logMessageClassesField = value;
	}

	/**
	 * Gets the product name.
	 *
	 * @return the product name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string productName
	public final String getproductName() {
		return this.productNameField;
	}

	/**
	 * Sets the product name.
	 *
	 * @param value the new product name
	 */
	public final void setproductName(String value) {
		this.productNameField = value;
	}

	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string applicationName
	public final String getapplicationName() {
		return this.applicationNameField;
	}

	/**
	 * Sets the application name.
	 *
	 * @param value the new application name
	 */
	public final void setapplicationName(String value) {
		this.applicationNameField = value;
	}

	/**
	 * Gets the environment name.
	 *
	 * @return the environment name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string environmentName
	public final String getenvironmentName() {
		return this.environmentNameField;
	}

	/**
	 * Sets the environment name.
	 *
	 * @param value the new environment name
	 */
	public final void setenvironmentName(String value) {
		this.environmentNameField = value;
	}

	/**
	 * Gets the promotion level name.
	 *
	 * @return the promotion level name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string promotionLevelName
	public final String getpromotionLevelName() {
		return this.promotionLevelNameField;
	}

	/**
	 * Sets the promotion level name.
	 *
	 * @param value the new promotion level name
	 */
	public final void setpromotionLevelName(String value) {
		this.promotionLevelNameField = value;
	}

	/**
	 * Gets the application version.
	 *
	 * @return the application version
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string applicationVersion
	public final String getapplicationVersion() {
		return this.applicationVersionField;
	}

	/**
	 * Sets the application version.
	 *
	 * @param value the new application version
	 */
	public final void setapplicationVersion(String value) {
		this.applicationVersionField = value;
	}

	/**
	 * Gets the application type.
	 *
	 * @return the application type
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public ApplicationTypeXml applicationType
	public final ApplicationTypeXml getapplicationType() {
		return this.applicationTypeField;
	}

	/**
	 * Sets the application type.
	 *
	 * @param value the new application type
	 */
	public final void setapplicationType(ApplicationTypeXml value) {
		this.applicationTypeField = value;
	}

	/**
	 * Gets the application description.
	 *
	 * @return the application description
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string applicationDescription
	public final String getapplicationDescription() {
		return this.applicationDescriptionField;
	}

	/**
	 * Sets the application description.
	 *
	 * @param value the new application description
	 */
	public final void setapplicationDescription(String value) {
		this.applicationDescriptionField = value;
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
	 * Gets the status.
	 *
	 * @return the status
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public SessionStatusXml status
	public final SessionStatusXml getstatus() {
		return this.statusField;
	}

	/**
	 * Sets the status.
	 *
	 * @param value the new status
	 */
	public final void setstatus(SessionStatusXml value) {
		this.statusField = value;
	}

	/**
	 * Gets the time zone caption.
	 *
	 * @return the time zone caption
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string timeZoneCaption
	public final String gettimeZoneCaption() {
		return this.timeZoneCaptionField;
	}

	/**
	 * Sets the time zone caption.
	 *
	 * @param value the new time zone caption
	 */
	public final void settimeZoneCaption(String value) {
		this.timeZoneCaptionField = value;
	}

	/**
	 * Gets the agent version.
	 *
	 * @return the agent version
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string agentVersion
	public final String getagentVersion() {
		return this.agentVersionField;
	}

	/**
	 * Sets the agent version.
	 *
	 * @param value the new agent version
	 */
	public final void setagentVersion(String value) {
		this.agentVersionField = value;
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
	 * Gets the user domain name.
	 *
	 * @return the user domain name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string userDomainName
	public final String getuserDomainName() {
		return this.userDomainNameField;
	}

	/**
	 * Sets the user domain name.
	 *
	 * @param value the new user domain name
	 */
	public final void setuserDomainName(String value) {
		this.userDomainNameField = value;
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
	 * Gets the dns domain name.
	 *
	 * @return the dns domain name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string dnsDomainName
	public final String getdnsDomainName() {
		return this.dnsDomainNameField;
	}

	/**
	 * Sets the dns domain name.
	 *
	 * @param value the new dns domain name
	 */
	public final void setdnsDomainName(String value) {
		this.dnsDomainNameField = value;
	}

	/**
	 * Gets the checks if is new.
	 *
	 * @return the checks if is new
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool isNew
	public final boolean getisNew() {
		return this.isNewField;
	}

	/**
	 * Sets the checks if is new.
	 *
	 * @param value the new checks if is new
	 */
	public final void setisNew(boolean value) {
		this.isNewField = value;
	}

	/**
	 * Gets the checks if is complete.
	 *
	 * @return the checks if is complete
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool isComplete
	public final boolean getisComplete() {
		return this.isCompleteField;
	}

	/**
	 * Sets the checks if is complete.
	 *
	 * @param value the new checks if is complete
	 */
	public final void setisComplete(boolean value) {
		this.isCompleteField = value;
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

	/**
	 * Gets the critical message count.
	 *
	 * @return the critical message count
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int criticalMessageCount
	public final int getcriticalMessageCount() {
		return this.criticalMessageCountField;
	}

	/**
	 * Sets the critical message count.
	 *
	 * @param value the new critical message count
	 */
	public final void setcriticalMessageCount(int value) {
		this.criticalMessageCountField = value;
	}

	/**
	 * Gets the error message count.
	 *
	 * @return the error message count
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int errorMessageCount
	public final int geterrorMessageCount() {
		return this.errorMessageCountField;
	}

	/**
	 * Sets the error message count.
	 *
	 * @param value the new error message count
	 */
	public final void seterrorMessageCount(int value) {
		this.errorMessageCountField = value;
	}

	/**
	 * Gets the warning message count.
	 *
	 * @return the warning message count
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int warningMessageCount
	public final int getwarningMessageCount() {
		return this.warningMessageCountField;
	}

	/**
	 * Sets the warning message count.
	 *
	 * @param value the new warning message count
	 */
	public final void setwarningMessageCount(int value) {
		this.warningMessageCountField = value;
	}

	/**
	 * Gets the update user.
	 *
	 * @return the update user
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string updateUser
	public final String getupdateUser() {
		return this.updateUserField;
	}

	/**
	 * Sets the update user.
	 *
	 * @param value the new update user
	 */
	public final void setupdateUser(String value) {
		this.updateUserField = value;
	}

	/**
	 * Gets the os platform code.
	 *
	 * @return the os platform code
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int osPlatformCode
	public final int getosPlatformCode() {
		return this.osPlatformCodeField;
	}

	/**
	 * Sets the os platform code.
	 *
	 * @param value the new os platform code
	 */
	public final void setosPlatformCode(int value) {
		this.osPlatformCodeField = value;
	}

	/**
	 * Gets the os version.
	 *
	 * @return the os version
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string osVersion
	public final String getosVersion() {
		return this.osVersionField;
	}

	/**
	 * Sets the os version.
	 *
	 * @param value the new os version
	 */
	public final void setosVersion(String value) {
		this.osVersionField = value;
	}

	/**
	 * Gets the os service pack.
	 *
	 * @return the os service pack
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string osServicePack
	public final String getosServicePack() {
		return this.osServicePackField;
	}

	/**
	 * Sets the os service pack.
	 *
	 * @param value the new os service pack
	 */
	public final void setosServicePack(String value) {
		this.osServicePackField = value;
	}

	/**
	 * Gets the os culture name.
	 *
	 * @return the os culture name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string osCultureName
	public final String getosCultureName() {
		return this.osCultureNameField;
	}

	/**
	 * Sets the os culture name.
	 *
	 * @param value the new os culture name
	 */
	public final void setosCultureName(String value) {
		this.osCultureNameField = value;
	}

	/**
	 * Gets the os architecture.
	 *
	 * @return the os architecture
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public ProcessorArchitectureXml osArchitecture
	public final ProcessorArchitectureXml getosArchitecture() {
		return this.osArchitectureField;
	}

	/**
	 * Sets the os architecture.
	 *
	 * @param value the new os architecture
	 */
	public final void setosArchitecture(ProcessorArchitectureXml value) {
		this.osArchitectureField = value;
	}

	/**
	 * Gets the os boot mode.
	 *
	 * @return the os boot mode
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public BootModeXml osBootMode
	public final BootModeXml getosBootMode() {
		return this.osBootModeField;
	}

	/**
	 * Sets the os boot mode.
	 *
	 * @param value the new os boot mode
	 */
	public final void setosBootMode(BootModeXml value) {
		this.osBootModeField = value;
	}

	/**
	 * Gets the os suite mask code.
	 *
	 * @return the os suite mask code
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int osSuiteMaskCode
	public final int getosSuiteMaskCode() {
		return this.osSuiteMaskCodeField;
	}

	/**
	 * Sets the os suite mask code.
	 *
	 * @param value the new os suite mask code
	 */
	public final void setosSuiteMaskCode(int value) {
		this.osSuiteMaskCodeField = value;
	}

	/**
	 * Gets the os product type code.
	 *
	 * @return the os product type code
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int osProductTypeCode
	public final int getosProductTypeCode() {
		return this.osProductTypeCodeField;
	}

	/**
	 * Sets the os product type code.
	 *
	 * @param value the new os product type code
	 */
	public final void setosProductTypeCode(int value) {
		this.osProductTypeCodeField = value;
	}

	/**
	 * Gets the runtime version.
	 *
	 * @return the runtime version
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string runtimeVersion
	public final String getruntimeVersion() {
		return this.runtimeVersionField;
	}

	/**
	 * Sets the runtime version.
	 *
	 * @param value the new runtime version
	 */
	public final void setruntimeVersion(String value) {
		this.runtimeVersionField = value;
	}

	/**
	 * Gets the runtime architecture.
	 *
	 * @return the runtime architecture
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public ProcessorArchitectureXml runtimeArchitecture
	public final ProcessorArchitectureXml getruntimeArchitecture() {
		return this.runtimeArchitectureField;
	}

	/**
	 * Sets the runtime architecture.
	 *
	 * @param value the new runtime architecture
	 */
	public final void setruntimeArchitecture(ProcessorArchitectureXml value) {
		this.runtimeArchitectureField = value;
	}

	/**
	 * Gets the current culture name.
	 *
	 * @return the current culture name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string currentCultureName
	public final String getcurrentCultureName() {
		return this.currentCultureNameField;
	}

	/**
	 * Sets the current culture name.
	 *
	 * @param value the new current culture name
	 */
	public final void setcurrentCultureName(String value) {
		this.currentCultureNameField = value;
	}

	/**
	 * Gets the current ui culture name.
	 *
	 * @return the current ui culture name
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string currentUiCultureName
	public final String getcurrentUiCultureName() {
		return this.currentUiCultureNameField;
	}

	/**
	 * Sets the current ui culture name.
	 *
	 * @param value the new current ui culture name
	 */
	public final void setcurrentUiCultureName(String value) {
		this.currentUiCultureNameField = value;
	}

	/**
	 * Gets the memory mb.
	 *
	 * @return the memory mb
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int memoryMb
	public final int getmemoryMb() {
		return this.memoryMbField;
	}

	/**
	 * Sets the memory mb.
	 *
	 * @param value the new memory mb
	 */
	public final void setmemoryMb(int value) {
		this.memoryMbField = value;
	}

	/**
	 * Gets the processors.
	 *
	 * @return the processors
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int processors
	public final int getprocessors() {
		return this.processorsField;
	}

	/**
	 * Sets the processors.
	 *
	 * @param value the new processors
	 */
	public final void setprocessors(int value) {
		this.processorsField = value;
	}

	/**
	 * Gets the processor cores.
	 *
	 * @return the processor cores
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int processorCores
	public final int getprocessorCores() {
		return this.processorCoresField;
	}

	/**
	 * Sets the processor cores.
	 *
	 * @param value the new processor cores
	 */
	public final void setprocessorCores(int value) {
		this.processorCoresField = value;
	}

	/**
	 * Gets the user interactive.
	 *
	 * @return the user interactive
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool userInteractive
	public final boolean getuserInteractive() {
		return this.userInteractiveField;
	}

	/**
	 * Sets the user interactive.
	 *
	 * @param value the new user interactive
	 */
	public final void setuserInteractive(boolean value) {
		this.userInteractiveField = value;
	}

	/**
	 * Gets the terminal server.
	 *
	 * @return the terminal server
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool terminalServer
	public final boolean getterminalServer() {
		return this.terminalServerField;
	}

	/**
	 * Sets the terminal server.
	 *
	 * @param value the new terminal server
	 */
	public final void setterminalServer(boolean value) {
		this.terminalServerField = value;
	}

	/**
	 * Gets the screen width.
	 *
	 * @return the screen width
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int screenWidth
	public final int getscreenWidth() {
		return this.screenWidthField;
	}

	/**
	 * Sets the screen width.
	 *
	 * @param value the new screen width
	 */
	public final void setscreenWidth(int value) {
		this.screenWidthField = value;
	}

	/**
	 * Gets the screen height.
	 *
	 * @return the screen height
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int screenHeight
	public final int getscreenHeight() {
		return this.screenHeightField;
	}

	/**
	 * Sets the screen height.
	 *
	 * @param value the new screen height
	 */
	public final void setscreenHeight(int value) {
		this.screenHeightField = value;
	}

	/**
	 * Gets the color depth.
	 *
	 * @return the color depth
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public int colorDepth
	public final int getcolorDepth() {
		return this.colorDepthField;
	}

	/**
	 * Sets the color depth.
	 *
	 * @param value the new color depth
	 */
	public final void setcolorDepth(int value) {
		this.colorDepthField = value;
	}

	/**
	 * Gets the command line.
	 *
	 * @return the command line
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string commandLine
	public final String getcommandLine() {
		return this.commandLineField;
	}

	/**
	 * Sets the command line.
	 *
	 * @param value the new command line
	 */
	public final void setcommandLine(String value) {
		this.commandLineField = value;
	}

	/**
	 * Gets the file size.
	 *
	 * @return the file size
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public long fileSize
	public final long getfileSize() {
		return this.fileSizeField;
	}

	/**
	 * Sets the file size.
	 *
	 * @param value the new file size
	 */
	public final void setfileSize(long value) {
		this.fileSizeField = value;
	}

	/**
	 * Gets the file available.
	 *
	 * @return the file available
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public bool fileAvailable
	public final boolean getfileAvailable() {
		return this.fileAvailableField;
	}

	/**
	 * Sets the file available.
	 *
	 * @param value the new file available
	 */
	public final void setfileAvailable(boolean value) {
		this.fileAvailableField = value;
	}

	/**
	 * Gets the computer id.
	 *
	 * @return the computer id
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string computerId
	public final String getcomputerId() {
		return this.computerIdField;
	}

	/**
	 * Sets the computer id.
	 *
	 * @param value the new computer id
	 */
	public final void setcomputerId(String value) {
		this.computerIdField = value;
	}

	/**
	 * Gets the duration sec.
	 *
	 * @return the duration sec
	 */
	public long getDurationSec() {
		return this.durationSecField;
	}

	/**
	 * Sets the duration sec.
	 *
	 * @param durationSecField the new duration sec
	 */
	public void setDurationSec(long durationSecField) {
		this.durationSecField = durationSecField;
	}

}