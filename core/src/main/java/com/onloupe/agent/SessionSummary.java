package com.onloupe.agent;

import java.awt.DisplayMode;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.PublisherConfiguration;
import com.onloupe.core.data.PathManager;
import com.onloupe.core.data.PathType;
import com.onloupe.core.monitor.LocalRepository;
import com.onloupe.core.serialization.monitor.LogMessagePacket;
import com.onloupe.core.serialization.monitor.SessionSummaryPacket;
import com.onloupe.core.util.SystemUtils;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ProcessorArchitecture;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.session.SessionStatus;
import com.onloupe.model.system.ApplicationType;
import com.onloupe.model.system.OSBootMode;
import com.onloupe.model.system.Version;


/**
 * Summary information about the entire session.
 * 
 * This information is available from sessions without loading the entire
 * session into memory.
 */
public class SessionSummary implements ISessionSummary {
	/**
	 * A default value for when the product name is unknown.
	 */
	public static final String UNKNOWN_PRODUCT = "Unknown Product";

	/**
	 * A default value for when the application name is unknown.
	 */
	public static final String UNKNOWN_APPLICATION = "Unknown Application";

	/** The is live. */
	private boolean isLive;
	
	/** The packet. */
	private SessionSummaryPacket packet;
	
	/** The critical count. */
	private int criticalCount;
	
	/** The error count. */
	private int errorCount;
	
	/** The warning count. */
	private int warningCount;
	
	/** The message count. */
	private int messageCount;
	
	/** The session status. */
	volatile private SessionStatus sessionStatus;
	
	/** The agent app type. */
	private ApplicationType agentAppType;

	/** The privacy enabled. */
	private boolean privacyEnabled;

