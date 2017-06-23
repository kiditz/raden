package org.slerp.core.validation;

import org.slerp.core.Dto;
import org.slerp.core.business.DefaultBusinessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestValidation {
	static Logger log = LoggerFactory.getLogger(TestValidation.class);

	public void testApp() {
		SuccessTest validation = new SuccessTest();
		Dto inputDto = new Dto();
		inputDto.put("long", Long.MAX_VALUE);
		inputDto.put("double", Double.MAX_VALUE);
		inputDto.put("float", Float.MAX_VALUE);
		inputDto.put("int", Integer.MAX_VALUE);
		inputDto.put("short", Short.MAX_VALUE);
		inputDto.put("string", "test");
		inputDto.put("empty", "success");
		inputDto.put("email", "kiditzbastara@gmail.com");
		inputDto.put("number", 1l);
		inputDto.put("phone", "087788044374");
		log.info("Input {}", inputDto);
		validation.handle(inputDto);
	}

	@NotBlankValidation({ "string", "empty", "email" })
	@NumberValidation({ "long", "double", "float", "short" })
	@EmailValidation("email")
	public static class SuccessTest extends DefaultBusinessFunction {

		public Dto handle(Dto inputDto) {
			return super.handle(inputDto);
		}
	}
}
