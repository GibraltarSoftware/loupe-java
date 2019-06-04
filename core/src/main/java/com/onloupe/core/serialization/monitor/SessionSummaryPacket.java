package com.onloupe.core.serialization.monitor;

import com.onloupe.core.data.SessionHeader;
import com.onloupe.core.serialization.FieldType;
import com.onloupe.core.serialization.IPacket;
import com.onloupe.core.serialization.PacketDefinition;
import com.onloupe.core.serialization.SerializedPacket;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ProcessorArchitecture;
import com.onloupe.model.system.ApplicationType;
import com.onloupe.model.system.OSBootMode;
import com.onloupe.model.system.Version;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class SessionSummaryPacket.
 */
public class SessionSummaryPacket extends GibraltarCachedPacket implements IPacket {
	
	/** The computer id. */
	// Stuff that aligns with SESSION table in index
	private UUID computerId;

	/** The product name. */
	private String productName;
	
	/** The application name. */
	private String applicationName;
	
	/** The environment name. */
	private String environmentName;
	
	/** The promotion level name. */
	private String promotionLevelName;
	
	/** The application version. */
	private Version applicationVersion;
	
	/** The application type. */
	private ApplicationType applicationType;
	
	/** The application description. */
	private String applicationDescription;
	
	/** The caption. */
	private String caption;
	
	/** The time zone caption. */
	private String timeZoneCaption;
	
	/** The end date time. */
	private OffsetDateTime endDateTime;
	
	/** The agent version. */
	private Version agentVersion;
	
	/** The user name. */
	private String userName;
	
	/** The user domain name. */
	private String userDomainName;
	
	/** The host name. */
	private String hostName;
	
	/** The dns domain name. */
	private String dnsDomainName;

	/** The os platform code. */
	// Stuff that aligns with SESSION_DETAILS table in index
	private int osPlatformCode;

	/** The os version. */
	private Version osVersion;
	
	/** The os service pack. */
	private String osServicePack;
	
	/** The os culture name. */
	private String osCultureName;
	
	/** The os architecture. */
	private ProcessorArchitecture osArchitecture;
	
	/** The os boot mode. */
	private OSBootMode osBootMode;
	
	/** The os suite mask code. */
	private int osSuiteMaskCode;
	
	/** The os product type code. */
	private int osProductTypeCode;
	
	/** The runtime version. */
	private Version runtimeVersion;
	
	/** The runtime architecture. */
	private ProcessorArchitecture runtimeArchitecture;
	
	/** The current culture name. */
	private String currentCultureName;
	
	/** The current UI culture name. */
	private String currentUICultureName;
	
	/** The memory MB. */
	private int memoryMB;
	
	/** The processors. */
	private int processors;
	
	/** The processor cores. */
	private int processorCores;
	
	/** The user interactive. */
	private boolean userInteractive;
	
	/** The terminal server. */
	private boolean terminalServer;
	
	/** The screen width. */
	private int screenWidth;
	
	/** The screen height. */
	private int screenHeight;
	
	/** The color depth. */
	private int colorDepth;
	
	/** The command line. */
	private String commandLine;

	/** The properties. */
	// App.Config properties
	private final Map<String, String> properties = new HashMap<>();

	/** The fully qualified user name. */
	// CALCULATED value
	private String fullyQualifiedUserName;

	/**
	 * Instantiates a new session summary packet.
	 */
	public SessionSummaryPacket() {
		super(true);
	}

