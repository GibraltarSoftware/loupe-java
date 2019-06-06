package com.onloupe.core.serialization;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * This helper class implements an enhanced run length encoding strategy to
 * compress arrays. It's enhanced in that it has an optimization for the case of
 * a sequence of unique values. The length of the sequence is written as a
 * negative value. This avoind the waste of preceding each value with a
 * runlength of 1 as would occur in classic RLE encoding.
 * 
 *
 * @param <T> Type of value contained in the array
 */
public class ArrayEncoder<T> {
	
	/** The read method. */
	private java.lang.reflect.Method readMethod;
	
	/** The write method. */
	private java.lang.reflect.Method writeMethod;

	/**
	 * Instantiates a new array encoder.
	 *
	 * @param clazz the clazz
	 * @throws NoSuchMethodException the no such method exception
	 * @throws SecurityException the security exception
	 */
	public ArrayEncoder(Class clazz) throws NoSuchMethodException, SecurityException {
		String readMethodName = "read" + clazz.getSimpleName();
		this.readMethod = IFieldReader.class.getMethod(readMethodName);
		this.writeMethod = IFieldWriter.class.getMethod("write", new java.lang.Class[] { clazz });
	}

	/**
	 * Are equal.
	 *
	 * @param left the left
	 * @param right the right
	 * @return true, if successful
	 */
	private boolean areEqual(T left, T right) {
		// We have to check if left is null (right can be checked by Equals itself)
		if (left == null) {
			// If right is also null, we're equal; otherwise, we're unequal!
			return right == null;
		}
		return left.equals(right);
	}

