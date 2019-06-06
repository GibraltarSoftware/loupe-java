package com.onloupe.configuration;

import java.util.Properties;

import com.onloupe.core.util.TypeUtils;
import com.onloupe.model.system.ApplicationType;
import com.onloupe.model.system.Version;


/**
 * The configuration of the publisher.
 */
public final class PublisherConfiguration {
	
	/**
	 * Instantiates a new publisher configuration.
	 */
	public PublisherConfiguration() {

	}

	/**
	 * Instantiates a new publisher configuration.
	 *
	 * @param props the props
	 */
	protected PublisherConfiguration(Properties props) {
		setProductName(props.getProperty("Publisher.ProductName"));
		setApplicationDescription(props.getProperty("Publisher.ApplicationDescription"));
		setApplicationName(props.getProperty("Publisher.ApplicationName"));
		
		String type = props.getProperty("Publisher.ApplicationType");
		if (TypeUtils.isNotBlank(type)) {
			ApplicationType applicationType = ApplicationType.valueOf(type);
			setApplicationType(applicationType != null ? applicationType : ApplicationType.UNKNOWN);
		}
		
		setApplicationVersionNumber(props.getProperty("Publisher.ApplicationVersionNumber", "1.0"));
		setApplicationVersion(new Version(props.getProperty("Publisher.ApplicationVersionNumber", "1.0")));
		setEnvironmentName(props.getProperty("Publisher.EnvironmentName"));
		setPromotionLevelName(props.getProperty("Publisher.PromotionLevelName"));
		setForceSynchronous(Boolean.valueOf(props.getProperty("Publisher.ForceSynchronous", String.valueOf(forceSynchronous))));
		setMaxQueueLength(Integer.valueOf(props.getProperty("Publisher.MaxQueueLength", String.valueOf(maxQueueLength))));
		setEnableAnonymousMode(Boolean.valueOf(props.getProperty("Publisher.EnableAnonymousMode", String.valueOf(enableAnonymousMode))));
		setEnableDebugMode(Boolean.valueOf(props.getProperty("Publisher.EnableDebugMode", String.valueOf(enableDebugMode))));
	}
	
	/**
	 * Instantiates a new publisher configuration.
	 *
	 * @param builder the builder
	 */
	private PublisherConfiguration(Builder builder) {
		this.productName = builder.productName;
		this.applicationDescription = builder.applicationDescription;
		this.applicationName = builder.applicationName;
		this.applicationType = builder.applicationType;
		this.applicationVersion = builder.applicationVersion;
		this.environmentName = builder.environmentName;
		this.promotionLevelName = builder.promotionLevelName;
		this.forceSynchronous = builder.forceSynchronous;
		this.maxQueueLength = builder.maxQueueLength;
		this.enableAnonymousMode = builder.enableAnonymousMode;
		this.enableDebugMode = builder.enableDebugMode;
	}
	
	/**
	 * Optional. The name of the product for logging purposes.
	 * 
	 * Generally unnecessary for windows services, console apps, and WinForm
	 * applications. Useful for web applications where there is no reasonable way of
	 * automatically determining product name from the assemblies that initiate
	 * logging.
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
	 * Optional. A description of the application to include with the session
	 * information.
	 * 
	 * Generally unnecessary for windows services, console apps, and WinForm
	 * applications. Useful for web applications where there is no reasonable way of
	 * automatically determining application description from the assemblies that
	 * initiate logging.
	 */
	private String applicationDescription;

	/**
	 * Gets the application description.
	 *
	 * @return the application description
	 */
	public String getApplicationDescription() {
		return this.applicationDescription;
	}

	/**
	 * Sets the application description.
	 *
	 * @param value the new application description
	 */
	public void setApplicationDescription(String value) {
		this.applicationDescription = value;
	}

	/**
	 * Optional. The name of the application for logging purposes.
	 * 
	 * Generally unnecessary for windows services, console apps, and WinForm
	 * applications. Useful for web applications where there is no reasonable way of
	 * automatically determining product name from the assemblies that initiate
	 * logging.
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
	 * Optional. The ApplicationType to treat the application as, overriding the
	 * Agent's automatic determination.
	 * 
	 * This setting is not generally necessary as the Agent will automatically
	 * determine the application type correctly in most typical windows services,
	 * console apps, WinForm applications, and ASP.NET applications. If the
	 * automatic determination is unsuccessful or incorrect with a particular
	 * application, the correct type can be configured with this setting to bypass
	 * the automatic determination. However, setting this incorrectly for the
	 * application could have undesirable effects.
	 */
	private ApplicationType applicationType = ApplicationType.UNKNOWN;

	/**
	 * Gets the application type.
	 *
	 * @return the application type
	 */
	public ApplicationType getApplicationType() {
		return this.applicationType;
	}

