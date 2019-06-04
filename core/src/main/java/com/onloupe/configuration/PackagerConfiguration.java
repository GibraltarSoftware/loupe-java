package com.onloupe.configuration;

import java.util.Properties;

import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * The configuration of the packager.
 */
public final class PackagerConfiguration {
	/**
	 * The default HotKey configuration string for the packager.
	 */
	public static final String DEFAULT_HOT_KEY = "Ctrl-Alt-F4";

	/**
	 * Instantiates a new packager configuration.
	 */
	public PackagerConfiguration() {

	}
	
	/**
	 * Instantiates a new packager configuration.
	 *
	 * @param props the props
	 */
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
	
	/**
	 * Instantiates a new packager configuration.
	 *
	 * @param builder the builder
	 */
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

	/**
	 * Gets the hot key.
	 *
	 * @return the hot key
	 */
	public String getHotKey() {
		return this.hotKey;
	}

	/**
	 * Sets the hot key.
	 *
	 * @param value the new hot key
	 */
	public void setHotKey(String value) {
		this.hotKey = value;
	}

	/**
	 * When true the user will be allowed to save the package to a file.
	 */
	private boolean allowFile = true;

	/**
	 * Gets the allow file.
	 *
	 * @return the allow file
	 */
	public boolean getAllowFile() {
		return this.allowFile;
	}

	/**
	 * Sets the allow file.
	 *
	 * @param value the new allow file
	 */
	public void setAllowFile(boolean value) {
		this.allowFile = value;
	}

	/** When true the user will be allowed to save the package directly to the root of a removable media volume. */
	private boolean allowRemovableMedia = true;

	/**
	 * Gets the allow removable media.
	 *
	 * @return the allow removable media
	 */
	public boolean getAllowRemovableMedia() {
		return this.allowRemovableMedia;
	}

	/**
	 * Sets the allow removable media.
	 *
	 * @param value the new allow removable media
	 */
	public void setAllowRemovableMedia(boolean value) {
		this.allowRemovableMedia = value;
	}

	/** When true the user will be allowed to send the package via email. */
	private boolean allowEmail = true;

	/**
	 * Gets the allow email.
	 *
	 * @return the allow email
	 */
	public boolean getAllowEmail() {
		return this.allowEmail;
	}

	/**
	 * Sets the allow email.
	 *
	 * @param value the new allow email
	 */
	public void setAllowEmail(boolean value) {
		this.allowEmail = value;
	}

	/** When true the user will be allowed to send sessions to a session data server. */
	private boolean allowServer = true;

	/**
	 * Gets the allow server.
	 *
	 * @return the allow server
	 */
	public boolean getAllowServer() {
		return this.allowServer;
	}

	/**
	 * Sets the allow server.
	 *
	 * @param value the new allow server
	 */
	public void setAllowServer(boolean value) {
		this.allowServer = value;
	}

	/**
	 * The email address to use as the sender&apos;s address
	 * 
	 * If specified, the user will not be given the option to override it.
	 */
	private String fromEmailAddress;

	/**
	 * Gets the from email address.
	 *
	 * @return the from email address
	 */
	public String getFromEmailAddress() {
		return this.fromEmailAddress;
	}

	/**
	 * Sets the from email address.
	 *
	 * @param value the new from email address
	 */
	public void setFromEmailAddress(String value) {
		this.fromEmailAddress = value;
	}

	/**
	 * The address to send the email to.
	 * 
	 * If specified, the user will not be given the option to override it.
	 */
	private String destinationEmailAddress;

	/**
	 * Gets the destination email address.
	 *
	 * @return the destination email address
	 */
	public String getDestinationEmailAddress() {
		return this.destinationEmailAddress;
	}

	/**
	 * Sets the destination email address.
	 *
	 * @param value the new destination email address
	 */
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

	/**
	 * Gets the product name.
	 *
	 * @return the product name
	 */
	public String getProductName() {
		return this.productName;
	}

	/**
	 * Sets the product name.
	 *
	 * @param value the new product name
	 */
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

	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
	public String getApplicationName() {
		return this.applicationName;
	}

	/**
	 * Sets the application name.
	 *
	 * @param value the new application name
	 */
	public void setApplicationName(String value) {
		this.applicationName = value;
	}

	/**
	 * Normalize the configuration options.
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
		
		/** The hot key. */
		private String hotKey;
		
		/** The allow file. */
		private boolean allowFile;
		
		/** The allow removable media. */
		private boolean allowRemovableMedia;
		
		/** The allow email. */
		private boolean allowEmail;
		
		/** The allow server. */
		private boolean allowServer;
		
		/** The from email address. */
		private String fromEmailAddress;
		
		/** The destination email address. */
		private String destinationEmailAddress;
		
		/** The product name. */
		private String productName;
		
		/** The application name. */
		private String applicationName;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
		}

		/**
		 * Hot key.
		 *
		 * @param hotKey the hot key
		 * @return the builder
		 */
		public Builder hotKey(String hotKey) {
			this.hotKey = hotKey;
			return this;
		}

		/**
		 * Allow file.
		 *
		 * @param allowFile the allow file
		 * @return the builder
		 */
		public Builder allowFile(boolean allowFile) {
			this.allowFile = allowFile;
			return this;
		}

		/**
		 * Allow removable media.
		 *
		 * @param allowRemovableMedia the allow removable media
		 * @return the builder
		 */
		public Builder allowRemovableMedia(boolean allowRemovableMedia) {
			this.allowRemovableMedia = allowRemovableMedia;
			return this;
		}

		/**
		 * Allow email.
		 *
		 * @param allowEmail the allow email
		 * @return the builder
		 */
		public Builder allowEmail(boolean allowEmail) {
			this.allowEmail = allowEmail;
			return this;
		}

		/**
		 * Allow server.
		 *
		 * @param allowServer the allow server
		 * @return the builder
		 */
		public Builder allowServer(boolean allowServer) {
			this.allowServer = allowServer;
			return this;
		}

		/**
		 * From email address.
		 *
		 * @param fromEmailAddress the from email address
		 * @return the builder
		 */
		public Builder fromEmailAddress(String fromEmailAddress) {
			this.fromEmailAddress = fromEmailAddress;
			return this;
		}

		/**
		 * Destination email address.
		 *
		 * @param destinationEmailAddress the destination email address
		 * @return the builder
		 */
		public Builder destinationEmailAddress(String destinationEmailAddress) {
			this.destinationEmailAddress = destinationEmailAddress;
			return this;
		}

		/**
		 * Product name.
		 *
		 * @param productName the product name
		 * @return the builder
		 */
		public Builder productName(String productName) {
			this.productName = productName;
			return this;
		}

		/**
		 * Application name.
		 *
		 * @param applicationName the application name
		 * @return the builder
		 */
		public Builder applicationName(String applicationName) {
			this.applicationName = applicationName;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the packager configuration
		 */
		public PackagerConfiguration build() {
			return new PackagerConfiguration(this);
		}
	}

	
}