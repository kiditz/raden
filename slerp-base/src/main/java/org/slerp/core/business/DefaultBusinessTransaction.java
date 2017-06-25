package org.slerp.core.business;

import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.core.validation.ValidatorAnnotationHandler;

public abstract class DefaultBusinessTransaction implements BusinessTransaction {
	@Override
	public void prepare(Dto inputDto) throws Exception {

	}

	public Dto handle(Dto inputDto) {
		try {
			prepare(inputDto);
		} catch (Throwable e) {
			throw new CoreException(e);
		}
		ValidatorAnnotationHandler.validate(inputDto, getClass());
		return null;
	}

}