	/**
	 * Sets the application type.
	 *
	 * @param value the new application type
	 */
	public void setApplicationType(ApplicationType value) {
		this.applicationType = value;
	}

	/**
	 * Optional. The version of the application for logging purposes.
	 * 
	 * <p>
	 * Generally unnecessary for windows services, console apps, and WinForm
	 * applications. Useful for web applications where there is no reasonable way of
	 * automatically determining product name from the assemblies that initiate
	 * logging.
	 * </p>
	 */
	private Version applicationVersion = new Version(2,2);

	/**
	 * Gets the application version.
	 *
	 * @return the application version
	 */
	public Version getApplicationVersion() {
		return this.applicationVersion;
	}

	/**
	 * Sets the application version.
	 *
	 * @param value the new application version
	 */
	public void setApplicationVersion(Version value) {
		this.applicationVersion = value;
	}

	/**
	 * We need this to load from JSON, because there's currently no custom binding
	 * and the standard binder doesn't use Version.Parse.
	 * 
	 * Added Attributes to hide in IntelliSense.
	 *
	 * @return the application version number
	 */
	public String getApplicationVersionNumber() {
		return getApplicationVersion() == null ? null : getApplicationVersion().toString();
	}

	/**
	 * Sets the application version number.
	 *
	 * @param value the new application version number
	 */
	public void setApplicationVersionNumber(String value) {
		setApplicationVersion(new Version(value));
	}

	/**
	 * Optional. The environment this session is running in.
	 * 
	 * Environments are useful for categorizing sessions, for example to indicate
	 * the hosting environment. If a value is provided it will be carried with the
	 * session data to upstream servers and clients. If the corresponding entry does
	 * not exist it will be automatically created.
	 */
	private String environmentName;

	/**
	 * Gets the environment name.
	 *
	 * @return the environment name
	 */
	public String getEnvironmentName() {
		return this.environmentName;
	}

