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

public class SessionSummaryPacket extends GibraltarCachedPacket implements IPacket {
	// Stuff that aligns with SESSION table in index
	private UUID computerId;

	private String productName;
	private String applicationName;
	private String environmentName;
	private String promotionLevelName;
	private Version applicationVersion;
	private ApplicationType applicationType;
	private String applicationDescription;
	private String caption;
	private String timeZoneCaption;
	private OffsetDateTime endDateTime;
	private Version agentVersion;
	private String userName;
	private String userDomainName;
	private String hostName;
	private String dnsDomainName;

	// Stuff that aligns with SESSION_DETAILS table in index
	private int osPlatformCode;

	private Version osVersion;
	private String osServicePack;
	private String osCultureName;
	private ProcessorArchitecture osArchitecture;
	private OSBootMode osBootMode;
	private int osSuiteMaskCode;
	private int osProductTypeCode;
	private Version runtimeVersion;
	private ProcessorArchitecture runtimeArchitecture;
	private String currentCultureName;
	private String currentUICultureName;
	private int memoryMB;
	private int processors;
	private int processorCores;
	private boolean userInteractive;
	private boolean terminalServer;
	private int screenWidth;
	private int screenHeight;
	private int colorDepth;
	private String commandLine;

	// App.Config properties
	private final Map<String, String> properties = new HashMap<>();

	// CALCULATED value
	private String fullyQualifiedUserName;

	public SessionSummaryPacket() {
		super(true);
	}

	/**
	 * Create a session summary packet from the provided session header
	 * 
	 * @param sessionHeader
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
	 */
	public final UUID getComputerId() {
		return this.computerId;
	}

	public final void setComputerId(UUID value) {
		this.computerId = value;
	}

	public final OffsetDateTime getEndDateTime() {
		return this.endDateTime;
	}

	public final void setEndDateTime(OffsetDateTime value) {
		this.endDateTime = value;
	}

	public final String getCaption() {
		return this.caption;
	}

	public final void setCaption(String value) {
		this.caption = setSafeStringValue(value, 1024);
	}

	public final String getTimeZoneCaption() {
		return this.timeZoneCaption;
	}

	public final void setTimeZoneCaption(String value) {
		this.timeZoneCaption = setSafeStringValue(value, 120);
	}

	public final String getProductName() {
		return this.productName;
	}

	public final void setProductName(String value) {
		this.productName = setSafeStringValue(value, 120);
	}

	public final String getApplicationName() {
		return this.applicationName;
	}

	public final void setApplicationName(String value) {
		this.applicationName = setSafeStringValue(value, 120);
	}

	public final String getEnvironmentName() {
		return this.environmentName;
	}

	public final void setEnvironmentName(String value) {
		this.environmentName = setSafeStringValue(value, 120);
	}

	public final String getPromotionLevelName() {
		return this.promotionLevelName;
	}

	public final void setPromotionLevelName(String value) {
		this.promotionLevelName = setSafeStringValue(value, 120);
	}

	public final ApplicationType getApplicationType() {
		return this.applicationType;
	}

	public final void setApplicationType(ApplicationType value) {
		this.applicationType = value;
	}

	public final String getApplicationDescription() {
		return this.applicationDescription;
	}

	public final void setApplicationDescription(String value) {
		this.applicationDescription = setSafeStringValue(value, 1024);
	}

	/**
	 * The version of the application that recorded the session
	 */
	public final Version getApplicationVersion() {
		return this.applicationVersion;
	}

	public final void setApplicationVersion(Version value) {
		this.applicationVersion = value;
	}

	public final Version getAgentVersion() {
		return this.agentVersion;
	}

	public final void setAgentVersion(Version value) {
		this.agentVersion = value;
	}

	public final String getHostName() {
		return this.hostName;
	}

	public final void setHostName(String value) {
		this.hostName = setSafeStringValue(value, 120);
	}

	public final String getDnsDomainName() {
		return this.dnsDomainName;
	}

	public final void setDnsDomainName(String value) {
		this.dnsDomainName = setSafeStringValue(value, 512);
	}

	public final String getUserName() {
		return this.userName;
	}

	public final void setUserName(String value) {
		this.userName = setSafeStringValue(value, 120);
		calculateFullyQualifiedUserName();
	}

	public final String getUserDomainName() {
		return this.userDomainName;
	}

	public final void setUserDomainName(String value) {
		this.userDomainName = setSafeStringValue(value, 50);
		calculateFullyQualifiedUserName();
	}

	public final int getOSPlatformCode() {
		return this.osPlatformCode;
	}

