package com.onloupe.configuration;

import java.util.Properties;

import com.onloupe.core.util.TypeUtils;

// TODO: Auto-generated Javadoc
/**
 * File Messenger Configuration.
 */
public final class SessionFileConfiguration implements IMessengerConfiguration {

	/**
	 * Instantiates a new session file configuration.
	 */
	public SessionFileConfiguration() {

	}
	
	/**
	 * Instantiates a new session file configuration.
	 *
	 * @param props the props
	 */
	protected SessionFileConfiguration(Properties props) {
		setEnabled(Boolean.valueOf(props.getProperty("SessionFile.Enabled", String.valueOf(enabled))));
		setAutoFlushInterval(Integer.valueOf(props.getProperty("SessionFile.AutoFlushInterval", String.valueOf(autoFlushInterval))));
		setIndexUpdateInterval(Integer.valueOf(props.getProperty("SessionFile.IndexUpdateInterval", String.valueOf(indexUpdateInterval))));
		setMaxFileSize(Integer.valueOf(props.getProperty("SessionFile.MaxFileSize", String.valueOf(maxFileSize))));
		setMaxFileDuration(Integer.valueOf(props.getProperty("SessionFile.MaxFileDuration", String.valueOf(maxFileDuration))));
		setEnableFilePruning(Boolean.valueOf(props.getProperty("SessionFile.EnableFilePruning", String.valueOf(enableFilePruning))));
		setMaxLocalDiskUsage(Integer.valueOf(props.getProperty("SessionFile.MaxLocalDiskUsage", String.valueOf(maxLocalDiskUsage))));
		setMaxLocalFileAge(Integer.valueOf(props.getProperty("SessionFile.MaxLocalFileAge", String.valueOf(maxLocalFileAge))));
		setMinimumFreeDisk(Integer.valueOf(props.getProperty("SessionFile.MinimumFreeDisk", String.valueOf(minimumFreeDisk))));
		setForceSynchronous(Boolean.valueOf(props.getProperty("SessionFile.ForceSynchronous", String.valueOf(forceSynchronous))));
		setMaxQueueLength(Integer.valueOf(props.getProperty("SessionFile.MaxQueueLength", String.valueOf(maxQueueLength))));
		setFolder(props.getProperty("SessionFile.Folder"));
	}
	
	/**
	 * Instantiates a new session file configuration.
	 *
	 * @param builder the builder
	 */
	private SessionFileConfiguration(Builder builder) {
		this.autoFlushInterval = builder.autoFlushInterval;
		this.folder = builder.folder;
		this.indexUpdateInterval = builder.indexUpdateInterval;
		this.maxFileSize = builder.maxFileSize;
		this.maxFileDuration = builder.maxFileDuration;
		this.enableFilePruning = builder.enableFilePruning;
		this.maxLocalDiskUsage = builder.maxLocalDiskUsage;
		this.maxLocalFileAge = builder.maxLocalFileAge;
		this.minimumFreeDisk = builder.minimumFreeDisk;
		this.forceSynchronous = builder.forceSynchronous;
		this.maxQueueLength = builder.maxQueueLength;
		this.enabled = builder.enabled;
	}
	
	/**
	 * The maximum number of seconds data can be held before it is flushed.
	 * 
	 * In addition to the default automatic flush due to the amount of information
	 * waiting to be written out the messenger will automatically flush to disk
	 * based on the number of seconds specified.
	 */
	private int autoFlushInterval = 15;

	/**
	 * Gets the auto flush interval.
	 *
	 * @return the auto flush interval
	 */
	public int getAutoFlushInterval() {
		return this.autoFlushInterval;
	}

	/**
	 * Sets the auto flush interval.
	 *
	 * @param value the new auto flush interval
	 */
	public void setAutoFlushInterval(int value) {
		this.autoFlushInterval = value;
	}

	/**
	 * The folder to store session files in unless explicitly overridden at runtime.
	 * 
	 * If null or empty, files will be stored in a central local application data
	 * folder which is the preferred setting.
	 */
	private String folder;

