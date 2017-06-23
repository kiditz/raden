package org.slerp;

import java.util.ArrayList;
import java.util.List;

import org.slerp.core.Dto;
import org.slerp.core.utils.Validator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class DtoTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public DtoTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(DtoTest.class);
	}

	public void testDto() {
		Dto inputDto = new Dto();
		inputDto.put("long", Long.MAX_VALUE);
		inputDto.put("double", Double.MAX_VALUE);
		inputDto.put("float", Float.MAX_VALUE);
		inputDto.put("int", Integer.MAX_VALUE);
		inputDto.put("short", Short.MAX_VALUE);
		inputDto.put("string", "This is test roger");
		
		Dto otherDto = new Dto();
		otherDto.put("input", new Dto(inputDto));
		inputDto.put("other", new Dto(otherDto));
		List<Dto> listDto = new ArrayList<Dto>();
		listDto.add(new Dto().put("test1", "test1"));
		listDto.add(new Dto().put("test2", "test2"));
		inputDto.put("list", listDto);
		System.out.println(inputDto);
	}

	public void testValidationUtils() {
		Dto inputDto = new Dto();
		inputDto.put("long", Long.MAX_VALUE);
		inputDto.put("double", Double.MAX_VALUE);
		inputDto.put("float", Float.MAX_VALUE);
		inputDto.put("int", Integer.MAX_VALUE);
		inputDto.put("short", Short.MAX_VALUE);
		inputDto.put("string", "This is test roger");
		inputDto.put("null", "success");
		inputDto.put("empty", "success");
		inputDto.put("email", "kiditzbastara@gmail.com");
		inputDto.put("number", 1l);
		inputDto.put("phone", "087788044374");
		
		Validator.validateKey("failed key", inputDto, "string");
		Validator.validateNotEmpty("failed blank", inputDto, "null");
		Validator.validateNotEmpty("failed blank", inputDto, "empty");
		Validator.validateEmail("failed blank", inputDto, "email");
		Validator.validateNumber("failed number", inputDto, "long");
		Validator.validatePhone("failed phone", inputDto, "phone");
		System.out.println("Validatiion " + inputDto);
	}
}