	/**
	 * Sets the environment name.
	 *
	 * @param value the new environment name
	 */
	public void setEnvironmentName(String value) {
		this.environmentName = value;
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
	private String promotionLevelName;

	/**
	 * Gets the promotion level name.
	 *
	 * @return the promotion level name
	 */
	public String getPromotionLevelName() {
		return this.promotionLevelName;
	}

	/**
	 * Sets the promotion level name.
	 *
	 * @param value the new promotion level name
	 */
	public void setPromotionLevelName(String value) {
		this.promotionLevelName = value;
	}

	/**
	 * When true, the publisher will treat all publish requests as write-through
	 * requests.
	 * 
	 * This overrides the write through request flag for all published requests,
	 * acting as if they are set true. This will slow down logging and change the
	 * degree of parallelism of multithreaded applications since each log message
	 * will block until it is committed to every configured messenger.
	 */
	private boolean forceSynchronous = false;

	/**
	 * Gets the force synchronous.
	 *
	 * @return the force synchronous
	 */
	public boolean getForceSynchronous() {
		return this.forceSynchronous;
	}

	/**
	 * Sets the force synchronous.
	 *
	 * @param value the new force synchronous
	 */
	public void setForceSynchronous(boolean value) {
		this.forceSynchronous = value;
	}

	/**
	 * The maximum number of queued messages waiting to be published.
	 * 
	 * Once the total number of messages waiting to be published exceeds the maximum
	 * queue length the log publisher will switch to a synchronous mode to catch up.
	 * This will cause the client to block until each new message is published.
	 */
	private int maxQueueLength = 2000;

	/**
	 * Gets the max queue length.
	 *
	 * @return the max queue length
	 */
	public int getMaxQueueLength() {
		return this.maxQueueLength;
	}

	/**
	 * Sets the max queue length.
	 *
	 * @param value the new max queue length
	 */
	public void setMaxQueueLength(int value) {
		this.maxQueueLength = value;
	}

	/**
	 * When true, the Agent will record session data without collecting
	 * personally-identifying information.
	 * 
	 * In anonymous mode the Agent will not collect personally-identifying
	 * information such as user name, user domain name, host name, host domain name,
	 * and the application's command line. Anonymous mode is disabled by default,
	 * and normal operation will collect this information automatically.
	 */
	private boolean enableAnonymousMode = false;

	/**
	 * Gets the enable anonymous mode.
	 *
	 * @return the enable anonymous mode
	 */
	public boolean getEnableAnonymousMode() {
		return this.enableAnonymousMode;
	}

	/**
	 * Sets the enable anonymous mode.
	 *
	 * @param value the new enable anonymous mode
	 */
	public void setEnableAnonymousMode(boolean value) {
		this.enableAnonymousMode = value;
	}

	/**
	 * When true, the Agent will include debug messages in logs. Not intended for
	 * production use
	 * 
	 * <p>
	 * Normally the Agent will fail silently and otherwise compensate for problems
	 * to ensure that it does not cause a problem for your application. When you are
	 * developing your application you can enable this mode to get more detail about
	 * why th Agent is behaving as it is and resolve issues.
	 * </p>
	 * <p>
	 * In debug mode the agent may throw exceptions to indicate calling errors it
	 * normally would just silently ignore. Therefore, this option is not
	 * recommended for consistent production use.
	 * </p>
	 */
	private boolean enableDebugMode = false;

	/**
	 * Gets the enable debug mode.
	 *
	 * @return the enable debug mode
	 */
	public boolean getEnableDebugMode() {
		return this.enableDebugMode;
	}

	/**
	 * Sets the enable debug mode.
	 *
	 * @param value the new enable debug mode
	 */
	public void setEnableDebugMode(boolean value) {
		this.enableDebugMode = value;
	}

	/**
	 * Normalize configuration data.
	 */
	public void sanitize() {
		if (getMaxQueueLength() <= 0) {
			setMaxQueueLength(2000);
		} else if (getMaxQueueLength() > 50000) {
			setMaxQueueLength(2000);
		}

		if (TypeUtils.isBlank(getProductName())) {
			setProductName(null);
		}

		if (TypeUtils.isBlank(getApplicationDescription())) {
			setApplicationDescription(null);
		}

		if (TypeUtils.isBlank(getApplicationName())) {
			setApplicationName(null);
		}

	}

	/**
	 * Creates builder to build {@link PublisherConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link PublisherConfiguration}.
	 */
	public static final class Builder {
		
		/** The product name. */
		private String productName;
		
		/** The application description. */
		private String applicationDescription;
		
		/** The application name. */
		private String applicationName;
		
		/** The application type. */
		private ApplicationType applicationType;
		
		/** The application version. */
		private Version applicationVersion;
		
		/** The environment name. */
		private String environmentName;
		
		/** The promotion level name. */
		private String promotionLevelName;
		
		/** The force synchronous. */
		private boolean forceSynchronous;
		
		/** The max queue length. */
		private int maxQueueLength;
		
		/** The enable anonymous mode. */
		private boolean enableAnonymousMode;
		
		/** The enable debug mode. */
		private boolean enableDebugMode;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
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
		 * Application description.
		 *
		 * @param applicationDescription the application description
		 * @return the builder
		 */
		public Builder applicationDescription(String applicationDescription) {
			this.applicationDescription = applicationDescription;
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
		 * Application type.
		 *
		 * @param applicationType the application type
		 * @return the builder
		 */
		public Builder applicationType(ApplicationType applicationType) {
			this.applicationType = applicationType;
			return this;
		}

		/**
		 * Application version.
		 *
		 * @param applicationVersion the application version
		 * @return the builder
		 */
		public Builder applicationVersion(Version applicationVersion) {
			this.applicationVersion = applicationVersion;
			return this;
		}

		/**
		 * Environment name.
		 *
		 * @param environmentName the environment name
		 * @return the builder
		 */
		public Builder environmentName(String environmentName) {
			this.environmentName = environmentName;
			return this;
		}

		/**
		 * Promotion level name.
		 *
		 * @param promotionLevelName the promotion level name
		 * @return the builder
		 */
		public Builder promotionLevelName(String promotionLevelName) {
			this.promotionLevelName = promotionLevelName;
			return this;
		}

		/**
		 * Force synchronous.
		 *
		 * @param forceSynchronous the force synchronous
		 * @return the builder
		 */
		public Builder forceSynchronous(boolean forceSynchronous) {
			this.forceSynchronous = forceSynchronous;
			return this;
		}

		/**
		 * Max queue length.
		 *
		 * @param maxQueueLength the max queue length
		 * @return the builder
		 */
		public Builder maxQueueLength(int maxQueueLength) {
			this.maxQueueLength = maxQueueLength;
			return this;
		}

		/**
		 * Enable anonymous mode.
		 *
		 * @param enableAnonymousMode the enable anonymous mode
		 * @return the builder
		 */
		public Builder enableAnonymousMode(boolean enableAnonymousMode) {
			this.enableAnonymousMode = enableAnonymousMode;
			return this;
		}

		/**
		 * Enable debug mode.
		 *
		 * @param enableDebugMode the enable debug mode
		 * @return the builder
		 */
		public Builder enableDebugMode(boolean enableDebugMode) {
			this.enableDebugMode = enableDebugMode;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the publisher configuration
		 */
		public PublisherConfiguration build() {
			return new PublisherConfiguration(this);
		}
	}
}