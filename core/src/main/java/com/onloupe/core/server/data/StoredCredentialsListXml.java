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
 * The Class StoredCredentialsListXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")][System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd", IsNullable=false)] public partial class StoredCredentialsListXml: object, System.ComponentModel.INotifyPropertyChanged
public class StoredCredentialsListXml {

	/** The credential field. */
	private StoredCredentialXml[] credentialField;

	/**
	 * Gets the credential.
	 *
	 * @return the credential
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlElementAttribute("credential")] public StoredCredentialXml[] credential
	public final StoredCredentialXml[] getcredential() {
		return this.credentialField;
	}

	/**
	 * Sets the credential.
	 *
	 * @param value the new credential
	 */
	public final void setcredential(StoredCredentialXml[] value) {
		this.credentialField = value;
	}

}