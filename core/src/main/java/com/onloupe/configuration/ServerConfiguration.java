package com.onloupe.configuration;

import java.util.Properties;

import com.onloupe.core.util.TypeUtils;


/**
 * The Class ServerConfiguration.
 */
public class ServerConfiguration {
	
	/**
	 * Initialize the server configuration from the application configuration.
	 */
	public ServerConfiguration() {
		
	}
	
	/**
	 * Instantiates a new server configuration.
	 *
	 * @param props the props
	 */
	protected ServerConfiguration(Properties props) {
		setEnabled(Boolean.valueOf(props.getProperty("Server.Enabled", String.valueOf(enabled))));
		setAutoSendSessions(Boolean.valueOf(props.getProperty("Server.AutoSendSessions", String.valueOf(autoSendSessions))));
		setAutoSendOnError(Boolean.valueOf(props.getProperty("Server.AutoSendOnError", String.valueOf(autoSendOnError))));
		setSendAllApplications(Boolean.valueOf(props.getProperty("Server.SendAllApplications", String.valueOf(sendAllApplications))));
		setPurgeSentSessions(Boolean.valueOf(props.getProperty("Server.PurgeSentSessions", String.valueOf(purgeSentSessions))));
		setCustomerName(props.getProperty("Server.CustomerName"));
		setUseGibraltarService(Boolean.valueOf(props.getProperty("Server.UseGibraltarService", String.valueOf(useGibraltarService))));
		setUseSsl(Boolean.valueOf(props.getProperty("Server.UseSsl", String.valueOf(useSsl))));
		setServer(props.getProperty("Server.Server"));
		setPort(Integer.parseInt(props.getProperty("Server.Port", String.valueOf(port))));
		setApplicationBaseDirectory(props.getProperty("Server.ApplicationBaseDirectory"));
		setRepository(props.getProperty("Server.Repository"));
	}

	/**
	 * Instantiates a new server configuration.
	 *
	 * @param builder the builder
	 */
	private ServerConfiguration(Builder builder) {
		this.enabled = builder.enabled;
		this.autoSendSessions = builder.autoSendSessions;
		this.autoSendOnError = builder.autoSendOnError;
		this.sendAllApplications = builder.sendAllApplications;
		this.purgeSentSessions = builder.purgeSentSessions;
		this.customerName = builder.customerName;
		this.useGibraltarService = builder.useGibraltarService;
		this.useSsl = builder.useSsl;
		this.server = builder.server;
		this.port = builder.port;
		this.applicationBaseDirectory = builder.applicationBaseDirectory;
		this.repository = builder.repository;
	}
	
	/**
	 * True by default, disables server communication when false.
	 */
	private boolean enabled = true;

	/**
	 * Gets the enabled.
	 *
	 * @return the enabled
	 */
	public final boolean getEnabled() {
		return this.enabled;
	}

	/**
	 * Sets the enabled.
	 *
	 * @param value the new enabled
	 */
	public final void setEnabled(boolean value) {
		this.enabled = value;
	}

	/**
	 * Indicates whether to automatically send session data to the server in the
	 * background.
	 * 
	 * Defaults to false, indicating data will only be sent on request via packager.
	 */
	private boolean autoSendSessions = false;

	/**
	 * Gets the auto send sessions.
	 *
	 * @return the auto send sessions
	 */
	public final boolean getAutoSendSessions() {
		return this.autoSendSessions;
	}

	/**
	 * Sets the auto send sessions.
	 *
	 * @param value the new auto send sessions
	 */
	public final void setAutoSendSessions(boolean value) {
		this.autoSendSessions = value;
	}

	/**
	 * Indicates whether to automatically send data to the server when error or
	 * critical messages are logged.
	 * 
	 * Defaults to true, indicating if the Auto Send Sessions option is also enabled
	 * data will be sent to the server after an error occurs (unless overridden by
	 * the MessageAlert event).
	 */
	private boolean autoSendOnError = true;

	/**
	 * Gets the auto send on error.
	 *
	 * @return the auto send on error
	 */
	public final boolean getAutoSendOnError() {
		return this.autoSendOnError;
	}

	/**
	 * Sets the auto send on error.
	 *
	 * @param value the new auto send on error
	 */
	public final void setAutoSendOnError(boolean value) {
		this.autoSendOnError = value;
	}

	/**
	 * Indicates whether to send data about all applications for this product to the
	 * server or just this application (the default)
	 * 
	 * Defaults to false, indicating just the current applications data will be
	 * sent. Requires that AutoSendSessions is enabled.
	 */
	private boolean sendAllApplications = false;

	/**
	 * Gets the send all applications.
	 *
	 * @return the send all applications
	 */
	public final boolean getSendAllApplications() {
		return this.sendAllApplications;
	}

	/**
	 * Sets the send all applications.
	 *
	 * @param value the new send all applications
	 */
	public final void setSendAllApplications(boolean value) {
		this.sendAllApplications = value;
	}

