package com.onloupe.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import com.onloupe.core.util.FileUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class AgentConfiguration.
 */
public final class AgentConfiguration {
	
	/** The Constant LOUPE_INTERNAL_PLATFORM_VALUE. */
	private static final String LOUPE_INTERNAL_PLATFORM_VALUE = "JAVA";
	
	/** The Constant LOUPE_INTERNAL_PLATFORM_KEY. */
	private static final String LOUPE_INTERNAL_PLATFORM_KEY = "LOUPE_INTERNAL_PLATFORM";

	/**
	 * Create a new agent configuration. This constructor will search for
	 * an available loupe properties file.
	 */
	public AgentConfiguration() {
		ClassLoader loader = AgentConfiguration.class.getClassLoader();
		URL configSource = loader.getResource("loupe.properties");
		
		// if there is no .properties file, we look for a .xml file
		if (configSource == null) {
			configSource = loader.getResource("loupe.xml");
		}
		
		// file found or not, continue
		configureAgent(configSource);
	}

	/**
	 * Instantiates a new agent configuration.
	 *
	 * @param configSource the config source
	 */
	public AgentConfiguration(URL configSource) {
		configureAgent(configSource);
	}
	
	/**
	 * Create a new agent configuration.
	 * 
	 * @param props A set of properties to configure the agent.
	 */
	public AgentConfiguration(Properties props) {
		configureAgent(props);
	}
	
	/**
	 * Create a new agent configuration, copying properties from existing one.
	 * 
	 * @param configuration An existing agent configuration.
	 */
	public AgentConfiguration(AgentConfiguration configuration) {
		setListener(configuration.getListener());
		setNetworkViewer(configuration.getNetworkViewer());
		setPackager(configuration.getPackager());
		setProperties(configuration.getProperties());
		setPublisher(configuration.getPublisher());
		setServer(configuration.getServer());
		setSessionFile(configuration.getSessionFile());
	}
	
	/**
	 * Instantiates a new agent configuration.
	 *
	 * @param builder the builder
	 */
	private AgentConfiguration(Builder builder) {
		this.listener = builder.listener;
		this.sessionFile = builder.sessionFile;
		this.packager = builder.packager;
		this.publisher = builder.publisher;
		this.server = builder.server;
		this.networkViewer = builder.networkViewer;
		this.properties = builder.properties;
	}
	
	/**
	 * Configure agent.
	 *
	 * @param configSource the config source
	 */
	private void configureAgent(URL configSource) {
		Properties props = new Properties();
		
		if (configSource != null) {
			File configFile = new File(configSource.getPath());
			try (FileInputStream fos = new FileInputStream(configFile)) {
				if (FileUtils.getFileExtension(configFile.getAbsolutePath()).equalsIgnoreCase(".XML")) {
					props.loadFromXML(fos);
				} else {
					props.load(fos);
				}
			} catch (Exception e) {
				// if we have a file but can't load props, notify and continue
				System.out.println("Loupe Agent: Error loading properties from " + configSource.getPath() + ".");
				e.printStackTrace();
				System.out.println("Loupe Agent: Initializing with default configuration.");
			}
		} else {
			// if we have no file, continue
			System.out.println("Loupe Agent: No configuration file specified.");
			System.out.println("Loupe Agent: Initializing with default configuration.");
		}
		
		configureAgent(props);
	}
	

	
	/**
	 * Configure agent.
	 *
	 * @param props the props
	 */
	private void configureAgent(Properties props) {
		if (props == null || props.isEmpty()) {
			// no props. defaults are assigned declaratively. nothing to do here.
			return;
		}
		
		setListener(new ListenerConfiguration(props));
		setSessionFile(new SessionFileConfiguration(props));
		setPackager(new PackagerConfiguration(props));
		setPublisher(new PublisherConfiguration(props));
		setServer(new ServerConfiguration(props));
		setNetworkViewer(new NetworkViewerConfiguration(props));		
	}

	/** The listener configuration. */
	private ListenerConfiguration listener = new ListenerConfiguration();

	/**
	 * Gets the listener.
	 *
	 * @return the listener
	 */
	public ListenerConfiguration getListener() {
		return this.listener;
	}

	/**
	 * Sets the listener.
	 *
	 * @param value the new listener
	 */
	private void setListener(ListenerConfiguration value) {
		this.listener = value;
	}

	/** The session data file configuration. */
	private SessionFileConfiguration sessionFile = new SessionFileConfiguration();

	/**
	 * Gets the session file.
	 *
	 * @return the session file
	 */
	public SessionFileConfiguration getSessionFile() {
		return this.sessionFile;
	}

	/**
	 * Sets the session file.
	 *
	 * @param value the new session file
	 */
	private void setSessionFile(SessionFileConfiguration value) {
		this.sessionFile = value;
	}