	/**
	 * Create a session summary packet from the provided session header.
	 *
	 * @param sessionHeader the session header
	 */
	public SessionSummaryPacket(SessionHeader sessionHeader) {
		super(sessionHeader.getId(), true);
		// Stuff that aligns with SESSION table in index
		this.computerId = sessionHeader.getComputerId();
		setTimestamp(sessionHeader.getStartDateTime());
		this.endDateTime = sessionHeader.getEndDateTime();
		this.caption = sessionHeader.getCaption();
		this.timeZoneCaption = sessionHeader.getTimeZoneCaption();
		this.productName = sessionHeader.getProduct();
		this.applicationName = sessionHeader.getApplication();
		this.environmentName = sessionHeader.getEnvironment();
		this.promotionLevelName = sessionHeader.getPromotionLevel();
		
		ApplicationType applicationType = ApplicationType.valueOf(sessionHeader.getApplicationTypeName());
		this.applicationType = applicationType != null ? applicationType : ApplicationType.UNKNOWN;
		
		this.applicationDescription = sessionHeader.getApplicationDescription();
		this.applicationVersion = sessionHeader.getApplicationVersion();
		this.agentVersion = sessionHeader.getAgentVersion();
		this.userName = sessionHeader.getUserName();
		this.userDomainName = sessionHeader.getUserDomainName();
		this.hostName = sessionHeader.getHostName();
		this.dnsDomainName = sessionHeader.getDnsDomainName();

		// Stuff that aligns with SESSION_DETAILS table in index
		this.osPlatformCode = sessionHeader.getOSPlatformCode();
		this.osVersion = sessionHeader.getOSVersion();
		this.osServicePack = sessionHeader.getOSServicePack();
		this.osCultureName = sessionHeader.getOSCultureName();
		this.osArchitecture = sessionHeader.getOSArchitecture();
		this.osBootMode = sessionHeader.getOSBootMode();
		this.osSuiteMaskCode = sessionHeader.getOSSuiteMask();
		this.osProductTypeCode = sessionHeader.getOSProductType();
		this.runtimeVersion = sessionHeader.getRuntimeVersion();
		this.runtimeArchitecture = sessionHeader.getRuntimeArchitecture();
		this.currentCultureName = sessionHeader.getCurrentCultureName();
		this.currentUICultureName = sessionHeader.getCurrentUICultureName();
		this.memoryMB = sessionHeader.getMemoryMB();
		this.processors = sessionHeader.getProcessors();
		this.processorCores = sessionHeader.getProcessorCores();
		this.userInteractive = sessionHeader.getUserInteractive();
		this.terminalServer = sessionHeader.getTerminalServer();
		this.screenWidth = sessionHeader.getScreenWidth();
		this.screenHeight = sessionHeader.getScreenHeight();
		this.colorDepth = sessionHeader.getColorDepth();
		this.commandLine = sessionHeader.getCommandLine();

		// and app.config properties.
		this.properties.clear();
		this.properties.putAll(new HashMap<>(sessionHeader.getProperties()));

		// finally calculated value.
		calculateFullyQualifiedUserName();
	}

	/**
	 * The unique Id of the local computer.
	 *
	 * @return the computer id
	 */
	public final UUID getComputerId() {
		return this.computerId;
	}

	/**
	 * Sets the computer id.
	 *
	 * @param value the new computer id
	 */
	public final void setComputerId(UUID value) {
		this.computerId = value;
	}

	/**
	 * Gets the end date time.
	 *
	 * @return the end date time
	 */
	public final OffsetDateTime getEndDateTime() {
		return this.endDateTime;
	}

	/**
	 * Sets the end date time.
	 *
	 * @param value the new end date time
	 */
	public final void setEndDateTime(OffsetDateTime value) {
		this.endDateTime = value;
	}

	/**
	 * Gets the caption.
	 *
	 * @return the caption
	 */
	public final String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public final void setCaption(String value) {
		this.caption = setSafeStringValue(value, 1024);
	}

	/**
	 * Gets the time zone caption.
	 *
	 * @return the time zone caption
	 */
	public final String getTimeZoneCaption() {
		return this.timeZoneCaption;
	}

	/**
	 * Sets the time zone caption.
	 *
	 * @param value the new time zone caption
	 */
	public final void setTimeZoneCaption(String value) {
		this.timeZoneCaption = setSafeStringValue(value, 120);
	}

	/**
	 * Gets the product name.
	 *
	 * @return the product name
	 */
	public final String getProductName() {
		return this.productName;
	}

