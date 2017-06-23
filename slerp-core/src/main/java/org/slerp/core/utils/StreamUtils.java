package org.slerp.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slerp.core.CoreException;

public class StreamUtils {
	public static final int DEFAULT_SIZE = 8192;

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