	/** The packager configuration. */
	private PackagerConfiguration packager = new PackagerConfiguration();

	/**
	 * Gets the packager.
	 *
	 * @return the packager
	 */
	public PackagerConfiguration getPackager() {
		return this.packager;
	}

	/**
	 * Sets the packager.
	 *
	 * @param value the new packager
	 */
	private void setPackager(PackagerConfiguration value) {
		this.packager = value;
	}

	/** The publisher configuration. */
	private PublisherConfiguration publisher = new PublisherConfiguration();

	/**
	 * Gets the publisher.
	 *
	 * @return the publisher
	 */
	public PublisherConfiguration getPublisher() {
		return this.publisher;
	}

	/**
	 * Sets the publisher.
	 *
	 * @param value the new publisher
	 */
	private void setPublisher(PublisherConfiguration value) {
		this.publisher = value;
	}

	/** The central server configuration. */
	private ServerConfiguration server = new ServerConfiguration();

	/**
	 * Gets the server.
	 *
	 * @return the server
	 */
	public ServerConfiguration getServer() {
		return this.server;
	}

	/**
	 * Sets the server.
	 *
	 * @param value the new server
	 */
	private void setServer(ServerConfiguration value) {
		this.server = value;
	}

	/** Configures real-time network log streaming. */
	private NetworkViewerConfiguration networkViewer = new NetworkViewerConfiguration();

	/**
	 * Gets the network viewer.
	 *
	 * @return the network viewer
	 */
	public NetworkViewerConfiguration getNetworkViewer() {
		return this.networkViewer;
	}

	/**
	 * Sets the network viewer.
	 *
	 * @param value the new network viewer
	 */
	private void setNetworkViewer(NetworkViewerConfiguration value) {
		this.networkViewer = value;
	}

	/** Application defined properties. */
	private Properties properties;

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * Sets the properties.
	 *
	 * @param value the new properties
	 */
	private void setProperties(Properties value) {
		this.properties = value;
	}

	/**
	 * Normalize configuration values.
	 */
	public void sanitize() {
		// we want to force everyone to load and sanitize so we know it's completed.
		getNetworkViewer().sanitize();
		getPackager().sanitize();
		getPublisher().sanitize();
		getSessionFile().sanitize();
		getServer().sanitize();
		
		if (getProperties() == null) {
			setProperties(new Properties());
		}
		
		getProperties().remove(LOUPE_INTERNAL_PLATFORM_KEY);
		getProperties().put(LOUPE_INTERNAL_PLATFORM_KEY, LOUPE_INTERNAL_PLATFORM_VALUE);
	}
	
	/**
	 * Creates builder to build {@link AgentConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link AgentConfiguration}.
	 */
	public static final class Builder {
		
		/** The listener. */
		private ListenerConfiguration listener;
		
		/** The session file. */
		private SessionFileConfiguration sessionFile;
		
		/** The packager. */
		private PackagerConfiguration packager;
		
		/** The publisher. */
		private PublisherConfiguration publisher;
		
		/** The server. */
		private ServerConfiguration server;
		
		/** The network viewer. */
		private NetworkViewerConfiguration networkViewer;
		
		/** The properties. */
		private Properties properties;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
		}

		/**
		 * Listener.
		 *
		 * @param listener the listener
		 * @return the builder
		 */
		public Builder listener(ListenerConfiguration listener) {
			this.listener = listener;
			return this;
		}

		/**
		 * Session file.
		 *
		 * @param sessionFile the session file
		 * @return the builder
		 */
		public Builder sessionFile(SessionFileConfiguration sessionFile) {
			this.sessionFile = sessionFile;
			return this;
		}

		/**
		 * Packager.
		 *
		 * @param packager the packager
		 * @return the builder
		 */
		public Builder packager(PackagerConfiguration packager) {
			this.packager = packager;
			return this;
		}

		/**
		 * Publisher.
		 *
		 * @param publisher the publisher
		 * @return the builder
		 */
		public Builder publisher(PublisherConfiguration publisher) {
			this.publisher = publisher;
			return this;
		}

		/**
		 * Server.
		 *
		 * @param server the server
		 * @return the builder
		 */
		public Builder server(ServerConfiguration server) {
			this.server = server;
			return this;
		}

		/**
		 * Network viewer.
		 *
		 * @param networkViewer the network viewer
		 * @return the builder
		 */
		public Builder networkViewer(NetworkViewerConfiguration networkViewer) {
			this.networkViewer = networkViewer;
			return this;
		}

		/**
		 * Properties.
		 *
		 * @param properties the properties
		 * @return the builder
		 */
		public Builder properties(Properties properties) {
			this.properties = properties;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the agent configuration
		 */
		public AgentConfiguration build() {
			return new AgentConfiguration(this);
		}
	}
	
}