package com.onloupe.api.logmessages;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.onloupe.api.Loupe;
import com.onloupe.api.logmessages.internal.BadlyBehavedClass;
import com.onloupe.core.logging.LogWriteMode;
import com.onloupe.core.util.TimeConversion;
import com.onloupe.model.log.LogMessageSeverity;

/** 
 Test the Log class's direct logging capabilities.  Metrics are not verified in this class
*/
public class LogTests
{

	/** 
	 Write a log message with an attached exception object.
	*/
	@Test
	public final void writeException()
	{
		Loupe.warning(new Exception("This is our test assertion exception"), null, "Test.Agent.LogMessages.Write", "Test of logging exception attachement.", null);

		Loupe.warning(new Exception("This is our top exception", new Exception("This is our middle exception", new Exception("This is our bottom exception"))), null, "Test.Agent.LogMessages.Write", "Test of logging exception attachement with nested exceptions.", null);
	}

	/** 
	 Write many messages so we can verify order in the viewer.  The goal is to write many messages so that the order has to be
	 controlled by sequence number, not timestamp.
	*/
	@Test
	public final void writeMessagesForOrderTesting()
	{
		for (int curLogMessage = 1; curLogMessage < 3000; curLogMessage++)
		{
			Loupe.traceVerbose("This is log message %d", curLogMessage);
		}
	}

	/** 
	 Write a log message using the full trace message entrance point.
	*/
	@Test
	public final void writeMessageFullFormat()
	{
		//do one that should be pinned on US
		Loupe.write(LogMessageSeverity.VERBOSE, "GibraltarTest", 0, null, LogWriteMode.QUEUED, null, "Test.Agent.LogMessages.WriteMessage", "This message should be verbose and ascribed to the LogTests class.", null);
		Loupe.write(LogMessageSeverity.CRITICAL, "GibraltarTest", 1, null, LogWriteMode.QUEUED, null, "Test.Agent.LogMessages.WriteMessage", "This message should be critical and ascribed to whatever is calling our test class.", null);
		Loupe.write(LogMessageSeverity.ERROR, "GibraltarTest", -1, null, LogWriteMode.QUEUED, null, "Test.Agent.LogMessages.WriteMessage", "This message should be error and also ascribed to the LogTests class.", null);
	}

	/** 
	 Write log messages including XML details.
	*/
	@Test
	public final void writeMessageWithDetails()
	{
		Loupe.verboseDetail("<test>of<simple/>XML</test>", "Test.Agent.LogMessages.WriteDetail", "Simple XML details", "XML details:\r\n%s", "<test>of<simple/>XML</test>");

		Loupe.verboseDetail("<?xml version=\"1.0\" encoding=\"utf-16\" ?><test><data index=\"1\" value=\"4\" /><data index=\"2\" value=\"8\" /></test>", "Test.Agent.LogMessages.WriteDetail", "Test data details", "Values:\r\n%s: %s\r\n%s: %s\r\n", 1, 4, 2, 8);
		Loupe.informationDetail("<?xml version=\"1.0\" encoding=\"utf-8\" ?><test><data index=\"3\" value=\"15\" /><data index=\"4\" value=\"16\" /></test>", "Test.Agent.LogMessages.WriteDetail", "Test data details", "Values:\r\n%s: %s\r\n%s: %s\r\n", 3, 15, 4, 16);
		Loupe.warningDetail("<?xml version=\"1.0\" encoding=\"utf-8\" ?><test><data index=\"5\" value=\"23\" /><data index=\"6\" value=\"42\" /></test>", "Test.Agent.LogMessages.WriteDetail", "Test data details", "Values:\r\n%s: %s\r\n%s: %s\r\n", 5, 23, 6, 42);

		Loupe.write(LogMessageSeverity.ERROR, "GibraltarTest", 0, null, LogWriteMode.QUEUED, "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + "<test complete=\"true\"><data index=\"1\" value=\"4\" /><data index=\"2\" value=\"8\" />\r\n" + "<data index=\"3\" value=\"15\" /><data index=\"4\" value=\"16\" />\r\n" + "<data index=\"5\" value=\"23\" /><data index=\"6\" value=\"42\" /></test>", "Test.Agent.LogMessages.WriteDetail", "Test data complete", "Test sequence data: %s, %s, %s, %s, %s, %s\r\n", 4, 8, 15, 16, 23, 42);

		Loupe.errorDetail("<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + "\r\n" +
"<configuration>" + "\r\n" +
"  <system.diagnostics>" + "\r\n" +
"    <trace autoflush=\"false\" indentsize=\"4\">" + "\r\n" +
"      <listeners>" + "\r\n" +
"        <add name=\"Gibraltar\" type=\"Gibraltar.Agent.Net.LogListener, Gibraltar\" />" + "\r\n" +
"      </listeners>" + "\r\n" +
"    </trace>" + "\r\n" +
"  </system.diagnostics>" + "\r\n" +
"</configuration>", "Test.Agent.LogMessages.WriteDetail", "Sample app.config", "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + "\r\n" +
"<configuration>" + "\r\n" +
"  <system.diagnostics>" + "\r\n" +
"    <trace autoflush=\"false\" indentsize=\"4\">" + "\r\n" +
"      <listeners>" + "\r\n" +
"        <add name=\"Gibraltar\" type=\"Gibraltar.Agent.Net.LogListener, Gibraltar\" />" + "\r\n" +
"      </listeners>" + "\r\n" +
"    </trace>" + "\r\n" +
"  </system.diagnostics>" + "\r\n" +
"</configuration>"); //TangibleVerbatimMultilineString

		Loupe.criticalDetail("<?xml version=\"1.0\" encoding=\"utf-8\" ?><test><data this=\"okay\" /><data this=\"malformed XML\"><data this=\"also okay\" /></test>", "Test.Agent.LogMessages.WriteDetail", "Test data bad details", "The details contains mis-matched XML:\r\n%s", "<?xml version=\"1.0\" encoding=\"utf-8\" ?><test><data this=\"okay\" /><data this=\"malformed XML\"><data this=\"also okay\" /></test>");
	}