	/**
	 * Indicates whether to remove sessions that have been sent from the local
	 * repository once confirmed by the server.
	 * 
	 * Defaults to false. Requires that AutoSendSessions is enabled.
	 */
	private boolean purgeSentSessions = false;

	/**
	 * Gets the purge sent sessions.
	 *
	 * @return the purge sent sessions
	 */
	public final boolean getPurgeSentSessions() {
		return this.purgeSentSessions;
	}

	/**
	 * Sets the purge sent sessions.
	 *
	 * @param value the new purge sent sessions
	 */
	public final void setPurgeSentSessions(boolean value) {
		this.purgeSentSessions = value;
	}

	/** The unique customer name when using the Gibraltar Loupe Service. */
	private String customerName;

	/**
	 * Gets the customer name.
	 *
	 * @return the customer name
	 */
	public final String getCustomerName() {
		return this.customerName;
	}

	/**
	 * Sets the customer name.
	 *
	 * @param value the new customer name
	 */
	public final void setCustomerName(String value) {
		this.customerName = value;
	}

	/**
	 * Indicates if the Gibraltar Loupe Service should be used instead of a private
	 * Loupe Server
	 * 
	 * If true then the customer name must be specified.
	 */
	private boolean useGibraltarService = false;

	/**
	 * Gets the use gibraltar service.
	 *
	 * @return the use gibraltar service
	 */
	public final boolean getUseGibraltarService() {
		return this.useGibraltarService;
	}

	/**
	 * Sets the use gibraltar service.
	 *
	 * @param value the new use gibraltar service
	 */
	public final void setUseGibraltarService(boolean value) {
		this.useGibraltarService = value;
	}

	/**
	 * Indicates if the connection to the Loupe Server should be encrypted with Ssl.
	 * 
	 * Only applies to a private Loupe Server.
	 */
	private boolean useSsl = false;

	/**
	 * Gets the use ssl.
	 *
	 * @return the use ssl
	 */
	public final boolean getUseSsl() {
		return this.useSsl;
	}

	/**
	 * Sets the use ssl.
	 *
	 * @param value the new use ssl
	 */
	public final void setUseSsl(boolean value) {
		this.useSsl = value;
	}

	/**
	 * The full DNS name of the server where the Loupe Server is located
	 * 
	 * Only applies to a private Loupe Server.
	 */
	private String server;

	/**
	 * Gets the server.
	 *
	 * @return the server
	 */
	public final String getServer() {
		return this.server;
	}

	/**
	 * Sets the server.
	 *
	 * @param value the new server
	 */
	public final void setServer(String value) {
		this.server = value;
	}

	/**
	 * An optional port number override for the server
	 * 
	 * Not required if the port is the traditional port (80 or 443). Only applies to
	 * a private Loupe Server.
	 */
	private int port = 0;

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public final int getPort() {
		return this.port;
	}

	/**
	 * Sets the port.
	 *
	 * @param value the new port
	 */
	public final void setPort(int value) {
		this.port = value;
	}

	/**
	 * The virtual directory on the host for the private Loupe Server
	 * 
	 * Only applies to a private Loupe Server.
	 */
	private String applicationBaseDirectory;

	/**
	 * Gets the application base directory.
	 *
	 * @return the application base directory
	 */
	public final String getApplicationBaseDirectory() {
		return this.applicationBaseDirectory;
	}

	/**
	 * Sets the application base directory.
	 *
	 * @param value the new application base directory
	 */
	public final void setApplicationBaseDirectory(String value) {
		this.applicationBaseDirectory = value;
	}

	/**
	 * The specific repository on the server to send the session to
	 * 
	 * Only applies to a private Loupe Server running Enterprise Edition.
	 */
	private String repository;

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	public final String getRepository() {
		return this.repository;
	}

	/**
	 * Sets the repository.
	 *
	 * @param value the new repository
	 */
	public final void setRepository(String value) {
		this.repository = value;
	}

	/**
	 * Check the current configuration information to see if it's valid for a
	 * connection, throwing relevant exceptions if not.
	 */
	public final void validate() {
		// check a special case: There is NO configuration information to speak of.
		if (!getUseGibraltarService() && (TypeUtils.isBlank(getCustomerName()))
				&& (TypeUtils.isBlank(getServer()))) {
			// no way you even tried to configure the SDS. lets use a different message.
			throw new IllegalStateException("No server connection configuration could be found");
		}

		if (getUseGibraltarService()) {
			if (TypeUtils.isBlank(getCustomerName())) {
				throw new IllegalStateException("A valid customer name is required to use the Gibraltar Hub Service,");
			}
		} else {
			if (TypeUtils.isBlank(getServer())) {
				throw new IllegalStateException("When using a private Hub a full server name is required");
			}

			if (getPort() < 0) {
				throw new IllegalStateException(
						"When overriding the connection port, a positive number must be specified.  Use zero to accept the default port.");
			}
		}
	}

