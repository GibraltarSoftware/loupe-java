package com.onloupe.core.server.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onloupe.core.util.TimeConversion;
import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.data.ProcessorArchitecture;
import com.onloupe.model.exception.GibraltarException;
import com.onloupe.model.log.LogMessageSeverity;
import com.onloupe.model.session.ISessionSummary;
import com.onloupe.model.system.OSBootMode;

/**
 * Convert between data representations of common repository objects
 */
public final class DataConverter {

	private static final DocumentBuilderFactory documentBuilderFactory;
	private static final DocumentBuilderFactory documentBuilderFactoryNSAware;
	private static final TransformerFactory transformerFactory;

	
	static {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactoryNSAware = DocumentBuilderFactory.newInstance();
		documentBuilderFactoryNSAware.setNamespaceAware(true);
		
		transformerFactory = TransformerFactory.newInstance();
	}

	/**
	 * Extract all of the data fields from a folder XML structure, validating it.
	 * 
	 * @param folderXml
	 * @param id
	 * @param version
	 * @param deleted
	 * @param name
	 * @param parentFolderId
	 * @param typeName
	 * @param selectCriteriaXml
	 * @param invalidMessage
	 * @return True if the structure is valid, false otherwise.
	 */
	public static boolean fromFolderXml(FolderXml folderXml, UUID id, Long version, Boolean deleted, String name,
			UUID parentFolderId, String typeName, String selectCriteriaXml, String invalidMessage) {
		boolean valid = true;

		id = UUID.fromString(folderXml.getid());
		version = folderXml.getversion();
		deleted = folderXml.getdeleted();
		invalidMessage = "";
		name = null;
		typeName = null;
		parentFolderId = null;
		selectCriteriaXml = "";

		if (folderXml.getfolderDetail() == null) {
			valid = false;
			invalidMessage = "No folder details were provided\r\n";
		} else {
			FolderDetailXml detail = folderXml.getfolderDetail();

			name = detail.getname();
			if (TypeUtils.isBlank(name)) {
				valid = false;
				invalidMessage += "No folder name was provided \r\n";
			}

			if (!TypeUtils.isBlank(detail.getparentFolderId())) {
				// if there's anything then it HAS to be a valid GUID.
				try {
					parentFolderId = UUID.fromString(detail.getparentFolderId());
				} catch (RuntimeException ex) {

					valid = false;
					invalidMessage += "The parent folder id wasn't valid\r\n";
				}
			}

			FolderTypeXml folderType = detail.getfolderType();
			if (folderType == FolderTypeXml.SEARCH) {
				selectCriteriaXml = detail.getselectionCriteriaXml();
				if (TypeUtils.isBlank(selectCriteriaXml)) {
					valid = false;
					invalidMessage += "No criteria were specified for the search folder\r\n";
				}
			}
			typeName = detail.getfolderType().toString();
		}

		return valid;
	}