	/**
	 * Gets the folder.
	 *
	 * @return the folder
	 */
	public String getFolder() {
		return this.folder;
	}

	/**
	 * Sets the folder.
	 *
	 * @param value the new folder
	 */
	public void setFolder(String value) {
		this.folder = value;
	}

	/**
	 * The number of seconds between index updates.
	 * 
	 * An index is maintained of session information including the number and types
	 * of messages and session status. It is updated automatically when a session is
	 * stopped and in some other situations.
	 */
	private int indexUpdateInterval = 15;

	/**
	 * Gets the index update interval.
	 *
	 * @return the index update interval
	 */
	public int getIndexUpdateInterval() {
		return this.indexUpdateInterval;
	}

	/**
	 * Sets the index update interval.
	 *
	 * @param value the new index update interval
	 */
	public void setIndexUpdateInterval(int value) {
		this.indexUpdateInterval = value;
	}

	/**
	 * The maximum number of megabytes in a single session file before a new file is
	 * started.
	 * 
	 * When the file reaches the maximum size it will be closed and a new file
	 * started. Due to compression effects and other data storage considerations,
	 * final files may end up slightly larger on disk or somewhat smaller. Setting
	 * to zero will allow files to grow to the maximum size allowed by the file
	 * format (2 GB)
	 */
	private int maxFileSize = 20;

	/**
	 * Gets the max file size.
	 *
	 * @return the max file size
	 */
	public int getMaxFileSize() {
		return this.maxFileSize;
	}

	/**
	 * Sets the max file size.
	 *
	 * @param value the new max file size
	 */
	public void setMaxFileSize(int value) {
		this.maxFileSize = value;
	}

	/**
	 * The maximum number of minutes in a single session file before a new file is
	 * started.
	 * 
	 * When the file reaches the maximum age it will be closed and a new file
	 * started. Setting to zero will allow the file to cover an unlimited period of
	 * time.
	 */
	private int maxFileDuration = 1440;

	/**
	 * Gets the max file duration.
	 *
	 * @return the max file duration
	 */
	public int getMaxFileDuration() {
		return this.maxFileDuration;
	}

	/**
	 * Sets the max file duration.
	 *
	 * @param value the new max file duration
	 */
	public void setMaxFileDuration(int value) {
		this.maxFileDuration = value;
	}

	/**
	 * When true, session files will be pruned for size or age.
	 * 
	 * By default session files older than a specified number of days are
	 * automatically deleted and the oldest files are removed when the total storage
	 * of all files for the same application exceeds a certain value. Setting this
	 * option to false will disable pruning.
	 */
	private boolean enableFilePruning = true;

	/**
	 * Gets the enable file pruning.
	 *
	 * @return the enable file pruning
	 */
	public boolean getEnableFilePruning() {
		return this.enableFilePruning;
	}

	/**
	 * Sets the enable file pruning.
	 *
	 * @param value the new enable file pruning
	 */
	public void setEnableFilePruning(boolean value) {
		this.enableFilePruning = value;
	}

	/**
	 * The maximum number of megabytes for all log files in megabytes on the local
	 * drive before older files are purged.
	 * 
	 * <p>
	 * When the maximum local disk usage is approached, files are purged by
	 * selecting the oldest files first. This limit may be exceeded temporarily by
	 * the maximum log size because the active file will not be purged. Size is
	 * specified in megabytes.
	 * </p>
	 * <p>
	 * Setting to any integer less than 1 will disable pruning by disk usage.
	 * </p>
	 */
	private int maxLocalDiskUsage = 150;

	/**
	 * Gets the max local disk usage.
	 *
	 * @return the max local disk usage
	 */
	public int getMaxLocalDiskUsage() {
		return this.maxLocalDiskUsage;
	}

	/**
	 * Sets the max local disk usage.
	 *
	 * @param value the new max local disk usage
	 */
	public void setMaxLocalDiskUsage(int value) {
		this.maxLocalDiskUsage = value;
	}