	public final void setOSPlatformCode(int value) {
		this.osPlatformCode = value;
	}

	public final Version getOSVersion() {
		return this.osVersion;
	}

	public final void setOSVersion(Version value) {
		this.osVersion = value;
	}

	public final String getOSServicePack() {
		return this.osServicePack;
	}

	public final void setOSServicePack(String value) {
		this.osServicePack = setSafeStringValue(value, 50);
	}

	public final String getOSCultureName() {
		return this.osCultureName;
	}

	public final void setOSCultureName(String value) {
		this.osCultureName = setSafeStringValue(value, 50);
	}

	public final ProcessorArchitecture getOSArchitecture() {
		return this.osArchitecture;
	}

	public final void setOSArchitecture(ProcessorArchitecture value) {
		this.osArchitecture = value;
	}

	public final OSBootMode getOSBootMode() {
		return this.osBootMode;
	}

	public final void setOSBootMode(OSBootMode value) {
		this.osBootMode = value;
	}

	public final int getOSSuiteMask() {
		return this.osSuiteMaskCode;
	}

	public final void setOSSuiteMask(int value) {
		this.osSuiteMaskCode = value;
	}

	public final int getOSProductType() {
		return this.osProductTypeCode;
	}

	public final void setOSProductType(int value) {
		this.osProductTypeCode = value;
	}

	public final Version getRuntimeVersion() {
		return this.runtimeVersion;
	}

	public final void setRuntimeVersion(Version value) {
		this.runtimeVersion = value;
	}

	public final ProcessorArchitecture getRuntimeArchitecture() {
		return this.runtimeArchitecture;
	}

	public final void setRuntimeArchitecture(ProcessorArchitecture value) {
		this.runtimeArchitecture = value;
	}

	public final String getCurrentCultureName() {
		return this.currentCultureName;
	}

	public final void setCurrentCultureName(String value) {
		this.currentCultureName = setSafeStringValue(value, 50);
	}

	public final String getCurrentUICultureName() {
		return this.currentUICultureName;
	}

	public final void setCurrentUICultureName(String value) {
		this.currentUICultureName = setSafeStringValue(value, 50);
	}

	public final int getMemoryMB() {
		return this.memoryMB;
	}

	public final void setMemoryMB(int value) {
		this.memoryMB = value;
	}

	public final int getProcessors() {
		return this.processors;
	}

	public final void setProcessors(int value) {
		this.processors = value;
	}

	public final int getProcessorCores() {
		return this.processorCores;
	}

	public final void setProcessorCores(int value) {
		this.processorCores = value;
	}

	public final boolean getUserInteractive() {
		return this.userInteractive;
	}

	public final void setUserInteractive(boolean value) {
		this.userInteractive = value;
	}

	public final boolean getTerminalServer() {
		return this.terminalServer;
	}

	public final void setTerminalServer(boolean value) {
		this.terminalServer = value;
	}

	public final int getScreenWidth() {
		return this.screenWidth;
	}

	public final void setScreenWidth(int value) {
		this.screenWidth = value;
	}

	public final int getScreenHeight() {
		return this.screenHeight;
	}

	public final void setScreenHeight(int value) {
		this.screenHeight = value;
	}

	public final int getColorDepth() {
		return this.colorDepth;
	}

	public final void setColorDepth(int value) {
		this.colorDepth = value;
	}

	public final String getCommandLine() {
		return this.commandLine;
	}

	public final void setCommandLine(String value) {
		this.commandLine = setSafeStringValue(value, 2048);
	}

	/**
	 * The fully qualified user name of the user the application was run as.
	 */
	public final String getFullyQualifiedUserName() {
		return this.fullyQualifiedUserName;
	}

	/**
	 * Application provided properties
	 */
	public final Map<String, String> getProperties() {
		return this.properties;
	}

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

	@Override
	public final void readFields(PacketDefinition definition, SerializedPacket packet) {
		throw new UnsupportedOperationException("Deserialization of agent data is not supported");
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
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
	 * @return true if the current object is equal to the <paramref name="other" />
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
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
	 * @param value
	 * @param maxLength
	 * @return
	 */
	private static String setSafeStringValue(String value, int maxLength) {
		String returnVal = (value != null) ? value : "";
		if (returnVal.length() > maxLength) {
			returnVal = returnVal.substring(0, maxLength);
		}

		return returnVal;
	}

	private void calculateFullyQualifiedUserName() {
		this.fullyQualifiedUserName = TypeUtils.isBlank(this.userDomainName) ? this.userName
				: this.userDomainName + "\\" + this.userName;
	}
}