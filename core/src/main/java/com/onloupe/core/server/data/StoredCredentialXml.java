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
 * The Class StoredCredentialXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")] public partial class StoredCredentialXml: object, System.ComponentModel.INotifyPropertyChanged
public class StoredCredentialXml {

	/** The key field. */
	private String keyField;

	/** The account field. */
	private String accountField;

	/** The password field. */
	private String passwordField;

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string key
	public final String getkey() {
		return this.keyField;
	}

	/**
	 * Sets the key.
	 *
	 * @param value the new key
	 */
	public final void setkey(String value) {
		this.keyField = value;
	}

	/**
	 * Gets the account.
	 *
	 * @return the account
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlAttributeAttribute()] public string account
	public final String getaccount() {
		return this.accountField;
	}

	/**
	 * Sets the account.
	 *
	 * @param value the new account
	 */
	public final void setaccount(String value) {
		this.accountField = value;
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