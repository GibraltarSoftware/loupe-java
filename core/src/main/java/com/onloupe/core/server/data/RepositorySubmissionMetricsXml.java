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
 * The Class RepositorySubmissionMetricsXml.
 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.CodeDom.Compiler.GeneratedCodeAttribute("xsd", "4.0.30319.33440")][System.Diagnostics.DebuggerStepThroughAttribute()][System.ComponentModel.DesignerCategoryAttribute("code")][System.Xml.Serialization.XmlTypeAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd")][System.Xml.Serialization.XmlRootAttribute(Namespace="http://www.gibraltarsoftware.com/Gibraltar/Repository.xsd", IsNullable=false)] public partial class RepositorySubmissionMetricsXml: object, System.ComponentModel.INotifyPropertyChanged
public class RepositorySubmissionMetricsXml {

	/** The metric field. */
	private RepositorySubmissionMetricXml[] metricField;

	/**
	 * Gets the metric.
	 *
	 * @return the metric
	 */
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [System.Xml.Serialization.XmlElementAttribute("metric")] public RepositorySubmissionMetricXml[] metric
	public final RepositorySubmissionMetricXml[] getmetric() {
		return this.metricField;
	}

	/**
	 * Sets the metric.
	 *
	 * @param value the new metric
	 */
	public final void setmetric(RepositorySubmissionMetricXml[] value) {
		this.metricField = value;
	}

}