	/**
	 * Sets the product name.
	 *
	 * @param value the new product name
	 */
	public final void setProductName(String value) {
		this.productName = setSafeStringValue(value, 120);
	}

	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
	public final String getApplicationName() {
		return this.applicationName;
	}

	/**
	 * Sets the application name.
	 *
	 * @param value the new application name
	 */
	public final void setApplicationName(String value) {
		this.applicationName = setSafeStringValue(value, 120);
	}

	/**
	 * Gets the environment name.
	 *
	 * @return the environment name
	 */
	public final String getEnvironmentName() {
		return this.environmentName;
	}

	/**
	 * Sets the environment name.
	 *
	 * @param value the new environment name
	 */
	public final void setEnvironmentName(String value) {
		this.environmentName = setSafeStringValue(value, 120);
	}

	/**
	 * Gets the promotion level name.
	 *
	 * @return the promotion level name
	 */
	public final String getPromotionLevelName() {
		return this.promotionLevelName;
	}

	/**
	 * Sets the promotion level name.
	 *
	 * @param value the new promotion level name
	 */
	public final void setPromotionLevelName(String value) {
		this.promotionLevelName = setSafeStringValue(value, 120);
	}

	/**
	 * Gets the application type.
	 *
	 * @return the application type
	 */
	public final ApplicationType getApplicationType() {
		return this.applicationType;
	}

	/**
	 * Sets the application type.
	 *
	 * @param value the new application type
	 */
	public final void setApplicationType(ApplicationType value) {
		this.applicationType = value;
	}

	/**
	 * Gets the application description.
	 *
	 * @return the application description
	 */
	public final String getApplicationDescription() {
		return this.applicationDescription;
	}

	/**
	 * Sets the application description.
	 *
	 * @param value the new application description
	 */
	public final void setApplicationDescription(String value) {
		this.applicationDescription = setSafeStringValue(value, 1024);
	}

	/**
	 * The version of the application that recorded the session.
	 *
	 * @return the application version
	 */
	public final Version getApplicationVersion() {
		return this.applicationVersion;
	}

	/**
	 * Sets the application version.
	 *
	 * @param value the new application version
	 */
	public final void setApplicationVersion(Version value) {
		this.applicationVersion = value;
	}

	/**
	 * Gets the agent version.
	 *
	 * @return the agent version
	 */
	public final Version getAgentVersion() {
		return this.agentVersion;
	}

	/**
	 * Sets the agent version.
	 *
	 * @param value the new agent version
	 */
	public final void setAgentVersion(Version value) {
		this.agentVersion = value;
	}

	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
	public final String getHostName() {
		return this.hostName;
	}

	/**
	 * Sets the host name.
	 *
	 * @param value the new host name
	 */
	public final void setHostName(String value) {
		this.hostName = setSafeStringValue(value, 120);
	}

	/**
	 * Gets the dns domain name.
	 *
	 * @return the dns domain name
	 */
	public final String getDnsDomainName() {
		return this.dnsDomainName;
	}

