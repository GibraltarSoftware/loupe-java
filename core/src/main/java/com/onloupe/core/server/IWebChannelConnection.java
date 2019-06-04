package com.onloupe.core.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.onloupe.core.NameValuePair;

// TODO: Auto-generated Javadoc
/**
 * The low level web client connection used by the web channel.
 */
public interface IWebChannelConnection {
	
	/**
	 * Downloads the resource with the specified URI to a byte array.
	 *
	 * @param relativeUrl the relative url
	 * @param timeout     The number of seconds to wait for a response to the
	 *                    request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] downloadData(String relativeUrl, Integer timeout) throws URISyntaxException;

	/**
	 * Download data.
	 *
	 * @param relativeUrl the relative url
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] downloadData(String relativeUrl) throws URISyntaxException;

	/**
	 * Download data.
	 *
	 * @param relativeUrl the relative url
	 * @param additionalHeaders the additional headers
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders) throws URISyntaxException;

	/**
	 * Downloads the resource with the specified URI to a byte array.
	 *
	 * @param relativeUrl the relative url
	 * @param additionalHeaders Extra headers to add to the request
	 * @param timeout           The number of seconds to wait for a response to the
	 *                          request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders, Integer timeout)
			throws URISyntaxException;

	/**
	 * Downloads the resource with the specified URI to a local file.
	 *
	 * @param relativeUrl the relative url
	 * @param destinationFileName the destination file name
	 * @param timeout             The number of seconds to wait for a response to
	 *                            the request
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	void downloadFile(String relativeUrl, String destinationFileName, Integer timeout) throws URISyntaxException, IOException;

	/**
	 * Downloads the resource with the specified URI to a string.
	 *
	 * @param relativeUrl the relative url
	 * @param timeout     The number of seconds to wait for a response to the
	 *                    request
	 * @return the string
	 * @throws URISyntaxException the URI syntax exception
	 */
	String downloadString(String relativeUrl, Integer timeout) throws URISyntaxException;

	/**
	 * Download string.
	 *
	 * @param relativeUrl the relative url
	 * @return the string
	 * @throws URISyntaxException the URI syntax exception
	 */
	String downloadString(String relativeUrl) throws URISyntaxException;

	/**
	 * Uploads the provided byte array to the specified URI using the provided
	 * method.
	 *
	 * @param relativeUrl       The URI of the resource to receive the data. This
	 *                          URI must identify a resource that can accept a
	 *                          request sent with the method specified.
	 * @param method            The HTTP method used to send the string to the
	 *                          resource. If null, the default is POST
	 * @param contentType       The content type to inform the server of for this
	 *                          file
	 * @param data the data
	 * @param additionalHeaders Extra headers to add to the request
	 * @param timeout           The number of seconds to wait for a response to the
	 *                          request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] uploadData(String relativeUrl, String method, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException;

	/**
	 * Upload data.
	 *
	 * @param relativeUrl the relative url
	 * @param contentType the content type
	 * @param data the data
	 * @param additionalHeaders the additional headers
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			java.util.List<NameValuePair<String>> additionalHeaders) throws URISyntaxException;

	/**
	 * Upload data.
	 *
	 * @param relativeUrl the relative url
	 * @param contentType the content type
	 * @param data the data
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] uploadData(String relativeUrl, String contentType, byte[] data) throws URISyntaxException;

	/**
	 * Upload data.
	 *
	 * @param relativeUrl the relative url
	 * @param contentType the content type
	 * @param data the data
	 * @param additionalHeaders the additional headers
	 * @param timeout the timeout
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException;

	/**
	 * Delete data.
	 *
	 * @param relativeUrl the relative url
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 */
	byte[] deleteData(String relativeUrl) throws URISyntaxException;

	/**
	 * Uploads the specified local file to the specified URI using the specified
	 * method.
	 *
	 * @param relativeUrl        The URI of the resource to receive the file. This
	 *                           URI must identify a resource that can accept a
	 *                           request sent with the method specified.
	 * @param contentType        The content type to inform the server of for this
	 *                           file
	 * @param sourceFileNamePath the source file name path
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath) throws URISyntaxException, IOException;

	/**
	 * Upload file.
	 *
	 * @param relativeUrl the relative url
	 * @param contentType the content type
	 * @param sourceFileNamePath the source file name path
	 * @param timeout the timeout
	 * @return the byte[]
	 * @throws URISyntaxException the URI syntax exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath, Integer timeout)
			throws URISyntaxException, IOException;

	/**
	 * Uploads the specified string to the specified resource, using the specified
	 * method.
	 *
	 * @param relativeUrl The URI of the resource to receive the string. This URI
	 *                    must identify a resource that can accept a request sent
	 *                    with the method specified.
	 * @param contentType The content type to inform the server of for this file
	 * @param data        The string to be uploaded.
	 * @return A string containing the body of the response from the resource
	 * @throws URISyntaxException the URI syntax exception
	 */

	String uploadString(String relativeUrl, String contentType, String data) throws URISyntaxException;

	/**
	 * Upload string.
	 *
	 * @param relativeUrl the relative url
	 * @param contentType the content type
	 * @param data the data
	 * @param timeout the timeout
	 * @return the string
	 * @throws URISyntaxException the URI syntax exception
	 */
	String uploadString(String relativeUrl, String contentType, String data, Integer timeout) throws URISyntaxException;

	/**
	 * Execute request.
	 *
	 * @param request the request
	 * @param retries the retries
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	void executeRequest(IWebRequest request, int retries) throws IOException, Exception;
}