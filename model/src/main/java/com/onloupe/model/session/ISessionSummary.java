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

// TODO: Auto-generated Javadoc
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
	 *  
	 * 	 The unique Id of the session.
	 *
	 * @return the id
	 */
	UUID getId();

	/**
	 *  
	 * 	 The link to this item on the server.
	 *
	 * @return the uri
	 */
	URI getUri();

	/**
	 *  
	 * 	 Indicates if the session has ever been viewed or exported.
	 *
	 * @return true, if is new
	 */
	boolean isNew();

	/**
	 *  
	 * 	 Indicates if all of the session data is stored that is expected to be available.
	 *
	 * @return true, if is complete
	 */
	boolean isComplete();

	/**
	 *  
	 * 	 Indicates if the session is currently running and a live stream is available.
	 *
	 * @return true, if is live
	 */
	boolean isLive();

	/**
	 *  
	 * 	 Indicates if session data is available.
	 * 	 
	 * 	 The session summary can be transfered separately from the session details
	 * 	 and isn't subject to pruning so it may be around long before or after the detailed data is.
	 *
	 * @return the checks for data
	 */
	boolean getHasData();

	/**
	 *  
	 * 	 The display caption of the time zone where the session was recorded.
	 *
	 * @return the time zone caption
	 */
	String getTimeZoneCaption();

	/**
	 *  
	 * 	 The date and time the session started.
	 *
	 * @return the start date time
	 */
	OffsetDateTime getStartDateTime();

	/**
	 *  
	 * 	 The date and time the session started.
	 *
	 * @return the display start date time
	 */
	OffsetDateTime getDisplayStartDateTime();

	/**
	 *  
	 * 	 The date and time the session ended or was last confirmed running in the time zone the user has requested for display.
	 *
	 * @return the end date time
	 */
	OffsetDateTime getEndDateTime();

	/**
	 *  
	 * 	 The date and time the session ended or was last confirmed running in the time zone the user has requested for display.
	 *
	 * @return the display end date time
	 */
	OffsetDateTime getDisplayEndDateTime();

	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	Duration getDuration();

	/**
	 *  
	 * 	 The date and time the session was added to the repository.
	 *
	 * @return the added date time
	 */
	OffsetDateTime getAddedDateTime();

	/**
	 *  
	 * 	 The date and time the session was added to the repository in the time zone the user has requested for display.
	 *
	 * @return the display added date time
	 */
	OffsetDateTime getDisplayAddedDateTime();

	/**
	 *  
	 * 	 The date and time the session header was last updated locally.
	 *
	 * @return the updated date time
	 */
	OffsetDateTime getUpdatedDateTime();

	/**
	 *  
	 * 	 The date and time the session header was last updated locally in the time zone the user has requested for display.
	 *
	 * @return the display updated date time
	 */
	OffsetDateTime getDisplayUpdatedDateTime();

	/**
	 *  
	 * 	 A display caption for the session.
	 *
	 * @return the caption
	 */
	String getCaption();

	/**
	 *  
	 * 	 The product name of the application that recorded the session.
	 *
	 * @return the product
	 */
	String getProduct();

	/**
	 *  
	 * 	 The title of the application that recorded the session.
	 *
	 * @return the application
	 */
	String getApplication();

	/**
	 *  
	 * 	 Optional.  The environment this session is running in.
	 * 	 
	 * 	 Environments are useful for categorizing sessions, for example to 
	 * 	 indicate the hosting environment. If a value is provided it will be 
	 * 	 carried with the session data to upstream servers and clients.  If the 
	 * 	 corresponding entry does not exist it will be automatically created.
	 *
	 * @return the environment
	 */
	String getEnvironment();

	/**
	 *  
	 * 	 Optional.  The promotion level of the session.
	 * 	 
	 * 	 Promotion levels are useful for categorizing sessions, for example to 
	 * 	 indicate whether it was run in development, staging, or production. 
	 * 	 If a value is provided it will be carried with the session data to upstream servers and clients.  
	 * 	 If the corresponding entry does not exist it will be automatically created.
	 *
	 * @return the promotion level
	 */
	String getPromotionLevel();

	/**
	 *  
	 * 	 The type of process the application ran as.
	 *
	 * @return the application type
	 */
	ApplicationType getApplicationType();

	/**
	 *  
	 * 	 The description of the application from its manifest.
	 *
	 * @return the application description
	 */
	String getApplicationDescription();

	/**
	 *  
	 * 	 The version of the application that recorded the session.
	 *
	 * @return the application version
	 */
	Version getApplicationVersion();

	/**
	 *  
	 * 	 The version of the Loupe Agent used to monitor the session.
	 *
	 * @return the agent version
	 */
	Version getAgentVersion();

	/**
	 *  
	 * 	 The host name / NetBIOS name of the computer that recorded the session.
	 * 	 
	 * 	 Does not include the domain name portion of the fully qualified DNS name.
	 *
	 * @return the host name
	 */
	String getHostName();

	/**
	 *  
	 * 	 The DNS domain name of the computer that recorded the session.  May be empty.
	 * 	 
	 * 	 Does not include the host name portion of the fully qualified DNS name.
	 *
	 * @return the dns domain name
	 */
	String getDnsDomainName();

	/**
	 *  
	 * 	 The fully qualified user name of the user the application was run as.
	 *
	 * @return the fully qualified user name
	 */
	String getFullyQualifiedUserName();

	/**
	 *  
	 * 	 The user Id that was used to run the session.
	 *
	 * @return the user name
	 */
	String getUserName();

	/**
	 *  
	 * 	 The domain of the user id that was used to run the session.
	 *
	 * @return the user domain name
	 */
	String getUserDomainName();

	/**
	 *  
	 * 	 The version information of the installed operating system (without service pack or patches).
	 *
	 * @return the OS version
	 */
	Version getOSVersion();

	/**
	 *  
	 * 	 The operating system service pack, if any.
	 *
	 * @return the OS service pack
	 */
	String getOSServicePack();

	/**
	 *  
	 * 	 The culture name of the underlying operating system installation.
	 *
	 * @return the OS culture name
	 */
	String getOSCultureName();

	/**
	 *  
	 * 	 The processor architecture of the operating system.
	 *
	 * @return the OS architecture
	 */
	ProcessorArchitecture getOSArchitecture();

	/**
	 *  
	 * 	 The boot mode of the operating system.
	 *
	 * @return the OS boot mode
	 */
	OSBootMode getOSBootMode();

	/**
	 *  
	 * 	 The OS Platform code, nearly always 1 indicating Windows NT.
	 *
	 * @return the OS platform code
	 */
	int getOSPlatformCode();

	/**
	 *  
	 * 	 The OS product type code, used to differentiate specific editions of various operating systems.
	 *
	 * @return the OS product type
	 */
	int getOSProductType();

	/**
	 *  
	 * 	 The OS Suite Mask, used to differentiate specific editions of various operating systems.
	 *
	 * @return the OS suite mask
	 */
	int getOSSuiteMask();

	/**
	 *  
	 * 	 The well known operating system family name, like Windows Vista or Windows Server 2003.
	 *
	 * @return the OS family name
	 */
	String getOSFamilyName();

	/**
	 *  
	 * 	 The edition of the operating system without the family name, such as Workstation or Standard Server.
	 *
	 * @return the OS edition name
	 */
	String getOSEditionName();

	/**
	 *  
	 * 	 The well known OS name and edition name.
	 *
	 * @return the OS full name
	 */
	String getOSFullName();

	/**
	 *  
	 * 	 The well known OS name, edition name, and service pack like Windows XP Professional Service Pack 3.
	 *
	 * @return the OS full name with service pack
	 */
	String getOSFullNameWithServicePack();

	/**
	 *  
	 * 	 The version of the .NET runtime that the application domain is running as.
	 *
	 * @return the runtime version
	 */
	Version getRuntimeVersion();

	/**
	 *  
	 * 	 The processor architecture the process is running as.
	 *
	 * @return the runtime architecture
	 */
	ProcessorArchitecture getRuntimeArchitecture();

	/**
	 *  
	 * 	 The current application culture name.
	 *
	 * @return the current culture name
	 */
	String getCurrentCultureName();

	/**
	 *  
	 * 	 The current user interface culture name.
	 *
	 * @return the current UI culture name
	 */
	String getCurrentUICultureName();

	/**
	 *  
	 * 	 The number of megabytes of installed memory in the host computer.
	 *
	 * @return the memory MB
	 */
	int getMemoryMB();

	/**
	 *  
	 * 	 The number of physical processor sockets in the host computer.
	 *
	 * @return the processors
	 */
	int getProcessors();

	/**
	 *  
	 * 	 The total number of processor cores in the host computer.
	 *
	 * @return the processor cores
	 */
	int getProcessorCores();

	/**
	 *  
	 * 	 Indicates if the session was run in a user interactive mode.
	 *
	 * @return the user interactive
	 */
	boolean getUserInteractive();

	/**
	 *  
	 * 	 Indicates if the session was run through terminal server.  Only applies to User Interactive sessions.
	 *
	 * @return the terminal server
	 */
	boolean getTerminalServer();

	/**
	 *  
	 * 	 The number of pixels wide of the virtual desktop.
	 *
	 * @return the screen width
	 */
	int getScreenWidth();

	/**
	 *  
	 * 	 The number of pixels tall for the virtual desktop.
	 *
	 * @return the screen height
	 */
	int getScreenHeight();

	/**
	 *  
	 * 	 The number of bits of color depth.
	 *
	 * @return the color depth
	 */
	int getColorDepth();

	/**
	 *  
	 * 	 The complete command line used to execute the process including arguments.
	 *
	 * @return the command line
	 */
	String getCommandLine();

	/**
	 *  
	 * 	 Optional. The unique tracking Id of the computer that recorded this session.
	 *
	 * @return the computer id
	 */
	UUID getComputerId();

	/**
	 *  
	 * 	 The final status of the session.
	 *
	 * @return the status
	 */
	SessionStatus getStatus();

	/**
	 *  
	 * 	 The number of messages in the messages collection.
	 * 	 
	 * 	 This value is cached for high performance and reflects all of the known messages.  If only part
	 * 	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 * 	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 * 	 files are missing.
	 *
	 * @return the message count
	 */
	int getMessageCount();

	/**
	 *  
	 * 	 The number of critical messages in the messages collection.
	 * 	 
	 * 	 This value is cached for high performance and reflects all of the known messages.  If only part
	 * 	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 * 	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 * 	 files are missing.
	 *
	 * @return the critical count
	 */
	int getCriticalCount();

	/**
	 *  
	 * 	 The number of error messages in the messages collection.
	 * 	 
	 * 	 This value is cached for high performance and reflects all of the known messages.  If only part
	 * 	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 * 	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 * 	 files are missing.
	 *
	 * @return the error count
	 */
	int getErrorCount();

	/**
	 *  
	 * 	 The number of warning messages in the messages collection.
	 * 	 
	 * 	 This value is cached for high performance and reflects all of the known messages.  If only part
	 * 	 of the files for a session are loaded, the totals as of the latest file loaded are used.  This means the
	 * 	 count of items may exceed the actual number of matching messages in the messages collection if earlier
	 * 	 files are missing.
	 *
	 * @return the warning count
	 */
	int getWarningCount();

	/**
	 *  
	 * 	 A copy of the collection of application specific properties. (Set via configuration at logging startup.  Do not modify here.)
	 *
	 * @return the properties
	 */
	Map<String, String> getProperties();

}