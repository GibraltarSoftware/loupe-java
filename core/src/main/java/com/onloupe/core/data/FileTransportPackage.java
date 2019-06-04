package com.onloupe.core.data;

import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * A transport package that is just being written out to a file.
 */
public class FileTransportPackage extends TransportPackageBase {
	
	/**
	 * Instantiates a new file transport package.
	 *
	 * @param product the product
	 * @param application the application
	 * @param package_Renamed the package renamed
	 * @param fileNamePath the file name path
	 */
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

	/**
	 * Gets the file name path.
	 *
	 * @return the file name path
	 */
	public final String getFileNamePath() {
		return this.fileNamePath;
	}

	/**
	 * Sets the file name path.
	 *
	 * @param value the new file name path
	 */
	private void setFileNamePath(String value) {
		this.fileNamePath = value;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.core.data.TransportPackageBase#onSend()
	 */
	@Override
	protected void onSend() {
		// do nothing
	}
}