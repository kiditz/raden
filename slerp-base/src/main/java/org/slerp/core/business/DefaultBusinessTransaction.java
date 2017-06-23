package org.slerp.core.business;

import org.slerp.core.Dto;
import org.slerp.core.validation.ValidatorAnnotationHandler;

public abstract class DefaultBusinessTransaction implements BusinessTransaction {

	public Dto handle(Dto inputDto) {
		prepare(inputDto);
		ValidatorAnnotationHandler.validate(inputDto, getClass());
		return null;
	}

}
