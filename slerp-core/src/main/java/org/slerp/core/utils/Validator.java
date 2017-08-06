package org.slerp.core.utils;

import java.util.regex.Matcher;

import org.slerp.core.CoreException;
import org.slerp.core.Domain;
import org.slerp.core.validation.Patterns;

public class Validator {

	public static void validateKey(String message, Domain inputDomain, String... keys) {
		for (String key : keys) {
			if (!inputDomain.containsKey(key)) {
				int lastIndex = key.lastIndexOf('.');
				if (lastIndex != -1) {
					String keyFirst = key.substring(0, lastIndex).trim();
					String keyLast = key.substring(lastIndex + 1, key.length()).trim();
					Domain rootDomain = inputDomain.getDomain(keyFirst);
					if (rootDomain == null) {
						throw new CoreException(message.replace("@", keyFirst));
					}
					if (!rootDomain.containsKey(keyLast)) {
						throw new CoreException(message.replace("@", keyFirst.concat(".").concat(keyLast)));
					}
				} else {
					throw new CoreException(message.replace("@", key));
				}
			}
		}
	}

	public static void validateNumber(String message, Domain inputDomain, String... keys) {
		validateKey(message, inputDomain, keys);
		for (String key : keys) {
			int lastIndex = key.lastIndexOf('.');
			if (lastIndex != -1) {
				String keyFirst = key.substring(0, lastIndex).trim();
				String keyLast = key.substring(lastIndex + 1, key.length()).trim();
				Domain rootDomain = inputDomain.getDomain(keyFirst);
				if (rootDomain == null) {
					throw new CoreException(message.replace("@", keyFirst));
				}
				if (!rootDomain.getString(keyLast).matches(".*[0-9].*"))
					throw new CoreException(message.replace("@", keyFirst.concat(".").concat(keyLast)));

			} else {
				if (!inputDomain.getString(key).matches(".*[0-9].*"))
					throw new CoreException(message.replace("@", key));
			}

		}
	}

	public static void validateNotEmpty(String message, Domain inputDomain, String... keys) {
		validateKey(message, inputDomain, keys);
		for (String key : keys) {
			int lastIndex = key.lastIndexOf('.');
			if (lastIndex != -1) {
				String keyFirst = key.substring(0, lastIndex).trim();
				String keyLast = key.substring(lastIndex + 1, key.length()).trim();
				Domain rootDomain = inputDomain.getDomain(keyFirst);
				if (rootDomain == null) {
					throw new CoreException(message.replace("@", keyFirst));
				}
				if (rootDomain.get(keyLast) == null || rootDomain.get(keyLast).toString().isEmpty())
					throw new CoreException(message.replace("@", keyFirst.concat(".").concat(keyLast)));
			} else {
				if (inputDomain.get(key) == null || inputDomain.get(key).toString().isEmpty())
					throw new CoreException(message.replace("@", key));
			}

		}
	}

	public static void validateEmail(String message, Domain inputDomain, String... keys) {
		validateKey(message, inputDomain, keys);

		for (String key : keys) {
			int lastIndex = key.lastIndexOf('.');
			if (lastIndex != -1) {
				String keyFirst = key.substring(0, lastIndex).trim();
				String keyLast = key.substring(lastIndex + 1, key.length()).trim();
				Domain rootDomain = inputDomain.getDomain(keyFirst);
				if (rootDomain == null) {
					throw new CoreException(message.replace("@", keyFirst));
				}
				Matcher matcher = Patterns.EMAIL_PATTERN.matcher(rootDomain.getString(keyLast));
				if (!matcher.matches())
					throw new CoreException(message.replace("@", keyFirst.concat(".").concat(keyLast)));
			} else {
				Matcher matcher = Patterns.EMAIL_PATTERN.matcher(inputDomain.getString(key));
				if (!matcher.matches())
					throw new CoreException(message.replace("@", key));
			}

		}
	}

	public static void validatePhone(String message, Domain inputDomain, String... keys) {
		validateKey(message, inputDomain, keys);

		for (String key : keys) {
			int lastIndex = key.lastIndexOf('.');
			if (lastIndex != -1) {
				String keyFirst = key.substring(0, lastIndex).trim();
				String keyLast = key.substring(lastIndex + 1, key.length()).trim();
				Domain rootDomain = inputDomain.getDomain(keyFirst);
				if (rootDomain == null) {
					throw new CoreException(message.replace("@", keyFirst));
				}
				Matcher matcher = Patterns.PHONE.matcher(rootDomain.getString(keyLast));
				if (!matcher.matches())
					throw new CoreException(message.replace("@", keyFirst.concat(".").concat(keyLast)));
			} else {
				Matcher matcher = Patterns.PHONE.matcher(inputDomain.getString(key));
				if (!matcher.matches())
					throw new CoreException(message.replace("@", key));
			}
		}
	}

	public static void main(String[] args) {
		Domain child = new Domain();
		child.put("schoolId", "1");

		Validator.validateNumber("required.@", new Domain().put("schoolId", child).put("test", 1l), "schoolId.schoolId",
				"test");
	}
}
