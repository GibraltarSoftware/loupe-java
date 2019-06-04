package com.onloupe.core.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpPut;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.onloupe.core.server.data.ClientRepositoryStatusXml;
import com.onloupe.core.server.data.ClientRepositoryXml;

// TODO: Auto-generated Javadoc
/**
 * Uploads the state of a client repository, adding it if necessary.
 */
public class ClientRepositoryUploadRequest extends WebChannelRequestBase {
	
	/**
	 * Create a new sessions version request.
	 *
	 * @param repositoryXml the repository xml
	 */
	public ClientRepositoryUploadRequest(ClientRepositoryXml repositoryXml) {
		super(true, true);
		setInputRepository(repositoryXml);
	}

	/** The repository data to commit to the server. */
	private ClientRepositoryXml inputRepository;

	/**
	 * Gets the input repository.
	 *
	 * @return the input repository
	 */
	public final ClientRepositoryXml getInputRepository() {
		return this.inputRepository;
	}

	/**
	 * Sets the input repository.
	 *
	 * @param value the new input repository
	 */
	private void setInputRepository(ClientRepositoryXml value) {
		this.inputRepository = value;
	}

	/**
	 * The repository data returned by the server as a result of the request.
	 */
	private ClientRepositoryXml responseRepository;

	/**
	 * Gets the response repository.
	 *
	 * @return the response repository
	 */
	public final ClientRepositoryXml getResponseRepository() {
		return this.responseRepository;
	}

	/**
	 * Sets the response repository.
	 *
	 * @param value the new response repository
	 */
	private void setResponseRepository(ClientRepositoryXml value) {
		this.responseRepository = value;
	}

	/**
	 * Implemented by inheritors to perform the request on the provided web client.
	 *
	 * @param connection the connection
	 * @throws Exception the exception
	 */
	@Override
	protected void onProcessRequest(IWebChannelConnection connection) throws Exception {
		byte[] requestedRepositoryRawData = connection.uploadData(generateResourceUri(), HttpPut.METHOD_NAME,
				XML_CONTENT_TYPE, convertXmlToByteArray(getInputRepository()), null, null);

		// now we deserialize the response which is the new state of the document.

		// now, this is supposed to be a sessions list...
		try (ByteArrayInputStream documentStream = new ByteArrayInputStream(requestedRepositoryRawData)) {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xml = documentBuilder.parse(documentStream);
			ClientRepositoryXml repositoryXml = new ClientRepositoryXml();

			Element clientRepositoryElement = xml.getDocumentElement();
			repositoryXml.setid(clientRepositoryElement.getAttribute("id"));
			repositoryXml.sethostName(clientRepositoryElement.getAttribute("name"));
			repositoryXml.setcomputerKey(clientRepositoryElement.getAttribute("computerKey"));
			repositoryXml.setpublicKey(clientRepositoryElement.getAttribute("publicKey"));
			repositoryXml.setcurrentSessionsVersion(
					Long.valueOf(clientRepositoryElement.getAttribute("currentSessionsVersion")));
			repositoryXml.setstatus(ClientRepositoryStatusXml.valueOf(clientRepositoryElement.getAttribute("status")));
			setResponseRepository(repositoryXml);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw e;
		}
	}

	/**
	 * Generate resource uri.
	 *
	 * @return the string
	 */
	private String generateResourceUri() {
		UUID repositoryId = UUID.fromString(getInputRepository().getid()); // to make sure we have a valid GUID
		return String.format("/Hub/Repositories/%1$s/Repository.xml", repositoryId);
	}

	/**
	 * Convert xml to byte array.
	 *
	 * @param xmlFragment the xml fragment
	 * @return the byte[]
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	protected static byte[] convertXmlToByteArray(ClientRepositoryXml xmlFragment) throws ParserConfigurationException {
		// we want to get a byte array
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document xml = documentBuilder.newDocument();
		Element clientRepositoryXml = xml.createElement("SessionXml");
		xml.appendChild(clientRepositoryXml);
		clientRepositoryXml.setAttribute("id", xmlFragment.getid());
		clientRepositoryXml.setAttribute("hostName", xmlFragment.gethostName());
		clientRepositoryXml.setAttribute("computerKey", xmlFragment.getcomputerKey());
		clientRepositoryXml.setAttribute("currentSessionsVersion",
				String.valueOf(xmlFragment.getcurrentSessionsVersion()));
		clientRepositoryXml.setAttribute("publicKey", xmlFragment.getpublicKey());
		clientRepositoryXml.setAttribute("status", xmlFragment.getstatus().toString());
		return xml.toString().getBytes();
	}
}