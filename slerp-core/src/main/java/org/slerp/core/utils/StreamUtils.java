package org.slerp.core.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;

import org.slerp.core.CoreException;

public class StreamUtils {
	public static final int DEFAULT_SIZE = 8192;
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	public static final byte[] EMPTY_BYTES = new byte[0];

	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
				throw new CoreException("Cannot close IO : " + e);
			}
		}
	}

	public static void copyStream(InputStream input, OutputStream output, int size) {
		byte[] bytes = new byte[size];
		int read;
		try {
			while (((read = input.read(bytes)) != -1)) {
				output.write(bytes, 0, read);
			}
		} catch (IOException e) {
			throw new CoreException(e);
		}
	}

	public static void copyStream(InputStream input, OutputStream output) {
		copyStream(input, output, DEFAULT_SIZE);
	}

	public static byte[] copyStreamToBytes(InputStream input, int estimasiUkuran) {
		ByteArrayOutputStream output = new ByteArrayOutputStream(Math.max(0, estimasiUkuran));
		copyStream(input, output);
		return output.toByteArray();
	}

	/**
	 * Copy the data from an {@link InputStream} to a string using the default
	 * charset.
	 * 
	 * @param approxStringLength
	 *            Used to preallocate a possibly correct sized StringBulder to
	 *            avoid an array copy.
	 * @throws IOException
	 */
	public static String copyStreamToString(InputStream input, int approxStringLength) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringWriter w = new StringWriter(Math.max(0, approxStringLength));
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];

		int charsRead;
		while ((charsRead = reader.read(buffer)) != -1) {
			w.write(buffer, 0, charsRead);
		}

		return w.toString();
	}

	public static class OptimizeByteArrayOutputStream extends ByteArrayOutputStream {

		public OptimizeByteArrayOutputStream(int size) {
			super(size);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.ByteArrayOutputStream#toByteArray()
		 */
		@Override
		public synchronized byte[] toByteArray() {
			if (count == buf.length) {
				return buf;
			} else {
				return super.toByteArray();
			}
		}

	}

}
