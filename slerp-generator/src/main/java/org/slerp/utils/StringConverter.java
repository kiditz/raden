package org.slerp.utils;

import java.io.File;

public class StringConverter {
	public static String convertCaseSensitive(String source, boolean firstCharUpper) {
		String split[] = source.split("_");
		StringBuilder builder = new StringBuilder();
		if (firstCharUpper) {
			split[0] = split[0].substring(0, 1).toUpperCase() + split[0].substring(1, split[0].length()).toLowerCase();
			builder.append(split[0]);
		} else {
			split[0] = split[0];
			builder.append(split[0]);
		}
		for (int i = 1; i < split.length; i++) {
			String src = split[i];
			src = src.substring(0, 1).toUpperCase() + src.substring(1, src.length()).toLowerCase();
			builder.append(src);
		}
		return builder.toString();
	}

	public static String getExtension(File file) {
		String nama = file.getName();
		int dot = nama.lastIndexOf('.');
		if (dot == -1) {
			return "";
		} else {
			return nama.substring(dot + 1);
		}
	}
	public static String getFilename(File file) {
		String nama = file.getName();
		int dot = nama.lastIndexOf('.');
		if (dot == -1) {
			return file.getName();
		} else {
			return nama.substring(0, dot);
		}
	}	
}