	/**
	 * Sets the dns domain name.
	 *
	 * @param value the new dns domain name
	 */
	public final void setDnsDomainName(String value) {
		this.dnsDomainName = setSafeStringValue(value, 512);
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public final String getUserName() {
		return this.userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param value the new user name
	 */
	public final void setUserName(String value) {
		this.userName = setSafeStringValue(value, 120);
		calculateFullyQualifiedUserName();
	}

	/**
	 * Gets the user domain name.
	 *
	 * @return the user domain name
	 */
	public final String getUserDomainName() {
		return this.userDomainName;
	}

	/**
	 * Sets the user domain name.
	 *
	 * @param value the new user domain name
	 */
	public final void setUserDomainName(String value) {
		this.userDomainName = setSafeStringValue(value, 50);
		calculateFullyQualifiedUserName();
	}

	/**
	 * Gets the OS platform code.
	 *
	 * @return the OS platform code
	 */
	public final int getOSPlatformCode() {
		return this.osPlatformCode;
	}

	/**
	 * Sets the OS platform code.
	 *
	 * @param value the new OS platform code
	 */
	public final void setOSPlatformCode(int value) {
		this.osPlatformCode = value;
	}

	/**
	 * Gets the OS version.
	 *
	 * @return the OS version
	 */
	public final Version getOSVersion() {
		return this.osVersion;
	}

	/**
	 * Sets the OS version.
	 *
	 * @param value the new OS version
	 */
	public final void setOSVersion(Version value) {
		this.osVersion = value;
	}

	/**
	 * Gets the OS service pack.
	 *
	 * @return the OS service pack
	 */
	public final String getOSServicePack() {
		return this.osServicePack;
	}

	/**
	 * Sets the OS service pack.
	 *
	 * @param value the new OS service pack
	 */
	public final void setOSServicePack(String value) {
		this.osServicePack = setSafeStringValue(value, 50);
	}

	/**
	 * Gets the OS culture name.
	 *
	 * @return the OS culture name
	 */
	public final String getOSCultureName() {
		return this.osCultureName;
	}

	/**
	 * Sets the OS culture name.
	 *
	 * @param value the new OS culture name
	 */
	public final void setOSCultureName(String value) {
		this.osCultureName = setSafeStringValue(value, 50);
	}

	/**
	 * Gets the OS architecture.
	 *
	 * @return the OS architecture
	 */
	public final ProcessorArchitecture getOSArchitecture() {
		return this.osArchitecture;
	}

	/**
	 * Sets the OS architecture.
	 *
	 * @param value the new OS architecture
	 */
	public final void setOSArchitecture(ProcessorArchitecture value) {
		this.osArchitecture = value;
	}

	/**
	 * Gets the OS boot mode.
	 *
	 * @return the OS boot mode
	 */
	public final OSBootMode getOSBootMode() {
		return this.osBootMode;
	}

	/**
	 * Sets the OS boot mode.
	 *
	 * @param value the new OS boot mode
	 */
	public final void setOSBootMode(OSBootMode value) {
		this.osBootMode = value;
	}

	/**
	 * Gets the OS suite mask.
	 *
	 * @return the OS suite mask
	 */
	public final int getOSSuiteMask() {
		return this.osSuiteMaskCode;
	}

	/**
	 * Sets the OS suite mask.
	 *
	 * @param value the new OS suite mask
	 */
	public final void setOSSuiteMask(int value) {
		this.osSuiteMaskCode = value;
	}

	/**
	 * Gets the OS product type.
	 *
	 * @return the OS product type
	 */
	public final int getOSProductType() {
		return this.osProductTypeCode;
	}

	/**
	 * Sets the OS product type.
	 *
	 * @param value the new OS product type
	 */
	public final void setOSProductType(int value) {
		this.osProductTypeCode = value;
	}

	/**
	 * Gets the runtime version.
	 *
	 * @return the runtime version
	 */
	public final Version getRuntimeVersion() {
		return this.runtimeVersion;
	}

	/**
	 * Sets the runtime version.
	 *
	 * @param value the new runtime version
	 */
	public final void setRuntimeVersion(Version value) {
		this.runtimeVersion = value;
	}

	/**
	 * Gets the runtime architecture.
	 *
	 * @return the runtime architecture
	 */
	public final ProcessorArchitecture getRuntimeArchitecture() {
		return this.runtimeArchitecture;
	}

	/**
	 * Sets the runtime architecture.
	 *
	 * @param value the new runtime architecture
	 */
	public final void setRuntimeArchitecture(ProcessorArchitecture value) {
		this.runtimeArchitecture = value;
	}

	/**
	 * Gets the current culture name.
	 *
	 * @return the current culture name
	 */
	public final String getCurrentCultureName() {
		return this.currentCultureName;
	}

	/**
	 * Sets the current culture name.
	 *
	 * @param value the new current culture name
	 */
	public final void setCurrentCultureName(String value) {
		this.currentCultureName = setSafeStringValue(value, 50);
	}

	/**
	 * Gets the current UI culture name.
	 *
	 * @return the current UI culture name
	 */
	public final String getCurrentUICultureName() {
		return this.currentUICultureName;
	}

	/**
	 * Sets the current UI culture name.
	 *
	 * @param value the new current UI culture name
	 */
	public final void setCurrentUICultureName(String value) {
		this.currentUICultureName = setSafeStringValue(value, 50);
	}

	/**
	 * Gets the memory MB.
	 *
	 * @return the memory MB
	 */
	public final int getMemoryMB() {
		return this.memoryMB;
	}

	/**
	 * Sets the memory MB.
	 *
	 * @param value the new memory MB
	 */
	public final void setMemoryMB(int value) {
		this.memoryMB = value;
	}

	/**
	 * Gets the processors.
	 *
	 * @return the processors
	 */
	public final int getProcessors() {
		return this.processors;
	}

	/**
	 * Sets the processors.
	 *
	 * @param value the new processors
	 */
	public final void setProcessors(int value) {
		this.processors = value;
	}

	/**
	 * Gets the processor cores.
	 *
	 * @return the processor cores
	 */
	public final int getProcessorCores() {
		return this.processorCores;
	}

	/**
	 * Sets the processor cores.
	 *
	 * @param value the new processor cores
	 */
	public final void setProcessorCores(int value) {
		this.processorCores = value;
	}

	/**
	 * Gets the user interactive.
	 *
	 * @return the user interactive
	 */
	public final boolean getUserInteractive() {
		return this.userInteractive;
	}

	/**
	 * Sets the user interactive.
	 *
	 * @param value the new user interactive
	 */
	public final void setUserInteractive(boolean value) {
		this.userInteractive = value;
	}

	/**
	 * Gets the terminal server.
	 *
	 * @return the terminal server
	 */
	public final boolean getTerminalServer() {
		return this.terminalServer;
	}

	/**
	 * Sets the terminal server.
	 *
	 * @param value the new terminal server
	 */
	public final void setTerminalServer(boolean value) {
		this.terminalServer = value;
	}

	/**
	 * Gets the screen width.
	 *
	 * @return the screen width
	 */
	public final int getScreenWidth() {
		return this.screenWidth;
	}

	/**
	 * Sets the screen width.
	 *
	 * @param value the new screen width
	 */
	public final void setScreenWidth(int value) {
		this.screenWidth = value;
	}

	/**
	 * Gets the screen height.
	 *
	 * @return the screen height
	 */
	public final int getScreenHeight() {
		return this.screenHeight;
	}

	/**
	 * Sets the screen height.
	 *
	 * @param value the new screen height
	 */
	public final void setScreenHeight(int value) {
		this.screenHeight = value;
	}

	/**
	 * Gets the color depth.
	 *
	 * @return the color depth
	 */
	public final int getColorDepth() {
		return this.colorDepth;
	}

	/**
	 * Sets the color depth.
	 *
	 * @param value the new color depth
	 */
	public final void setColorDepth(int value) {
		this.colorDepth = value;
	}

	/**
	 * Gets the command line.
	 *
	 * @return the command line
	 */
	public final String getCommandLine() {
		return this.commandLine;
	}

	/**
	 * Sets the command line.
	 *
	 * @param value the new command line
	 */
	public final void setCommandLine(String value) {
		this.commandLine = setSafeStringValue(value, 2048);
	}

	/**
	 * The fully qualified user name of the user the application was run as.
	 *
	 * @return the fully qualified user name
	 */
	public final String getFullyQualifiedUserName() {
		return this.fullyQualifiedUserName;
	}

	/**
	 * Application provided properties.
	 *
	 * @return the properties
	 */
	public final Map<String, String> getProperties() {
		return this.properties;
	}

	/** The Constant SERIALIZATION_VERSION. */
	private static final int SERIALIZATION_VERSION = 3;

	/**
	 * The list of packets that this packet depends on.
	 * 
	 * @return An array of IPackets, or null if there are no dependencies.
	 */
	@Override
	public final List<IPacket> getRequiredPackets() {
		return super.getRequiredPackets();
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writePacketDefinition(com.onloupe.core.serialization.PacketDefinition)
	 */
	@Override
	public void writePacketDefinition(PacketDefinition definition) {
		super.writePacketDefinition(definition.getParentIPacket());

		definition.setVersion(SERIALIZATION_VERSION);

		// Session index information
		definition.getFields().add("ComputerId", FieldType.GUID);
		definition.getFields().add("ProductName", FieldType.STRING);
		definition.getFields().add("ApplicationName", FieldType.STRING);
		definition.getFields().add("EnvironmentName", FieldType.STRING);
		definition.getFields().add("PromotionLevelName", FieldType.STRING);
		definition.getFields().add("ApplicationType", FieldType.INT);
		definition.getFields().add("ApplicationVersion", FieldType.STRING);
		definition.getFields().add("ApplicationDescription", FieldType.STRING);
		definition.getFields().add("Caption", FieldType.STRING);
		definition.getFields().add("TimeZoneCaption", FieldType.STRING);
		definition.getFields().add("EndDateTime", FieldType.DATE_TIME_OFFSET);
		definition.getFields().add("AgentVersion", FieldType.STRING);
		definition.getFields().add("UserName", FieldType.STRING);
		definition.getFields().add("UserDomainName", FieldType.STRING);
		definition.getFields().add("HostName", FieldType.STRING);
		definition.getFields().add("DNSDomainName", FieldType.STRING);

		// Session Details index information
		definition.getFields().add("OSPlatformCode", FieldType.INT);
		definition.getFields().add("OSVersion", FieldType.STRING);
		definition.getFields().add("OSServicePack", FieldType.STRING);
		definition.getFields().add("OSCultureName", FieldType.STRING);
		definition.getFields().add("OSArchitecture", FieldType.INT);
		definition.getFields().add("OSBootMode", FieldType.INT);
		definition.getFields().add("OSSuiteMaskCode", FieldType.INT);
		definition.getFields().add("OSProductTypeCode", FieldType.INT);
		definition.getFields().add("RuntimeVersion", FieldType.STRING);
		definition.getFields().add("RuntimeArchitecture", FieldType.INT);
		definition.getFields().add("CurrentCultureName", FieldType.STRING);
		definition.getFields().add("CurrentUICultureName", FieldType.STRING);
		definition.getFields().add("MemoryMB", FieldType.INT);
		definition.getFields().add("Processors", FieldType.INT);
		definition.getFields().add("ProcessorCores", FieldType.INT);
		definition.getFields().add("UserInteractive", FieldType.BOOL);
		definition.getFields().add("TerminalServer", FieldType.BOOL);
		definition.getFields().add("ScreenWidth", FieldType.INT);
		definition.getFields().add("ScreenHeight", FieldType.INT);
		definition.getFields().add("ColorDepth", FieldType.INT);
		definition.getFields().add("CommandLine", FieldType.STRING);

		// App.Config options
		for (Map.Entry<String, String> property : this.properties.entrySet()) {
			definition.getFields().add(property.getKey(), FieldType.STRING);
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#writeFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void writeFields(PacketDefinition definition, SerializedPacket packet) {
		super.writeFields(definition.getParentIPacket(), packet.getParentIPacket());

		// Write out session stuff
		packet.setField("ComputerId", this.computerId != null ? this.computerId : new UUID(0,0));
		packet.setField("ProductName", this.productName);
		packet.setField("ApplicationName", this.applicationName);
		packet.setField("EnvironmentName", this.environmentName);
		packet.setField("PromotionLevelName", this.promotionLevelName);
		packet.setField("ApplicationVersion", this.applicationVersion != null ? this.applicationVersion.toString() : null);
		packet.setField("ApplicationType", this.applicationType != null ? this.applicationType.getValue() : null);
		packet.setField("ApplicationDescription", this.applicationDescription);
		packet.setField("Caption", this.caption);
		packet.setField("TimeZoneCaption", this.timeZoneCaption);
		packet.setField("EndDateTime", this.endDateTime);
		packet.setField("AgentVersion", this.agentVersion != null ? this.agentVersion.toString() : null);
		packet.setField("UserName", this.userName);
		packet.setField("UserDomainName", this.userDomainName);
		packet.setField("HostName", this.hostName);
		packet.setField("DNSDomainName", this.dnsDomainName);

		// write out session details stuff
		packet.setField("OSPlatformCode", this.osPlatformCode);
		packet.setField("OSVersion", this.osVersion.toString());
		packet.setField("OSServicePack", this.osServicePack);
		packet.setField("OSCultureName", this.osCultureName);
		packet.setField("OSArchitecture", this.osArchitecture != null ? this.osArchitecture.getValue() : null);
		packet.setField("OSBootMode", this.osBootMode != null ? this.osBootMode.getValue() : null);
		packet.setField("OSSuiteMaskCode", this.osSuiteMaskCode);
		packet.setField("OSProductTypeCode", this.osProductTypeCode);
		packet.setField("RuntimeVersion", this.runtimeVersion != null ? this.runtimeVersion.toString() : null);
		packet.setField("RuntimeArchitecture", this.runtimeArchitecture != null ? this.runtimeArchitecture.getValue() : null);
		packet.setField("CurrentCultureName", this.currentCultureName);
		packet.setField("CurrentUICultureName", this.currentUICultureName);
		packet.setField("MemoryMB", this.memoryMB);
		packet.setField("Processors", this.processors);
		packet.setField("ProcessorCores", this.processorCores);
		packet.setField("UserInteractive", this.userInteractive);
		packet.setField("TerminalServer", this.terminalServer);
		packet.setField("ScreenWidth", this.screenWidth);
		packet.setField("ScreenHeight", this.screenHeight);
		packet.setField("ColorDepth", this.colorDepth);
		packet.setField("CommandLine", this.commandLine);

		// write out application config provided stuff
		for (Map.Entry<String, String> property : this.properties.entrySet()) {
			packet.setField(property.getKey(), property.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.serialization.monitor.GibraltarCachedPacket#readFields(com.onloupe.core.serialization.PacketDefinition, com.onloupe.core.serialization.SerializedPacket)
	 */
	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	@Override
	public boolean equals(Object other) {
		// use our type-specific override
		return equals(other instanceof SessionSummaryPacket ? (SessionSummaryPacket) other : null);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 */
	public final boolean equals(SessionSummaryPacket other) {
		// Careful - can be null
		if (other == null) {
			return false; // since we're a live object we can't be equal.
		}

		boolean isEqual = ((getComputerId().equals(other.getComputerId())) && (getCaption().equals(other.getCaption()))
				&& getEndDateTime().isEqual(other.getEndDateTime())) && getProductName().equals(other.getProductName())
				&& (getApplicationName().equals(other.getApplicationName()))
				&& (getEnvironmentName().equals(other.getEnvironmentName()))
				&& (getPromotionLevelName().equals(other.getPromotionLevelName()))
				&& (getApplicationType() == other.getApplicationType())
				&& (getApplicationDescription().equals(other.getApplicationDescription()))
				&& getApplicationVersion().equals(other.getApplicationVersion())
				&& (getHostName().equals(other.getHostName())) && (getDnsDomainName().equals(other.getDnsDomainName()))
				&& getAgentVersion().equals(other.getAgentVersion())
				&& (getUserDomainName().equals(other.getUserDomainName()))
				&& (getUserName().equals(other.getUserName()))
				&& (getTimeZoneCaption().equals(other.getTimeZoneCaption())) && (super.equals(other));

		// if we're equal so far, keep digging
		if (isEqual) {
			for (Map.Entry<String, String> property : this.properties.entrySet()) {
				// does the comparable one exist?
				if (other.getProperties().containsKey(property.getKey())) {
					isEqual = property.getValue().equals(other.getProperties().get(property.getKey()));
				} else {
					// they are clearly not equal
					isEqual = false;
				}

				if (!isEqual) {
					break;
				}
			}
		}

		return isEqual;
	}

	/**
	 * Provides a representative hash code for objects of this type to spread out
	 * distribution in hash tables.
	 * 
	 * Objects which consider themselves to be Equal (a.Equals(b) returns true) are
	 * expected to have the same hash code. Objects which are not Equal may have the
	 * same hash code, but minimizing such overlaps helps with efficient operation
	 * of hash tables.
	 * 
	 * @return an int representing the hash code calculated for the contents of this
	 *         object
	 * 
	 */
	@Override
	public int hashCode() {
		int myHash = super.hashCode(); // Fold in hash code for inherited base type

		if (this.computerId != null) {
			myHash ^= this.computerId.hashCode(); // Fold in hash code for string ProductName
		}

		if (this.productName != null) {
			myHash ^= this.productName.hashCode(); // Fold in hash code for string ProductName
		}

		if (this.applicationName != null) {
			myHash ^= this.applicationName.hashCode(); // Fold in hash code for string ApplicationName
		}

		if (this.environmentName != null) {
			myHash ^= this.environmentName.hashCode(); // Fold in hash code for string EnvironmentName
		}

		if (this.promotionLevelName != null) {
			myHash ^= this.promotionLevelName.hashCode(); // Fold in hash code for string PromotionName
		}

		if (this.applicationVersion != null) {
			myHash ^= this.applicationVersion.hashCode(); // Fold in hash code for string ApplicationVersion
		}

		myHash ^= this.applicationType.hashCode(); // Fold in hash code for string ApplicationTypeName

		if (this.applicationDescription != null) {
			myHash ^= this.applicationDescription.hashCode(); // Fold in hash code for string ApplicationDescription
		}

		if (this.caption != null) {
			myHash ^= this.caption.hashCode(); // Fold in hash code for string Caption
		}

		if (this.timeZoneCaption != null) {
			myHash ^= this.timeZoneCaption.hashCode(); // Fold in hash code for string TimeZoneCaption
		}

		myHash ^= this.endDateTime.hashCode(); // Fold in hash code for DateTimeOffset member EndDateTime

		if (this.agentVersion != null) {
			myHash ^= this.agentVersion.hashCode(); // Fold in hash code for string MonitorVersion
		}

		if (this.userName != null) {
			myHash ^= this.userName.hashCode(); // Fold in hash code for string UserName
		}

		if (this.userDomainName != null) {
			myHash ^= this.userDomainName.hashCode(); // Fold in hash code for string UserDomainName
		}

		if (this.hostName != null) {
			myHash ^= this.hostName.hashCode(); // Fold in hash code for string HostName
		}

		if (this.dnsDomainName != null) {
			myHash ^= this.dnsDomainName.hashCode(); // Fold in hash code for string DNSDomainName
		}

		// Not bothering with dictionary of Properties

		return myHash;
	}

	/**
	 * Eliminates nulls and ensures that the string value isn't too long.
	 *
	 * @param value the value
	 * @param maxLength the max length
	 * @return the string
	 */
	private static String setSafeStringValue(String value, int maxLength) {
		String returnVal = (value != null) ? value : "";
		if (returnVal.length() > maxLength) {
			returnVal = returnVal.substring(0, maxLength);
		}

		return returnVal;
	}

	/**
	 * Calculate fully qualified user name.
	 */
	private void calculateFullyQualifiedUserName() {
		this.fullyQualifiedUserName = TypeUtils.isBlank(this.userDomainName) ? this.userName
				: this.userDomainName + "\\" + this.userName;
	}
}