package com.onloupe.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import com.onloupe.core.util.FileUtils;

public final class AgentConfiguration {
	
	private static final String LOUPE_INTERNAL_PLATFORM_VALUE = "JAVA";
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
	
	private AgentConfiguration(Builder builder) {
		this.listener = builder.listener;
		this.sessionFile = builder.sessionFile;
		this.packager = builder.packager;
		this.publisher = builder.publisher;
		this.server = builder.server;
		this.networkViewer = builder.networkViewer;
		this.properties = builder.properties;
	}
	
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

	/**
	 * The listener configuration
	 */
	private ListenerConfiguration listener = new ListenerConfiguration();

	public ListenerConfiguration getListener() {
		return this.listener;
	}

	private void setListener(ListenerConfiguration value) {
		this.listener = value;
	}

	/**
	 * The session data file configuration
	 */
	private SessionFileConfiguration sessionFile = new SessionFileConfiguration();

	public SessionFileConfiguration getSessionFile() {
		return this.sessionFile;
	}

	private void setSessionFile(SessionFileConfiguration value) {
		this.sessionFile = value;
	}

	/**
	 * The packager configuration
	 */
	private PackagerConfiguration packager = new PackagerConfiguration();

	public PackagerConfiguration getPackager() {
		return this.packager;
	}

	private void setPackager(PackagerConfiguration value) {
		this.packager = value;
	}

	/**
	 * The publisher configuration
	 */
	private PublisherConfiguration publisher = new PublisherConfiguration();

	public PublisherConfiguration getPublisher() {
		return this.publisher;
	}

	private void setPublisher(PublisherConfiguration value) {
		this.publisher = value;
	}

	/**
	 * The central server configuration
	 */
	private ServerConfiguration server = new ServerConfiguration();

	public ServerConfiguration getServer() {
		return this.server;
	}

	private void setServer(ServerConfiguration value) {
		this.server = value;
	}

	/**
	 * Configures real-time network log streaming
	 */
	private NetworkViewerConfiguration networkViewer = new NetworkViewerConfiguration();

	public NetworkViewerConfiguration getNetworkViewer() {
		return this.networkViewer;
	}

	private void setNetworkViewer(NetworkViewerConfiguration value) {
		this.networkViewer = value;
	}

	/**
	 * Application defined properties
	 */
	private Properties properties;

	public Properties getProperties() {
		return this.properties;
	}

	private void setProperties(Properties value) {
		this.properties = value;
	}

	/**
	 * Normalize configuration values
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
		private ListenerConfiguration listener;
		private SessionFileConfiguration sessionFile;
		private PackagerConfiguration packager;
		private PublisherConfiguration publisher;
		private ServerConfiguration server;
		private NetworkViewerConfiguration networkViewer;
		private Properties properties;

		private Builder() {
		}

		public Builder listener(ListenerConfiguration listener) {
			this.listener = listener;
			return this;
		}

		public Builder sessionFile(SessionFileConfiguration sessionFile) {
			this.sessionFile = sessionFile;
			return this;
		}

		public Builder packager(PackagerConfiguration packager) {
			this.packager = packager;
			return this;
		}

		public Builder publisher(PublisherConfiguration publisher) {
			this.publisher = publisher;
			return this;
		}

		public Builder server(ServerConfiguration server) {
			this.server = server;
			return this;
		}

		public Builder networkViewer(NetworkViewerConfiguration networkViewer) {
			this.networkViewer = networkViewer;
			return this;
		}

		public Builder properties(Properties properties) {
			this.properties = properties;
			return this;
		}

		public AgentConfiguration build() {
			return new AgentConfiguration(this);
		}
	}
	
}