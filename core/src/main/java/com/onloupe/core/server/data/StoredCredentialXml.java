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
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class StoredCredentialXml: object, System.ComponentModel.INotifyPropertyChanged
public class StoredCredentialXml {

	private String keyField;

	private String accountField;

	private String passwordField;

	/**
	 * <remarks/>
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string key
	public final String getkey() {
		return this.keyField;
	}

	public final void setkey(String value) {
		this.keyField = value;
	}

	/**
	 * <remarks/>
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string account
	public final String getaccount() {
		return this.accountField;
	}

	public final void setaccount(String value) {
		this.accountField = value;
	}

	/**
	 * <remarks/>
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string password
	public final String getpassword() {
		return this.passwordField;
	}

	public final void setpassword(String value) {
		this.passwordField = value;
	}

}