package org.slerp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slerp.core.Domain;
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

	public void testDomain() {
		Domain inputDomain = new Domain();
		inputDomain.put("long", Long.MAX_VALUE);
		inputDomain.put("double", Double.MAX_VALUE);
		inputDomain.put("float", Float.MAX_VALUE);
		inputDomain.put("int", Integer.MAX_VALUE);
		inputDomain.put("short", Short.MAX_VALUE);
		inputDomain.put("string", "This is test roger");

		Domain otherDomain = new Domain();
		otherDomain.put("input", new Domain(inputDomain));
		inputDomain.put("other", new Domain(otherDomain));
		List<Domain> listDomain = new ArrayList<Domain>();
		listDomain.add(new Domain().put("test1", "test1"));
		listDomain.add(new Domain().put("test2", "test2"));
		inputDomain.put("list", listDomain);
		System.out.println(inputDomain);
	}

	public void testValidationUtils() {
		Domain inputDomain = new Domain();
		inputDomain.put("long", Long.MAX_VALUE);
		inputDomain.put("double", Double.MAX_VALUE);
		inputDomain.put("float", Float.MAX_VALUE);
		inputDomain.put("int", Integer.MAX_VALUE);
		inputDomain.put("short", Short.MAX_VALUE);
		inputDomain.put("string", "This is test roger");
		inputDomain.put("null", "success");
		inputDomain.put("empty", "success");
		inputDomain.put("email", "kiditzbastara@gmail.com");
		inputDomain.put("number", 1l);
		inputDomain.put("phone", "087788044374");

		Validator.validateKey("failed key", inputDomain, "string");
		Validator.validateNotEmpty("failed blank", inputDomain, "null");
		Validator.validateNotEmpty("failed blank", inputDomain, "empty");
		Validator.validateEmail("failed blank", inputDomain, "email");
		Validator.validateNumber("failed number", inputDomain, "long");
		Validator.validatePhone("failed phone", inputDomain, "phone");
		System.out.println("Validatiion " + inputDomain);
	}

	public void testIgnoreUnknownProperties() throws Exception {
		Domain domain = new Domain();
		domain.put("username", "kiditz");
		domain.put("password", "rioters7");
		User user = domain.convertTo(User.class);
		assertTrue(user != null);
		assertEquals("kiditz", user.getUsername());
		assertEquals(null, user.getHashedPassword());
		System.err.println(user.getUsername());
	}

	public void testIgnoreNullValue() throws Exception {
		Domain domain = new Domain();
		domain.put("value", null);
		assertEquals(null, domain.get("value"));
	}
	public void testGetList()  {
		Domain domain = new Domain();
		domain.put("list", Arrays.asList(1, 2, 3, 4, 5));
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) domain.getList("list");
		System.err.println(list);
		
	}
	static class User {
		private String username;
		private String hashedPassword;

		public String getUsername() {
			return username;
		}

		public void setHashedPassword(String hashedPassword) {
			this.hashedPassword = hashedPassword;
		}

		public String getHashedPassword() {
			return hashedPassword;
		}

		public void setUsername(String username) {
			this.username = username;
		}
	}
}