	/**
	 * Create a new session summary as the live collection session for the current
	 * process
	 * 
	 * This constructor figures out all of the summary information when invoked,
	 * which can take a moment.
	 *
	 * @param configuration the configuration
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SessionSummary(AgentConfiguration configuration) throws IOException {
		this.isLive = true;
		this.packet = new SessionSummaryPacket();
		this.sessionStatus = SessionStatus.RUNNING;

		this.privacyEnabled = configuration.getPublisher().getEnableAnonymousMode();
		InetAddress host;
		try {
			host = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			throw e1;
		}

		try {
			this.packet.setID(UUID.randomUUID());
			this.packet.setCaption(null);

			// this stuff all tends to succeed
			if (this.privacyEnabled) {
				this.packet.setUserName("");
				this.packet.setUserDomainName("");
			} else if (SystemUtils.isWindows()) {
				this.packet.setUserName(TypeUtils.trimToEmpty(SystemUtils.getUserName()));
				this.packet.setUserDomainName(TypeUtils.trimToEmpty(SystemUtils.getSystemProperty("USERDOMAIN")));
			} else {
				this.packet.setUserName(TypeUtils.trimToEmpty(SystemUtils.getUserName()));
				this.packet.setUserDomainName(TypeUtils.trimToEmpty(host.getHostName()));
			}

			this.packet.setTimeZoneCaption(SystemUtils.getUserTimezone());
			this.packet.setEndDateTime(getStartDateTime()); // we want to ALWAYS have an end time, and since we just
															// created
			// our start time we need to move that over to end time

			// this stuff, on the other hand, doesn't always succeed

			// Lets see if the user has already picked some things for us...
			PublisherConfiguration publisherConfig = configuration.getPublisher();
			String productName = null, applicationName = null, applicationDescription = null;
			Version applicationVersion = null;

			getApplicationType();
			// what kind of process are we?
			
			this.agentAppType = publisherConfig.getApplicationType();
			
			if (this.agentAppType == ApplicationType.UNKNOWN) {
				if (SystemUtils.isWindows()) {
					this.agentAppType = ApplicationType.WINDOWS;
				}
			}

			this.packet.setApplicationType(this.agentAppType); // Finally, set the application type from our determined
																// type.

			// OK, now apply configuration overrides or what we discovered...
			this.packet.setProductName(TypeUtils.isBlank(publisherConfig.getProductName()) ? productName
					: publisherConfig.getProductName());
			this.packet.setApplicationName(TypeUtils.isBlank(publisherConfig.getApplicationName()) ? applicationName
					: publisherConfig.getApplicationName());
			this.packet.setApplicationVersion(
					(publisherConfig.getApplicationVersion() != null) ? publisherConfig.getApplicationVersion()
							: applicationVersion);
			this.packet.setApplicationDescription(
					TypeUtils.isBlank(publisherConfig.getApplicationDescription()) ? applicationDescription
							: publisherConfig.getApplicationDescription());
			this.packet.setEnvironmentName(publisherConfig.getEnvironmentName());
			this.packet.setPromotionLevelName(publisherConfig.getPromotionLevelName());

			// Finally, no nulls allowed! Fix any...
			this.packet.setProductName(
					TypeUtils.isBlank(this.packet.getProductName()) ? "Unknown" : this.packet.getProductName());
			this.packet.setApplicationName(TypeUtils.isBlank(this.packet.getApplicationName()) ? "Unknown"
					: this.packet.getApplicationName());
			Version tempVar = this.packet.getApplicationVersion();
			this.packet.setApplicationVersion((tempVar != null) ? tempVar : new Version(0, 0));
			String tempVar2 = this.packet.getApplicationDescription();
			this.packet.setApplicationDescription((tempVar2 != null) ? tempVar2 : "");
			String tempVar3 = this.packet.getEnvironmentName();
			this.packet.setEnvironmentName((tempVar3 != null) ? tempVar3 : "");
			String tempVar4 = this.packet.getPromotionLevelName();
			this.packet.setPromotionLevelName((tempVar4 != null) ? tempVar4 : "");

			this.packet.setComputerId(getComputerIdSafe(this.packet.getProductName(), configuration));
			this.packet.setAgentVersion(getAgentVersionSafe());
		} catch (RuntimeException ex) {
			// we really don't want an init error to fail us, not here!

		}

		if (!this.privacyEnabled) {
			try {
				this.packet.setHostName(host.getHostName());
				this.packet.setDnsDomainName((host.getCanonicalHostName() != null) ? host.getCanonicalHostName() : "");
			} catch (java.lang.Exception e) {
				// fallback to environment names
				try {
					this.packet.setHostName(host.getHostName());
				} catch (RuntimeException ex) {
					// we really don't want an init error to fail us, not here!

					this.packet.setHostName("unknown");
				}
				this.packet.setDnsDomainName("");
			}
		} else {
			// Privacy mode. Don't store "personally-identifying information".
			this.packet.setHostName("anonymous");
			this.packet.setDnsDomainName("");
		}

		// TODO KENDALL - We want to add in the OS information if we can feasibly get
		// it; this was dropped for .NET Core which is why it's missing here.
		if (SystemUtils.isWindows()) {
			this.packet.setOSPlatformCode(2); // Win32NT
			this.packet.setOSServicePack(""); // BUG
		} else if (SystemUtils.isLinux()) {
			this.packet.setOSPlatformCode(4); // Unix
			this.packet.setOSServicePack(""); // BUG
		} else if (SystemUtils.isMacOsX()) {
			this.packet.setOSPlatformCode(6); // OSX
			this.packet.setOSServicePack(""); // BUG
		}
		
		this.packet.setOSVersion(new Version(SystemUtils.getOsVersion()));

		try {
			String arch = SystemUtils.getOsArch();
			if (TypeUtils.isNotBlank(arch)) {
				ProcessorArchitecture architecture = arch.endsWith("64") ? ProcessorArchitecture.AMD64
						: ProcessorArchitecture.X86;
				this.packet.setOSArchitecture(architecture);
				this.packet.setRuntimeArchitecture(architecture);
			}
			
			this.packet.setOSCultureName(Locale.getDefault().getDisplayName());
			this.packet.setCurrentCultureName(Locale.getDefault().getDisplayName());
			this.packet.setCurrentUICultureName(Locale.getDefault().getDisplayName());

			this.packet.setOSBootMode(OSBootMode.NORMAL);
			this.packet.setRuntimeVersion(new Version(SystemUtils.getJavaVersion()));

			this.packet.setProcessors(Runtime.getRuntime().availableProcessors());
			this.packet.setProcessorCores(this.packet.getProcessors()); // BUG
			this.packet.setMemoryMB(SystemUtils.getTotalMemory());
			this.packet.setUserInteractive(false);

			// find the active screen resolution
			DisplayMode displayMode = SystemUtils.getDisplayMode();
			if (displayMode != null) {
				this.packet.setTerminalServer(false);
				this.packet.setColorDepth(displayMode.getBitDepth());
				this.packet.setScreenHeight(Double.valueOf(displayMode.getHeight()).intValue());
				this.packet.setScreenWidth(Double.valueOf(displayMode.getWidth()).intValue());
			}

			if (this.privacyEnabled) {
				this.packet.setCommandLine("");
			} else {
				try (StringWriter writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer)){
					System.getProperties().list(printWriter);
					this.packet.setCommandLine(writer.getBuffer().toString());
				}
			}
		} catch (Exception ex) {
			// we really don't want an init error to fail us, not here!
		}

		// now do user defined properties
		try {
			for (Map.Entry<Object, Object> keyValuePair : configuration.getProperties().entrySet()) {
				this.packet.getProperties().put(keyValuePair.getKey().toString(), keyValuePair.getValue().toString());
			}
		} catch (Exception ex) {
			// we aren't expecting any errors, but best be safe.

		}

		this.packet.setCaption(this.packet.getApplicationName());
	}

	/**
	 * Instantiates a new session summary.
	 *
	 * @param packet the packet
	 */
	public SessionSummary(SessionSummaryPacket packet) {
		if (packet == null) {
			throw new NullPointerException("packet");
		}

		this.packet = packet;
		this.sessionStatus = SessionStatus.UNKNOWN; // it should be set for us in a minute...
	}

