package com.onloupe.api.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.onloupe.configuration.AgentConfiguration;
import com.onloupe.configuration.ListenerConfiguration;
import com.onloupe.configuration.NetworkViewerConfiguration;
import com.onloupe.configuration.PackagerConfiguration;
import com.onloupe.configuration.PublisherConfiguration;
import com.onloupe.configuration.ServerConfiguration;
import com.onloupe.configuration.SessionFileConfiguration;
import com.onloupe.model.system.ApplicationType;
import com.onloupe.model.system.Version;

public class PropertiesTests
{

	// false = test with generated props. true = test with props on classpath
	private static final boolean EXTERNAL = false;

	@BeforeAll
	public static final void createJsonFile() throws FileNotFoundException, IOException
	{
		if (!EXTERNAL) {
			(new File("loupe.properties")).delete();
			try (FileOutputStream fos = new FileOutputStream(new File("loupe.properties"));
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					BufferedWriter writer = new BufferedWriter(osw)) {
				for (String line : PROPS_SOURCE) {
					writer.write(line);
					writer.newLine();
				}
				writer.flush();
			} 
		}
	}

	@AfterAll
	public static final void deleteJsonFile()
	{
		if (!EXTERNAL) {
			(new File("loupe.properties")).delete();
		}
	}

	@Test
	public final void listenerValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertFalse(actual.getListener().getAutoTraceRegistration());
		Assertions.assertFalse(actual.getListener().getEnableConsole());
		Assertions.assertFalse(actual.getListener().getEnableNetworkEvents());
		Assertions.assertFalse(actual.getListener().getEndSessionOnTraceClose());
		
