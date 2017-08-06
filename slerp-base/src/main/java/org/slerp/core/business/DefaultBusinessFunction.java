package org.slerp.core.business;

import org.slerp.core.Domain;
import org.slerp.core.validation.ValidatorAnnotationHandler;

public abstract class DefaultBusinessFunction implements BusinessFunction {
	public Domain handle(Domain inputDomain) {
		ValidatorAnnotationHandler.validate(inputDomain, getClass());
		return null;
	}
}