	/**
	 * The number of days that log files are retained.
	 * 
	 * 
	 * <p>
	 * Log files that were collected longer than the retention interval ago will be
	 * removed regardless of space constraints.
	 * </p>
	 * <p>
	 * Setting to any integer less than 1 will disable pruning by age.
	 * </p>
	 * 
	 */
	private int maxLocalFileAge = 90;

	/**
	 * Gets the max local file age.
	 *
	 * @return the max local file age
	 */
	public int getMaxLocalFileAge() {
		return this.maxLocalFileAge;
	}

	/**
	 * Sets the max local file age.
	 *
	 * @param value the new max local file age
	 */
	public void setMaxLocalFileAge(int value) {
		this.maxLocalFileAge = value;
	}

	/**
	 * The minimum amount of free disk space for logging.
	 * 
	 * If the amount of free disk space falls below this value, existing log files
	 * will be removed to free space. If no more log files are available, logging
	 * will stop until adequate space is freed.
	 */
	private int minimumFreeDisk = 200;

	/**
	 * Gets the minimum free disk.
	 *
	 * @return the minimum free disk
	 */
	public int getMinimumFreeDisk() {
		return this.minimumFreeDisk;
	}

	/**
	 * Sets the minimum free disk.
	 *
	 * @param value the new minimum free disk
	 */
	public void setMinimumFreeDisk(int value) {
		this.minimumFreeDisk = value;
	}

