package com.onloupe.api.logmessages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.onloupe.core.util.IOUtils;

/** 
 Summary description for FileLogger.
*/
public class DumbFileLogger
{
	/** 
	 The name of the file to which this Logger is writing.
	*/
	private String _fileName;

	/** 
	 Gets and sets the file name.
	*/
	public final String getFileName()
	{
		return _fileName;
	}
	public final void setFileName(String value)
	{
		_fileName = value;
	}

	/** 
	 Create a new instance of FileLogger.
	 
	 @param aFileName The name of the file to which this Logger should write.
	*/
	public DumbFileLogger(String aFileName)
	{
		setFileName(aFileName);
	}

	/** 
	 Create a new FileStream.
	 
	 @return The newly created FileStream.
	 * @throws FileNotFoundException 
	*/
	private FileOutputStream createFileStream() throws FileNotFoundException
	{
		return new FileOutputStream(getFileName(), true);
	}

	/** 
	 Get the FileStream.
	 Create the directory structure if necessary.
	 
	 @return The FileStream.
	 * @throws FileNotFoundException 
	*/
	private FileOutputStream getFileStream() throws FileNotFoundException
	{
		try
		{
			return createFileStream();
		}
		catch (FileNotFoundException e)
		{
			(new File((new File(getFileName())).getParent())).mkdirs();
			return createFileStream();
		}
	}

	/** 
	 Write the String to the file.
	 
	 @param s The String representing the LogEntry being logged.
	 @return true upon success, false upon failure.
	*/
	public final boolean writeToLog(String s)
	{
		FileOutputStream writer = null;
		try
		{
			writer = getFileStream();
			writer.write(new String(s + System.lineSeparator()).getBytes());
		}
		catch (java.lang.Exception e)
		{
			return false;
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
		return true;
	}

}