	/**
	 * Convert a byte array to a Server Configuration XML object without relying on
	 * XML Serializer
	 * 
	 * @param rawData
	 * @return
	 * @throws Exception
	 */
	public static HubConfigurationXml byteArrayToHubConfigurationXml(byte[] rawData) throws Exception {
		HubConfigurationXml configurationXml = new HubConfigurationXml();

		try (ByteArrayInputStream documentStream = new ByteArrayInputStream(rawData)) {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document xml = documentBuilder.parse(documentStream);

			Element hubConfigurationNode = xml.getDocumentElement();

			if (hubConfigurationNode == null) {
				throw new GibraltarException("There is no server configuration data in the provided raw data");
			}

			// read up our attributes
			String id = hubConfigurationNode.getAttribute("id");
			if (TypeUtils.isNotBlank(id)) {
				configurationXml.setid(id);
			}

			String redirectRequested = hubConfigurationNode.getAttribute("redirectRequested");
			if (TypeUtils.isNotBlank(redirectRequested)) {
				configurationXml.setredirectRequested(Boolean.parseBoolean(redirectRequested));
			}

			String status = hubConfigurationNode.getAttribute("status");
			if (TypeUtils.isNotBlank(status)) {
				configurationXml.setstatus(HubStatusXml.valueOf(status.toUpperCase()));
			}

			String timeToLive = hubConfigurationNode.getAttribute("timeToLive");
			if (TypeUtils.isNotBlank(timeToLive)) {
				configurationXml.settimeToLive(Integer.parseInt(timeToLive));
			}

			String protocolVersion = hubConfigurationNode.getAttribute("protocolVersion");
			if (TypeUtils.isNotBlank(protocolVersion)) {
				configurationXml.setprotocolVersion(protocolVersion);
			}

			// we only read redirect information if we actually got a redirect request.
			if (configurationXml.getredirectRequested()) {
				String redirectApplicationBaseDirectory = hubConfigurationNode
						.getAttribute("redirectApplicationBaseDirectory");
				if (TypeUtils.isNotBlank(redirectApplicationBaseDirectory)) {
					configurationXml.setredirectApplicationBaseDirectory(redirectApplicationBaseDirectory);
				}

				String redirectCustomerName = hubConfigurationNode.getAttribute("redirectCustomerName");
				if (TypeUtils.isNotBlank(redirectCustomerName)) {
					configurationXml.setredirectCustomerName(redirectCustomerName);
				}

				String redirectHostName = hubConfigurationNode.getAttribute("redirectHostName");
				if (TypeUtils.isNotBlank(redirectHostName)) {
					configurationXml.setredirectHostName(redirectHostName);
				}

				String redirectPort = hubConfigurationNode.getAttribute("redirectPort");
				if (TypeUtils.isNotBlank(redirectPort)) {
					configurationXml.setredirectPort(Integer.parseInt(redirectPort));
					configurationXml.setredirectPortSpecified(true);
				}

				String redirectUseGibraltarSds = hubConfigurationNode.getAttribute("redirectUseGibraltarSds");
				if (TypeUtils.isNotBlank(redirectUseGibraltarSds)) {
					configurationXml.setredirectUseGibraltarSds(Boolean.parseBoolean(redirectUseGibraltarSds));
					configurationXml.setredirectUseGibraltarSdsSpecified(true);
				}

				String redirectUseSsl = hubConfigurationNode.getAttribute("redirectUseSsl");
				if (TypeUtils.isNotBlank(redirectUseSsl)) {
					configurationXml.setredirectUseSsl(Boolean.parseBoolean(redirectUseSsl));
					configurationXml.setredirectUseSslSpecified(true);
				}
			}

			// now move on to the child elements.. I'm avoiding XPath to avoid failure due
			// to XML schema variations
			if (hubConfigurationNode.hasChildNodes()) {
				NodeList nodeList = hubConfigurationNode.getChildNodes();
				Node expirationDtNode = null;
				Node publicKeyNode = null;
				Node liveStreamNode = null;
				for (int i = 0; i < nodeList.getLength(); i++) {
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						switch (node.getNodeName()) {
						case "expirationDt":
							expirationDtNode = node;
							break;
						case "publicKey":
							publicKeyNode = node;
							break;
						case "liveStream":
							liveStreamNode = node;
							break;
						default:
							break;
						}

						if ((expirationDtNode != null) && (publicKeyNode != null) && (liveStreamNode != null)) {
							break;
						}
					}
				}

				if (expirationDtNode != null) {
					Element expirationDtElement = (Element) expirationDtNode;
					String dateTimeRaw = expirationDtElement.getAttribute("DateTime");
					String timeZoneOffset = expirationDtElement.getAttribute("Offset");

					if (TypeUtils.isNotBlank(dateTimeRaw) && TypeUtils.isNotBlank(timeZoneOffset)) {
						configurationXml.setexpirationDt(new DateTimeOffsetXml());
						configurationXml.getexpirationDt().setDateTime(LocalDateTime.parse(dateTimeRaw));
						configurationXml.getexpirationDt().setOffset(Integer.parseInt(timeZoneOffset));
					}
				}

				if (publicKeyNode != null) {
					configurationXml.setpublicKey(publicKeyNode.getNodeValue());
				}

				if (liveStreamNode != null) {
					Element liveStreamElement = (Element) liveStreamNode;
					String agentPortRaw = liveStreamElement.getAttribute("agentPort");
					String clientPortRaw = liveStreamElement.getAttribute("clientPort");
					String useSslRaw = liveStreamElement.getAttribute("useSsl");

					if (TypeUtils.isNotBlank(agentPortRaw) && TypeUtils.isNotBlank(clientPortRaw)
							&& TypeUtils.isNotBlank(useSslRaw)) {
						configurationXml.setliveStream(new LiveStreamServerXml());
						configurationXml.getliveStream().setagentPort(Integer.parseInt(agentPortRaw));
						configurationXml.getliveStream().setclientPort(Integer.parseInt(clientPortRaw));
						configurationXml.getliveStream().setuseSsl(Boolean.parseBoolean(useSslRaw));
					}
				}
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw e;
		}

		return configurationXml;
	}

