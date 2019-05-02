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

/**
 * Used at the start of a data stream to contain the session summary
 * 
 * The session header subsumes a SessionStartInfoPacket, but both should be
 * included in a stream because the SessionHeader is really a cache of the
 * session start info packet that is easy to access.
 */
public final class SessionHeader implements ISessionSummary {
	private final Object lock = new Object();

	// You just can't do a binary format without recording version information
	private int majorVersion;
	private int minorVersion;

	// Stuff that aligns with SESSION table in index
	private UUID computerId;
	private UUID sessionId;
	private OffsetDateTime sessionStartDateTime;
	private OffsetDateTime sessionEndDateTime;
	private String caption;
	private String productName;
	private String applicationName;
	private String environmentName;
	private String promotionLevelName;
	private Version applicationVersion;
	private String applicationTypeName;
	private String applicationDescription;
	private String timeZoneCaption;
	private Version agentVersion;
	private String userName;
	private String userDomainName;
	private String hostName;
	private String dnsDomainName;
	private String sessionStatusName;
	private int messageCount;
	private int criticalCount;
	private int errorCount;
	private int warningCount;

	// Stuff that aligns with SESSION_DETAILS table in index
	private int OSPlatformCode;
	private Version OSVersion;
	private String OSServicePack;
	private String OSCultureName;
	private ProcessorArchitecture OSArchitecture;
	private OSBootMode osBootMode;
	private int OSSuiteMaskCode;
	private int OSProductTypeCode;
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
	private final Map<String, String> properties = new HashMap<String, String>();

	// file specific information (for this file)
	private boolean hasFileInfo;
	private UUID fileID;
	private OffsetDateTime fileStartDateTime;
	private OffsetDateTime fileEndDateTime;
	private boolean valid;
	private boolean lastFile;
	private int fileSequence;
	private int offsetSessionEndDateTime;
	private int offsetMessageCount;
	private int offsetCriticalCount;
	private int offsetErrorCount;
	private int offsetWarningCount;

	// cached serialized data (for when we're in a fixed representation and want
	// performance)
	private byte[] lastRawData; // raw data is JUST the session header stuff, not file or CRC, so you can't
								// return just it.

	private String fullyQualifiedUserName;
	private int hashCode;
	private SessionStatus sessionStatus;

	/**
	 * Create a new header from the provided session summary information
	 * 
	 * @param sessionSummary
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
	 * @param properties
	 */
	public SessionHeader(Map<String, String> properties) {
		this.majorVersion = FileHeader.defaultMajorVersion;
		this.minorVersion = FileHeader.defaultMinorVersion;

		this.properties.clear();
		this.properties.putAll(new HashMap<String, String>(properties));

		setIsNew(true);
	}

	/**
	 * Create a new session header by reading the provided byte array
	 * 
	 * @param data
	 */
	public SessionHeader(byte[] data) {
		this.valid = loadStream(ByteBuffer.wrap(data), data.length);
	}

	/**
	 * Create a new session header by reading the provided stream, which must
	 * contain ONLY the header
	 * 
	 * @param data
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
	 * Export the file header into a raw data array
	 * 
	 * @return
	 * @throws IOException 
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
	 * The major version of the binary format of the session header
	 */
	public int getMajorVersion() {
		return this.majorVersion;
	}

