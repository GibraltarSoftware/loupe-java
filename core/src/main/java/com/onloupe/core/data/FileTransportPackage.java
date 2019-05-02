package com.onloupe.core.data;

import com.onloupe.core.util.TypeUtils;

/**
 * A transport package that is just being written out to a file.
 */
public class FileTransportPackage extends TransportPackageBase {
	public FileTransportPackage(String product, String application, SimplePackage package_Renamed,
			String fileNamePath) {
		super(product, application, package_Renamed);
		if (TypeUtils.isBlank(fileNamePath)) {
			throw new NullPointerException("fileNamePath");
		}

		setFileNamePath(fileNamePath);
	}

	/**
	 * The full file name and path to write out to.
	 */
	private String fileNamePath;

	public final String getFileNamePath() {
		return this.fileNamePath;
	}

	private void setFileNamePath(String value) {
		this.fileNamePath = value;
	}

	@Override
	protected void onSend() {
		// do nothing
	}
}