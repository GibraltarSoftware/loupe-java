# Loupe Agent for Java #

The Loupe Agent provides a generic facility for capturing log messages, exceptions, and metrics
from Java applications.  This product was written in Java 1.8, and no warranty is made for the stability
or maximum efficiency of the product in earlier versions of Java.

## What's In This Repository ##

This repository contains a maven reactor encompassing the supported inventory of Loupe, including:

* Loupe model: The project containing our common entities.
* Loupe core: The project containing the generic facility for our functionality. This is what powers our
appenders and what one would use to extend Loupe.
* Loupe api: A layer of convenience functionality for those who wish to use Loupe directly.
* Loupe log4j support: Our appender for log4j support.
* Loupe log4j2 support: Our appender for log4j2 support.
* Loupe logback support: Our appender for logback support.

## How do I add Loupe to my Application? ##

### Download Loupe ###

To add Loupe to your application, first use GIT to clone our repository:

    https://github.com/GibraltarSoftware/loupe-java.git
    
or

    git@github.com:GibraltarSoftware/loupe-java.git

### Add Loupe to your application ###

Loupe can be added to your application in a variety of ways, documented below. 

#### Using your Local Maven Repository ####

Loupe will soon be available in Maven Central. Until then, we recommend deploying Loupe to your local maven repository.

##### Deploy Loupe to your repository #####

Navigate to the root of the parent project folder, you will see a POM file. Execute:

    mvn clean install

This will deploy Loupe to your local Maven repository.

##### Install Loupe in your application #####

Add the following dependency to your application's pom.xml:

		<dependency>
			<groupId>com.onloupe</groupId>
			<artifactId>loupe-logback-support</artifactId>
			<version><...version indicated...></version>
		</dependency>

The version number will be evident in the parent POM file of Loupe.

Execute the necessary commands to build the application in Maven.

#### Build and deploy Loupe manually ####

If you do not wish to use Maven, Loupe can be built and deployed manually.

##### Build Loupe #####

Navigate to the root of the parent project folder, you will see a POM file. Execute:

    mvn clean install
    
This will generate the necessary artifacts to install and run Loupe. 

These artifacts can be found relative to the parent root:

    <product>/target/<product>-<version>.jar

Note: Any manual installation must include the following jars:

* model
* core

Ergo, installing loupe-log4j2-support would include deployment of jars:

* model
* core
* loupe-log4j2-support

...plus the collateral dependencies detailed in the parent POM file.

##### Install Loupe #####

Once you've selected your jar files, add them to the classpath of the container or java runtime as your setup
requires. They will then be available to your application.

Additional dependency injection platforms (gradle, etc) are under consideration for future inclusion.

### Configuring Loupe ###

Loupe can be configured in a variety of ways

#### AgentConfiguration object injection ####

Loupe can be configured directly using its configuration object, for a robust example:

```java
    AgentConfiguration configuration = AgentConfiguration.builder()
				.networkViewer(NetworkViewerConfiguration.builder()
						.allowLocalClients(false)
						.allowRemoteClients(true)
						.maxQueueLength(10)
						.enabled(false).build())
				.server(ServerConfiguration.builder()
						.enabled(false)
						.autoSendSessions(true)
						.autoSendOnError(false)
						.sendAllApplications(true)
						.purgeSentSessions(true)
						.customerName("GibraltarSoftware")
						.useGibraltarService(true)
						.useSsl(true)
						.server("onloupe.com")
						.port(81)
						.applicationBaseDirectory("C:\\base\\directory")
						.repository("ourRepo").build())
				.listener(ListenerConfiguration.builder()
						.autoTraceRegistration(false)
						.enableConsole(false)
						.enableNetworkEvents(false)
						.endSessionOnTraceClose(false).build())
				.sessionFile(SessionFileConfiguration.builder()
						.enabled(false)
						.autoFlushInterval(42)
						.indexUpdateInterval(42)
						.maxFileSize(42)
						.maxFileDuration(42)
						.enableFilePruning(false)
						.maxLocalDiskUsage(42)
						.maxLocalFileAge(42)
						.forceSynchronous(true)
						.maxQueueLength(42)
						.minimumFreeDisk(42)
						.folder("C:\\Temp").build())
				.publisher(PublisherConfiguration.builder()
						.productName("Sample")
						.applicationDescription("Sample App")
						.applicationName("Loupe")
						.applicationType(ApplicationType.ASP_NET)
						.applicationVersion(new Version(42, 0, 0))
						.environmentName("Development")
						.promotionLevelName("QA")
						.forceSynchronous(true)
						.maxQueueLength(10)
						.enableAnonymousMode(true)
						.enableDebugMode(true).build())
				.packager(PackagerConfiguration.builder()
						.hotKey("Ctrl-Alt-F5")
						.allowFile(false)
						.allowRemovableMedia(false)
						.allowEmail(false)
						.allowServer(false)
						.fromEmailAddress("ryan@gibraltarsoftware.com")
						.destinationEmailAddress("kendall@gibraltarsoftware.com")
						.productName("Sample")
						.applicationName("Loupe").build())
				.build();
```
The configuration object can then be injected in an explicit call to start Loupe:

```java
    Log.start(configuration);
```
or, if using loupe-api:

```java
    Loupe.start(configuration);
```
    
#### Properties files ####

Loupe can also be configured using properties files. The properties file need only be placed on the classpath,
and Loupe will detect and utilize it. Loupe supports the PROPERTIES or XML file format.

##### Properties file #####

Name: loupe.properties

	Publisher.ProductName=TestApps
	Publisher.ApplicationName=SpringTests
	Server.AutoSendSessions=true
	Server.Server=hub.gibraltarsoftware.com
	Server.Repository=esymmetrix
	Server.UseSsl=true
	NetworkViewer.AllowRemoteClients=true
	
##### XML properties file #####

Nane: loupe.xml

```xml
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
	<entry key="Publisher.ProductName">TestApps</entry>
	<entry key="Publisher.ApplicationName">SpringTests</entry>
	<entry key="Server.AutoSendSessions">true</entry>
	<entry key="Server.Server">hub.gibraltarsoftware.com</entry>
	<entry key="Server.Repository">esymmetrix</entry>
	<entry key="Server.UseSsl">true</entry>
	<entry key="NetworkViewer.AllowRemoteClients">true</entry>
</properties>
```

#### Configuring Loupe appenders for logging platforms ####

##### Logback #####

###### Maven configuration ######

```
		<dependency>
			<groupId>com.onloupe</groupId>
			<artifactId>loupe-logback-support</artifactId>
			<version>... desired version ...</version>
		</dependency>
```

###### Logback configuration ######

Add the following appender to logback.xml, scoped in accordance with your choosing.

```xml
<appender name="loupe" class="com.onloupe.appenders.logback.LoupeLogbackAppender"/>
```

##### Log4j2 #####

###### Maven configuration ######

```
		<dependency>
			<groupId>com.onloupe</groupId>
			<artifactId>loupe-log4j2-support</artifactId>
			<version>... desired version ...</version>
		</dependency>
```

###### Log4j2 configuration ######

First, the configuration route must include the package path to the Loupe appender.

```xml
<Configuration packages="com.onloupe.appenders.log4j">
```

Next, add the appender to the appenders inventory:

```xml
<LoupeLog4jAppender name="Loupe" />
```

Finally, add our appender to any scope of your choosing.

##### Log4j #####

###### Maven configuration ######

```
		<dependency>
			<groupId>com.onloupe</groupId>
			<artifactId>loupe-log4j-support</artifactId>
			<version>... desired version ...</version>
		</dependency>
```

###### Log4j configuration ######

Add the appender to the appenders inventory:

```xml
<appender name="LoupeLog4jAppender" class="com.onloupe.appenders.log4j.LoupeLog4jAppender"/>
```

Finally, add our appender to any scope of your choosing.

## Creating Custom Metrics in Loupe ##

Loupe supports two types of metrics:

### Event Metrics ###

Loupe uses Event Metrics to capture data about singular events at a specific point in time.

The example below pertains to metrics concerning a hypothetical database cache:

```java
@EventMetricClass(namespace = "Samples", categoryName = "Database.Engine", 
	counterName = "Cache - Declarative", caption = "Simple Cache", 
	description = "Performance metrics for the database engine.")
public class CacheEventMetric
{
	private String instanceName;
	private int pages;
	private int size;

	public CacheEventMetric(String instanceName, int pages, int size) {
		super();
		this.instanceName = instanceName;
		this.pages = pages;
		this.size = size;
	}
	
	// An optional member to be automatically queried for the instance name 
	// to use for this event metric on this data object instance.
	@EventMetricInstanceName
	public final String getInstanceName() {
		return instanceName;
	}
	
	// An event metric value representing the average of pages in the cache over time.
	@EventMetricValue(name = "pages", summaryFunction = SummaryFunction.AVERAGE, 
		caption = "Pages", description = "Total number of pages in cache.")
	public final int getPages() {
		return pages;
	}
	
	// An event metric value representing the average size of the cache over time.
	@EventMetricValue(name = "size", summaryFunction = SummaryFunction.AVERAGE, 
		unitCaption = "Bytes", caption = "Cache Size", 
		description = "Total number of bytes used by pages in cache.")
	public final int getSize() {
		return size;
	}
}
```

To utilize the above event metric class, the following code is executed:

```java
// register the class and bind the fields.
EventMetric.register(CacheEventMetric.class);

// capture a metric
CacheEventMetric oneMetric = new CacheEventMetric("one", 388, 2048);
EventMetric.write(defaultInstance);

// create another metric
CacheEventMetric metric = new CacheEventMetric("two", 122, 1024);
// if an attribute is not found, we fall back to the instance name "one"
EventMetric.write(metric, "one");
```

### Sampled Metrics ###

Loupe uses Sampled Metrics to capture data points at a defined interval over the course of time.

```java
@SampledMetricClass(namespace = "Samples", categoryName = "Database.Engine")
public class CacheSampledMetric
{
	private String instanceName;
	private int pages;
	private int size;

	public CacheSampledMetric(String instanceName, int pages, int size) {
		super();
		this.instanceName = instanceName;
		this.pages = pages;
		this.size = size;
	}
	
	// An optional member to be automatically queried for the instance name 
	// to use for this sampled metric on this data object instance.
	@SampledMetricInstanceName
	public final String getInstanceName() {
		return instanceName;
	}
	
	// A sampled metric value representing the number of pages at the point in time of
	// execution.
	@SampledMetricValue(counterName = "pages", samplingType = SamplingType.RAW_COUNT, 
		caption = "Pages in Cache", description = "Total number of pages in cache")
	public final int getPages() {
		return pages;
	}
	
	// A sampled metric value representing the average size of the cache over time.
	@SampledMetricValue(counterName = "size", samplingType = SamplingType.RAW_COUNT, 
		unitCaption = "Bytes", caption = "Cache Size", 
		description = "Total number of bytes used by pages in cache")
	public final int getSize() {
		return size;
	}
}
```
To utilize the above event metric class, the following code is executed:

```java
// register our metric type
SampledMetric.register(CacheSampledMetric.class);

// write a metric with the ascribed values
SampledMetric.write(new CacheSampledMetric("cacheMetric", 122, 1024));
```

## Where are my local log files? ##

You may define which directory Loupe will use to store log files (extension .glf) by defining the following property:

```
SessionFile.Folder=<fully qualified path>
```

If this value is undefined, Loupe will attempt to place log files in the most sensible location available based on the state of the host operating system.

### Windows ###

First, Loupe will attempt to use the common application data folder:

```
C:\ProgramData\Gibraltar\Local Logs
```

If the common application data folder is unavailable, the local data folder will be attempted:

```
C:\Users\<user name>\AppData\Gibraltar\Local Logs
```

Failing both of these, the Java tmp io directory will be used.

### Linux ###

First, Loupe will attempt to use the common logging area:

```
/var/log/Gibraltar/Local Logs
```

By default, this folder is generally not available for write operations by users outside of it's group. This will need to be handled accordingly when provisioning the user running the Loupe agent or appenders.

Otherwise, Loupe will attempt to use a hidden folder in the user home directory:

```
/home/<user name>/.logs/Gibraltar/Local Logs
```

Failing both of these, the Java tmp io directory will be used.

## How do I view my local log files?? ##

Download the latest version of [Loupe desktop](https://my.onloupe.com/Licenses).

This software runs on Windows only, but can still open GLF files generated by linux hosted applications.

## License Information ##

Loupe is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).