	/**
	 * Convert a byte array to sessions list XML object without relying on XML
	 * Serializer
	 * 
	 * @param rawData
	 * @return
	 * @throws Exception
	 */
	public static SessionsListXml byteArrayToSessionsListXml(byte[] rawData) throws Exception {
		SessionsListXml sessionsListXml = new SessionsListXml();
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(rawData)) {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document xml = documentBuilder.parse(inputStream);

			// to load up the first element.
			NodeList sessionsXmlList = xml.getElementsByTagName("SessionsListXml");
			if (sessionsXmlList.getLength() != 1) {
				// it isn't a sessions list..
				throw new GibraltarException("The provided XML data is not a sessions list");
			}

			Element sessionList = (Element) sessionsXmlList.item(0);

			String sessionsVersionRaw = sessionList.getAttribute("version");
			if (TypeUtils.isNotBlank(sessionsVersionRaw)) {
				sessionsListXml.setversion(Long.parseLong(sessionsVersionRaw));
			}

			NodeList sessionsNodes = sessionList.getElementsByTagName("sessions");
			if (sessionsNodes.getLength() == 1) {
				NodeList sessionNodes = sessionsNodes.item(0).getChildNodes();
				List<SessionXml> sessions = new ArrayList<SessionXml>();
				for (int i = 0; i < sessionNodes.getLength(); i++) {
					Element element = (Element) sessionNodes.item(i);
					if (element.getLocalName().equals("session")) {
						String guidRaw = element.getAttribute("id");
						String versionRaw = element.getAttribute("version");
						String deletedRaw = element.getAttribute("deleted");

						// now convert to a SessionXml object and add to the item.
						SessionXml newSession = new SessionXml();
						newSession.setid(guidRaw);
						newSession.setversion(Long.parseLong(versionRaw));
						newSession.setdeleted(Boolean.parseBoolean(deletedRaw));
						sessions.add(newSession);
					}
				}
				sessionsListXml.setsessions(sessions.toArray(new SessionXml[0]));
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw e;
		}

		return sessionsListXml;
	}

	/**
	 * Convert a raw byte array to a session files list without using .NET XML
	 * Serialization
	 * 
	 * @param rawData
	 * @return
	 * @throws Exception
	 */
	public static SessionFilesListXml byteArrayToSessionFilesListXml(byte[] rawData) throws Exception {
		SessionFilesListXml sessionsFilesListXml = new SessionFilesListXml();
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(rawData)) {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document xml = documentBuilder.parse(inputStream);

			// to load up the first element.
			NodeList sessionsXmlList = xml.getElementsByTagName("SessionFilesListXml");
			Element sessionList = (Element) sessionsXmlList.item(0);
			if (sessionList == null) {
				// it isn't a sessions list..
				throw new GibraltarException("The provided XML data is not a sessions list");
			}

			String sessionsVersionRaw = sessionList.getAttribute("version");
			if (TypeUtils.isNotBlank(sessionsVersionRaw)) {
				sessionsFilesListXml.setversion(Long.parseLong(sessionsVersionRaw));
			}

			NodeList filesNodes = sessionList.getElementsByTagName("sessions");
			if (filesNodes.getLength() == 1) {
				// this is a repeating section so we have to be ready for that...
				List<SessionFileXml> files = new ArrayList<SessionFileXml>();
				NodeList sessionNodes = filesNodes.item(0).getChildNodes();
				for (int i = 0; i < sessionNodes.getLength(); i++) {
					Element element = (Element) sessionNodes.item(i);
					if (element.getLocalName().equals("file")) {
						String guidRaw = element.getAttribute("id");
						String versionRaw = element.getAttribute("version");
						String sequenceRaw = element.getAttribute("sequence");

						// now convert to a SessionFileXml object and add to the item.
						SessionFileXml newFile = new SessionFileXml();
						newFile.setid(guidRaw);
						newFile.setsequence(Integer.parseInt(sequenceRaw));
						newFile.setversion(Long.parseLong(versionRaw));
						files.add(newFile);
					}
				}
				sessionsFilesListXml.setfiles(files.toArray(new SessionFileXml[0]));
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw e;
		}

		return sessionsFilesListXml;
	}