	/**
	 * This helper method uses reflection to invoke the proper method to read a
	 * value from the stream of type T.
	 *
	 * @param reader the reader
	 * @return the t
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private T readValue(IFieldReader reader)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return (T) this.readMethod.invoke(reader, new Object[0]);
	}

	/**
	 * This helper method uses reflection to invoke the proper method to write a
	 * value to the stream of type T.
	 *
	 * @param writer the writer
	 * @param value the value
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void writeValue(IFieldWriter writer, T value)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.writeMethod.invoke(writer, new Object[] { value });
	}

	/**
	 * Reads an array of type T from the stream.
	 *
	 * @param reader Data stream to read
	 * @return Array of type T
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public final T[] read(IFieldReader reader) throws IOException {
		// The array always starts with its length. Since the length can't be
		// negative we pass it unsigned (which gives slightly better compression)
		int length = reader.readPositiveInt();
		T[] array = (T[]) new Object[length];

		// The array values are stored as a sequence of runs. Each run represents
		// either a sequence of repeating values or a sequence of unique values.
		int index = 0;
		while (index < length) {
			int runLength = reader.readInt();
			if (runLength > 0) {
				// a positive runLength indicates a run of repeating values.
				// So, we only need to store the actual value once.
				T value;
				try {
					value = readValue(reader);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new IOException(e);
				}
				for (int i = 0; i < runLength; i++) {
					array[index++] = value;
				}
			} else // runLength < 0
			{
				// a negative runLength indicates a run of unique values
				for (int i = runLength; i < 0; i++) {
					// in this case, we need to store each value
					T value;
					try {
						value = readValue(reader);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new IOException(e);
					}
					array[index++] = value;
				}
			}
		}

		return array;
	}

	/**
	 * Writes an array of type T to the stream.
	 *
	 * @param array  Data to be written
	 * @param writer Stream to write the data into
	 * @throws Exception the exception
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public final void write(T[] array, IFieldWriter writer) throws Exception {
		writer.writePositive(array.length);

		final WriteRun writeRun = new WriteRun();
		
		// iterate across the array writing out a series of "runs" in which each
		// run is either a repetition of the same value or a sequence of unique values.
		while (writeRun.getCurrentIndex() < array.length) {
			// check for the end of the array
			if (writeRun.getPeekIndex() < array.length) {
				// is this the start of a new run?
				if (writeRun.getRunLength() == 0) {
					// is this a run or repeated values?
					if (areEqual(array[writeRun.getPeekIndex()], array[writeRun.getPeekIndex() - 1])) {
						// since the first two values match, we know we have a run of at least 2
						// repeating values
						writeRun.setRunLength(2);
					} else {
						// if the first two values differ, we have a run of at least 1 unique value
						writeRun.setRunLength(-1);
					}
					writeRun.setPeekIndex(writeRun.getPeekIndex() + 1);
				} else if (writeRun.getRunLength() > 0) {
					// is the run of repeating values continuing?
					if (areEqual(array[writeRun.getPeekIndex()], array[writeRun.getPeekIndex() - 1])) {
						writeRun.setPeekIndex(writeRun.getPeekIndex() + 1);
						writeRun.setRunLength(writeRun.getRunLength() + 1);
					} else {
						writeRun(array, writer, writeRun);
					}
				} else // runLength < 0
				{
					// is the run of unique values continuing?
					if (!areEqual(array[writeRun.getPeekIndex()], array[writeRun.getPeekIndex() - 1])) {
						// we decrement because we're accumulating a negative length
						writeRun.setPeekIndex(writeRun.getPeekIndex() + 1);
						writeRun.setRunLength(writeRun.getRunLength() - 1);
					} else {
						// don't include the last value because it is part of the next (repeating) run
						writeRun(array, writer, writeRun);
					}
				}
			} else {
				writeRun(array, writer, writeRun);
			}
		}
	}

	/**
	 * Helper method to write out a single run (either repating or unique values).
	 *
	 * @param array the array
	 * @param writer the writer
	 * @param writeRun the write run
	 * @throws Exception the exception
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	private void writeRun(T[] array, IFieldWriter writer, WriteRun writeRun) throws Exception {
		// This handles the edge case of the last run containing only one value
		if (writeRun.getCurrentIndex() == array.length - 1) {
			writeRun.setRunLength(-1);
		}

		// Write the length of the run first
		writer.write(writeRun.getRunLength());

		// is this a repeating run?
		if (writeRun.getRunLength() > 0) {
			// for a repeating run, write the value once but advance the index by runLength
			writeValue(writer, array[writeRun.getCurrentIndex()]);
			writeRun.setCurrentIndex(writeRun.getCurrentIndex() + writeRun.getRunLength());
		} else {
			int runLength = writeRun.getRunLength();
			int currentIndex = writeRun.getCurrentIndex();
			
			// for a unique run, write each value
			while (runLength < 0) {
				writeValue(writer, array[currentIndex++]);
				runLength++;
			}
			
			writeRun.setRunLength(runLength);
			writeRun.setCurrentIndex(currentIndex);
		}

		// Having written this run, get ready for the next one
		writeRun.setPeekIndex(writeRun.getCurrentIndex() + 1);
		writeRun.setRunLength(0);
	}
	
	/**
	 * The Class WriteRun.
	 */
	private class WriteRun {
		
		/** The current index. */
		private int currentIndex = 0;
		
		/** The peek index. */
		private int peekIndex = currentIndex + 1;
		
		/** The run length. */
		private int runLength = 0;
		
		/**
		 * Gets the current index.
		 *
		 * @return the current index
		 */
		public int getCurrentIndex() {
			return currentIndex;
		}
		
		/**
		 * Sets the current index.
		 *
		 * @param currentIndex the new current index
		 */
		public void setCurrentIndex(int currentIndex) {
			this.currentIndex = currentIndex;
		}
		
		/**
		 * Gets the peek index.
		 *
		 * @return the peek index
		 */
		public int getPeekIndex() {
			return peekIndex;
		}
		
		/**
		 * Sets the peek index.
		 *
		 * @param peekIndex the new peek index
		 */
		public void setPeekIndex(int peekIndex) {
			this.peekIndex = peekIndex;
		}
		
		/**
		 * Gets the run length.
		 *
		 * @return the run length
		 */
		public int getRunLength() {
			return runLength;
		}
		
		/**
		 * Sets the run length.
		 *
		 * @param runLength the new run length
		 */
		public void setRunLength(int runLength) {
			this.runLength = runLength;
		}
		
	}
}