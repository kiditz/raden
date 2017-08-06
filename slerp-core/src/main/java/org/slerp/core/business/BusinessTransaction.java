package org.slerp.core.business;

import org.slerp.core.Domain;

public interface BusinessTransaction extends BusinessLayer{
	public void prepare(Domain inputDomain) throws Throwable;
}
