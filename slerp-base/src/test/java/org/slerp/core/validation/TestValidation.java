package org.slerp.core.validation;

import org.slerp.core.Domain;
import org.slerp.core.business.DefaultBusinessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestValidation {
	static Logger log = LoggerFactory.getLogger(TestValidation.class);

	public void testApp() {
		SuccessTest validation = new SuccessTest();
		Domain inputDomain = new Domain();
		inputDomain.put("long", Long.MAX_VALUE);
		inputDomain.put("double", Double.MAX_VALUE);
		inputDomain.put("float", Float.MAX_VALUE);
		inputDomain.put("int", Integer.MAX_VALUE);
		inputDomain.put("short", Short.MAX_VALUE);
		inputDomain.put("string", "test");
		inputDomain.put("empty", "success");
		inputDomain.put("email", "kiditzbastara@gmail.com");
		inputDomain.put("number", 1l);
		inputDomain.put("phone", "087788044374");
		log.info("Input {}", inputDomain);
		validation.handle(inputDomain);
	}

	@NotBlankValidation({ "string", "empty", "email" })
	@NumberValidation({ "long", "double", "float", "short" })
	@EmailValidation("email")
	public static class SuccessTest extends DefaultBusinessFunction {

		public Domain handle(Domain inputDomain) {
			return super.handle(inputDomain);
		}
	}
}
