package org.slerp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.core.ConcurentDto;
import org.slerp.core.CoreException;
import org.slerp.core.business.BusinessFunction;
import org.slerp.core.business.BusinessTransaction;
import org.slerp.utils.EntityUtils;
import org.slerp.utils.JpaParser;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

public class JUnitTestGenerator {
	private String srcDir;
	private String packageRepo;
	private String packageTarget;
	private String packageEntity;
	private JpaParser parser;
	private File businessFile;
	private ConcurentDto parseResult;
	private ConcurentDto business;
	private File outputFile;
	public static Map<String, String> primitivType = new HashMap<>();
	public static Map<String, String> numberType = new HashMap<>();
	public static Map<String, String> stringType = new HashMap<>();
	static {
		primitivType.put("int", "java.lang.Integer");
		primitivType.put("Integer", "java.lang.Integer");
		primitivType.put("Float", "java.lang.Float");
		primitivType.put("float", "java.lang.Float");
		primitivType.put("Double", "java.lang.Double");
		primitivType.put("double", "java.lang.Double");
		primitivType.put("Long", "java.lang.Long");
		primitivType.put("long", "java.lang.Long");
		primitivType.put("Short", "java.lang.Short");
		primitivType.put("short", "java.lang.Short");
		primitivType.put("BigDecimal", "java.math.BigDecimal");
		primitivType.put("Date", "java.util.Date");
		primitivType.put("String", "java.lang.String");
		primitivType.put("List", "java.util.List");

		numberType.put("int", "java.lang.Integer");
		numberType.put("Integer", "java.lang.Integer");
		numberType.put("Float", "java.lang.Float");
		numberType.put("float", "java.lang.Float");
		numberType.put("Double", "java.lang.Double");
		numberType.put("double", "java.lang.Double");
		numberType.put("Long", "java.lang.Long");
		numberType.put("long", "java.lang.Long");
		numberType.put("Short", "java.lang.Short");
		numberType.put("short", "java.lang.Short");
		numberType.put("BigDecimal", "java.math.BigDecimal");

		stringType.put("String", "java.lang.String");
	}

	public JUnitTestGenerator(String srcDir, String packageEntity, String packageRepo, String packageTarget) {
		this.srcDir = srcDir;
		this.packageEntity = packageEntity;
		this.packageRepo = packageRepo;
		this.packageTarget = packageTarget;
		try {
			this.business = EntityUtils
					.readBusiness(new File(this.srcDir, this.packageTarget.replace(".", "/").concat("/")));
		} catch (IOException e) {
			throw new CoreException(e);
		}
	}