	/**
	 * Normalize the configuration data.
	 */
	public final void sanitize() {
		if (TypeUtils.isBlank(getServer())) {
			setServer(null);
		} else {
			setServer(getServer().trim());

			if (TypeUtils.startsWithIgnoreCase(getServer(), "https://")) {
				setServer(getServer().substring(8));
			} else if (TypeUtils.startsWithIgnoreCase(getServer(), "http://")) {
				setServer(getServer().substring(7));
			}

			if (TypeUtils.endsWithIgnoreCase(getServer(), "/")) {
				setServer(getServer().substring(0, getServer().length() - 1));
			}
		}

		if (TypeUtils.isBlank(getCustomerName())) {
			setCustomerName(null);
		} else {
			setCustomerName(getCustomerName().trim());
		}

		if (TypeUtils.isBlank(getApplicationBaseDirectory())) {
			setApplicationBaseDirectory(null);
		} else {
			setApplicationBaseDirectory(getApplicationBaseDirectory().trim());

			if (TypeUtils.startsWithIgnoreCase(getApplicationBaseDirectory(), "/")) {
				setApplicationBaseDirectory(getApplicationBaseDirectory().substring(1));
			}

			if (TypeUtils.endsWithIgnoreCase(getApplicationBaseDirectory(), "/")) {
				setApplicationBaseDirectory(
						getApplicationBaseDirectory().substring(0, getApplicationBaseDirectory().length() - 1));
			}
		}

		if (getPort() < 0) {
			setPort(0);
		}

		if ((getUseGibraltarService() && TypeUtils.isBlank(getCustomerName()))
				|| (!getUseGibraltarService() && TypeUtils.isBlank(getServer()))) {
			setEnabled(false); // we can't be enabled because we aren't plausibly configured.
		}
	}

	/**
	 * Creates builder to build {@link ServerConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link ServerConfiguration}.
	 */
	public static final class Builder {
		
		/** The enabled. */
		private boolean enabled;
		
		/** The auto send sessions. */
		private boolean autoSendSessions;
		
		/** The auto send on error. */
		private boolean autoSendOnError;
		
		/** The send all applications. */
		private boolean sendAllApplications;
		
		/** The purge sent sessions. */
		private boolean purgeSentSessions;
		
		/** The customer name. */
		private String customerName;
		
		/** The use gibraltar service. */
		private boolean useGibraltarService;
		
		/** The use ssl. */
		private boolean useSsl;
		
		/** The server. */
		private String server;
		
		/** The port. */
		private int port;
		
		/** The application base directory. */
		private String applicationBaseDirectory;
		
		/** The repository. */
		private String repository;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
		}

		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		/**
		 * Auto send sessions.
		 *
		 * @param autoSendSessions the auto send sessions
		 * @return the builder
		 */
		public Builder autoSendSessions(boolean autoSendSessions) {
			this.autoSendSessions = autoSendSessions;
			return this;
		}

		/**
		 * Auto send on error.
		 *
		 * @param autoSendOnError the auto send on error
		 * @return the builder
		 */
		public Builder autoSendOnError(boolean autoSendOnError) {
			this.autoSendOnError = autoSendOnError;
			return this;
		}

		/**
		 * Send all applications.
		 *
		 * @param sendAllApplications the send all applications
		 * @return the builder
		 */
		public Builder sendAllApplications(boolean sendAllApplications) {
			this.sendAllApplications = sendAllApplications;
			return this;
		}

		/**
		 * Purge sent sessions.
		 *
		 * @param purgeSentSessions the purge sent sessions
		 * @return the builder
		 */
		public Builder purgeSentSessions(boolean purgeSentSessions) {
			this.purgeSentSessions = purgeSentSessions;
			return this;
		}

		/**
		 * Customer name.
		 *
		 * @param customerName the customer name
		 * @return the builder
		 */
		public Builder customerName(String customerName) {
			this.customerName = customerName;
			return this;
		}

		/**
		 * Use gibraltar service.
		 *
		 * @param useGibraltarService the use gibraltar service
		 * @return the builder
		 */
		public Builder useGibraltarService(boolean useGibraltarService) {
			this.useGibraltarService = useGibraltarService;
			return this;
		}

		/**
		 * Use ssl.
		 *
		 * @param useSsl the use ssl
		 * @return the builder
		 */
		public Builder useSsl(boolean useSsl) {
			this.useSsl = useSsl;
			return this;
		}

		/**
		 * Server.
		 *
		 * @param server the server
		 * @return the builder
		 */
		public Builder server(String server) {
			this.server = server;
			return this;
		}

		/**
		 * Port.
		 *
		 * @param port the port
		 * @return the builder
		 */
		public Builder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Application base directory.
		 *
		 * @param applicationBaseDirectory the application base directory
		 * @return the builder
		 */
		public Builder applicationBaseDirectory(String applicationBaseDirectory) {
			this.applicationBaseDirectory = applicationBaseDirectory;
			return this;
		}

		/**
		 * Repository.
		 *
		 * @param repository the repository
		 * @return the builder
		 */
		public Builder repository(String repository) {
			this.repository = repository;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the server configuration
		 */
		public ServerConfiguration build() {
			return new ServerConfiguration(this);
		}
	}

}