	/**
	 * When true, the session file will treat all write requests as write-through
	 * requests.
	 * 
	 * This overrides the write through request flag for all published requests,
	 * acting as if they are set true. This will slow down logging and change the
	 * degree of parallelism of multithreaded applications since each log message
	 * will block until it is committed.
	 */
	private boolean forceSynchronous = false;

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#getForceSynchronous()
	 */
	@Override
	public boolean getForceSynchronous() {
		return this.forceSynchronous;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#setForceSynchronous(boolean)
	 */
	@Override
	public void setForceSynchronous(boolean value) {
		this.forceSynchronous = value;
	}

	/**
	 * The maximum number of queued messages waiting to be processed by the session
	 * file
	 * 
	 * Once the total number of messages waiting to be processed exceeds the maximum
	 * queue length the session file will switch to a synchronous mode to catch up.
	 * This will not cause the application to experience synchronous logging
	 * behavior unless the publisher queue is also filled.
	 */
	private int maxQueueLength = 2000;

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#getMaxQueueLength()
	 */
	@Override
	public int getMaxQueueLength() {
		return this.maxQueueLength;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#setMaxQueueLength(int)
	 */
	@Override
	public void setMaxQueueLength(int value) {
		this.maxQueueLength = value;
	}

	/**
	 * When false, the session file is disabled even if otherwise configured.
	 * 
	 * This allows for explicit disable/enable without removing the existing
	 * configuration or worrying about the default configuration.
	 */
	private boolean enabled = true;

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#getEnabled()
	 */
	@Override
	public boolean getEnabled() {
		return this.enabled;
	}

	/* (non-Javadoc)
	 * @see com.onloupe.configuration.IMessengerConfiguration#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean value) {
		this.enabled = value;
	}

	/**
	 * Normalize the configuration.
	 */
	public void sanitize() {
		if (TypeUtils.isBlank(getFolder())) {
			setFolder(null);
		}

		if (getAutoFlushInterval() <= 0) {
			setAutoFlushInterval(15);
		}

		if (getIndexUpdateInterval() <= 0) {
			setIndexUpdateInterval(15);
		}

		if (getMaxFileDuration() < 1) {
			setMaxFileDuration(1576800); // three years, treated as infinite because really - is a process going to run
											// longer than that?
		}

		if (getMaxFileSize() <= 0) {
			setMaxFileSize(1024); // 1GB override when set to zero.
		}

		if (getMaxLocalDiskUsage() <= 0) {
			setMaxLocalDiskUsage(0); // we intelligently disable at this point
		} else {
			// make sure our max file size can fit within our max local disk usage
			if (getMaxLocalDiskUsage() < getMaxFileSize()) {
				setMaxFileSize(getMaxLocalDiskUsage());
			}
		}

		if (getMaxLocalFileAge() <= 0) {
			setMaxLocalFileAge(0); // we intelligently disable at this point
		}

		if (getMinimumFreeDisk() <= 0) {
			setMinimumFreeDisk(50);
		}

		if (getMaxQueueLength() <= 0) {
			setMaxQueueLength(2000);
		} else if (getMaxQueueLength() > 50000) {
			setMaxQueueLength(50000);
		}
	}

	/**
	 * Creates builder to build {@link SessionFileConfiguration}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link SessionFileConfiguration}.
	 */
	public static final class Builder {
		
		/** The auto flush interval. */
		private int autoFlushInterval;
		
		/** The folder. */
		private String folder;
		
		/** The index update interval. */
		private int indexUpdateInterval;
		
		/** The max file size. */
		private int maxFileSize;
		
		/** The max file duration. */
		private int maxFileDuration;
		
		/** The enable file pruning. */
		private boolean enableFilePruning;
		
		/** The max local disk usage. */
		private int maxLocalDiskUsage;
		
		/** The max local file age. */
		private int maxLocalFileAge;
		
		/** The minimum free disk. */
		private int minimumFreeDisk;
		
		/** The force synchronous. */
		private boolean forceSynchronous;
		
		/** The max queue length. */
		private int maxQueueLength;
		
		/** The enabled. */
		private boolean enabled;

		/**
		 * Instantiates a new builder.
		 */
		private Builder() {
		}

		/**
		 * Auto flush interval.
		 *
		 * @param autoFlushInterval the auto flush interval
		 * @return the builder
		 */
		public Builder autoFlushInterval(int autoFlushInterval) {
			this.autoFlushInterval = autoFlushInterval;
			return this;
		}

		/**
		 * Folder.
		 *
		 * @param folder the folder
		 * @return the builder
		 */
		public Builder folder(String folder) {
			this.folder = folder;
			return this;
		}

		/**
		 * Index update interval.
		 *
		 * @param indexUpdateInterval the index update interval
		 * @return the builder
		 */
		public Builder indexUpdateInterval(int indexUpdateInterval) {
			this.indexUpdateInterval = indexUpdateInterval;
			return this;
		}

		/**
		 * Max file size.
		 *
		 * @param maxFileSize the max file size
		 * @return the builder
		 */
		public Builder maxFileSize(int maxFileSize) {
			this.maxFileSize = maxFileSize;
			return this;
		}

		/**
		 * Max file duration.
		 *
		 * @param maxFileDuration the max file duration
		 * @return the builder
		 */
		public Builder maxFileDuration(int maxFileDuration) {
			this.maxFileDuration = maxFileDuration;
			return this;
		}

		/**
		 * Enable file pruning.
		 *
		 * @param enableFilePruning the enable file pruning
		 * @return the builder
		 */
		public Builder enableFilePruning(boolean enableFilePruning) {
			this.enableFilePruning = enableFilePruning;
			return this;
		}

		/**
		 * Max local disk usage.
		 *
		 * @param maxLocalDiskUsage the max local disk usage
		 * @return the builder
		 */
		public Builder maxLocalDiskUsage(int maxLocalDiskUsage) {
			this.maxLocalDiskUsage = maxLocalDiskUsage;
			return this;
		}

		/**
		 * Max local file age.
		 *
		 * @param maxLocalFileAge the max local file age
		 * @return the builder
		 */
		public Builder maxLocalFileAge(int maxLocalFileAge) {
			this.maxLocalFileAge = maxLocalFileAge;
			return this;
		}

		/**
		 * Minimum free disk.
		 *
		 * @param minimumFreeDisk the minimum free disk
		 * @return the builder
		 */
		public Builder minimumFreeDisk(int minimumFreeDisk) {
			this.minimumFreeDisk = minimumFreeDisk;
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
		 * Builds the.
		 *
		 * @return the session file configuration
		 */
		public SessionFileConfiguration build() {
			return new SessionFileConfiguration(this);
		}
	}
}