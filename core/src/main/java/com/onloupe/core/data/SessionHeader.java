package com.onloupe.core.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.onloupe.agent.SessionSummary;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ProcessorArchitecture;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.session.SessionStatus;
import com.onloupe.model.system.ApplicationType;
import com.onloupe.model.system.OSBootMode;
import com.onloupe.model.system.Version;

// TODO: Auto-generated Javadoc
/**
 * Used at the start of a data stream to contain the session summary
 * 
 * The session header subsumes a SessionStartInfoPacket, but both should be
 * included in a stream because the SessionHeader is really a cache of the
 * session start info packet that is easy to access.
 */
public final class SessionHeader implements ISessionSummary {
	
	/** The lock. */
	private final Object lock = new Object();

	/** The major version. */
	// You just can't do a binary format without recording version information
	private int majorVersion;
	
	/** The minor version. */
	private int minorVersion;

	/** The computer id. */
	// Stuff that aligns with SESSION table in index
	private UUID computerId;
	
	/** The session id. */
	private UUID sessionId;
	
	/** The session start date time. */
	private OffsetDateTime sessionStartDateTime;
	
	/** The session end date time. */
	private OffsetDateTime sessionEndDateTime;
	
	/** The caption. */
	private String caption;
	
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
	
	/** The application type name. */
	private String applicationTypeName;
	
	/** The application description. */
	private String applicationDescription;
	
	/** The time zone caption. */
	private String timeZoneCaption;
	
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
	
	/** The session status name. */
	private String sessionStatusName;
	
	/** The message count. */
	private int messageCount;
	
	/** The critical count. */
	private int criticalCount;
	
	/** The error count. */
	private int errorCount;
	
	/** The warning count. */
	private int warningCount;

	/** The OS platform code. */
	// Stuff that aligns with SESSION_DETAILS table in index
	private int OSPlatformCode;
	
	/** The OS version. */
	private Version OSVersion;
	
	/** The OS service pack. */
	private String OSServicePack;
	
	/** The OS culture name. */
	private String OSCultureName;
	
	/** The OS architecture. */
	private ProcessorArchitecture OSArchitecture;
	
	/** The os boot mode. */
	private OSBootMode osBootMode;
	
	/** The OS suite mask code. */
	private int OSSuiteMaskCode;
	
	/** The OS product type code. */
	private int OSProductTypeCode;
	
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
	private final Map<String, String> properties = new HashMap<String, String>();

	/** The has file info. */
	// file specific information (for this file)
	private boolean hasFileInfo;
	
	/** The file ID. */
	private UUID fileID;
	
	/** The file start date time. */
	private OffsetDateTime fileStartDateTime;
	
	/** The file end date time. */
	private OffsetDateTime fileEndDateTime;
	
	/** The valid. */
	private boolean valid;
	
	/** The last file. */
	private boolean lastFile;
	
	/** The file sequence. */
	private int fileSequence;
	
	/** The offset session end date time. */
	private int offsetSessionEndDateTime;
	
	/** The offset message count. */
	private int offsetMessageCount;
	
	/** The offset critical count. */
	private int offsetCriticalCount;
	
	/** The offset error count. */
	private int offsetErrorCount;
	
	/** The offset warning count. */
	private int offsetWarningCount;

	// cached serialized data (for when we're in a fixed representation and want
	/** The last raw data. */
	// performance)
	private byte[] lastRawData; // raw data is JUST the session header stuff, not file or CRC, so you can't
								// return just it.

	/** The fully qualified user name. */
								private String fullyQualifiedUserName;
	
	/** The hash code. */
	private int hashCode;
	
	/** The session status. */
	private SessionStatus sessionStatus;

	/**
	 * Create a new header from the provided session summary information.
	 *
	 * @param sessionSummary the session summary
	 */
	public SessionHeader(SessionSummary sessionSummary) {
		this(sessionSummary.getProperties());
		// copy the values from the session start info. We make a copy because it'd be
		// deadly if any of this changed
		// while we were alive - and while none of it should, lets just not count on
		// that,OK?
		// SESSION index information
		setId(sessionSummary.getId());
		setComputerId(sessionSummary.getComputerId());
		setProduct(sessionSummary.getProduct());
		setApplication(sessionSummary.getApplication());
		setEnvironment(sessionSummary.getEnvironment());
		setPromotionLevel(sessionSummary.getPromotionLevel());
		setApplicationVersion(sessionSummary.getApplicationVersion());
		setApplicationTypeName(String.valueOf(sessionSummary.getApplicationType()));
		setApplicationDescription(sessionSummary.getApplicationDescription());
		setCaption(sessionSummary.getCaption());
		setStatusName(String.valueOf(sessionSummary.getStatus()));
		setTimeZoneCaption(sessionSummary.getTimeZoneCaption());
		setStartDateTime(sessionSummary.getStartDateTime());
		setEndDateTime(sessionSummary.getEndDateTime());
		setAgentVersion(sessionSummary.getAgentVersion());
		setUserName(sessionSummary.getUserName());
		setUserDomainName(sessionSummary.getUserDomainName());
		setHostName(sessionSummary.getHostName());
		setDnsDomainName(sessionSummary.getDnsDomainName());
		setMessageCount(sessionSummary.getMessageCount());
		setCriticalCount(sessionSummary.getCriticalCount());
		setErrorCount(sessionSummary.getErrorCount());
		setWarningCount(sessionSummary.getWarningCount());

		// SESSION DETAIL index information
		setOSPlatformCode(sessionSummary.getOSPlatformCode());
		setOSVersion(sessionSummary.getOSVersion());
		setOSServicePack(sessionSummary.getOSServicePack());
		setOSCultureName(sessionSummary.getOSCultureName());
		setOSArchitecture(sessionSummary.getOSArchitecture());
		setOSBootMode(sessionSummary.getOSBootMode());
		setOSSuiteMask(sessionSummary.getOSSuiteMask());
		setOSProductType(sessionSummary.getOSProductType());
		setRuntimeVersion(sessionSummary.getRuntimeVersion());
		setRuntimeArchitecture(sessionSummary.getRuntimeArchitecture());
		setCurrentCultureName(sessionSummary.getCurrentCultureName());
		setCurrentUICultureName(sessionSummary.getCurrentUICultureName());
		setMemoryMB(sessionSummary.getMemoryMB());
		setProcessors(sessionSummary.getProcessors());
		setProcessorCores(sessionSummary.getProcessorCores());
		setUserInteractive(sessionSummary.getUserInteractive());
		setTerminalServer(sessionSummary.getTerminalServer());
		setScreenWidth(sessionSummary.getScreenWidth());
		setScreenHeight(sessionSummary.getScreenHeight());
		setColorDepth(sessionSummary.getColorDepth());
		setCommandLine(sessionSummary.getCommandLine());
	}

