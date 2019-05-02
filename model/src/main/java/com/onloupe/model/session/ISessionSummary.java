package com.onloupe.model.session;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import com.onloupe.model.data.ProcessorArchitecture;
import com.onloupe.model.system.ApplicationType;
import com.onloupe.model.system.OSBootMode;
import com.onloupe.model.system.Version;

/** An interface for accessing Summary information about the current session.
 
 	<p>The session summary includes all of the information that is available in
	 Loupe to categorize the session. This includes the product,
	 application, and version information that was detected by Loupe (or overridden
	 in the application configuration) as well as a range of information about the
	 current computing environment (such as Operating System Family and process
	 architecture).</p>
 
*/
public interface ISessionSummary
{

	/** 
	 The unique Id of the session.
	*/
	UUID getId();

	/** 
	 The link to this item on the server.
	*/
	URI getUri();

	/** 
	 Indicates if the session has ever been viewed or exported.
	*/
	boolean isNew();

	/** 
	 Indicates if all of the session data is stored that is expected to be available.
	*/
	boolean isComplete();

	/** 
	 Indicates if the session is currently running and a live stream is available.
	*/
	boolean isLive();

	/** 
	 Indicates if session data is available.
	 
	 The session summary can be transfered separately from the session details
	 and isn't subject to pruning so it may be around long before or after the detailed data is.
	*/
	boolean getHasData();

	/** 
	 The display caption of the time zone where the session was recorded.
	*/
	String getTimeZoneCaption();

	/** 
	 The date and time the session started.
	*/
	OffsetDateTime getStartDateTime();

	/** 
	 The date and time the session started.
	*/
	OffsetDateTime getDisplayStartDateTime();

	/** 
	 The date and time the session ended or was last confirmed running in the time zone the user has requested for display.
	*/
	OffsetDateTime getEndDateTime();

	/** 
	 The date and time the session ended or was last confirmed running in the time zone the user has requested for display.
	*/
	OffsetDateTime getDisplayEndDateTime();

	/** 
	 The time range between the start and end of this session.
	*/
	Duration getDuration();

	/** 
	 The date and time the session was added to the repository.
	*/
	OffsetDateTime getAddedDateTime();

	/** 
	 The date and time the session was added to the repository in the time zone the user has requested for display.
	*/
	OffsetDateTime getDisplayAddedDateTime();

	/** 
	 The date and time the session header was last updated locally.
	*/
	OffsetDateTime getUpdatedDateTime();

	/** 
	 The date and time the session header was last updated locally in the time zone the user has requested for display.
	*/
	OffsetDateTime getDisplayUpdatedDateTime();

	/** 
	 A display caption for the session.
	*/
	String getCaption();

	/** 
	 The product name of the application that recorded the session.
	*/
	String getProduct();

	/** 
	 The title of the application that recorded the session.
	*/
	String getApplication();

	/** 
	 Optional.  The environment this session is running in.
	 
	 Environments are useful for categorizing sessions, for example to 
	 indicate the hosting environment. If a value is provided it will be 
	 carried with the session data to upstream servers and clients.  If the 
	 corresponding entry does not exist it will be automatically created.
	*/
	String getEnvironment();

	/** 
	 Optional.  The promotion level of the session.
	 
	 Promotion levels are useful for categorizing sessions, for example to 
	 indicate whether it was run in development, staging, or production. 
	 If a value is provided it will be carried with the session data to upstream servers and clients.  
	 If the corresponding entry does not exist it will be automatically created.
	*/
	String getPromotionLevel();

	/** 
	 The type of process the application ran as.
	*/
	ApplicationType getApplicationType();

	/** 
	 The description of the application from its manifest.
	*/
	String getApplicationDescription();

	/** 
	 The version of the application that recorded the session.
	*/
	Version getApplicationVersion();

	/** 
	 The version of the Loupe Agent used to monitor the session.
	*/
	Version getAgentVersion();

	/** 
	 The host name / NetBIOS name of the computer that recorded the session.
	 
	 Does not include the domain name portion of the fully qualified DNS name.
	*/
	String getHostName();

