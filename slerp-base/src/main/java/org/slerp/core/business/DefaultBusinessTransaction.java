package org.slerp.core.business;

import org.slerp.core.CoreException;
import org.slerp.core.Domain;
import org.slerp.core.validation.ValidatorAnnotationHandler;

public abstract class DefaultBusinessTransaction implements BusinessTransaction {

	@Override
	public abstract void prepare(Domain inputDomain) throws Exception;

	public Domain handle(Domain inputDomain) {
		try {
			ValidatorAnnotationHandler.validate(inputDomain, getClass());
			this.prepare(inputDomain);			
		} catch (Throwable e) {
			throw new CoreException(e);
		}
		return null;
	}

}
