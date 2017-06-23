package org.slerp.core.validation;

import java.lang.annotation.Annotation;

import org.slerp.core.Dto;
import org.slerp.core.utils.Validator;

public class ValidatorAnnotationHandler {
	public static void validate(Dto inputDto, Class<?> srcClass) {
		Annotation[] annotations = srcClass.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation instanceof KeyValidation) {
				KeyValidation validation = (KeyValidation) annotation;
				String message = validation.message();
				String[] keys = validation.value();
				boolean required = validation.required();
				if (required) {
					Validator.validateKey(message, inputDto, keys);
				}
			} else if (annotation instanceof EmailValidation) {
				EmailValidation validation = (EmailValidation) annotation;
				String message = validation.message();
				String[] keys = validation.value();
				boolean required = validation.required();
				if (required) {
					Validator.validateEmail(message, inputDto, keys);
				}
			} else if (annotation instanceof NotBlankValidation) {
				NotBlankValidation validation = (NotBlankValidation) annotation;
				String message = validation.message();
				String[] keys = validation.value();
				boolean required = validation.required();
				if (required) {
					Validator.validateNotEmpty(message, inputDto, keys);
				}
			} else if (annotation instanceof NumberValidation) {
				NumberValidation validation = (NumberValidation) annotation;
				String message = validation.message();
				String[] keys = validation.value();
				boolean required = validation.required();
				if (required) {
					Validator.validateNumber(message, inputDto, keys);
				}
			}
		}
	}
}