		actual = loadBuilder();
		Assertions.assertFalse(actual.getListener().getAutoTraceRegistration());
		Assertions.assertFalse(actual.getListener().getEnableConsole());
		Assertions.assertFalse(actual.getListener().getEnableNetworkEvents());
		Assertions.assertFalse(actual.getListener().getEndSessionOnTraceClose());
	}

	@Test
	public final void testSessionFileValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertFalse(actual.getSessionFile().getEnabled());
		Assertions.assertEquals(42, actual.getSessionFile().getAutoFlushInterval());
		Assertions.assertEquals(42, actual.getSessionFile().getIndexUpdateInterval());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxFileSize());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxFileDuration());
		Assertions.assertFalse(actual.getSessionFile().getEnableFilePruning());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxLocalDiskUsage());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxLocalFileAge());
		Assertions.assertEquals(42, actual.getSessionFile().getMinimumFreeDisk());
		Assertions.assertTrue(actual.getSessionFile().getForceSynchronous());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxQueueLength());
		Assertions.assertEquals("C:\\Temp", actual.getSessionFile().getFolder());
		
		actual = loadBuilder();
		Assertions.assertFalse(actual.getSessionFile().getEnabled());
		Assertions.assertEquals(42, actual.getSessionFile().getAutoFlushInterval());
		Assertions.assertEquals(42, actual.getSessionFile().getIndexUpdateInterval());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxFileSize());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxFileDuration());
		Assertions.assertFalse(actual.getSessionFile().getEnableFilePruning());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxLocalDiskUsage());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxLocalFileAge());
		Assertions.assertEquals(42, actual.getSessionFile().getMinimumFreeDisk());
		Assertions.assertTrue(actual.getSessionFile().getForceSynchronous());
		Assertions.assertEquals(42, actual.getSessionFile().getMaxQueueLength());
		Assertions.assertEquals("C:\\Temp", actual.getSessionFile().getFolder());
	}

	@Test
	public final void testPackagerValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertEquals("Ctrl-Alt-F5", actual.getPackager().getHotKey());
		Assertions.assertFalse(actual.getPackager().getAllowFile());
		Assertions.assertFalse(actual.getPackager().getAllowRemovableMedia());
		Assertions.assertFalse(actual.getPackager().getAllowEmail());
		Assertions.assertFalse(actual.getPackager().getAllowServer());
		Assertions.assertEquals("mark@rendlelabs.com", actual.getPackager().getFromEmailAddress());
		Assertions.assertEquals("mark@rendlelabs.com", actual.getPackager().getDestinationEmailAddress());
		Assertions.assertEquals("Sample", actual.getPackager().getProductName());
		Assertions.assertEquals("Loupe", actual.getPackager().getApplicationName());
		
		actual = loadBuilder();
		Assertions.assertEquals("Ctrl-Alt-F5", actual.getPackager().getHotKey());
		Assertions.assertFalse(actual.getPackager().getAllowFile());
		Assertions.assertFalse(actual.getPackager().getAllowRemovableMedia());
		Assertions.assertFalse(actual.getPackager().getAllowEmail());
		Assertions.assertFalse(actual.getPackager().getAllowServer());
		Assertions.assertEquals("mark@rendlelabs.com", actual.getPackager().getFromEmailAddress());
		Assertions.assertEquals("mark@rendlelabs.com", actual.getPackager().getDestinationEmailAddress());
		Assertions.assertEquals("Sample", actual.getPackager().getProductName());
		Assertions.assertEquals("Loupe", actual.getPackager().getApplicationName());
	}

	@Test
	public final void testPublisherValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertEquals("Sample", actual.getPublisher().getProductName());
		Assertions.assertEquals("Sample App", actual.getPublisher().getApplicationDescription());
		Assertions.assertEquals("Loupe", actual.getPublisher().getApplicationName());
		Assertions.assertEquals(ApplicationType.ASP_NET, actual.getPublisher().getApplicationType());
		Assertions.assertTrue(new Version(42,0,0).compareTo(actual.getPublisher().getApplicationVersion()) == 0);
		Assertions.assertEquals("Development", actual.getPublisher().getEnvironmentName());
		Assertions.assertEquals("QA", actual.getPublisher().getPromotionLevelName());
		Assertions.assertTrue(actual.getPublisher().getForceSynchronous());
		Assertions.assertEquals(10, actual.getPublisher().getMaxQueueLength());
		Assertions.assertTrue(actual.getPublisher().getEnableAnonymousMode());
		Assertions.assertTrue(actual.getPublisher().getEnableDebugMode());

		actual = loadBuilder();
		Assertions.assertEquals("Sample", actual.getPublisher().getProductName());
		Assertions.assertEquals("Sample App", actual.getPublisher().getApplicationDescription());
		Assertions.assertEquals("Loupe", actual.getPublisher().getApplicationName());
		Assertions.assertEquals(ApplicationType.ASP_NET, actual.getPublisher().getApplicationType());
		Assertions.assertTrue(new Version(42,0,0).compareTo(actual.getPublisher().getApplicationVersion()) == 0);
		Assertions.assertEquals("Development", actual.getPublisher().getEnvironmentName());
		Assertions.assertEquals("QA", actual.getPublisher().getPromotionLevelName());
		Assertions.assertTrue(actual.getPublisher().getForceSynchronous());
		Assertions.assertEquals(10, actual.getPublisher().getMaxQueueLength());
		Assertions.assertTrue(actual.getPublisher().getEnableAnonymousMode());
		Assertions.assertTrue(actual.getPublisher().getEnableDebugMode());
	}

	@Test
	public final void testServerValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertFalse(actual.getServer().getEnabled());
		Assertions.assertTrue(actual.getServer().getAutoSendSessions());
		Assertions.assertFalse(actual.getServer().getAutoSendOnError());
		Assertions.assertTrue(actual.getServer().getSendAllApplications());
		Assertions.assertTrue(actual.getServer().getPurgeSentSessions());
		Assertions.assertEquals("RendleLabs", actual.getServer().getCustomerName());
		Assertions.assertTrue(actual.getServer().getUseGibraltarService());
		Assertions.assertTrue(actual.getServer().getUseSsl());
		Assertions.assertEquals("onloupe.com", actual.getServer().getServer());
		Assertions.assertEquals(81, actual.getServer().getPort());
		Assertions.assertEquals("C:\\inetpub\\foo", actual.getServer().getApplicationBaseDirectory());
		Assertions.assertEquals("quux", actual.getServer().getRepository());	
		
		actual = loadBuilder();
		Assertions.assertFalse(actual.getServer().getEnabled());
		Assertions.assertTrue(actual.getServer().getAutoSendSessions());
		Assertions.assertFalse(actual.getServer().getAutoSendOnError());
		Assertions.assertTrue(actual.getServer().getSendAllApplications());
		Assertions.assertTrue(actual.getServer().getPurgeSentSessions());
		Assertions.assertEquals("RendleLabs", actual.getServer().getCustomerName());
		Assertions.assertTrue(actual.getServer().getUseGibraltarService());
		Assertions.assertTrue(actual.getServer().getUseSsl());
		Assertions.assertEquals("onloupe.com", actual.getServer().getServer());
		Assertions.assertEquals(81, actual.getServer().getPort());
		Assertions.assertEquals("C:\\inetpub\\foo", actual.getServer().getApplicationBaseDirectory());
		Assertions.assertEquals("quux", actual.getServer().getRepository());
	}

	@Test
	public final void testNetworkViewerValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertFalse(actual.getNetworkViewer().getAllowLocalClients());
		Assertions.assertTrue(actual.getNetworkViewer().getAllowRemoteClients());
		assert 10 == actual.getNetworkViewer().getMaxQueueLength();
		Assertions.assertFalse(actual.getNetworkViewer().getEnabled());
		
		actual = loadBuilder();
		Assertions.assertFalse(actual.getNetworkViewer().getAllowLocalClients());
		Assertions.assertTrue(actual.getNetworkViewer().getAllowRemoteClients());
		assert 10 == actual.getNetworkViewer().getMaxQueueLength();
		Assertions.assertFalse(actual.getNetworkViewer().getEnabled());
	}

	@Test
	public final void testPropertiesValues() throws FileNotFoundException, IOException
	{
		AgentConfiguration actual = load();
		Assertions.assertNotNull(actual.getProperties());
		Assertions.assertEquals(actual.getProperties().getProperty("LOUPE_INTERNAL_PLATFORM"), "JAVA");
	}

	//rework in java native properties
	private static AgentConfiguration load() throws FileNotFoundException, IOException
	{
		if (!EXTERNAL) {
			try (FileInputStream fis = new FileInputStream(new File("loupe.properties"))) {
				Properties props = new Properties();
				props.load(fis);
				AgentConfiguration config = new AgentConfiguration(props);
				config.sanitize();
				return config;
			}
		} else {
			return new AgentConfiguration();
		}
	}
	
	private static AgentConfiguration loadBuilder() {	
		return AgentConfiguration.builder()
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
						.customerName("RendleLabs")
						.useGibraltarService(true)
						.useSsl(true)
						.server("onloupe.com")
						.port(81)
						.applicationBaseDirectory("C:\\inetpub\\foo")
						.repository("quux").build())
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
						.fromEmailAddress("mark@rendlelabs.com")
						.destinationEmailAddress("mark@rendlelabs.com")
						.productName("Sample")
						.applicationName("Loupe").build())
				.build();
	}

	// JSON with different values from defaults to ensure values are coming from config.
	private static final String[] PROPS_SOURCE = new String[] {"Listener.AutoTraceRegistration=false", 
			"Listener.EnableConsole=false", 
			"Listener.EnableNetworkEvents=false", 
			"Listener.EndSessionOnTraceClose=false", 
			"SessionFile.Enabled=false", 
			"SessionFile.AutoFlushInterval=42", 
			"SessionFile.IndexUpdateInterval=42", 
			"SessionFile.MaxFileSize=42", 
			"SessionFile.MaxFileDuration=42", 
			"SessionFile.EnableFilePruning=false", 
			"SessionFile.MaxLocalDiskUsage=42", 
			"SessionFile.MaxLocalFileAge=42", 
			"SessionFile.MinimumFreeDisk=42", 
			"SessionFile.ForceSynchronous=true", 
			"SessionFile.MaxQueueLength=42", 
			"SessionFile.Folder=C:\\\\Temp", 
			"Packager.HotKey=Ctrl-Alt-F5", 
			"Packager.AllowFile=false", 
			"Packager.AllowRemovableMedia=false", 
			"Packager.AllowEmail=false", 
			"Packager.AllowServer=false", 
			"Packager.FromEmailAddress=mark@rendlelabs.com", 
			"Packager.DestinationEmailAddress=mark@rendlelabs.com", 
			"Packager.ProductName=Sample", 
			"Packager.ApplicationName=Loupe", 
			"Publisher.ProductName=Sample", 
			"Publisher.ApplicationDescription=Sample App", 
			"Publisher.ApplicationName=Loupe", 
			"Publisher.ApplicationType=ASP_NET", 
			"Publisher.ApplicationVersionNumber=42.0.0", 
			"Publisher.EnvironmentName=Development", 
			"Publisher.PromotionLevelName=QA", 
			"Publisher.ForceSynchronous=true", 
			"Publisher.MaxQueueLength=10", 
			"Publisher.EnableAnonymousMode=true", 
			"Publisher.EnableDebugMode=true", 
			"Server.Enabled=false", 
			"Server.AutoSendSessions=true", 
			"Server.AutoSendOnError=false", 
			"Server.SendAllApplications=true", 
			"Server.PurgeSentSessions=true", 
			"Server.CustomerName=RendleLabs", 
			"Server.UseGibraltarService=true", 
			"Server.UseSsl=true", 
			"Server.Server=onloupe.com", 
			"Server.Port=81", 
			"Server.ApplicationBaseDirectory=C:\\\\inetpub\\\\foo", 
			"Server.Repository=quux", 
			"NetworkViewer.AllowLocalClients=false", 
			"NetworkViewer.AllowRemoteClients=true", 
			"NetworkViewer.MaxQueueLength=10", 
			"NetworkViewer.Enabled=false",
			"Foo=Bar",
			"Bar=baz"};
}