package org.slerp.core.business;

import org.slerp.core.Dto;
import org.slerp.core.validation.ValidatorAnnotationHandler;

public abstract class DefaultBusinessFunction implements BusinessFunction {
	public Dto handle(Dto inputDto) {
		ValidatorAnnotationHandler.validate(inputDto, getClass());
		return null;
	}

}