	/**
	 * Converts a session XML object to a byte array without relying on XML
	 * Serializer
	 * 
	 * @param sessionXml
	 * @return
	 * @throws ParserConfigurationException
	 */
	public static byte[] sessionXmlToByteArray(SessionXml sessionXml) throws Exception {
		Document xml = documentBuilderFactoryNSAware.newDocumentBuilder().newDocument();

		Element sessionXmlElement = xml.createElement("SessionXml");
		xml.appendChild(sessionXmlElement);

		sessionXmlElement.setAttribute("id", sessionXml.getid());
		sessionXmlElement.setAttribute("version", String.valueOf(sessionXml.getversion()));
		sessionXmlElement.setAttribute("deleted", String.valueOf(sessionXml.getdeleted()));

		if (sessionXml.getisCompleteSpecified()) {
			sessionXmlElement.setAttribute("isComplete", String.valueOf(sessionXml.getisComplete()));
		}

		// start the session detail
		SessionDetailXml detailXml = sessionXml.getsessionDetail();

		if (detailXml != null) {
			Element detailXmlElement = xml.createElement("sessionDetail");
			sessionXmlElement.appendChild(detailXmlElement);

			detailXmlElement.setAttribute("productName", detailXml.getproductName());
			detailXmlElement.setAttribute("applicationName", detailXml.getapplicationName());
			detailXmlElement.setAttribute("environmentName", detailXml.getenvironmentName());
			detailXmlElement.setAttribute("promotionLevelName", detailXml.getpromotionLevelName());
			detailXmlElement.setAttribute("applicationVersion", detailXml.getapplicationVersion());
			detailXmlElement.setAttribute("applicationType", detailXml.getapplicationType().toString()); // enums
																											// won't
																											// auto-convert
			detailXmlElement.setAttribute("applicationDescription", detailXml.getapplicationDescription());
			detailXmlElement.setAttribute("caption", detailXml.getcaption());
			detailXmlElement.setAttribute("status", detailXml.getstatus().toString()); // enums won't
																						// auto-convert
			detailXmlElement.setAttribute("timeZoneCaption", detailXml.gettimeZoneCaption());
			detailXmlElement.setAttribute("durationSec", String.valueOf(detailXml.getDurationSec()));
			detailXmlElement.setAttribute("agentVersion", detailXml.getagentVersion());
			detailXmlElement.setAttribute("userName", detailXml.getuserName());
			detailXmlElement.setAttribute("userDomainName", detailXml.getuserDomainName());
			detailXmlElement.setAttribute("hostName", detailXml.gethostName());
			detailXmlElement.setAttribute("dnsDomainName", detailXml.getdnsDomainName());
			detailXmlElement.setAttribute("isNew", String.valueOf(detailXml.getisNew()));
			detailXmlElement.setAttribute("isComplete", String.valueOf(detailXml.getisComplete()));
			detailXmlElement.setAttribute("messageCount", String.valueOf(detailXml.getmessageCount()));
			detailXmlElement.setAttribute("criticalMessageCount", String.valueOf(detailXml.getcriticalMessageCount()));
			detailXmlElement.setAttribute("errorMessageCount", String.valueOf(detailXml.geterrorMessageCount()));
			detailXmlElement.setAttribute("warningMessageCount", String.valueOf(detailXml.getwarningMessageCount()));
			detailXmlElement.setAttribute("updateUser", detailXml.getupdateUser());
			detailXmlElement.setAttribute("osPlatformCode", String.valueOf(detailXml.getosPlatformCode()));
			detailXmlElement.setAttribute("osVersion", detailXml.getosVersion());
			detailXmlElement.setAttribute("osServicePack", detailXml.getosServicePack());
			detailXmlElement.setAttribute("osCultureName", detailXml.getosCultureName());
			detailXmlElement.setAttribute("osArchitecture", detailXml.getosArchitecture().toString()); // enums
																										// won't
																										// auto-convert
			detailXmlElement.setAttribute("osBootMode", detailXml.getosBootMode().toString()); // enums won't
																								// auto-convert
			detailXmlElement.setAttribute("osSuiteMaskCode", String.valueOf(detailXml.getosSuiteMaskCode()));
			detailXmlElement.setAttribute("osProductTypeCode", String.valueOf(detailXml.getosProductTypeCode()));
			detailXmlElement.setAttribute("runtimeVersion", detailXml.getruntimeVersion());
			detailXmlElement.setAttribute("runtimeArchitecture", detailXml.getruntimeArchitecture().toString()); // enums
																													// won't
																													// auto-convert
			detailXmlElement.setAttribute("currentCultureName", detailXml.getcurrentCultureName());
			detailXmlElement.setAttribute("currentUiCultureName", detailXml.getcurrentUiCultureName());
			detailXmlElement.setAttribute("memoryMb", String.valueOf(detailXml.getmemoryMb()));
			detailXmlElement.setAttribute("processors", String.valueOf(detailXml.getprocessors()));
			detailXmlElement.setAttribute("processorCores", String.valueOf(detailXml.getprocessorCores()));
			detailXmlElement.setAttribute("userInteractive", String.valueOf(detailXml.getuserInteractive()));
			detailXmlElement.setAttribute("terminalServer", String.valueOf(detailXml.getterminalServer()));
			detailXmlElement.setAttribute("screenWidth", String.valueOf(detailXml.getscreenWidth()));
			detailXmlElement.setAttribute("screenHeight", String.valueOf(detailXml.getscreenHeight()));
			detailXmlElement.setAttribute("colorDepth", String.valueOf(detailXml.getcolorDepth()));
			detailXmlElement.setAttribute("commandLine", detailXml.getcommandLine());
			detailXmlElement.setAttribute("fileSize", String.valueOf(detailXml.getfileSize()));
			detailXmlElement.setAttribute("fileAvailable", String.valueOf(detailXml.getfileAvailable()));
			detailXmlElement.setAttribute("computerId", detailXml.getcomputerId());

			// and now the elements
			dateTimeOffsetXmlToXmlWriter(detailXmlElement, "startDt", detailXml.getstartDt());
			dateTimeOffsetXmlToXmlWriter(detailXmlElement, "endDt", detailXml.getendDt());
			dateTimeOffsetXmlToXmlWriter(detailXmlElement, "addedDt", detailXml.getaddedDt());
			dateTimeOffsetXmlToXmlWriter(detailXmlElement, "updatedDt", detailXml.getupdatedDt());
		}

		xml.setXmlStandalone(true);
		DOMSource domSource = new DOMSource(xml);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(domSource, result);
		return writer.toString().getBytes();
	}