	/**
	 * Overrides the native recorded product and application information with the
	 * specified values to reflect the server rules.
	 *
	 * @param productName the product name
	 * @param applicationName the application name
	 * @param applicationVersion the application version
	 * @param environmentName the environment name
	 * @param promotionLevelName the promotion level name
	 */
	public final void applyMappingOverrides(String productName, String applicationName, Version applicationVersion,
			String environmentName, String promotionLevelName) {
		this.packet.setProductName(productName);
		this.packet.setApplicationName(applicationName);
		this.packet.setApplicationVersion(applicationVersion);
		this.packet.setEnvironmentName(environmentName);
		this.packet.setPromotionLevelName(promotionLevelName);

		this.packet.setCaption(applicationName); // this is what the packet constructor does.
	}

	/**
	 * The unique Id of the session.
	 *
	 * @return the id
	 */
	@Override
	public final UUID getId() {
		return this.packet.getID();
	}

	/**
	 * The link to this item on the server.
	 *
	 * @return the uri
	 */
	@Override
	public final URI getUri() {
		throw new UnsupportedOperationException("Links are not supported in this context");
	}

	/**
	 * Indicates if the session has ever been viewed or exported.
	 *
	 * @return true, if is new
	 */
	@Override
	public final boolean isNew() {
		return true;
	}

	/**
	 * Indicates if all of the session data is stored that is expected to be
	 * available.
	 *
	 * @return true, if is complete
	 */
	@Override
	public final boolean isComplete() {
		return true;
	}

	/**
	 * Indicates if session data is available.
	 * 
	 * The session summary can be transfered separately from the session details and
	 * isn't subject to pruning so it may be around long before or after the
	 * detailed data is.
	 *
	 * @return the checks for data
	 */
	@Override
	public final boolean getHasData() {
		return false;
	}

	/**
	 * The unique Id of the local computer.
	 *
	 * @return the computer id
	 */
	@Override
	public final UUID getComputerId() {
		return this.packet.getComputerId();
	}

