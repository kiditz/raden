package org.slerp.core.utils;

import java.util.regex.Matcher;

import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.core.validation.Patterns;

public class Validator {

	public static void validateKey(String message, Dto inputDto, String... keys) {
		for (String key : keys) {
			if (!inputDto.containsKey(key))
				throw new CoreException(message.replace("@", key));
		}
	}

	public static void validateNumber(String message, Dto inputDto, String... keys) {
		validateKey(message, inputDto, keys);
		for (String key : keys) {
			if (!inputDto.getString(key).matches(".*[0-9].*"))
				throw new CoreException(message.replace("@", key));
		}
	}

	public static void validateNotEmpty(String message, Dto inputDto, String... keys) {
		validateKey(message, inputDto, keys);
		for (String key : keys) {
			if (inputDto.get(key) == null || inputDto.get(key).toString().isEmpty())
				throw new CoreException(message.replace("@", key));
		}
	}

	public static void validateEmail(String message, Dto inputDto, String... keys) {
		validateKey(message, inputDto, keys);

		for (String key : keys) {
			Matcher matcher = Patterns.EMAIL_PATTERN.matcher(inputDto.getString(key));
			if (!matcher.matches())
				throw new CoreException(message.replace("@", key));
		}
	}

	public static void validatePhone(String message, Dto inputDto, String... keys) {
		validateKey(message, inputDto, keys);

		for (String key : keys) {
			Matcher matcher = Patterns.PHONE.matcher(inputDto.getString(key));
			if (!matcher.matches())
				throw new CoreException(message.replace("@", key));
		}
	}
}