	private static void dateTimeOffsetXmlToXmlWriter(Element parent, String elementName,
			DateTimeOffsetXml dateTimeOffsetXml) {
		if (dateTimeOffsetXml == null) {
			return;
		}

		Element dateTimeOffsetElement = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(dateTimeOffsetElement);

		// RKELLIHER TODO format
		dateTimeOffsetElement.setAttribute("DateTime", dateTimeOffsetXml.getDateTime().toString());
		dateTimeOffsetElement.setAttribute("Offset", String.valueOf(dateTimeOffsetXml.getOffset()));
	}

	/**
	 * convert a client repository set of information from its field form to XML
	 * 
	 * @return
	 */
	public static ClientRepositoryXml toClientRepositoryXml(UUID id, String hostName, String computerKey,
			String statusName, OffsetDateTime addedDt, long currentSessionsVersion, String publicKey,
			OffsetDateTime lastContactDt) {
		ClientRepositoryXml newObject = new ClientRepositoryXml();

		newObject.setid(id.toString());
		newObject.setaddedDt(toDateTimeOffsetXml(addedDt));
		newObject.setcomputerKey(computerKey);
		newObject.setcurrentSessionsVersion(currentSessionsVersion);
		newObject.sethostName(hostName);
		newObject.setlastContactDt(toDateTimeOffsetXml(lastContactDt));
		newObject.setpublicKey(publicKey);
		newObject.setstatus(toClientRepositoryStatusXml(statusName));

		return newObject;
	}

