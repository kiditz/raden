package org.slerp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.core.business.DefaultBusinessFunction;
import org.slerp.utils.EntityUtils;
import org.slerp.utils.StringConverter;

public class FunctionGenerator implements Generator {
	public String packageName;
	public String packageRepoName;
	public String packageTarget;
	public String srcDir;
	private String methodName;
	public Dto params = new Dto();
	private String query = "";
	public FunctionType type;

	public FunctionGenerator(String packageName, String packageRepoName, String srcDir, String methodName) {
		this.packageName = packageName;
		this.packageRepoName = packageRepoName;
		if (Strings.isNullOrEmpty(packageTarget))
			packageTarget = packageRepoName;
		this.methodName = methodName;
		this.srcDir = srcDir;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	@Override
	public void generate(String query) {
		this.query = query;
		System.err.println("Query : " + query);
		String className = FunctionGenerator.getReturnByQuery(this.query);
		System.out.println("Class Name : " + className);
		if (Strings.isNullOrEmpty(className)) {
			throw new CoreException("Cannot Find Class By Query" + query);
		}

		File repositoryFile = new File(srcDir,
				packageRepoName.replace(".", "/").concat("/").concat(className.concat("Repository").concat(".java")));
		try {
			JavaInterfaceSource inf = Roaster.parse(JavaInterfaceSource.class, repositoryFile);
			List<MethodSource<JavaInterfaceSource>> methodSources = inf.getMethods();
			for (MethodSource<JavaInterfaceSource> methodSource : methodSources) {
				if (methodSource.getName().equalsIgnoreCase(methodName))
					inf.removeMethod(methodSource);
			}
			MethodSource<JavaInterfaceSource> method = null;

			if (type == FunctionType.LIST) {
				method = inf.addMethod().setName(methodName)
						.setReturnType("List<" + packageName.concat(".").concat(className) + ">").setPublic();
				inf.addImport(List.class);
			} else if (type == FunctionType.SINGLE) {
				method = inf.addMethod().setName(methodName).setReturnType(packageName.concat(".").concat(className))
						.setPublic();
			} else {
				method = inf.addMethod().setName(methodName)
						.setReturnType("Page<" + packageName.concat(".").concat(className) + ">").setPublic();
				inf.addImport("org.springframework.data.domain.Page");
				inf.addImport("org.springframework.data.domain.Pageable");
			}

			String[] keyValidationValues = new String[type == FunctionType.PAGE ? params.size() + 2 : params.size()];
			Set<String> numberValidationValues = new TreeSet<>();
			Set<String> stringValidationValues = new TreeSet<>();
			int i = 0;
			for (Entry<Object, Object> entry : params.entrySet()) {
				keyValidationValues[i] = entry.getKey().toString();
				if (JUnitTestGenerator.numberType.containsValue(entry.getValue())) {
					numberValidationValues.add(entry.getKey().toString());
				}
				if (JUnitTestGenerator.stringType.containsValue(entry.getValue())) {
					stringValidationValues.add(entry.getKey().toString());
				}
				method.addParameter(entry.getValue().toString(), entry.getKey().toString())
						.addAnnotation("org.springframework.data.repository.query.Param")
						.setStringValue(entry.getKey().toString());
				i++;
			}
			System.err.println(numberValidationValues.toString());
			if (type == FunctionType.PAGE) {
				keyValidationValues[i++] = "page";
				keyValidationValues[i++] = "size";
				method.addParameter("Pageable", "pageable");
			}
			method.addAnnotation("org.springframework.data.jpa.repository.Query").setStringValue(getQuery());
			FileWriter writer = new FileWriter(repositoryFile);
			writer.write(inf.toString());
			writer.close();
			JavaClassSource cls = Roaster.create(JavaClassSource.class);
			cls.setName(Strings.capitalize(methodName)).setPublic().extendSuperType(DefaultBusinessFunction.class);
			cls.setPackage(packageTarget);
			cls.addImport(packageName.concat(".").concat(className));
			cls.addAnnotation("org.springframework.stereotype.Service");
			cls.addAnnotation("org.slerp.core.validation.KeyValidation").setStringArrayValue(keyValidationValues);
			cls.addAnnotation("org.slerp.core.validation.NumberValidation")
					.setStringArrayValue(numberValidationValues.toArray(new String[] {}));
			cls.addAnnotation("org.slerp.core.validation.NotBlankValidation")
					.setStringArrayValue(stringValidationValues.toArray(new String[] {}));
			MethodSource<JavaClassSource> clsMethod = cls.addMethod().setName("handle");
			clsMethod.setPublic();
			clsMethod.setReturnType(Dto.class);
			String methodParam = Strings.uncapitalize(getReturnByQuery(query)).concat("Dto");
			clsMethod.addParameter("Dto", methodParam);
			cls.addImport(Dto.class);
			StringBuffer buffer = new StringBuffer();
			String repoName = Strings.uncapitalize(StringConverter.getFilename(repositoryFile));
			cls.addField().setName(repoName)
					.setType(packageRepoName.concat(".").concat(StringConverter.getFilename(repositoryFile)))
					.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
			String var = Strings.uncapitalize(className);
			if (!params.isEmpty()) {
				for (Entry<Object, Object> entry : params.entrySet()) {
					if (type == FunctionType.LIST) {
						buffer.append("List".concat("<").concat(className).concat(">")).append(" ")
								.append(var.concat("List"));
						buffer.append(" = ").append(repoName.concat(".").concat(methodName));
						buffer.append("(").append(var.concat("Dto").concat(".get").concat(entry.getValue().toString())
								.concat("(\"").concat(entry.getKey().toString()).concat("\")")).append(");\n");
						buffer.append(
								"return new Dto().put(\"" + var.concat("List") + "\", " + var.concat("List") + ");");
						cls.addImport(List.class);
					} else if (type == FunctionType.SINGLE) {

						buffer.append(className).append(" ").append(var);
						buffer.append(" = ").append(repoName.concat(".").concat(methodName));
						buffer.append("(").append(var.concat("Dto").concat(".get").concat(entry.getValue().toString())
								.concat("(\"").concat(entry.getKey().toString()).concat("\")")).append(");\n");
						buffer.append("return new Dto().put(\"" + var + "\", " + var + ");");
					} else if (type == FunctionType.PAGE) {
						buffer.append("int page = " + var.concat("Dto") + ".getInt(\"page\");");
						buffer.append("int size = " + var.concat("Dto") + ".getInt(\"size\");");
						buffer.append("Page".concat("<").concat(className).concat(">")).append(" ")
								.append(var.concat("Page"));
						buffer.append(" = ").append(repoName.concat(".").concat(methodName));
						buffer.append("(")
								.append(var.concat("Dto").concat(".get").concat(entry.getValue().toString())
										.concat("(\"").concat(entry.getKey().toString()).concat("\")").concat(", ")
										.concat("new PageRequest(page, size)"))
								.append(");\n");
						buffer.append(
								"return new Dto().put(\"" + var.concat("Page") + "\", " + var.concat("Page") + ");");
						cls.addImport("org.springframework.data.domain.Page");
						cls.addImport("org.springframework.data.domain.PageRequest");
						method.addParameter("Pageable", "pageable");
					} else {
						throw new CoreException("You need to choose the function type");
					}
				}
			} else {
				if (type == FunctionType.LIST) {
					buffer.append("List".concat("<").concat(className).concat(">")).append(" ")
							.append(var.concat("List"));
					buffer.append(" = ").append(repoName.concat(".").concat(methodName)).append("();\n");
					buffer.append("return new Dto(" + var.concat("List") + ");");
					cls.addImport(List.class);
				} else if (type == FunctionType.SINGLE) {
					buffer.append(className).append(" ").append(var);
					buffer.append(" = ").append(repoName.concat(".").concat(methodName)).append("();\n");
					buffer.append("return new Dto(" + var + ");");
				} else {
					buffer.append("int page = " + var.concat("Dto") + ".getInt(\"page\");");
					buffer.append("int size = " + var.concat("Dto") + ".getInt(\"size\");");
					buffer.append("Page".concat("<").concat(className).concat(">")).append(" ")
							.append(var.concat("Page"));
					buffer.append(" = ").append(repoName.concat(".").concat(methodName))
							.append("(new PageRequest(page, size));\n");
					buffer.append("return new Dto(" + var.concat("Page") + ");");
					cls.addImport("org.springframework.data.domain.Page");
					cls.addImport("org.springframework.data.domain.PageRequest");
				}
			}

			clsMethod.addAnnotation(Override.class);
			clsMethod.setBody(buffer.toString());
			File file = new File(srcDir,
					packageTarget.replace(".", "/").concat("/").concat(Strings.capitalize(methodName)).concat(".java"));
			if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
				throw new CoreException("Failsed to create directory " + file.getPath());
			}
			writer = new FileWriter(file);
			writer.write(cls.toString());
			writer.close();
			System.err.println(file.getAbsolutePath());
			System.err.println("Generated Successfully Created : " + cls.getCanonicalName().concat(".java"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new CoreException("Cannot find file " + repositoryFile.getName() + " in package " + packageRepoName
					+ " error : " + e);
		}
	}

	public Dto getEntity() throws IOException {
		return EntityUtils.readEntities(new File(srcDir));
	}

	public static List<String> getParamsByQuery(String query) {
		String[] split = query.split(" ");
		List<String> params = new ArrayList<String>();
		for (String q : split) {
			if (q.startsWith(":")) {
				params.add(q.replace(":", "").trim());
			}
		}
		System.err.println(params);
		return params;
	}

	public static String getReturnByQuery(String query) {
		String[] split = query.split(" ");
		for (int i = 0; i < split.length; i++) {
			if (split[i].equalsIgnoreCase("FROM")) {
				return split[i + 1];
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.err.println(JUnitTestGenerator.numberType.toString());
		System.out.println(
				FunctionGenerator.getReturnByQuery("SELECT p FROM Category p WHERE p.categoryName = :categoryName"));
		FunctionGenerator generator = new FunctionGenerator("org.slerp.ecommerce.entity",
				"org.slerp.ecommerce.repository", "/home/kiditz/apps/framework/slerp-ecommerce-service/src/main/java/",
				"getProduct");
		generator.packageTarget = "org.slerp.ecommerce.service.product";
		String query = "SELECT p FROM Category p WHERE p.categoryName = :categoryName AND p.categoryId = :categoryId";
		generator.type = FunctionType.PAGE;
		List<String> params = FunctionGenerator.getParamsByQuery(query);
		Dto paramDto = new Dto();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		for (String param : params) {
			System.out.print("Type for " + param + " : ");
			String type = scanner.nextLine();
			if (JUnitTestGenerator.primitivType.get(type) != null)
				paramDto.put(param, JUnitTestGenerator.primitivType.get(type));
			else
				throw new CoreException("Data Type Not Found");
		}

		generator.params = paramDto;
		// System.out.println();

		generator.generate(query);
		scanner.close();
	}

	static public enum FunctionType {
		PAGE, SINGLE, LIST
	}
}
