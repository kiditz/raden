package org.slerp.core.business;

import org.slerp.core.Dto;

public interface BusinessTransaction extends BusinessLayer{
	public void prepare(Dto inputDto);
}