	/**
	 * Create a single session XML object from its minimal raw information.
	 * 
	 * @param id
	 * @param version
	 * @param deleted
	 * @return
	 */
	public static SessionXml toSessionXml(UUID id, long version, boolean deleted) {
		SessionXml newObject = new SessionXml();

		newObject.setid(id.toString());
		newObject.setversion(version);
		newObject.setdeleted(deleted);

		return newObject;
	}

	/**
	 * Create a sessionXml from the session summary provided
	 * 
	 * @return
	 */
	public static SessionXml toSessionXml(ISessionSummary sessionSummary) {
		if (sessionSummary == null) {
			throw new NullPointerException("sessionSummary");
		}

		SessionXml newObject = toSessionXml(sessionSummary.getId(), 0L, false, false, true,
				sessionSummary.getStartDateTime(), sessionSummary.getEndDateTime(),
				sessionSummary.getFullyQualifiedUserName(), sessionSummary.getProduct(),
				sessionSummary.getApplication(), sessionSummary.getEnvironment(), sessionSummary.getPromotionLevel(),
				sessionSummary.getApplicationVersion().toString(), sessionSummary.getApplicationType().toString(),
				sessionSummary.getApplicationDescription(), sessionSummary.getCaption(),
				sessionSummary.getStatus().toString(), sessionSummary.getTimeZoneCaption(),
				sessionSummary.getStartDateTime(), sessionSummary.getEndDateTime(), sessionSummary.getDuration(),
				sessionSummary.getAgentVersion().toString(), sessionSummary.getUserName(),
				sessionSummary.getUserDomainName(), sessionSummary.getHostName(), sessionSummary.getDnsDomainName(),
				sessionSummary.getMessageCount(), sessionSummary.getCriticalCount(), sessionSummary.getErrorCount(),
				sessionSummary.getWarningCount(), sessionSummary.getOSPlatformCode(),
				sessionSummary.getOSVersion().toString(), sessionSummary.getOSServicePack(),
				sessionSummary.getOSCultureName(), sessionSummary.getOSArchitecture().toString(),
				sessionSummary.getOSBootMode().toString(), sessionSummary.getOSSuiteMask(),
				sessionSummary.getOSProductType(), sessionSummary.getRuntimeVersion().toString(),
				sessionSummary.getRuntimeArchitecture().toString(), sessionSummary.getCurrentCultureName(),
				sessionSummary.getCurrentUICultureName(), sessionSummary.getMemoryMB(), sessionSummary.getProcessors(),
				sessionSummary.getProcessorCores(), sessionSummary.getUserInteractive(),
				sessionSummary.getTerminalServer(), sessionSummary.getScreenWidth(), sessionSummary.getScreenHeight(),
				sessionSummary.getColorDepth(), sessionSummary.getCommandLine(), false, 0,
				sessionSummary.getProperties());

		return newObject;
	}

