package com.onloupe.core.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.onloupe.core.NameValuePair;

/**
 * The low level web client connection used by the web channel.
 */
public interface IWebChannelConnection {
	/**
	 * Downloads the resource with the specified URI to a byte array
	 * 
	 * @param relativeUrl
	 * @param timeout     The number of seconds to wait for a response to the
	 *                    request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException
	 */
	byte[] downloadData(String relativeUrl, Integer timeout) throws URISyntaxException;

	byte[] downloadData(String relativeUrl) throws URISyntaxException;

	/**
	 * Downloads the resource with the specified URI to a byte array
	 * 
	 * @param relativeUrl
	 * @param additionalHeaders Extra headers to add to the request
	 * @param timeout           The number of seconds to wait for a response to the
	 *                          request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException
	 */

	byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders) throws URISyntaxException;

	byte[] downloadData(String relativeUrl, List<NameValuePair<String>> additionalHeaders, Integer timeout)
			throws URISyntaxException;

	/**
	 * Downloads the resource with the specified URI to a local file.
	 * 
	 * @param relativeUrl
	 * @param destinationFileName
	 * @param timeout             The number of seconds to wait for a response to
	 *                            the request
	 * @throws URISyntaxException
	 * @throws IOException 
	 */

	void downloadFile(String relativeUrl, String destinationFileName, Integer timeout) throws URISyntaxException, IOException;

	/**
	 * Downloads the resource with the specified URI to a string
	 * 
	 * @param relativeUrl
	 * @param timeout     The number of seconds to wait for a response to the
	 *                    request
	 * @return
	 * @throws URISyntaxException
	 */

	String downloadString(String relativeUrl) throws URISyntaxException;

	String downloadString(String relativeUrl, Integer timeout) throws URISyntaxException;

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
	 * @param data
	 * @param additionalHeaders Extra headers to add to the request
	 * @param timeout           The number of seconds to wait for a response to the
	 *                          request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException
	 */

	byte[] uploadData(String relativeUrl, String method, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException;

	byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			java.util.List<NameValuePair<String>> additionalHeaders) throws URISyntaxException;

	byte[] uploadData(String relativeUrl, String contentType, byte[] data) throws URISyntaxException;

	byte[] uploadData(String relativeUrl, String contentType, byte[] data,
			List<NameValuePair<String>> additionalHeaders, Integer timeout) throws URISyntaxException;

	byte[] deleteData(String relativeUrl) throws URISyntaxException;

	/**
	 * Uploads the specified local file to the specified URI using the specified
	 * method
	 * 
	 * @param relativeUrl        The URI of the resource to receive the file. This
	 *                           URI must identify a resource that can accept a
	 *                           request sent with the method specified.
	 * @param method             The HTTP method used to send the string to the
	 *                           resource. If null, the default is POST
	 * @param contentType        The content type to inform the server of for this
	 *                           file
	 * @param sourceFileNamePath
	 * @param timeout            The number of seconds to wait for a response to the
	 *                           request
	 * @return A byte array containing the body of the response from the resource
	 * @throws URISyntaxException
	 * @throws IOException 
	 */

	byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath) throws URISyntaxException, IOException;

	byte[] uploadFile(String relativeUrl, String contentType, String sourceFileNamePath, Integer timeout)
			throws URISyntaxException, IOException;

	/**
	 * Uploads the specified string to the specified resource, using the specified
	 * method
	 * 
	 * @param relativeUrl The URI of the resource to receive the string. This URI
	 *                    must identify a resource that can accept a request sent
	 *                    with the method specified.
	 * @param method      The HTTP method used to send the string to the resource.
	 *                    If null, the default is POST
	 * @param contentType The content type to inform the server of for this file
	 * @param data        The string to be uploaded.
	 * @param timeout     The number of seconds to wait for a response to the
	 *                    request
	 * @return A string containing the body of the response from the resource
	 * @throws URISyntaxException
	 */

	String uploadString(String relativeUrl, String contentType, String data) throws URISyntaxException;

	String uploadString(String relativeUrl, String contentType, String data, Integer timeout) throws URISyntaxException;

	void executeRequest(IWebRequest request, int retries) throws IOException, Exception;
}