	/**
	 * Create a new session header with the specified properties collection. All
	 * other values are unset.
	 *
	 * @param properties the properties
	 */
	public SessionHeader(Map<String, String> properties) {
		this.majorVersion = FileHeader.defaultMajorVersion;
		this.minorVersion = FileHeader.defaultMinorVersion;

		this.properties.clear();
		this.properties.putAll(new HashMap<String, String>(properties));

		setIsNew(true);
	}

	/**
	 * Create a new session header by reading the provided byte array.
	 *
	 * @param data the data
	 */
	public SessionHeader(byte[] data) {
		this.valid = loadStream(ByteBuffer.wrap(data), data.length);
	}

	/**
	 * Create a new session header by reading the provided stream, which must
	 * contain ONLY the header.
	 *
	 * @param data the data
	 * @param length The number of bytes to read from the stream for the header (or
	 *               zero to read the whole stream)
	 */
	public SessionHeader(ByteBuffer data, int length) {
		if (data == null) {
			throw new NullPointerException("data");
		}

		this.valid = loadStream(data, length);
	}

	/**
	 * Export the file header into a raw data array.
	 *
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public byte[] rawData() throws IOException {
		synchronized (this.lock) {
			ByteArrayOutputStream rawData = new ByteArrayOutputStream();

			// two paths for this: We either already have a cached value or we don't.
			if (this.lastRawData == null) {
				// gotta make the last raw data.
				rawData.write(BinarySerializer.serializeValue(this.majorVersion));
				rawData.write(BinarySerializer.serializeValue(this.minorVersion));
				rawData.write(BinarySerializer.serializeValue(this.sessionId));

				if (FileHeader.supportsComputerId(this.majorVersion, this.minorVersion)) {
					rawData.write(BinarySerializer
							.serializeValue(this.computerId != null ? this.computerId : new UUID(0, 0)));
				}

				rawData.write(BinarySerializer.serializeValue(this.productName));
				rawData.write(BinarySerializer.serializeValue(this.applicationName));

				if (FileHeader.supportsEnvironmentAndPromotion(this.majorVersion, this.minorVersion)) {
					rawData.write(BinarySerializer.serializeValue(this.environmentName));
					rawData.write(BinarySerializer.serializeValue(this.promotionLevelName));
				}

				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.applicationVersion)));
				rawData.write(BinarySerializer.serializeValue(this.applicationTypeName));
				rawData.write(BinarySerializer.serializeValue(this.applicationDescription));
				rawData.write(BinarySerializer.serializeValue(this.caption));
				rawData.write(BinarySerializer.serializeValue(this.timeZoneCaption));
				rawData.write(BinarySerializer.serializeValue(this.sessionStatusName));
				rawData.write(BinarySerializer.serializeValue(this.sessionStartDateTime));

				// OK, where we are now is the end date time position which is variable
				this.offsetSessionEndDateTime = rawData.size();
				rawData.write(BinarySerializer.serializeValue(this.sessionEndDateTime));
				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.agentVersion)));
				rawData.write(BinarySerializer.serializeValue(this.userName));
				rawData.write(BinarySerializer.serializeValue(this.userDomainName));
				rawData.write(BinarySerializer.serializeValue(this.hostName));
				rawData.write(BinarySerializer.serializeValue(this.dnsDomainName));

				// For each message count we need to record our position
				this.offsetMessageCount = rawData.size();
				rawData.write(BinarySerializer.serializeValue(this.messageCount));

				this.offsetCriticalCount = rawData.size();
				rawData.write(BinarySerializer.serializeValue(this.criticalCount));

				this.offsetErrorCount = rawData.size();
				rawData.write(BinarySerializer.serializeValue(this.errorCount));

				this.offsetWarningCount = rawData.size();
				rawData.write(BinarySerializer.serializeValue(this.warningCount));

				// Stuff that aligns with SESSION_DETAILS table in index
				rawData.write(BinarySerializer.serializeValue(this.OSPlatformCode));
				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.OSVersion)));
				rawData.write(BinarySerializer.serializeValue(this.OSServicePack));
				rawData.write(BinarySerializer.serializeValue(this.OSCultureName));
				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.OSArchitecture)));
				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.osBootMode)));
				rawData.write(BinarySerializer.serializeValue(this.OSSuiteMaskCode));
				rawData.write(BinarySerializer.serializeValue(this.OSProductTypeCode));
				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.runtimeVersion)));
				rawData.write(BinarySerializer.serializeValue(String.valueOf(this.runtimeArchitecture)));
				rawData.write(BinarySerializer.serializeValue(this.currentCultureName));
				rawData.write(BinarySerializer.serializeValue(this.currentUICultureName));
				rawData.write(BinarySerializer.serializeValue(this.memoryMB));
				rawData.write(BinarySerializer.serializeValue(this.processors));
				rawData.write(BinarySerializer.serializeValue(this.processorCores));
				rawData.write(BinarySerializer.serializeValue(this.userInteractive));
				rawData.write(BinarySerializer.serializeValue(this.terminalServer));
				rawData.write(BinarySerializer.serializeValue(this.screenWidth));
				rawData.write(BinarySerializer.serializeValue(this.screenHeight));
				rawData.write(BinarySerializer.serializeValue(this.colorDepth));
				rawData.write(BinarySerializer.serializeValue(this.commandLine));

				// Application provided Properties
				// now write off properties as a set of name/value pairs
				// we have to write out how many properties there are so we know how many to
				// read back
				rawData.write(BinarySerializer.serializeValue(this.properties.size()));

				for (Map.Entry<String, String> property : this.properties.entrySet()) {
					rawData.write(BinarySerializer.serializeValue(property.getKey()));
					rawData.write(BinarySerializer.serializeValue(property.getValue()));
				}

				// cache the raw data so we don't have to recalc it every time.
				this.lastRawData = rawData.toByteArray();
			} else {
				// copy the last raw data we have into the stream.
				rawData.write(this.lastRawData, 0, this.lastRawData.length);
			}

			// BEGIN FILE INFO (added every time)
			if (this.hasFileInfo) {
				rawData.write(BinarySerializer.serializeValue(this.fileID));
				rawData.write(BinarySerializer.serializeValue(this.fileSequence));
				rawData.write(BinarySerializer.serializeValue(this.fileStartDateTime));
				rawData.write(BinarySerializer.serializeValue(this.fileEndDateTime));
				rawData.write(BinarySerializer.serializeValue(this.lastFile));
			}

			// CRC CALC
			// Now we need to calculate the header CRC
			rawData.write(BinarySerializer.calculateCRC(rawData.toByteArray(), rawData.size()));
			return rawData.toByteArray();
		}
	}

	/**
	 * The major version of the binary format of the session header.
	 *
	 * @return the major version
	 */
	public int getMajorVersion() {
		return this.majorVersion;
	}