	/**
	 * The display caption of the time zone where the session was recorded.
	 *
	 * @return the time zone caption
	 */
	@Override
	public final String getTimeZoneCaption() {
		return this.packet.getTimeZoneCaption();
	}

	/**
	 * The date and time the session started.
	 *
	 * @return the start date time
	 */
	@Override
	public final OffsetDateTime getStartDateTime() {
		return this.packet.getTimestamp();
	}

	/**
	 * The date and time the session started.
	 *
	 * @return the display start date time
	 */
	@Override
	public final OffsetDateTime getDisplayStartDateTime() {
		return getStartDateTime();
	}

	/**
	 * The date and time the session ended or was last confirmed running.
	 *
	 * @return the end date time
	 */
	@Override
	public final OffsetDateTime getEndDateTime() {
		if (this.isLive) {
			// we're the live session and still kicking - we haven't ended yet!
			this.packet.setEndDateTime(OffsetDateTime.now());
		}

		return this.packet.getEndDateTime();
	}

	/**
	 * Sets the end date time.
	 *
	 * @param value the new end date time
	 */
	public final void setEndDateTime(OffsetDateTime value) {
		this.packet.setEndDateTime(value);
	}

	/**
	 * The date and time the session ended or was last confirmed running in the time
	 * zone the user has requested for display.
	 *
	 * @return the display end date time
	 */
	@Override
	public final OffsetDateTime getDisplayEndDateTime() {
		return getEndDateTime();
	}

	/**
	 * The time range between the start and end of this session, or the last message
	 * logged if the session ended unexpectedly.
	 *
	 * @return the duration
	 */
	@Override
	public final Duration getDuration() {
		return Duration.between(getStartDateTime(), getEndDateTime());
	}

	/**
	 * The date and time the session was added to the repository.
	 *
	 * @return the added date time
	 */
	@Override
	public final OffsetDateTime getAddedDateTime() {
		return getStartDateTime();
	}

	/**
	 * The date and time the session was added to the repository in the time zone
	 * the user has requested for display.
	 *
	 * @return the display added date time
	 */
	@Override
	public final OffsetDateTime getDisplayAddedDateTime() {
		return getStartDateTime();
	}

	/**
	 * The date and time the session was added to the repository.
	 *
	 * @return the updated date time
	 */
	@Override
	public final OffsetDateTime getUpdatedDateTime() {
		return getEndDateTime();
	}

	/**
	 * The date and time the session header was last updated locally in the time
	 * zone the user has requested for display.
	 *
	 * @return the display updated date time
	 */
	@Override
	public final OffsetDateTime getDisplayUpdatedDateTime() {
		return getEndDateTime();
	}