	/** 
	 The DNS domain name of the computer that recorded the session.  May be empty.
	 
	 Does not include the host name portion of the fully qualified DNS name.
	*/
	String getDnsDomainName();

	/** 
	 The fully qualified user name of the user the application was run as.
	*/
	String getFullyQualifiedUserName();

	/** 
	 The user Id that was used to run the session.
	*/
	String getUserName();

	/** 
	 The domain of the user id that was used to run the session.
	*/
	String getUserDomainName();

	/** 
	 The version information of the installed operating system (without service pack or patches).
	*/
	Version getOSVersion();

	/** 
	 The operating system service pack, if any.
	*/
	String getOSServicePack();

	/** 
	 The culture name of the underlying operating system installation.
	*/
	String getOSCultureName();

	/** 
	 The processor architecture of the operating system.
	*/
	ProcessorArchitecture getOSArchitecture();

	/** 
	 The boot mode of the operating system.
	*/
	OSBootMode getOSBootMode();

	/** 
	 The OS Platform code, nearly always 1 indicating Windows NT.
	*/
	int getOSPlatformCode();

	/** 
	 The OS product type code, used to differentiate specific editions of various operating systems.
	*/
	int getOSProductType();

	/** 
	 The OS Suite Mask, used to differentiate specific editions of various operating systems.
	*/
	int getOSSuiteMask();

	/** 
	 The well known operating system family name, like Windows Vista or Windows Server 2003.
	*/
	String getOSFamilyName();

	/** 
	 The edition of the operating system without the family name, such as Workstation or Standard Server.
	*/
	String getOSEditionName();

	/** 
	 The well known OS name and edition name.
	*/
	String getOSFullName();

	/** 
	 The well known OS name, edition name, and service pack like Windows XP Professional Service Pack 3.
	*/
	String getOSFullNameWithServicePack();

	/** 
	 The version of the .NET runtime that the application domain is running as.
	*/
	Version getRuntimeVersion();

	/** 
	 The processor architecture the process is running as.
	*/
	ProcessorArchitecture getRuntimeArchitecture();

	/** 
	 The current application culture name.
	*/
	String getCurrentCultureName();

	/** 
	 The current user interface culture name.
	*/
	String getCurrentUICultureName();

	/** 
	 The number of megabytes of installed memory in the host computer.
	*/
	int getMemoryMB();

	/** 
	 The number of physical processor sockets in the host computer.
	*/
	int getProcessors();

	/** 
	 The total number of processor cores in the host computer.
	*/
	int getProcessorCores();

	/** 
	 Indicates if the session was run in a user interactive mode.
	*/
	boolean getUserInteractive();

	/** 
	 Indicates if the session was run through terminal server.  Only applies to User Interactive sessions.
	*/
	boolean getTerminalServer();

	/** 
	 The number of pixels wide of the virtual desktop.
	*/
	int getScreenWidth();

	/** 
	 The number of pixels tall for the virtual desktop.
	*/
	int getScreenHeight();

	/** 
	 The number of bits of color depth.
	*/
	int getColorDepth();

	/** 
	 The complete command line used to execute the process including arguments.
	*/
	String getCommandLine();

	/** 
	 Optional. The unique tracking Id of the computer that recorded this session.
	*/
	UUID getComputerId();

	/** 
	 The final status of the session.
	*/
	SessionStatus getStatus();

	/** 
	 The number of messages in the messages collection.
	 
	 This value is cached for high performance and reflects all of the known messages.  If only part
	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 files are missing.
	*/
	int getMessageCount();

	/** 
	 The number of critical messages in the messages collection.
	 
	 This value is cached for high performance and reflects all of the known messages.  If only part
	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 files are missing.
	*/
	int getCriticalCount();

	/** 
	 The number of error messages in the messages collection.
	 
	 This value is cached for high performance and reflects all of the known messages.  If only part
	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 files are missing.
	*/
	int getErrorCount();

	/** 
	 The number of warning messages in the messages collection.
	 
	 This value is cached for high performance and reflects all of the known messages.  If only part
	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 files are missing.
	*/
	int getWarningCount();

	/** 
	 A copy of the collection of application specific properties. (Set via configuration at logging startup.  Do not modify here.)
	*/
	Map<String, String> getProperties();

}