	/**
	 * Create a single session XML object from its detail information
	 */
	public static SessionXml toSessionXml(UUID id, long version, boolean deleted, boolean isComplete, boolean isNew,
			OffsetDateTime addedDt, OffsetDateTime updatedDt, String updatedUser, String productName,
			String applicationName, String environmentName, String promotionLevelName, String applicationVersion,
			String applicationTypeName, String applicationDescription, String caption, String statusName,
			String timeZoneCaption, OffsetDateTime startDt, OffsetDateTime endDt, Duration duration,
			String agentVersion, String userName, String userDomainName, String hostName, String dNSDomainName,
			int messageCount, int criticalMessageCount, int errorMessageCount, int warningMessageCount,
			int oSPlatformCode, String oSVersion, String oSServicePack, String oSCultureName, String oSArchitectureName,
			String oSBootModeName, int oSSuiteMaskCode, int oSProductTypeCode, String runtimeVersion,
			String runtimeArchitectureName, String currentCultureName, String currentUICultureName, int memoryMB,
			int processors, int processorCores, boolean userInteractive, boolean terminalServer, int screenWidth,
			int screenHeight, int colorDepth, String commandLine, boolean fileAvailable, int fileSize,
			Map<String, String> properties) {
		SessionXml newObject = toSessionXml(id, version, deleted);

		SessionDetailXml newDetailObject = new SessionDetailXml();

		newDetailObject.setaddedDt(toDateTimeOffsetXml(addedDt));
		newDetailObject.setagentVersion(agentVersion);
		newDetailObject.setapplicationDescription(applicationDescription);
		newDetailObject.setapplicationName(applicationName);
		newDetailObject.setenvironmentName(environmentName);
		newDetailObject.setpromotionLevelName(promotionLevelName);
		newDetailObject.setapplicationType(toApplicationTypeXml(applicationTypeName));
		newDetailObject.setapplicationVersion(applicationVersion);
		newDetailObject.setcaption(caption);
		newDetailObject.setcolorDepth(colorDepth);
		newDetailObject.setcommandLine(commandLine);
		newDetailObject.setcriticalMessageCount(criticalMessageCount);
		newDetailObject.setcurrentCultureName(currentCultureName);
		newDetailObject.setcurrentUiCultureName(currentUICultureName);
		newDetailObject.setdnsDomainName(dNSDomainName);
		newDetailObject.setDurationSec(duration != null ? TimeUnit.NANOSECONDS.toSeconds(duration.toNanos()) : 0);
		newDetailObject.setendDt(toDateTimeOffsetXml(endDt));
		newDetailObject.seterrorMessageCount(errorMessageCount);
		newDetailObject.setfileAvailable(fileAvailable);
		newDetailObject.setfileSize(fileSize);
		newDetailObject.sethostName(hostName);
		newDetailObject.setisComplete(isComplete);
		newDetailObject.setisNew(isNew);
		newDetailObject.setmemoryMb(memoryMB);
		newDetailObject.setmessageCount(messageCount);
		newDetailObject.setosArchitecture(toProcessorArchitectureXml(oSArchitectureName));
		newDetailObject.setosBootMode(toBootModeXml(oSBootModeName));
		newDetailObject.setosCultureName(oSCultureName);
		newDetailObject.setosPlatformCode(oSPlatformCode);
		newDetailObject.setosProductTypeCode(oSProductTypeCode);
		newDetailObject.setosServicePack(oSServicePack);
		newDetailObject.setosSuiteMaskCode(oSSuiteMaskCode);
		newDetailObject.setosVersion(oSVersion);
		newDetailObject.setprocessorCores(processorCores);
		newDetailObject.setprocessors(processors);
		newDetailObject.setproductName(productName);
		newDetailObject.setruntimeArchitecture(toProcessorArchitectureXml(runtimeArchitectureName));
		newDetailObject.setruntimeVersion(runtimeVersion);
		newDetailObject.setscreenHeight(screenHeight);
		newDetailObject.setscreenWidth(screenWidth);
		newDetailObject.setstartDt(toDateTimeOffsetXml(startDt));
		newDetailObject.setstatus(toSessionStatusXml(statusName));
		newDetailObject.setterminalServer(terminalServer);
		newDetailObject.settimeZoneCaption(timeZoneCaption);
		newDetailObject.setupdatedDt(toDateTimeOffsetXml(updatedDt));
		newDetailObject.setupdateUser(updatedUser);
		newDetailObject.setuserDomainName(userDomainName);
		newDetailObject.setuserInteractive(userInteractive);
		newDetailObject.setuserName(userName);
		newDetailObject.setwarningMessageCount(warningMessageCount);
		newDetailObject.setproperties(toSessionPropertiesXml(properties));
		newObject.setsessionDetail(newDetailObject);

		return newObject;
	}

	/**
	 * Convert a properties dictionary to a session property XML array
	 * 
	 * @param properties
	 * @return
	 */
	public static SessionPropertyXml[] toSessionPropertiesXml(Map<String, String> properties) {
		if ((properties == null) || (properties.isEmpty())) {
			return null;
		}

		ArrayList<SessionPropertyXml> propertiesXml = new ArrayList<SessionPropertyXml>(properties.size());

		for (Map.Entry<String, String> property : properties.entrySet()) {
			SessionPropertyXml currentProperty = toSessionPropertyXml(UUID.randomUUID(), property.getKey(),
					property.getValue()); // we are banking on it not actually using that ID any
											// more....
			propertiesXml.add(currentProperty);
		}

		return propertiesXml.toArray(new SessionPropertyXml[0]);
	}

	/**
	 * Create a single session property object from its raw data elements.
	 * 
	 * @param id
	 * @param name
	 * @param value
	 * @return
	 */
	public static SessionPropertyXml toSessionPropertyXml(UUID id, String name, String value) {
		SessionPropertyXml newObject = new SessionPropertyXml();

		newObject.setid(id.toString());
		newObject.setname(name);
		newObject.setvalue(value);

		return newObject;
	}