	public void setMajorVersion(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.majorVersion = value;
		}
	}

	/**
	 * The minor version of the binary format of the session header
	 */
	public int getMinorVersion() {
		return this.minorVersion;
	}

	public void setMinorVersion(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.minorVersion = value;
		}
	}

	/**
	 * The unique Id of the session
	 */
	@Override
	public UUID getId() {
		return this.sessionId;
	}

	public void setId(UUID value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.sessionId = value;
		}
	}

	/**
	 * The link to this item on the server
	 */
	@Override
	public URI getUri() {
		throw new UnsupportedOperationException("Links are not supported in this context");
	}

	/**
	 * Indicates if all of the session data is stored that is expected to be
	 * available
	 */
	@Override
	public boolean isComplete() {
		return this.hasFileInfo ? this.lastFile : false;
	}

	/**
	 * The unique Id of the computer
	 */
	@Override
	public UUID getComputerId() {
		return this.computerId;
	}

	public void setComputerId(UUID value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.computerId = value;
		}
	}

	/**
	 * A display caption for the session
	 */
	@Override
	public String getCaption() {
		return this.caption;
	}

	public void setCaption(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.caption = value;
		}
	}

	/**
	 * The product name of the application that recorded the session
	 */
	@Override
	public String getProduct() {
		return this.productName;
	}

	public void setProduct(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.productName = value;
		}
	}

	/**
	 * The title of the application that recorded the session
	 */
	@Override
	public String getApplication() {
		return this.applicationName;
	}

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
	 */
	@Override
	public String getEnvironment() {
		return this.environmentName;
	}

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
	 */
	@Override
	public String getPromotionLevel() {
		return this.promotionLevelName;
	}

	public void setPromotionLevel(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.promotionLevelName = value;
		}
	}

	/**
	 * The type of process the application ran as.
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
	 */
	public String getApplicationTypeName() {
		return this.applicationTypeName;
	}

	public void setApplicationTypeName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationTypeName = value;
		}
	}

	/**
	 * The description of the application from its manifest.
	 */
	@Override
	public String getApplicationDescription() {
		return this.applicationDescription;
	}

	public void setApplicationDescription(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationDescription = value;
		}
	}

	/**
	 * The version of the application that recorded the session
	 */
	@Override
	public Version getApplicationVersion() {
		return this.applicationVersion;
	}

	public void setApplicationVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.applicationVersion = value;
		}
	}

	/**
	 * The version of the Gibraltar Agent used to monitor the session
	 */
	@Override
	public Version getAgentVersion() {
		return this.agentVersion;
	}

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
	 */
	@Override
	public String getHostName() {
		return this.hostName;
	}

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
	 */
	@Override
	public String getDnsDomainName() {
		return this.dnsDomainName;
	}

	public void setDnsDomainName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.dnsDomainName = value;
		}
	}

	/**
	 * The display caption of the time zone where the session was recorded
	 */
	@Override
	public String getTimeZoneCaption() {
		return this.timeZoneCaption;
	}

	public void setTimeZoneCaption(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.timeZoneCaption = value;
		}
	}

	/**
	 * The user Id that was used to run the session
	 */
	@Override
	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.fullyQualifiedUserName = null;
			this.userName = value;
		}
	}

	/**
	 * The domain of the user id that was used to run the session
	 */
	@Override
	public String getUserDomainName() {
		return this.userDomainName;
	}

	public void setUserDomainName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.fullyQualifiedUserName = null;
			this.userDomainName = value;
		}
	}

	/**
	 * The fully qualified user name of the user the application was run as.
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
	 * The date and time the session started
	 */
	@Override
	public OffsetDateTime getStartDateTime() {
		return this.sessionStartDateTime;
	}

	public void setStartDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.sessionStartDateTime = value;
		}
	}

	/**
	 * The date and time the session started
	 */
	@Override
	public OffsetDateTime getDisplayStartDateTime() {
		return getStartDateTime();
	}

	/**
	 * The date and time the session ended or was last confirmed running
	 */
	@Override
	public OffsetDateTime getEndDateTime() {
		return this.sessionEndDateTime;
	}

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
	 * The date and time the session ended
	 */
	@Override
	public OffsetDateTime getDisplayEndDateTime() {
		return getEndDateTime();
	}

	/**
	 * The duration of the session. May be zero indicating unknown
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
	 */
	@Override
	public SessionStatus getStatus() {
		return this.sessionStatus;
	}

	/**
	 * The status of the session (based on the SessionStatus enumeration)
	 */
	public String getStatusName() {
		return this.sessionStatusName;
	}

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
	 * The total number of log messages recorded in the session
	 */
	@Override
	public int getMessageCount() {
		return this.messageCount;
	}

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
	 * The total number of critical severity log messages recorded in the session
	 */
	@Override
	public int getCriticalCount() {
		return this.criticalCount;
	}

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
	 * The total number of error severity log messages recorded in the session
	 */
	@Override
	public int getErrorCount() {
		return this.errorCount;
	}

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
	 * The total number of warning severity log messages recorded in the session
	 */
	@Override
	public int getWarningCount() {
		return this.warningCount;
	}

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
	 * pack or patches)
	 */
	@Override
	public Version getOSVersion() {
		return this.OSVersion;
	}

	public void setOSVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSVersion = value;
		}
	}

	/**
	 * The operating system service pack, if any.
	 */
	@Override
	public String getOSServicePack() {
		return this.OSServicePack;
	}

	public void setOSServicePack(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSServicePack = value;
		}
	}

	/**
	 * The culture name of the underlying operating system installation
	 */
	@Override
	public String getOSCultureName() {
		return this.OSCultureName;
	}

	public void setOSCultureName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSCultureName = value;
		}
	}

	/**
	 * The OS Platform code, nearly always 1 indicating Windows NT
	 */
	@Override
	public int getOSPlatformCode() {
		return this.OSPlatformCode;
	}

	public void setOSPlatformCode(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSPlatformCode = value;
		}
	}

	/**
	 * The OS product type code, used to differentiate specific editions of various
	 * operating systems.
	 */
	@Override
	public int getOSProductType() {
		return this.OSProductTypeCode;
	}

	public void setOSProductType(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSProductTypeCode = value;
		}
	}

	/**
	 * The OS Suite Mask, used to differentiate specific editions of various
	 * operating systems.
	 */
	@Override
	public int getOSSuiteMask() {
		return this.OSSuiteMaskCode;
	}

	public void setOSSuiteMask(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSSuiteMaskCode = value;
		}
	}

	/**
	 * The well known operating system family name, like Windows Vista or Windows
	 * Server 2003.
	 */
	@Override
	public String getOSFamilyName() {
		return ""; // BUG
	}

	/**
	 * The edition of the operating system without the family name, such as
	 * Workstation or Standard Server.
	 */
	@Override
	public String getOSEditionName() {
		return ""; // BUG
	}

	/**
	 * The well known OS name and edition name
	 */
	@Override
	public String getOSFullName() {
		return ""; // BUG
	}

	/**
	 * The well known OS name, edition name, and service pack like Windows XP
	 * Professional Service Pack 3
	 */
	@Override
	public String getOSFullNameWithServicePack() {
		return ""; // BUG
	}

	/**
	 * The processor architecture of the operating system.
	 */
	@Override
	public ProcessorArchitecture getOSArchitecture() {
		return this.OSArchitecture;
	}

	public void setOSArchitecture(ProcessorArchitecture value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.OSArchitecture = value;
		}
	}

	/**
	 * The boot mode of the operating system.
	 */
	@Override
	public OSBootMode getOSBootMode() {
		return this.osBootMode;
	}

	public void setOSBootMode(OSBootMode value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.osBootMode = value;
		}
	}

	/**
	 * The version of the .NET runtime that the application domain is running as.
	 */
	@Override
	public Version getRuntimeVersion() {
		return this.runtimeVersion;
	}

	public void setRuntimeVersion(Version value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.runtimeVersion = value;
		}
	}

	/**
	 * The processor architecture the process is running as.
	 */
	@Override
	public ProcessorArchitecture getRuntimeArchitecture() {
		return this.runtimeArchitecture;
	}

	public void setRuntimeArchitecture(ProcessorArchitecture value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.runtimeArchitecture = value;
		}
	}

	/**
	 * The current application culture name.
	 */
	@Override
	public String getCurrentCultureName() {
		return this.currentCultureName;
	}

	public void setCurrentCultureName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.currentCultureName = value;
		}
	}

	/**
	 * The current user interface culture name.
	 */
	@Override
	public String getCurrentUICultureName() {
		return this.currentUICultureName;
	}

	public void setCurrentUICultureName(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.currentUICultureName = value;
		}
	}

	/**
	 * The number of megabytes of installed memory in the host computer.
	 */
	@Override
	public int getMemoryMB() {
		return this.memoryMB;
	}

	public void setMemoryMB(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.memoryMB = value;
		}
	}

	/**
	 * The number of physical processor sockets in the host computer.
	 */
	@Override
	public int getProcessors() {
		return this.processors;
	}

	public void setProcessors(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.processors = value;
		}
	}

	/**
	 * The total number of processor cores in the host computer.
	 */
	@Override
	public int getProcessorCores() {
		return this.processorCores;
	}

	public void setProcessorCores(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.processorCores = value;
		}
	}

	/**
	 * Indicates if the session was run in a user interactive mode.
	 */
	@Override
	public boolean getUserInteractive() {
		return this.userInteractive;
	}

	public void setUserInteractive(boolean value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.userInteractive = value;
		}
	}

	/**
	 * Indicates if the session was run through terminal server. Only applies to
	 * User Interactive sessions.
	 */
	@Override
	public boolean getTerminalServer() {
		return this.terminalServer;
	}

	public void setTerminalServer(boolean value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.terminalServer = value;
		}
	}

	/**
	 * The number of pixels wide of the virtual desktop.
	 */
	@Override
	public int getScreenWidth() {
		return this.screenWidth;
	}

	public void setScreenWidth(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.screenWidth = value;
		}
	}

	/**
	 * The number of pixels tall for the virtual desktop.
	 */
	@Override
	public int getScreenHeight() {
		return this.screenHeight;
	}

	public void setScreenHeight(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.screenHeight = value;
		}
	}

	/**
	 * The number of bits of color depth.
	 */
	@Override
	public int getColorDepth() {
		return this.colorDepth;
	}

	public void setColorDepth(int value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.colorDepth = value;
		}
	}

	/**
	 * The complete command line used to execute the process including arguments.
	 */
	@Override
	public String getCommandLine() {
		return this.commandLine;
	}

	public void setCommandLine(String value) {
		synchronized (this.lock) {
			this.lastRawData = null;
			this.commandLine = value;
		}
	}

	/**
	 * The unique id of the file the session header is associated with
	 */
	public UUID getFileId() {
		return this.fileID;
	}

	public void setFileId(UUID value) {
		synchronized (this.lock) {
			this.fileID = value;
			this.hasFileInfo = true;
		}
	}

	/**
	 * The date and time that this file became the active file for the session
	 */
	public OffsetDateTime getFileStartDateTime() {
		return this.fileStartDateTime;
	}

	public void setFileStartDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			this.fileStartDateTime = value;
		}
	}

	/**
	 * The date and time that this file was no longer the active file for the
	 * session.
	 */
	public OffsetDateTime getFileEndDateTime() {
		return this.fileEndDateTime;
	}

	public void setFileEndDateTime(OffsetDateTime value) {
		synchronized (this.lock) {
			this.fileEndDateTime = value;
		}
	}

	/**
	 * The sequence of this file in the set of files for the session
	 */
	public int getFileSequence() {
		return this.fileSequence;
	}

	public void setFileSequence(int value) {
		synchronized (this.lock) {
			this.fileSequence = value;
		}
	}

	/**
	 * A collection of properties used to provided extended information about the
	 * session
	 */
	@Override
	public Map<String, String> getProperties() {
		return this.properties;
	}

	/**
	 * The date and time the session was added to the repository
	 */
	@Override
	public OffsetDateTime getAddedDateTime() {
		return this.sessionStartDateTime;
	}

	/**
	 * The date and time the session was added to the repository
	 */
	@Override
	public OffsetDateTime getDisplayAddedDateTime() {
		return this.sessionStartDateTime;
	}

	/**
	 * The date and time the session was added to the repository
	 */
	@Override
	public OffsetDateTime getUpdatedDateTime() {
		return this.fileEndDateTime;
	}

	/**
	 * The date and time the session was added to the repository
	 */
	@Override
	public OffsetDateTime getDisplayUpdatedDateTime() {
		return this.fileEndDateTime;
	}

	/**
	 * True if this is the last file recorded for the session.
	 */
	public boolean isLastFile() {
		return this.lastFile;
	}

	public void setIsLastFile(boolean value) {
		synchronized (this.lock) {
			this.lastFile = value;
		}
	}

	/**
	 * True if the session header is valid (has not been corrupted)
	 * 
	 * @return
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

	@Override
	public boolean isNew() {
		return this.isNew;
	}

	public void setIsNew(boolean value) {
		this.isNew = value;
	}

	/**
	 * Indicates if the session is currently running and a live stream is available.
	 */
	private boolean isLive;

	@Override
	public boolean isLive() {
		return this.isLive;
	}

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

	@Override
	public boolean getHasData() {
		return this.hasData;
	}

	public void setHasData(boolean value) {
		this.hasData = value;
	}

	/**
	 * True if the session header contains the extended file information
	 */
	public boolean getHasFileInfo() {
		return this.hasFileInfo;
	}

	/**
	 * Indicates if the binary stream supports fragments or only single-stream
	 * transfer (the pre-3.0 format)
	 */
	public boolean getSupportsFragments() {
		return FileHeader.supportsFragments(this.majorVersion, this.minorVersion);
	}

	/**
	 * Indicates whether the current object is equal to another object of the same
	 * type.
	 * 
	 * @return true if the current object is equal to the <paramref name="other"/>
	 *         parameter; otherwise, false.
	 * 
	 * @param other An object to compare with this object.
	 * 
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
	 * @return true if the specified <see cref="T:System.Object"/> is equal to the
	 *         current <see cref="T:System.Object"/>; otherwise, false.
	 * 
	 * @param obj The <see cref="T:System.Object"/> to compare with the current
	 *            <see cref="T:System.Object"/>.
	 * @exception T:System.NullReferenceException The <paramref name="obj"/>
	 *                                            parameter is null.
	 *                                            <filterpriority>2</filterpriority>
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
	 *         <filterpriority>2</filterpriority>
	 */
	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			calculateHash();
		}

		return this.hashCode;
	}

	/**
	 * Performance optimized status converter
	 * 
	 * @param name
	 * @return
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

	private void calculateHash() {
		int myHash = this.sessionId.hashCode(); // we're base class so we start the hashing.

		this.hashCode = myHash;
	}

}