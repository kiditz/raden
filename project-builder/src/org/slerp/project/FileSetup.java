package org.slerp.project;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class FileSetup {
	private static byte[] readResource(String resource, String path) {
		InputStream in = null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 10];
			in = FileSetup.class.getResourceAsStream(path + resource);
			if (in == null)
				throw new RuntimeException("Couldn't read resource '" + resource + "'");
			int read = 0;
			while ((read = in.read(buffer)) > 0) {
				bytes.write(buffer, 0, read);
			}
			return bytes.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read resource '" + resource + "'", e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}

	public static void copyAndReplace(String outputDir, Project project, Map<String, String> values) {
		File out = new File(outputDir);
		if (!out.exists() && !out.mkdirs()) {
			throw new RuntimeException("Couldn't create output directory '" + out.getAbsolutePath() + "'");
		}

		for (ProjectFile file : project.files) {
			copyFile(file, out, values);
		}
	}

	public static void copyFile(ProjectFile file, File out, Map<String, String> values) {
		File outFile = new File(out, file.outputName);
		if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
			throw new RuntimeException("Couldn't create dir '" + outFile.getAbsolutePath() + "'");
		}

		if (file.isTemplate) {
			String txt;
			txt = readResourceAsString(file.resourceName, file.resourceLoc);
			txt = replace(txt, values);
			writeFile(outFile, txt);
		} else {
			writeFile(outFile, readResource(file.resourceName, file.resourceLoc));
		}
	}

	private static String replace(String txt, Map<String, String> values) {
		for (String key : values.keySet()) {
			String value = values.get(key);
			txt = txt.replace(key, value);
		}
		return txt;
	}

	@SuppressWarnings("unused")
	private static byte[] readResource(File file) {
		InputStream in = null;
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 10];
			in = new FileInputStream(file);
			if (in == null)
				throw new RuntimeException("Couldn't read resource '" + file.getAbsoluteFile() + "'");
			int read = 0;
			while ((read = in.read(buffer)) > 0) {
				bytes.write(buffer, 0, read);
			}
			return bytes.toByteArray();
		} catch (Throwable e) {
			throw new RuntimeException("Couldn't read resource '" + file.getAbsoluteFile() + "'", e);
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}

	public static String readResourceAsString(String resource, String path) {
		try {
			return new String(readResource(resource, path), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readResourceAsString(File file) {
		try {
			return new String(readResource(file), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeFile(File outFile, String text) {
		try {
			writeFile(outFile, text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void writeFile(File outFile, byte[] bytes) {
		OutputStream out = null;

		try {
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			out.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't write file '" + outFile.getAbsolutePath() + "'", e);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}
}