	/**
	 * Sets the major version.
	 *
	 * @param value the new major version
	 */
	public void setMajorVersion(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.majorVersion = value;
		}
	}

	/**
	 * The minor version of the binary format of the session header.
	 *
	 * @return the minor version
	 */
	public int getMinorVersion() {
		return this.minorVersion;
	}

	/**
	 * Sets the minor version.
	 *
	 * @param value the new minor version
	 */
	public void setMinorVersion(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.minorVersion = value;
		}
	}

	/**
	 * The unique Id of the session.
	 *
	 * @return the id
	 */
	@Override
	public UUID getId() {
		return this.sessionId;
	}

	/**
	 * Sets the id.
	 *
	 * @param value the new id
	 */
	public void setId(UUID value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.sessionId = value;
		}
	}

	/**
	 * The link to this item on the server.
	 *
	 * @return the uri
	 */
	@Override
	public URI getUri() {
		throw new UnsupportedOperationException("Links are not supported in this context");
	}

	/**
	 * Indicates if all of the session data is stored that is expected to be
	 * available.
	 *
	 * @return true, if is complete
	 */
	@Override
	public boolean isComplete() {
		return this.hasFileInfo ? this.lastFile : false;
	}

	/**
	 * The unique Id of the computer.
	 *
	 * @return the computer id
	 */
	@Override
	public UUID getComputerId() {
		return this.computerId;
	}

	/**
	 * Sets the computer id.
	 *
	 * @param value the new computer id
	 */
	public void setComputerId(UUID value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.computerId = value;
		}
	}

	/**
	 * A display caption for the session.
	 *
	 * @return the caption
	 */
	@Override
	public String getCaption() {
		return this.caption;
	}

	/**
	 * Sets the caption.
	 *
	 * @param value the new caption
	 */
	public void setCaption(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.caption = value;
		}
	}

	/**
	 * The product name of the application that recorded the session.
	 *
	 * @return the product
	 */
	@Override
	public String getProduct() {
		return this.productName;
	}

	/**
	 * Sets the product.
	 *
	 * @param value the new product
	 */
	public void setProduct(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.productName = value;
		}
	}

	/**
	 * The title of the application that recorded the session.
	 *
	 * @return the application
	 */
	@Override
	public String getApplication() {
		return this.applicationName;
	}

	/**
	 * Sets the application.
	 *
	 * @param value the new application
	 */
	public void setApplication(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationName = value;
		}
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
	public String getEnvironment() {
		return this.environmentName;
	}

	/**
	 * Sets the environment.
	 *
	 * @param value the new environment
	 */
	public void setEnvironment(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.environmentName = value;
		}
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
	public String getPromotionLevel() {
		return this.promotionLevelName;
	}

	/**
	 * Sets the promotion level.
	 *
	 * @param value the new promotion level
	 */
	public void setPromotionLevel(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.promotionLevelName = value;
		}
	}

	/**
	 * The type of process the application ran as.
	 *
	 * @return the application type
	 */
	@Override
	public ApplicationType getApplicationType() {
		return TypeUtils.isNotBlank(applicationTypeName) 
				? ApplicationType.valueOf(this.applicationTypeName.toUpperCase())
				: null;
	}

	/**
	 * The type of process the application ran as.
	 * 
	 * Not an enumeration because the ApplicationType enum isn't accessible at this
	 * level.
	 *
	 * @return the application type name
	 */
	public String getApplicationTypeName() {
		return this.applicationTypeName;
	}

	/**
	 * Sets the application type name.
	 *
	 * @param value the new application type name
	 */
	public void setApplicationTypeName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationTypeName = value;
		}
	}

	/**
	 * The description of the application from its manifest.
	 *
	 * @return the application description
	 */
	@Override
	public String getApplicationDescription() {
		return this.applicationDescription;
	}

	/**
	 * Sets the application description.
	 *
	 * @param value the new application description
	 */
	public void setApplicationDescription(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationDescription = value;
		}
	}

	/**
	 * The version of the application that recorded the session.
	 *
	 * @return the application version
	 */
	@Override
	public Version getApplicationVersion() {
		return this.applicationVersion;
	}

	/**
	 * Sets the application version.
	 *
	 * @param value the new application version
	 */
	public void setApplicationVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationVersion = value;
		}
	}

	/**
	 * The version of the Gibraltar Agent used to monitor the session.
	 *
	 * @return the agent version
	 */
	@Override
	public Version getAgentVersion() {
		return this.agentVersion;
	}

	/**
	 * Sets the agent version.
	 *
	 * @param value the new agent version
	 */
	public void setAgentVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.agentVersion = value;
		}
	}

	/**
	 * The host name / NetBIOS name of the computer that recorded the session
	 * 
	 * Does not include the domain name portion of the fully qualified DNS name.
	 *
	 * @return the host name
	 */
	@Override
	public String getHostName() {
		return this.hostName;
	}

	/**
	 * Sets the host name.
	 *
	 * @param value the new host name
	 */
	public void setHostName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.hostName = value;
		}
	}

	/**
	 * The DNS domain name of the computer that recorded the session. May be empty.
	 * 
	 * Does not include the host name portion of the fully qualified DNS name.
	 *
	 * @return the dns domain name
	 */
	@Override
	public String getDnsDomainName() {
		return this.dnsDomainName;
	}

	/**
	 * Sets the dns domain name.
	 *
	 * @param value the new dns domain name
	 */
	public void setDnsDomainName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.dnsDomainName = value;
		}
	}

	/**
	 * The display caption of the time zone where the session was recorded.
	 *
	 * @return the time zone caption
	 */
	@Override
	public String getTimeZoneCaption() {
		return this.timeZoneCaption;
	}

	/**
	 * Sets the time zone caption.
	 *
	 * @param value the new time zone caption
	 */
	public void setTimeZoneCaption(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.timeZoneCaption = value;
		}
	}

	/**
	 * The user Id that was used to run the session.
	 *
	 * @return the user name
	 */
	@Override
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param value the new user name
	 */
	public void setUserName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.fullyQualifiedUserName = null;
			this.userName = value;
		}
	}

	/**
	 * The domain of the user id that was used to run the session.
	 *
	 * @return the user domain name
	 */
	@Override
	public String getUserDomainName() {
		return this.userDomainName;
	}

	/**
	 * Sets the user domain name.
	 *
	 * @param value the new user domain name
	 */
	public void setUserDomainName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.fullyQualifiedUserName = null;
			this.userDomainName = value;
		}
	}

	/**
	 * The fully qualified user name of the user the application was run as.
	 *
	 * @return the fully qualified user name
	 */
	@Override
	public String getFullyQualifiedUserName() {
		if (this.fullyQualifiedUserName == null) {
			this.fullyQualifiedUserName = TypeUtils.isBlank(getUserDomainName()) ? getUserName()
					: getUserDomainName() + "\\" + getUserName();
		}
		return this.fullyQualifiedUserName;
	}

	/**
	 * The date and time the session started.
	 *
	 * @return the start date time
	 */
	@Override
	public OffsetDateTime getStartDateTime() {
		return this.sessionStartDateTime;
	}

	/**
	 * Sets the start date time.
	 *
	 * @param value the new start date time
	 */
	public void setStartDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.sessionStartDateTime = value;
		}
	}

	/**
	 * The date and time the session started.
	 *
	 * @return the display start date time
	 */
	@Override
	public OffsetDateTime getDisplayStartDateTime() {
		return getStartDateTime();
	}

	/**
	 * The date and time the session ended or was last confirmed running.
	 *
	 * @return the end date time
	 */
	@Override
	public OffsetDateTime getEndDateTime() {
		return this.sessionEndDateTime;
	}

	/**
	 * Sets the end date time.
	 *
	 * @param value the new end date time
	 */
	public void setEndDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			// this is an updatable field, so if we already have the raw data and can update
			// it, lets do that.
			if (this.lastRawData != null) {
				// protect against case that should never happen - we have the raw data, but not
				// the offset to the value.
				if (this.offsetSessionEndDateTime == 0) {
					this.lastRawData = null;
				} else {
					// update the date at that point.
					byte[] binaryValue = BinarySerializer.serializeValue(value);
					ByteBuffer.wrap(binaryValue).put(this.lastRawData, this.offsetSessionEndDateTime,
							binaryValue.length);
				}
			}

			this.sessionEndDateTime = value;
		}
	}

	/**
	 * The date and time the session ended.
	 *
	 * @return the display end date time
	 */
	@Override
	public OffsetDateTime getDisplayEndDateTime() {
		return getEndDateTime();
	}

	/**
	 * The duration of the session. May be zero indicating unknown
	 *
	 * @return the duration
	 */
	@Override
	public Duration getDuration() {
		Duration duration = Duration.between(this.sessionStartDateTime, this.sessionEndDateTime);

		if (duration.isNegative()) {
			duration = Duration.ZERO;
		}

		return duration;
	}

	/**
	 * The final status of the session.
	 *
	 * @return the status
	 */
	@Override
	public SessionStatus getStatus() {
		return this.sessionStatus;
	}

	/**
	 * The status of the session (based on the SessionStatus enumeration).
	 *
	 * @return the status name
	 */
	public String getStatusName() {
		return this.sessionStatusName;
	}

	/**
	 * Sets the status name.
	 *
	 * @param value the new status name
	 */
	public void setStatusName(String value) {
		synchronized (this.lock) {
			// only do this change if we actually have a change...
			if (!TypeUtils.equals(this.sessionStatusName, value)) {
				this.lastRawData = null;
				this.sessionStatusName = value;

				this.sessionStatus = statusNameToStatus(this.sessionStatusName);
			}
		}
	}

	/**
	 * The total number of log messages recorded in the session.
	 *
	 * @return the message count
	 */
	@Override
	public int getMessageCount() {
		return this.messageCount;
	}

	/**
	 * Sets the message count.
	 *
	 * @param value the new message count
	 */
	public void setMessageCount(int value) {
		synchronized (this.lock) {
			// this is an updatable field, so if we already have the raw data and can update
			// it, lets do that.
			if (this.lastRawData != null) {
				// protect against case that should never happen - we have the raw data, but not
				// the offset to the value.
				if (this.offsetMessageCount == 0) {
					this.lastRawData = null;
				} else {
					// update the number at that point.
					byte[] binaryValue = BinarySerializer.serializeValue(value);
					ByteBuffer.wrap(binaryValue).put(this.lastRawData, this.offsetMessageCount, binaryValue.length);
				}
			}

			this.messageCount = value;
		}
	}

	/**
	 * The total number of critical severity log messages recorded in the session.
	 *
	 * @return the critical count
	 */
	@Override
	public int getCriticalCount() {
		return this.criticalCount;
	}

	/**
	 * Sets the critical count.
	 *
	 * @param value the new critical count
	 */
	public void setCriticalCount(int value) {
		synchronized (this.lock) {
			// this is an updatable field, so if we already have the raw data and can update
			// it, lets do that.
			if (this.lastRawData != null) {
				// protect against case that should never happen - we have the raw data, but not
				// the offset to the value.
				if (this.offsetCriticalCount == 0) {
					this.lastRawData = null;
				} else {
					// update the number at that point.
					byte[] binaryValue = BinarySerializer.serializeValue(value);
					ByteBuffer.wrap(binaryValue).put(this.lastRawData, this.offsetCriticalCount, binaryValue.length);
				}
			}

			this.criticalCount = value;
		}
	}

	/**
	 * The total number of error severity log messages recorded in the session.
	 *
	 * @return the error count
	 */
	@Override
	public int getErrorCount() {
		return this.errorCount;
	}

	/**
	 * Sets the error count.
	 *
	 * @param value the new error count
	 */
	public void setErrorCount(int value) {
		synchronized (this.lock) {
			// this is an updatable field, so if we already have the raw data and can update
			// it, lets do that.
			if (this.lastRawData != null) {
				// protect against case that should never happen - we have the raw data, but not
				// the offset to the value.
				if (this.offsetErrorCount == 0) {
					this.lastRawData = null;
				} else {
					// update the number at that point.
					byte[] binaryValue = BinarySerializer.serializeValue(value);
					ByteBuffer.wrap(binaryValue).put(this.lastRawData, this.offsetErrorCount, binaryValue.length);
				}
			}

			this.errorCount = value;
		}
	}

	/**
	 * The total number of warning severity log messages recorded in the session.
	 *
	 * @return the warning count
	 */
	@Override
	public int getWarningCount() {
		return this.warningCount;
	}

	/**
	 * Sets the warning count.
	 *
	 * @param value the new warning count
	 */
	public void setWarningCount(int value) {
		synchronized (this.lock) {
			// this is an updatable field, so if we already have the raw data and can update
			// it, lets do that.
			if (this.lastRawData != null) {
				// protect against case that should never happen - we have the raw data, but not
				// the offset to the value.
				if (this.offsetWarningCount == 0) {
					this.lastRawData = null;
				} else {
					// update the number at that point.
					byte[] binaryValue = BinarySerializer.serializeValue(value);
					ByteBuffer.wrap(binaryValue).put(this.lastRawData, this.offsetWarningCount, binaryValue.length);
				}
			}

			this.warningCount = value;
		}
	}

	/**
	 * The version information of the installed operating system (without service
	 * pack or patches).
	 *
	 * @return the OS version
	 */
	@Override
	public Version getOSVersion() {
		return this.OSVersion;
	}

	/**
	 * Sets the OS version.
	 *
	 * @param value the new OS version
	 */
	public void setOSVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSVersion = value;
		}
	}

	/**
	 * The operating system service pack, if any.
	 *
	 * @return the OS service pack
	 */
	@Override
	public String getOSServicePack() {
		return this.OSServicePack;
	}

	/**
	 * Sets the OS service pack.
	 *
	 * @param value the new OS service pack
	 */
	public void setOSServicePack(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSServicePack = value;
		}
	}

	/**
	 * The culture name of the underlying operating system installation.
	 *
	 * @return the OS culture name
	 */
	@Override
	public String getOSCultureName() {
		return this.OSCultureName;
	}

	/**
	 * Sets the OS culture name.
	 *
	 * @param value the new OS culture name
	 */
	public void setOSCultureName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSCultureName = value;
		}
	}

	/**
	 * The OS Platform code, nearly always 1 indicating Windows NT.
	 *
	 * @return the OS platform code
	 */
	@Override
	public int getOSPlatformCode() {
		return this.OSPlatformCode;
	}

	/**
	 * Sets the OS platform code.
	 *
	 * @param value the new OS platform code
	 */
	public void setOSPlatformCode(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSPlatformCode = value;
		}
	}

	/**
	 * The OS product type code, used to differentiate specific editions of various
	 * operating systems.
	 *
	 * @return the OS product type
	 */
	@Override
	public int getOSProductType() {
		return this.OSProductTypeCode;
	}

	/**
	 * Sets the OS product type.
	 *
	 * @param value the new OS product type
	 */
	public void setOSProductType(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSProductTypeCode = value;
		}
	}

	/**
	 * The OS Suite Mask, used to differentiate specific editions of various
	 * operating systems.
	 *
	 * @return the OS suite mask
	 */
	@Override
	public int getOSSuiteMask() {
		return this.OSSuiteMaskCode;
	}

	/**
	 * Sets the OS suite mask.
	 *
	 * @param value the new OS suite mask
	 */
	public void setOSSuiteMask(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSSuiteMaskCode = value;
		}
	}

	/**
	 * The well known operating system family name, like Windows Vista or Windows
	 * Server 2003.
	 *
	 * @return the OS family name
	 */
	@Override
	public String getOSFamilyName() {
		return ""; // BUG
	}

	/**
	 * The edition of the operating system without the family name, such as
	 * Workstation or Standard Server.
	 *
	 * @return the OS edition name
	 */
	@Override
	public String getOSEditionName() {
		return ""; // BUG
	}

	/**
	 * The well known OS name and edition name.
	 *
	 * @return the OS full name
	 */
	@Override
	public String getOSFullName() {
		return ""; // BUG
	}

	/**
	 * The well known OS name, edition name, and service pack like Windows XP
	 * Professional Service Pack 3.
	 *
	 * @return the OS full name with service pack
	 */
	@Override
	public String getOSFullNameWithServicePack() {
		return ""; // BUG
	}

	/**
	 * The processor architecture of the operating system.
	 *
	 * @return the OS architecture
	 */
	@Override
	public ProcessorArchitecture getOSArchitecture() {
		return this.OSArchitecture;
	}

	/**
	 * Sets the OS architecture.
	 *
	 * @param value the new OS architecture
	 */
	public void setOSArchitecture(ProcessorArchitecture value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSArchitecture = value;
		}
	}

	/**
	 * The boot mode of the operating system.
	 *
	 * @return the OS boot mode
	 */
	@Override
	public OSBootMode getOSBootMode() {
		return this.osBootMode;
	}

	/**
	 * Sets the OS boot mode.
	 *
	 * @param value the new OS boot mode
	 */
	public void setOSBootMode(OSBootMode value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.osBootMode = value;
		}
	}

	/**
	 * The version of the .NET runtime that the application domain is running as.
	 *
	 * @return the runtime version
	 */
	@Override
	public Version getRuntimeVersion() {
		return this.runtimeVersion;
	}

	/**
	 * Sets the runtime version.
	 *
	 * @param value the new runtime version
	 */
	public void setRuntimeVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.runtimeVersion = value;
		}
	}

	/**
	 * The processor architecture the process is running as.
	 *
	 * @return the runtime architecture
	 */
	@Override
	public ProcessorArchitecture getRuntimeArchitecture() {
		return this.runtimeArchitecture;
	}

	/**
	 * Sets the runtime architecture.
	 *
	 * @param value the new runtime architecture
	 */
	public void setRuntimeArchitecture(ProcessorArchitecture value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.runtimeArchitecture = value;
		}
	}

	/**
	 * The current application culture name.
	 *
	 * @return the current culture name
	 */
	@Override
	public String getCurrentCultureName() {
		return this.currentCultureName;
	}

	/**
	 * Sets the current culture name.
	 *
	 * @param value the new current culture name
	 */
	public void setCurrentCultureName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.currentCultureName = value;
		}
	}

	/**
	 * The current user interface culture name.
	 *
	 * @return the current UI culture name
	 */
	@Override
	public String getCurrentUICultureName() {
		return this.currentUICultureName;
	}

	/**
	 * Sets the current UI culture name.
	 *
	 * @param value the new current UI culture name
	 */
	public void setCurrentUICultureName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.currentUICultureName = value;
		}
	}

	/**
	 * The number of megabytes of installed memory in the host computer.
	 *
	 * @return the memory MB
	 */
	@Override
	public int getMemoryMB() {
		return this.memoryMB;
	}

	/**
	 * Sets the memory MB.
	 *
	 * @param value the new memory MB
	 */
	public void setMemoryMB(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.memoryMB = value;
		}
	}

	/**
	 * The number of physical processor sockets in the host computer.
	 *
	 * @return the processors
	 */
	@Override
	public int getProcessors() {
		return this.processors;
	}

	/**
	 * Sets the processors.
	 *
	 * @param value the new processors
	 */
	public void setProcessors(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.processors = value;
		}
	}

	/**
	 * The total number of processor cores in the host computer.
	 *
	 * @return the processor cores
	 */
	@Override
	public int getProcessorCores() {
		return this.processorCores;
	}

	/**
	 * Sets the processor cores.
	 *
	 * @param value the new processor cores
	 */
	public void setProcessorCores(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.processorCores = value;
		}
	}

	/**
	 * Indicates if the session was run in a user interactive mode.
	 *
	 * @return the user interactive
	 */
	@Override
	public boolean getUserInteractive() {
		return this.userInteractive;
	}

	/**
	 * Sets the user interactive.
	 *
	 * @param value the new user interactive
	 */
	public void setUserInteractive(boolean value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.userInteractive = value;
		}
	}

	/**
	 * Indicates if the session was run through terminal server. Only applies to
	 * User Interactive sessions.
	 *
	 * @return the terminal server
	 */
	@Override
	public boolean getTerminalServer() {
		return this.terminalServer;
	}

	/**
	 * Sets the terminal server.
	 *
	 * @param value the new terminal server
	 */
	public void setTerminalServer(boolean value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.terminalServer = value;
		}
	}

	/**
	 * The number of pixels wide of the virtual desktop.
	 *
	 * @return the screen width
	 */
	@Override
	public int getScreenWidth() {
		return this.screenWidth;
	}

	/**
	 * Sets the screen width.
	 *
	 * @param value the new screen width
	 */
	public void setScreenWidth(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.screenWidth = value;
		}
	}

	/**
	 * The number of pixels tall for the virtual desktop.
	 *
	 * @return the screen height
	 */
	@Override
	public int getScreenHeight() {
		return this.screenHeight;
	}

	/**
	 * Sets the screen height.
	 *
	 * @param value the new screen height
	 */
	public void setScreenHeight(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.screenHeight = value;
		}
	}

	/**
	 * The number of bits of color depth.
	 *
	 * @return the color depth
	 */
	@Override
	public int getColorDepth() {
		return this.colorDepth;
	}

	/**
	 * Sets the color depth.
	 *
	 * @param value the new color depth
	 */
	public void setColorDepth(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.colorDepth = value;
		}
	}

	/**
	 * The complete command line used to execute the process including arguments.
	 *
	 * @return the command line
	 */
	@Override
	public String getCommandLine() {
		return this.commandLine;
	}

	/**
	 * Sets the command line.
	 *
	 * @param value the new command line
	 */
	public void setCommandLine(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.commandLine = value;
		}
	}

	/**
	 * The unique id of the file the session header is associated with.
	 *
	 * @return the file id
	 */
	public UUID getFileId() {
		return this.fileID;
	}

	/**
	 * Sets the file id.
	 *
	 * @param value the new file id
	 */
	public void setFileId(UUID value) {
		synchronized (this.lock) {
			this.fileID = value;
			this.hasFileInfo = true;
		}
	}

	/**
	 * The date and time that this file became the active file for the session.
	 *
	 * @return the file start date time
	 */
	public OffsetDateTime getFileStartDateTime() {
		return this.fileStartDateTime;
	}

	/**
	 * Sets the file start date time.
	 *
	 * @param value the new file start date time
	 */
	public void setFileStartDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			this.fileStartDateTime = value;
		}
	}

	/**
	 * The date and time that this file was no longer the active file for the
	 * session.
	 *
	 * @return the file end date time
	 */
	public OffsetDateTime getFileEndDateTime() {
		return this.fileEndDateTime;
	}

	/**
	 * Sets the file end date time.
	 *
	 * @param value the new file end date time
	 */
	public void setFileEndDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			this.fileEndDateTime = value;
		}
	}

	/**
	 * The sequence of this file in the set of files for the session.
	 *
	 * @return the file sequence
	 */
	public int getFileSequence() {
		return this.fileSequence;
	}

	/**
	 * Sets the file sequence.
	 *
	 * @param value the new file sequence
	 */
	public void setFileSequence(int value) {
		synchronized (this.lock) {
			this.fileSequence = value;
		}
	}

	/**
	 * A collection of properties used to provided extended information about the
	 * session.
	 *
	 * @return the properties
	 */
	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	}

	/**
	 * The date and time the session was added to the repository.
	 *
	 * @return the added date time
	 */
	@Override
	public OffsetDateTime getAddedDateTime() {
		return this.sessionStartDateTime;
	}

	/**
	 * The date and time the session was added to the repository.
	 *
	 * @return the display added date time
	 */
	@Override
	public OffsetDateTime getDisplayAddedDateTime() {
		return this.sessionStartDateTime;
	}

	/**
	 * The date and time the session was added to the repository.
	 *
	 * @return the updated date time
	 */
	@Override
	public OffsetDateTime getUpdatedDateTime() {
		return this.fileEndDateTime;
	}

	/**
	 * The date and time the session was added to the repository.
	 *
	 * @return the display updated date time
	 */
	@Override
	public OffsetDateTime getDisplayUpdatedDateTime() {
		return this.fileEndDateTime;
	}

	/**
	 * True if this is the last file recorded for the session.
	 *
	 * @return true, if is last file
	 */
	public boolean isLastFile() {
		return this.lastFile;
	}

	/**
	 * Sets the checks if is last file.
	 *
	 * @param value the new checks if is last file
	 */
	public void setIsLastFile(boolean value) {
		synchronized (this.lock) {
			this.lastFile = value;
		}
	}

	/**
	 * True if the session header is valid (has not been corrupted).
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return this.valid;
	}

	/**
	 * Indicates if the session has ever been viewed or exported
	 * 
	 * Changes to this property are not persisted.
	 */
	private boolean isNew;

	/* (non-Javadoc)
	 * @see com.onloupe.model.session.ISessionSummary#isNew()
	 */
	@Override
	public boolean isNew() {
		return this.isNew;
	}

	/**
	 * Sets the checks if is new.
	 *
	 * @param value the new checks if is new
	 */
	public void setIsNew(boolean value) {
		this.isNew = value;
	}

	/**
	 * Indicates if the session is currently running and a live stream is available.
	 */
	private boolean isLive;

	/* (non-Javadoc)
	 * @see com.onloupe.model.session.ISessionSummary#isLive()
	 */
	@Override
	public boolean isLive() {
		return this.isLive;
	}

	/**
	 * Sets the checks if is live.
	 *
	 * @param value the new checks if is live
	 */
	public void setIsLive(boolean value) {
		this.isLive = value;
	}

	/**
	 * Indicates if session data is available.
	 * 
	 * The session summary can be transfered separately from the session details and
	 * isn't subject to pruning so it may be around long before or after the
	 * detailed data is.
	 */
	private boolean hasData;

	/* (non-Javadoc)
	 * @see com.onloupe.model.session.ISessionSummary#getHasData()
	 */
	@Override
	public boolean getHasData() {
		return this.hasData;
	}

	/**
	 * Sets the checks for data.
	 *
	 * @param value the new checks for data
	 */
	public void setHasData(boolean value) {
		this.hasData = value;
	}

	/**
	 * True if the session header contains the extended file information.
	 *
	 * @return the checks for file info
	 */
	public boolean getHasFileInfo() {
		return this.hasFileInfo;
	}

	/**
	 * Indicates if the binary stream supports fragments or only single-stream
	 * transfer (the pre-3.0 format)
	 *
	 * @return the supports fragments
	 */
	public boolean getSupportsFragments() {
		return FileHeader.supportsFragments(this.majorVersion, this.minorVersion);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 *
	 * @param other An object to compare with this object.
	 * @return true if the current object is equal to the <paramref name="other"/>
	 *         parameter; otherwise, false.
	 */
	public boolean equals(SessionHeader other) {
		// Careful, it could be null; check it without recursion
		if (other == null) {
			return false; // Since we're a live object we can't be equal to a null instance.
		}

		return this.sessionId.equals(other.getId());
	}

	/**
	 * Determines whether the specified <see cref="T:System.Object"/> is equal to
	 * the current <see cref="T:System.Object"/>.
	 *
	 * @param obj The <see cref="T:System.Object"/> to compare with the current
	 *            <see cref="T:System.Object"/>.
	 * @return true if the specified <see cref="T:System.Object"/> is equal to the
	 *         current <see cref="T:System.Object"/>; otherwise, false.
	 */
	@Override
	public boolean equals(Object obj) {
		return equals(obj instanceof SessionHeader ? (SessionHeader) obj : null);
	}

	/**
	 * Serves as a hash function for a particular type.
	 * 
	 * @return A hash code for the current <see cref="T:System.Object"/>.
	 * 
	 *         
	 */
	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			calculateHash();
		}

		return this.hashCode;
	}

	/**
	 * Performance optimized status converter.
	 *
	 * @param name the name
	 * @return the session status
	 */
	private SessionStatus statusNameToStatus(String name) {
		SessionStatus status = SessionStatus.UNKNOWN;
		// first check using the casing it *should* be
		switch (name) {
		case "Running":
		case "running":
			status = SessionStatus.RUNNING;
			break;
		case "Normal":
		case "normal":
			status = SessionStatus.NORMAL;
			break;
		case "Crashed":
		case "crashed":
			status = SessionStatus.CRASHED;
			break;
		}

		// If we got a miss then convert the string and check again.
		if (status == SessionStatus.UNKNOWN) {
			switch (name.toLowerCase(Locale.ROOT)) {
			case "crashed":
				status = SessionStatus.CRASHED;
				break;
			case "normal":
				status = SessionStatus.NORMAL;
				break;
			case "running":
				status = SessionStatus.RUNNING;
				break;
			case "unknown":
			default:
				status = SessionStatus.UNKNOWN;
				break;
			}
		}

		return status;
	}

	/**
	 * Load stream.
	 *
	 * @param rawData the raw data
	 * @param length the length
	 * @return true, if successful
	 */
	private boolean loadStream(ByteBuffer rawData, int length) {
		boolean isValid = false;
		int startingPosition = rawData.position(); // mark it here

		// The current file version information.
		this.majorVersion = rawData.getInt();
		this.minorVersion = rawData.getInt();

		// Now check for compatibility.
		if (this.majorVersion > FileHeader.defaultMajorVersion) {
			// stop now - we don't know how to decode this.
			return false;
		}

		// Session information
		this.sessionId = BinarySerializer.deserializeUUIDValue(rawData);

		if (FileHeader.supportsComputerId(this.majorVersion, this.minorVersion)) {
			this.computerId = BinarySerializer.deserializeUUIDValue(rawData);
		}

		this.productName = BinarySerializer.deserializeStringValue(rawData);

		this.applicationName = BinarySerializer.deserializeStringValue(rawData);

		if (FileHeader.supportsEnvironmentAndPromotion(this.majorVersion, this.minorVersion)) {
			this.environmentName = BinarySerializer.deserializeStringValue(rawData);
			this.promotionLevelName = BinarySerializer.deserializeStringValue(rawData);
		}

		this.applicationVersion = new Version(BinarySerializer.deserializeStringValue(rawData));
		this.applicationTypeName = BinarySerializer.deserializeStringValue(rawData);
		this.applicationDescription = BinarySerializer.deserializeStringValue(rawData);
		this.caption = BinarySerializer.deserializeStringValue(rawData);
		this.timeZoneCaption = BinarySerializer.deserializeStringValue(rawData);
		this.sessionStatusName = BinarySerializer.deserializeStringValue(rawData);
		this.sessionStatus = statusNameToStatus(this.sessionStatusName);
		this.sessionStartDateTime = BinarySerializer.deserializeOffsetDateTimeValue(rawData);
		this.sessionEndDateTime = BinarySerializer.deserializeOffsetDateTimeValue(rawData);
		this.agentVersion = new Version(BinarySerializer.deserializeStringValue(rawData));
		this.userName = BinarySerializer.deserializeStringValue(rawData);
		this.userDomainName = BinarySerializer.deserializeStringValue(rawData);
		this.hostName = BinarySerializer.deserializeStringValue(rawData);
		this.dnsDomainName = BinarySerializer.deserializeStringValue(rawData);

		this.messageCount = rawData.getInt();
		this.criticalCount = rawData.getInt();
		this.errorCount = rawData.getInt();
		this.warningCount = rawData.getInt();

		// The session Details
		this.OSPlatformCode = rawData.getInt();
		this.OSVersion = new Version(BinarySerializer.deserializeStringValue(rawData));

		this.OSServicePack = BinarySerializer.deserializeStringValue(rawData);
		this.OSCultureName = BinarySerializer.deserializeStringValue(rawData);
		this.OSArchitecture = ProcessorArchitecture.valueOf(BinarySerializer.deserializeStringValue(rawData).toUpperCase());
		this.osBootMode = OSBootMode.valueOf(BinarySerializer.deserializeStringValue(rawData).toUpperCase());

		this.OSSuiteMaskCode = rawData.getInt();
		this.OSProductTypeCode = rawData.getInt();

		this.runtimeVersion = new Version(BinarySerializer.deserializeStringValue(rawData));
		this.runtimeArchitecture = ProcessorArchitecture.valueOf(BinarySerializer.deserializeStringValue(rawData).toUpperCase());

		this.currentCultureName = BinarySerializer.deserializeStringValue(rawData);
		this.currentUICultureName = BinarySerializer.deserializeStringValue(rawData);

		this.memoryMB = rawData.getInt();
		this.processors = rawData.getInt();
		this.processorCores = rawData.getInt();

		this.userInteractive = BinarySerializer.deserializeBooleanValue(rawData);
		this.terminalServer = BinarySerializer.deserializeBooleanValue(rawData);
		this.screenWidth = rawData.getInt();
		this.screenHeight = rawData.getInt();
		this.colorDepth = rawData.getInt();
		this.commandLine = BinarySerializer.deserializeStringValue(rawData);

		// now the application properties
		int numberOfProperties = rawData.getInt();
		for (int curProperty = 0; curProperty < numberOfProperties; curProperty++) {
			String name, value;
			name = BinarySerializer.deserializeStringValue(rawData);
			value = BinarySerializer.deserializeStringValue(rawData);
			this.properties.put(name, value);
		}

		// we may have been passed a length or not - if so trust it.
		long remainingLength = (length == 0) ? rawData.remaining() : length;
		if (rawData.position() < remainingLength - 4) {
			this.hasFileInfo = true;

			// BEGIN FILE INFO
			this.fileID = BinarySerializer.deserializeUUIDValue(rawData);
			this.fileSequence = rawData.getInt();

			this.fileStartDateTime = BinarySerializer.deserializeOffsetDateTimeValue(rawData);
			this.fileEndDateTime = BinarySerializer.deserializeOffsetDateTimeValue(rawData);
			this.lastFile = BinarySerializer.deserializeBooleanValue(rawData);
		}

		// now lets get the CRC and check it...
		int dataLength = rawData.position() - startingPosition; // be sure to offset for wherever the stream was when it
																// started

		// make a new copy of the header up to the start of the CRC
		byte[] headerBytes = new byte[dataLength];
		rawData.position(startingPosition); // ...and right here we'll blow up if this isn't a seakable stream. Be
											// warned.
		rawData.get(headerBytes, 0, headerBytes.length);

		// now read the CRC (this will leave the stream in the right position)
		byte[] crcValue = new byte[4];
		rawData.get(crcValue, 0, crcValue.length);

		byte[] crcComparision = BinarySerializer.calculateCRC(headerBytes, headerBytes.length);

		isValid = Arrays.equals(crcComparision, crcValue);

		return isValid;
	}

	/**
	 * Calculate hash.
	 */
	private void calculateHash() {
		int myHash = this.sessionId.hashCode(); // we're base class so we start the hashing.

		this.hashCode = myHash;
	}

}