	/**
	 * The time range between the start and end of this session, or the last message
	 * logged if the session ended unexpectedly. Formatted as a string in HH:MM:SS
	 * format.
	 *
	 * @return the duration short
	 */
	public final String getDurationShort() {
		String formattedDuration;

		Duration duration = getDuration();

		// we have to format it manually; I couldn't find anything built-in that would
		// format a Duration.
		if (duration.toDays() > 0) {
			// It spans at least a day, so put Days in front, too
			formattedDuration = String.format("%02d:%02d:%02d:%02d", duration.toDays(), duration.toHours(),
					duration.toMinutes(), TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
		} else {
			// It spans less than a day, so leave Days off
			formattedDuration = String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutes(),
					TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
		}

		return formattedDuration;
	}

	/**
	 * A display caption for the session.
	 *
	 * @return the caption
	 */
	@Override
	public final String getCaption() {
		return this.packet.getCaption();
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setCaption(String value) {
		if (!this.packet.getCaption().equals(value)) {
			this.packet.setCaption(value);
		}
	}

	/**
	 * The product name of the application that recorded the session.
	 *
	 * @return the product
	 */
	@Override
	public final String getProduct() {
		return this.packet.getProductName();
	}

	/**
	 * The title of the application that recorded the session.
	 *
	 * @return the application
	 */
	@Override
	public final String getApplication() {
		return this.packet.getApplicationName();
	}

	/**
	 * Optional. The environment this session is running in.
	 * 
	 * Environments are useful for categorizing sessions, for example to indicate
	 * the hosting environment. If a value is provided it will be carried with the
	 * session data to upstream servers and clients. If the corresponding entry does
	 * not exist it will be automatically created.
	 *
	 * @return the environment
	 */
	@Override
	public final String getEnvironment() {
		return this.packet.getEnvironmentName();
	}

	/**
	 * Optional. The promotion level of the session.
	 * 
	 * Promotion levels are useful for categorizing sessions, for example to
	 * indicate whether it was run in development, staging, or production. If a
	 * value is provided it will be carried with the session data to upstream
	 * servers and clients. If the corresponding entry does not exist it will be
	 * automatically created.
	 *
	 * @return the promotion level
	 */
	@Override
	public final String getPromotionLevel() {
		return this.packet.getPromotionLevelName();
	}

	/**
	 * The type of process the application ran as (as declared or detected for
	 * recording). (See AgentAppType for internal Agent use.)
	 *
	 * @return the application type
	 */
	@Override
	public final ApplicationType getApplicationType() {
		return this.packet.getApplicationType();
	}

	/**
	 * The type of process the application ran as (as seen by the Agent internally).
	 *
	 * @return the agent app type
	 */
	public final ApplicationType getAgentAppType() {
		return this.agentAppType;
	}

	/**
	 * The description of the application from its manifest.
	 *
	 * @return the application description
	 */
	@Override
	public final String getApplicationDescription() {
		return this.packet.getApplicationDescription();
	}

	/**
	 * The version of the application that recorded the session.
	 *
	 * @return the application version
	 */
	@Override
	public final Version getApplicationVersion() {
		return this.packet.getApplicationVersion();
	}

	/**
	 * The version of the Gibraltar Agent used to monitor the session.
	 *
	 * @return the agent version
	 */
	@Override
	public final Version getAgentVersion() {
		return this.packet.getAgentVersion();
	}

	/**
	 * The host name / NetBIOS name of the computer that recorded the session
	 * 
	 * Does not include the domain name portion of the fully qualified DNS name.
	 *
	 * @return the host name
	 */
	@Override
	public final String getHostName() {
		return this.packet.getHostName();
	}

	/**
	 * The DNS domain name of the computer that recorded the session. May be empty.
	 * 
	 * Does not include the host name portion of the fully qualified DNS name.
	 *
	 * @return the dns domain name
	 */
	@Override
	public final String getDnsDomainName() {
		return this.packet.getDnsDomainName();
	}

	/**
	 * The fully qualified user name of the user the application was run as.
	 *
	 * @return the fully qualified user name
	 */
	@Override
	public final String getFullyQualifiedUserName() {
		return this.packet.getFullyQualifiedUserName();
	}

	/**
	 * The user Id that was used to run the session.
	 *
	 * @return the user name
	 */
	@Override
	public final String getUserName() {
		return this.packet.getUserName();
	}

	/**
	 * The domain of the user id that was used to run the session.
	 *
	 * @return the user domain name
	 */
	@Override
	public final String getUserDomainName() {
		return this.packet.getUserDomainName();
	}

	/**
	 * The version information of the installed operating system (without service
	 * pack or patches).
	 *
	 * @return the OS version
	 */
	@Override
	public final Version getOSVersion() {
		return this.packet.getOSVersion();
	}

	/**
	 * The operating system service pack, if any.
	 *
	 * @return the OS service pack
	 */
	@Override
	public final String getOSServicePack() {
		return this.packet.getOSServicePack();
	}

	/**
	 * The culture name of the underlying operating system installation.
	 *
	 * @return the OS culture name
	 */
	@Override
	public final String getOSCultureName() {
		return this.packet.getOSCultureName();
	}

	/**
	 * The processor architecture of the operating system.
	 *
	 * @return the OS architecture
	 */
	@Override
	public final ProcessorArchitecture getOSArchitecture() {
		return this.packet.getOSArchitecture();
	}

	/**
	 * The boot mode of the operating system.
	 *
	 * @return the OS boot mode
	 */
	@Override
	public final OSBootMode getOSBootMode() {
		return this.packet.getOSBootMode();
	}

	/**
	 * The OS Platform code, nearly always 1 indicating Windows NT.
	 *
	 * @return the OS platform code
	 */
	@Override
	public final int getOSPlatformCode() {
		return this.packet.getOSPlatformCode();
	}

	/**
	 * The OS product type code, used to differentiate specific editions of various
	 * operating systems.
	 *
	 * @return the OS product type
	 */
	@Override
	public final int getOSProductType() {
		return this.packet.getOSProductType();
	}

	/**
	 * The OS Suite Mask, used to differentiate specific editions of various
	 * operating systems.
	 *
	 * @return the OS suite mask
	 */
	@Override
	public final int getOSSuiteMask() {
		return this.packet.getOSSuiteMask();
	}

	/**
	 * The well known operating system family name, like Windows Vista or Windows
	 * Server 2003.
	 *
	 * @return the OS family name
	 */
	@Override
	public final String getOSFamilyName() {
		return ""; // BUG
	}

	/**
	 * The edition of the operating system without the family name, such as
	 * Workstation or Standard Server.
	 *
	 * @return the OS edition name
	 */
	@Override
	public final String getOSEditionName() {
		return ""; // BUG
	}

	/**
	 * The well known OS name and edition name.
	 *
	 * @return the OS full name
	 */
	@Override
	public final String getOSFullName() {
		return ""; // BUG
	}

	/**
	 * The well known OS name, edition name, and service pack like Windows XP
	 * Professional Service Pack 3.
	 *
	 * @return the OS full name with service pack
	 */
	@Override
	public final String getOSFullNameWithServicePack() {
		return ""; // BUG
	}

	/**
	 * The version of the .NET runtime that the application domain is running as.
	 *
	 * @return the runtime version
	 */
	@Override
	public final Version getRuntimeVersion() {
		return this.packet.getRuntimeVersion();
	}

	/**
	 * The processor architecture the process is running as.
	 *
	 * @return the runtime architecture
	 */
	@Override
	public final ProcessorArchitecture getRuntimeArchitecture() {
		return this.packet.getRuntimeArchitecture();
	}

	/**
	 * The current application culture name.
	 *
	 * @return the current culture name
	 */
	@Override
	public final String getCurrentCultureName() {
		return this.packet.getCurrentCultureName();
	}

	/**
	 * The current user interface culture name.
	 *
	 * @return the current UI culture name
	 */
	@Override
	public final String getCurrentUICultureName() {
		return this.packet.getCurrentUICultureName();
	}

	/**
	 * The number of megabytes of installed memory in the host computer.
	 *
	 * @return the memory MB
	 */
	@Override
	public final int getMemoryMB() {
		return this.packet.getMemoryMB();
	}

	/**
	 * The number of physical processor sockets in the host computer.
	 *
	 * @return the processors
	 */
	@Override
	public final int getProcessors() {
		return this.packet.getProcessors();
	}

	/**
	 * The total number of processor cores in the host computer.
	 *
	 * @return the processor cores
	 */
	@Override
	public final int getProcessorCores() {
		return this.packet.getProcessorCores();
	}

	/**
	 * Indicates if the session was run in a user interactive mode.
	 *
	 * @return the user interactive
	 */
	@Override
	public final boolean getUserInteractive() {
		return this.packet.getUserInteractive();
	}

	/**
	 * Indicates if the session was run through terminal server. Only applies to
	 * User Interactive sessions.
	 *
	 * @return the terminal server
	 */
	@Override
	public final boolean getTerminalServer() {
		return this.packet.getTerminalServer();
	}

	/**
	 * The number of pixels wide of the virtual desktop.
	 *
	 * @return the screen width
	 */
	@Override
	public final int getScreenWidth() {
		return this.packet.getScreenWidth();
	}

	/**
	 * The number of pixels tall for the virtual desktop.
	 *
	 * @return the screen height
	 */
	@Override
	public final int getScreenHeight() {
		return this.packet.getScreenHeight();
	}

	/**
	 * The number of bits of color depth.
	 *
	 * @return the color depth
	 */
	@Override
	public final int getColorDepth() {
		return this.packet.getColorDepth();
	}

	/**
	 * The complete command line used to execute the process including arguments.
	 *
	 * @return the command line
	 */
	@Override
	public final String getCommandLine() {
		return this.packet.getCommandLine();
	}

	/**
	 * The final status of the session.
	 *
	 * @return the status
	 */
	@Override
	public final SessionStatus getStatus() {
		return this.sessionStatus;
	}

	/**
	 * Sets the status.
	 *
	 * @param value the new status
	 */
	public final void setStatus(SessionStatus value) {
		this.sessionStatus = value;
	}

	/**
	 * The number of messages in the messages collection.
	 * 
	 * This value is cached for high performance and reflects all of the known
	 * messages. If only part of the files for a session are loaded, the totals as
	 * of the latest file loaded are used. This means the count of items may exceed
	 * the actual number of matching messages in the messages collection if earlier
	 * files are missing.
	 *
	 * @return the message count
	 */
	@Override
	public final int getMessageCount() {
		return this.messageCount;
	}

	/**
	 * Sets the message count.
	 *
	 * @param value the new message count
	 */
	public final void setMessageCount(int value) {
		this.messageCount = value;
	}

	/**
	 * The number of critical messages in the messages collection.
	 * 
	 * This value is cached for high performance and reflects all of the known
	 * messages. If only part of the files for a session are loaded, the totals as
	 * of the latest file loaded are used. This means the count of items may exceed
	 * the actual number of matching messages in the messages collection if earlier
	 * files are missing.
	 *
	 * @return the critical count
	 */
	@Override
	public final int getCriticalCount() {
		return this.criticalCount;
	}

	/**
	 * Sets the critical count.
	 *
	 * @param value the new critical count
	 */
	public final void setCriticalCount(int value) {
		this.criticalCount = value;
	}

	/**
	 * The number of error messages in the messages collection.
	 * 
	 * This value is cached for high performance and reflects all of the known
	 * messages. If only part of the files for a session are loaded, the totals as
	 * of the latest file loaded are used. This means the count of items may exceed
	 * the actual number of matching messages in the messages collection if earlier
	 * files are missing.
	 *
	 * @return the error count
	 */
	@Override
	public final int getErrorCount() {
		return this.errorCount;
	}

	/**
	 * Sets the error count.
	 *
	 * @param value the new error count
	 */
	public final void setErrorCount(int value) {
		this.errorCount = value;
	}

	/**
	 * The number of error messages in the messages collection.
	 * 
	 * This value is cached for high performance and reflects all of the known
	 * messages. If only part of the files for a session are loaded, the totals as
	 * of the latest file loaded are used. This means the count of items may exceed
	 * the actual number of matching messages in the messages collection if earlier
	 * files are missing.
	 *
	 * @return the warning count
	 */
	@Override
	public final int getWarningCount() {
		return this.warningCount;
	}

	/**
	 * Sets the warning count.
	 *
	 * @param value the new warning count
	 */
	public final void setWarningCount(int value) {
		this.warningCount = value;
	}

	/**
	 * A collection of application specific properties.
	 *
	 * @return the properties
	 */
	@Override
	public final Map<String, String> getProperties() {
		return this.packet.getProperties();
	}

	/**
	 * Generates a reasonable default caption for the provided session that has no
	 * caption.
	 *
	 * @param sessionSummary The session summary object to generate a default
	 *                       caption for
	 * @return The default caption
	 */
	public static String defaultCaption(SessionSummary sessionSummary) {
		String defaultCaption = "";

		// We are currently shooting for <appname> <Short Date> <Short time>
		if (TypeUtils.isBlank(sessionSummary.getApplication())) {
			defaultCaption += "(Unknown app)";
		} else {
			// we want to truncate the application if it's over a max length
			if (sessionSummary.getApplication().length() > 32) {
				defaultCaption += sessionSummary.getApplication().substring(0, 32);
			} else {
				defaultCaption += sessionSummary.getApplication();
			}
		}

		defaultCaption += " " + String.format("%d", sessionSummary.getStartDateTime());

		defaultCaption += " " + sessionSummary.getStartDateTime().toString();

		return defaultCaption;
	}

	/**
	 * Gets the packet.
	 *
	 * @return the packet
	 */
	public final SessionSummaryPacket getPacket() {
		return this.packet;
	}

	/**
	 * Gets the privacy enabled.
	 *
	 * @return the privacy enabled
	 */
	public final boolean getPrivacyEnabled() {
		return this.privacyEnabled;
	}

	/**
	 * Inspect the provided packet to update relevant statistics.
	 *
	 * @param packet A Log message packet to count
	 */
	public final void updateMessageStatistics(LogMessagePacket packet) {
		this.messageCount++;

		switch (packet.getSeverity()) {
		case CRITICAL:
			this.criticalCount++;
			break;
		case ERROR:
			this.errorCount++;
			break;
		case WARNING:
			this.warningCount++;
			break;
		default:
			break;
		}
	}

	/**
	 * Clear the existing statistic counters
	 * 
	 * Typically used before the messages are recounted to ensure they can be
	 * correctly updated.
	 */
	public final void clearMessageStatistics() {
		this.messageCount = 0;
		this.criticalCount = 0;
		this.errorCount = 0;
		this.warningCount = 0;
	}

	/**
	 * Gets the agent version safe.
	 *
	 * @return the agent version safe
	 */
	private static Version getAgentVersionSafe() {
		Version version = new Version(4, 0);

		return version;
	}

	/**
	 * Gets the computer id safe.
	 *
	 * @param product the product
	 * @param configuration the configuration
	 * @return the computer id safe
	 */
	private static UUID getComputerIdSafe(String product, AgentConfiguration configuration) {
		UUID computerId = null; // we can't fail, this is a good default value since upstream items will treat
								// it as a "don't know"
		try {
			// first see if we have a GUID file in the system-wide location.
			String preferredPath = PathManager.findBestPath(PathType.COLLECTION);
			File computerIdFile = Paths.get(preferredPath).resolve(LocalRepository.COMPUTER_KEY_FILE).toFile();

			if (!computerIdFile.isFile()) {
				// see if we have a repository Id we should copy...
				String repositoryPath = LocalRepository.calculateRepositoryPath(product,
						configuration.getSessionFile().getFolder());
				String repositoryIdFile = Paths.get(repositoryPath).resolve(LocalRepository.REPOSITORY_KEY_FILE)
						.toString();
				if ((new File(repositoryIdFile)).isFile()) {
					// try to copy it as a candidate..
					try {
						Files.copy(Paths.get(repositoryIdFile), computerIdFile.toPath(),
								StandardCopyOption.COPY_ATTRIBUTES);
					} catch (Exception ex) {

					}
				}
			} else {
				// read back the existing repository id
				String rawComputerId = new String(Files.readAllBytes(computerIdFile.toPath()), StandardCharsets.UTF_8);
				computerId = UUID.fromString(rawComputerId);
			}

			// create a new repository id
			if (computerId == null) {
				computerId = UUID.randomUUID();
				Files.write(computerIdFile.toPath(), computerId.toString().getBytes(),
						StandardOpenOption.TRUNCATE_EXISTING);
				Files.setAttribute(computerIdFile.toPath(), "dos:hidden", true);
			}
		} catch (RuntimeException | IOException ex) {

			computerId = null; // we shouldn't trust anything we have- it's probably a dynamically created id.
		}

		return computerId;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.model.session.ISessionSummary#isLive()
	 */
	@Override
	public boolean isLive() {
		return isLive;
	}

}