	/**
	 * Create a single session file object from its raw data elements.
	 * 
	 * @param id
	 * @param sequence
	 * @param version
	 * @return
	 */
	public static SessionFileXml toSessionFileXml(UUID id, int sequence, long version) {
		SessionFileXml newObject = new SessionFileXml();

		newObject.setid(id.toString());
		newObject.setsequence(sequence);
		newObject.setversion(version);

		return newObject;
	}

	/**
	 * Convert an application type to its XML equivalent.
	 * 
	 * @param typeName
	 * @return
	 */
	public static ApplicationTypeXml toApplicationTypeXml(String typeName) {
		return ApplicationTypeXml.valueOf(typeName);
	}

	/**
	 * Convert a boot mode to its XML equivalent
	 * 
	 * @param typeName
	 * @return
	 */
	public static BootModeXml toBootModeXml(String typeName) {
		return BootModeXml.valueOf(typeName);
	}

	/**
	 * Convert a boot mode to its XML equivalent
	 * 
	 * @param statusName
	 * @return
	 */
	public static ClientRepositoryStatusXml toClientRepositoryStatusXml(String statusName) {
		return ClientRepositoryStatusXml.valueOf(statusName);
	}

	/**
	 * Convert a folder type to its XML equivalent
	 * 
	 * @param typeName
	 * @return
	 */
	public static FolderTypeXml toFolderType(String typeName) {
		return FolderTypeXml.valueOf(typeName);
	}

	/**
	 * Convert a processor architecture to its XML equivalent.
	 * 
	 * @param statusName
	 * @return
	 */
	public static ProcessorArchitectureXml toProcessorArchitectureXml(String statusName) {
		return ProcessorArchitectureXml.valueOf(statusName);
	}

	/**
	 * Convert a session status to its XML equivalent.
	 * 
	 * @param statusName
	 * @return
	 */
	public static SessionStatusXml toSessionStatusXml(String statusName) {
		return SessionStatusXml.valueOf(statusName);
	}

	/**
	 * Convert a DateTimeOffset value to its XML equivalent.
	 * 
	 * @param dateTime
	 * @return
	 */
	public static DateTimeOffsetXml toDateTimeOffsetXml(OffsetDateTime dateTime) {
		DateTimeOffsetXml newObject = new DateTimeOffsetXml();
		newObject.setDateTime(dateTime.toLocalDateTime());
		newObject.setOffset((int) TimeUnit.SECONDS.toMinutes(dateTime.getOffset().getTotalSeconds()));

		return newObject;
	}

	/**
	 * Convert the DateTimeOffset XML structure to its native form
	 * 
	 * @param dateTime
	 * @return
	 */
	public static OffsetDateTime fromDateTimeOffsetXml(DateTimeOffsetXml dateTime) {
		if (dateTime == null) {
			return TimeConversion.MIN;
		}

		LocalDateTime sourceDateTime = dateTime.getDateTime();
		return OffsetDateTime.of(sourceDateTime, ZoneOffset.ofHours(dateTime.getOffset()));
	}

	/**
	 * Convert a log message severity to its XML equivalent.
	 * 
	 * @param severityName
	 * @return
	 */
	public static LogMessageSeverityXml toLogMessageSeverityXml(String severityName) {
		return LogMessageSeverityXml.valueOf(severityName);
	}

	/**
	 * Convert a log message severity XML to its native form
	 * 
	 * @param severityName
	 * @return
	 */
	public static LogMessageSeverity fromLogMessageSeverityXml(String severityName) {
		return LogMessageSeverity.valueOf(severityName);
	}

	/**
	 * Convert the provided processor architecture to our normal enumeration
	 * 
	 * @param architectureXml
	 * @return
	 */
	public static ProcessorArchitecture fromProcessorArchitectureXml(ProcessorArchitectureXml architectureXml) {
		return ProcessorArchitecture.valueOf(architectureXml.toString());
	}

	/**
	 * Convert the provided bot mode to our normal enumeration
	 * 
	 * @param bootModeXml
	 * @return
	 */
	public static OSBootMode fromBootModeXml(BootModeXml bootModeXml) {
		return OSBootMode.valueOf(bootModeXml.toString());
	}

}