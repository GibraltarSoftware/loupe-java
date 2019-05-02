package com.onloupe.configuration;

import java.util.Properties;

import com.onloupe.core.util.TypeUtils;

/**
 * The configuration of the packager.
 */
public final class PackagerConfiguration {
	/**
	 * The default HotKey configuration string for the packager.
	 */
	public static final String DEFAULT_HOT_KEY = "Ctrl-Alt-F4";

	public PackagerConfiguration() {

	}
	
	protected PackagerConfiguration(Properties props) {
		setHotKey(props.getProperty("Packager.HotKey", PackagerConfiguration.DEFAULT_HOT_KEY)); 
		setAllowFile(Boolean.valueOf(props.getProperty("Packager.AllowFile", String.valueOf(allowFile)))); 
		setAllowRemovableMedia(Boolean.valueOf(props.getProperty("Packager.AllowRemovableMedia", String.valueOf(allowRemovableMedia)))); 
		setAllowEmail(Boolean.valueOf(props.getProperty("Packager.AllowEmail", String.valueOf(allowEmail)))); 
		setAllowServer(Boolean.valueOf(props.getProperty("Packager.AllowServer", String.valueOf(allowServer)))); 
		setFromEmailAddress(props.getProperty("Packager.FromEmailAddress")); 
		setDestinationEmailAddress(props.getProperty("Packager.DestinationEmailAddress")); 
		setProductName(props.getProperty("Packager.ProductName")); 
		setApplicationName(props.getProperty("Packager.ApplicationName"));
	}
	
	private PackagerConfiguration(Builder builder) {
		this.hotKey = builder.hotKey;
		this.allowFile = builder.allowFile;
		this.allowRemovableMedia = builder.allowRemovableMedia;
		this.allowEmail = builder.allowEmail;
		this.allowServer = builder.allowServer;
		this.fromEmailAddress = builder.fromEmailAddress;
		this.destinationEmailAddress = builder.destinationEmailAddress;
		this.productName = builder.productName;
		this.applicationName = builder.applicationName;
	}
	
	/**
	 * The key sequence used to pop up the packager.
	 */
	private String hotKey = DEFAULT_HOT_KEY;

	public String getHotKey() {
		return this.hotKey;
	}

	public void setHotKey(String value) {
		this.hotKey = value;
	}

	/**
	 * When true the user will be allowed to save the package to a file.
	 */
	private boolean allowFile = true;

	public boolean getAllowFile() {
		return this.allowFile;
	}

	public void setAllowFile(boolean value) {
		this.allowFile = value;
	}

	/**
	 * When true the user will be allowed to save the package directly to the root
	 * of a removable media volume
	 */
	private boolean allowRemovableMedia = true;

	public boolean getAllowRemovableMedia() {
		return this.allowRemovableMedia;
	}

	public void setAllowRemovableMedia(boolean value) {
		this.allowRemovableMedia = value;
	}

	/**
	 * When true the user will be allowed to send the package via email
	 */
	private boolean allowEmail = true;

	public boolean getAllowEmail() {
		return this.allowEmail;
	}

	public void setAllowEmail(boolean value) {
		this.allowEmail = value;
	}

	/**
	 * When true the user will be allowed to send sessions to a session data server
	 */
	private boolean allowServer = true;

	public boolean getAllowServer() {
		return this.allowServer;
	}

	public void setAllowServer(boolean value) {
		this.allowServer = value;
	}

	/**
	 * The email address to use as the sender&apos;s address
	 * 
	 * If specified, the user will not be given the option to override it.
	 */
	private String fromEmailAddress;

	public String getFromEmailAddress() {
		return this.fromEmailAddress;
	}

	public void setFromEmailAddress(String value) {
		this.fromEmailAddress = value;
	}

	/**
	 * The address to send the email to.
	 * 
	 * If specified, the user will not be given the option to override it.
	 */
	private String destinationEmailAddress;

	public String getDestinationEmailAddress() {
		return this.destinationEmailAddress;
	}

	public void setDestinationEmailAddress(String value) {
		this.destinationEmailAddress = value;
	}

	/**
	 * The product name to use instead of the current application.
	 * 
	 * Primarily used in the Packager.exe.config file to specify the end-user
	 * product and application you want to package information for instead of the
	 * current application. If specified, the name must exactly match the name shown
	 * in Loupe for the product.
	 * <p>
	 * To limit the package to one application within a product specify the
	 * applicationName as well as the productName. Specifying just the product name
	 * will cause the package to contain all applications for the specified product.
	 * </p>
	 */
	private String productName;

	public String getProductName() {
		return this.productName;
	}

	public void setProductName(String value) {
		this.productName = value;
	}

	/**
	 * The application name to use instead of the current application.
	 * 
	 * <p>
	 * Primarily used in the Packager.exe.config file to specify the end-user
	 * application you want to package information for instead of the current
	 * application. If specified, the name must exactly match the name shown in
	 * Loupe for the application.
	 * </p>
	 * <p>
	 * Application name is ignored if product name is not also specified.
	 * </p>
	 */
	private String applicationName;

	public String getApplicationName() {
		return this.applicationName;
	}

	public void setApplicationName(String value) {
		this.applicationName = value;
	}

	/**
	 * Normalize the configuration options
	 */
	public void sanitize() {
		if (TypeUtils.isBlank(getHotKey())) {
			setHotKey(DEFAULT_HOT_KEY);
		}

		if (TypeUtils.isBlank(getProductName())) {
			setProductName(null);
		}

		if (TypeUtils.isBlank(getApplicationName())) {
			setApplicationName(null);
		}

		if (TypeUtils.isBlank(getFromEmailAddress())) {
			setFromEmailAddress(null);
		}

		if (TypeUtils.isBlank(getDestinationEmailAddress())) {
			setDestinationEmailAddress(null);
		}
	}

	/**
	 * Creates builder to build {@link PackagerConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link PackagerConfiguration}.
	 */
	public static final class Builder {
		private String hotKey;
		private boolean allowFile;
		private boolean allowRemovableMedia;
		private boolean allowEmail;
		private boolean allowServer;
		private String fromEmailAddress;
		private String destinationEmailAddress;
		private String productName;
		private String applicationName;

		private Builder() {
		}

		public Builder hotKey(String hotKey) {
			this.hotKey = hotKey;
			return this;
		}

		public Builder allowFile(boolean allowFile) {
			this.allowFile = allowFile;
			return this;
		}

		public Builder allowRemovableMedia(boolean allowRemovableMedia) {
			this.allowRemovableMedia = allowRemovableMedia;
			return this;
		}

		public Builder allowEmail(boolean allowEmail) {
			this.allowEmail = allowEmail;
			return this;
		}

		public Builder allowServer(boolean allowServer) {
			this.allowServer = allowServer;
			return this;
		}

		public Builder fromEmailAddress(String fromEmailAddress) {
			this.fromEmailAddress = fromEmailAddress;
			return this;
		}

		public Builder destinationEmailAddress(String destinationEmailAddress) {
			this.destinationEmailAddress = destinationEmailAddress;
			return this;
		}

		public Builder productName(String productName) {
			this.productName = productName;
			return this;
		}

		public Builder applicationName(String applicationName) {
			this.applicationName = applicationName;
			return this;
		}

		public PackagerConfiguration build() {
			return new PackagerConfiguration(this);
		}
	}

	
}