	public void generate() {
		JavaClassSource cls = Roaster.create(JavaClassSource.class);
		String className = parseResult.getString("className").concat("Test");
		String packageName = parseResult.getString("packageName").concat(".test");
		String mode = parseResult.getString("mode");
		cls.addField("static private Logger log = LoggerFactory.getLogger(" + className + ".class)");
		cls.addImport("org.slf4j.Logger");
		cls.addImport("org.slf4j.LoggerFactory");
		cls.addImport("org.slerp.core.Domain");
		cls.addField().setName(Strings.uncapitalize(parseResult.getString("className")))
				.setType(mode.equals("transaction") ? BusinessTransaction.class : BusinessFunction.class)
				.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
		cls.setName(className).setPackage(packageName).setPublic();
		cls.addAnnotation("org.junit.runner.RunWith").setLiteralValue("SpringJUnit4ClassRunner.class");
		cls.addImport("org.springframework.test.context.junit4.SpringJUnit4ClassRunner");
		cls.addAnnotation("org.springframework.test.context.ContextConfiguration").setStringValue("locations",
				"classpath:applicationContext.xml");
		cls.addAnnotation("org.springframework.test.context.TestExecutionListeners")
				.setClassArrayValue("listeners", DirtiesContextTestExecutionListener.class,
						TransactionalTestExecutionListener.class, DependencyInjectionTestExecutionListener.class)
				.setLiteralValue("inheritListeners", "false");
		cls.addAnnotation("org.springframework.test.annotation.Rollback");
		cls.extendSuperType(AbstractTransactionalJUnit4SpringContextTests.class);
		MethodSource<JavaClassSource> methodPrepare = cls.addMethod("prepare()").setPublic();
		methodPrepare.addAnnotation("org.junit.Before");
		StringBuilder prepareBody = new StringBuilder();
		prepareBody.append("executeSqlScript(\"classpath:"
				+ packageName.replace(".", "/").concat("/").concat(className).concat(".sql") + "\", false);\n");
		methodPrepare.setBody(prepareBody.toString());

		MethodSource<JavaClassSource> method = cls.addMethod("testSuccess()").setPublic();
		method.addAnnotation("org.junit.Test");
		StringBuilder buffer = new StringBuilder();
		for (ConcurentDto field : getFields()) {
			if (field == null)
				continue;
			String type = field.getString("fieldType");

			String value = field.getString("value");
			String name = field.getString("fieldName");
			if (field.getBoolean("isForeignKey"))
				continue;
			if (type.contains(".")) {
				if (!type.contains("lang") && !field.getBoolean("isJoin")) {
					cls.addImport(type);
				}
			}
			if (value.isEmpty())
				value = "null";

			String simpleType = type.substring(type.lastIndexOf('.') + 1, type.length());

			String parseValue = parseValue(type, value);

			if (className.startsWith("Add") || className.startsWith("Edit")) {
				if (field.getBoolean("isJoin")) {
					buffer.append("Domain".concat(" ").concat(name).concat(" = new Domain();\n"));
					buffer.append(name).append(".put(").append("\"" + name + "\"").append(", ").append(parseValue)
							.append(");\n");
				} else {
					buffer.append(simpleType.concat(" ").concat(name).concat(" = ").concat(parseValue).concat(";\n"));
				}
			} else {
				buffer.append(simpleType.concat(" ").concat(name).concat(" = ").concat(parseValue).concat(";\n"));
			}
		}

		cls.addImport("org.assertj.core.api.Assertions");
		buffer.append("\n");

		String entityName = parseResult.getDto("entity").getString("className");
		String defaultDto = Strings.uncapitalize(entityName).concat("Domain");
		buffer.append("Domain ").append(defaultDto).append(" ").append(" = new Domain();\n");
		// Write Dto
		for (ConcurentDto field : getFields()) {
			if (field == null)
				continue;
			String name = field.getString("fieldName");
			buffer.append(defaultDto).append(".put(\"").append(name).append("\"").append(", ").append(name)
					.append(");\n");
		}
		// Write handle

		buffer.append("Domain output" + entityName + " = ")
				.append(Strings.uncapitalize(parseResult.getString("className"))).append(".handle(").append(defaultDto)
				.append(");\n");
		buffer.append("log.info(\"Result Test {}\", output" + entityName + ");");
		buffer.append("\n");

		// Write Assert
		for (ConcurentDto field : getFields()) {
			if (field == null)
				continue;
			String name = field.getString("fieldName");
			if (!field.getBoolean("isJoin"))
				buffer.append(
						"Assertions.assertThat(" + defaultDto + ".get(\"" + name + "\")).isEqualTo(" + name + ");");
		}

		method.setBody(buffer.toString());
		String outputDirStr = businessFile.getParent().substring(0, businessFile.getParent().lastIndexOf("java") + 4);

		outputDirStr = outputDirStr.replaceAll("main", "test");
		String outputDirStrResources = outputDirStr.replaceAll("java", "resources");
		File outputDir = new File(outputDirStr);
		outputFile = new File(outputDir, packageName.replace(".", "/").concat("/").concat(className).concat(".java"));
		if (!outputFile.getParentFile().isDirectory())
			outputFile.getParentFile().mkdirs();
		try {
			FileWriter writer = new FileWriter(outputFile);
			writer.write(cls.toString());
			writer.close();

			System.out.println("Generator successfully created " + cls.getCanonicalName().concat(".java"));
			File outputDirResource = new File(outputDirStrResources);

			File outputSql = new File(outputDirResource,
					packageName.replace(".", "/").concat("/").concat(className).concat(".sql"));
			if (!outputSql.getParentFile().isDirectory())
				outputSql.getParentFile().mkdirs();
			writer = new FileWriter(outputSql);
			writer.write("/*\n");
			writer.write("TODO : Handle Sql File");
			writer.write("*/\n");
			writer.close();
			System.out.println("Generator successfully created " + cls.getCanonicalName().concat(".sql"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new CoreException(e);
		} finally {
		}

	}

	public void parse(String className) throws IOException {		
		try {
			if (business.get(className) == null)
				throw new CoreException("Failed to find business file with name " + className);
			System.err.println(business);
			businessFile = new File(business.getString(className));

			parser = new JpaParser(businessFile, packageEntity, packageRepo);
			parser.setUseEntity(true);
			parseResult = parser.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ConcurentDto getBusiness() {
		return business;
	}

	private String parseValue(String type, String value) {
		String parseValue = null;
		if (type.equals("java.lang.String") && !value.equals("null")) {
			parseValue = "\"" + value + "\"";
		} else if (type.equals("java.math.BigDecimal")) {
			parseValue = "BigDecimal.valueOf(" + ((value.equals("null")) ? "0" : value) + ")";
		} else if (type.equals("java.util.Date")) {
			parseValue = "new Date()";
		} else if (type.equals("java.lang.Long") && !value.equals("null")) {
			parseValue = value + "l";
		} else if (type.equals("java.lang.Double") && !value.equals("null")) {
			parseValue = value + "d";
		} else if (type.equals("java.lang.Short") && !value.equals("null")) {
			parseValue = value + "s";
		} else if (type.equals("java.lang.Float") && !value.equals("null")) {
			parseValue = value + "f";
		} else {
			parseValue = value;
		}
		return parseValue;

	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);
		JUnitTestGenerator generator = new JUnitTestGenerator(
				"/home/kiditz/apps/workspace-2017/school-service/src/main/java/", "org.slerp.school.entity",
				"org.slerp.school.repository", "org.slerp.school.service.task");
		generator.parse("AddTaskUser");
		Set<ConcurentDto> fields = generator.getFields();
		generate(fields, scanner);
		scanner.close();
		generator.generate();
	}

	private static void generate(Set<ConcurentDto> fieldSet, Scanner scanner) {
		List<ConcurentDto> fields = new ArrayList<>();
		fieldSet.forEach(fields::add);
		for (int i = 0; i < fields.size(); i++) {
			ConcurentDto field = fields.get(i);
			String dataType = field.getString("fieldType");
			if (dataType.equals("java.lang.Object")) {
				System.out.print("\n");
				System.out.print("Type for (" + field.getString("fieldName") + ") : ");
				String tempType = scanner.nextLine();
				dataType = JUnitTestGenerator.primitivType.get(tempType);
			}
			if (dataType == null) {
				throw new CoreException(
						"Cannot found data type please choose between " + JUnitTestGenerator.primitivType.keySet());
			}
			String simpleType = dataType.substring(dataType.lastIndexOf('.') + 1, dataType.length());

			System.out
					.print((i + 1) + ". " + field.getString("fieldName").concat("(" + simpleType + ") ").concat(" : "));
			String value = scanner.nextLine();
			field.put("fieldType", dataType);
			field.put("value", value);
		}

	}

	public Set<ConcurentDto> getFields() {
		return parser.getFields();
	}

	public void setFields(Set<ConcurentDto> fields) {
		parser.setFields(fields);
	}

	public JpaParser getParser() {
		return parser;
	}

	public void setParseResult(ConcurentDto parseResult) {
		this.parseResult = parseResult;
	}

	public File getOutputFile() {
		return outputFile;
	}

}