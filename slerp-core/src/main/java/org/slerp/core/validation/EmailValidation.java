package org.slerp.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EmailValidation {
	String[] value() default "invalid.value.@";

	boolean required() default true;

	String message() default "invalid.value.@";
}