	/** 
	 Test our handling of errors in formatted log message calls.
	 * @throws InterruptedException 
	 * @throws IOException 
	*/
	@Test
	public final void writeBadFormat() throws InterruptedException, IOException
	{
		Loupe.traceVerbose("This is a test\tof a bad format call to Loupe.traceVerbose()\n%s, %s, %s", "zero", 1);
		Loupe.traceVerbose("This is a test\r\nof a legal format call to Loupe.traceVerbose()\t%s,\t%s,\t%s", 0, null, "two");
		Loupe.traceVerbose("This is a test\n\rof a bad format call to Loupe.traceVerbose()\t%s,\t%s,\n%s\t%s", null, "one", "\"two\"");
		Loupe.traceVerbose((String)null, 0, "null format test", 2);
		Loupe.traceVerbose("", "empty format test", 1);
		
		Thread.sleep(2000); // Give it time to put stuff in the Loupe.

		return;
	}

	@Test
	public final void writeExceptionAttributedMessages()
	{
		try
		{
			BadlyBehavedClass innerTest = new BadlyBehavedClass();
			innerTest.methodThatThrowsException();
		}
		catch (Exception ex)
		{
			Loupe.error(ex, null, true, "Test.Agent.LogMessages.Exception Attribution", "This should be attributed to the exception's call stack", "Not to the WriteExceptionAttributedMessages method.");
			Loupe.critical(ex, null, true, "Test.Agent.LogMessages.Exception Attribution", "This should be attributed to the exception's call stack", "Not to the WriteExceptionAttributedMessages method.");
			Loupe.error(ex, null, true, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should be attributed to the exception's call stack", "Not to the WriteExceptionAttributedMessages method.");
			Loupe.critical(ex, null, true, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should be attributed to the exception's call stack", "Not to the WriteExceptionAttributedMessages method.");

			Loupe.error(ex, null, false, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.critical(ex, null, false, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.error(ex, null, false, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.critical(ex, null, false, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");

			Loupe.error(null, null, true, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to missing exception", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.critical(null, null, true, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to missing exception", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.error(null, null, true, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to missing exception", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.critical(null, null, true, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to missing exception", "It should be attributed to the WriteExceptionAttributedMessages method.");

			IllegalStateException exWithoutCallStack = new IllegalStateException();
			Loupe.error(exWithoutCallStack, null, true, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to exception lacking call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.critical(exWithoutCallStack, null, true, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to exception lacking call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.error(exWithoutCallStack, null, true, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to exception lacking call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
			Loupe.critical(exWithoutCallStack, null, true, LogWriteMode.QUEUED, "Test.Agent.LogMessages.Exception Attribution", "This should NOT be attributed to the exception's call stack due to exception lacking call stack", "It should be attributed to the WriteExceptionAttributedMessages method.");
		}
	}

	@Test
	public final void betterLogSample() throws IOException
	{
		Loupe.verbose("Your.Category", "This is our first message", "Verbose is like a debug or success message - below all other severities.");

		Loupe.critical("Period.Delimited.Category", "This is a critical problem", "We are writing a test message with multiple insertion strings: %s %s %s", "string", 124, LocalDateTime.now().format(TimeConversion.CS_DATETIMEOFFSET_FORMAT));

		Loupe.warning("Period.Delimited.Category", "This might be a problem problem", "You don't have to provide insertion strings if you don't want to");

		//Any of the different severities can include details of an exception.  Don't bother 
		//dumping it in the message area; it'll all be showing in the Analyst under the Exception.
		RuntimeException ex = new IllegalStateException("This is an example invalid operation exception");
		Loupe.error(ex, null, "Your Application.Exceptions", "We had an odd exception but managed to recover", "Here's a description of what we were doing.  We don't need to provide exception data.");

		//If you think the application might crash immediately after you call to record the message
		//you might want to make just this message synchronous.
		Loupe.critical(LogWriteMode.WAIT_FOR_COMMIT, "Your.Category", "We had a problem and may crash", "Just like our first method above we can now provide extended detail with insertion strings");

		Loupe.verbose("Your.Category", "This is our lowest severity message", "Verbose is like a debug or success message - below all other severities.");
	}

}