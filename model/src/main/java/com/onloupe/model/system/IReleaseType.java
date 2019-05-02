package com.onloupe.model.system;

/** 
 Release types indicate the quality level and exposure of a release.
 
 Use release types to indicate the role of a specific application build - ranging from the most 
 raw build which has had little or no testing (the lowest sequence release type) to the most public, 
 published build (the highest sequence release type).
*/
public interface IReleaseType
{
	/** 
	 A unique name of this release type within the set of release types.
	*/
	String getName();

	/** 
	 The order of this release type in the set of release types.  Higher values are closer to final release, lower are closer to development.
	 
	 The lowest release type is the raw development release type, automatically used for each build.
	 The highest release type is the most public, production release type.
	*/
	int getSequence();

	/** 
	 The display caption for this release type.
	 
	 This value can be edited to change how the release type displays.  It defaults
	 to the name.
	*/
	String getCaption();

	/** 
	 Optional. A description of this release type.
	*/
	String getDescription();
}