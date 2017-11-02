package org.slerp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.core.ConcurentDto;
import org.slerp.core.Domain;
import org.slerp.utils.EntityUtils;
import org.slerp.utils.JpaParser;
import org.springframework.util.StringUtils;

public class ApiGenerator {
	private String srcDir;
	private String apiDir;
	private String packageController;
	private String packageService;
	private String packageEntity;
	private String packageRepository;
	List<JpaParser> parsers = new ArrayList<>();
	private File file;

	public ApiGenerator(String apiDir, String srcDir, String packageEntity, String packageRepository,
			String packageService, String packageController) {
		this.srcDir = srcDir;
		this.apiDir = apiDir;
		this.packageService = packageService;
		this.packageRepository = packageRepository;
		this.packageController = packageController;
		this.packageEntity = packageEntity;
	}

	public void parse() throws IOException {
		File file = new File(srcDir, packageService.replace(".", "/").concat("/"));
		// System.err.println(file);
		ConcurentDto scanService = EntityUtils.readBusiness(file);

		for (Entry<Object, Object> entry : scanService.entrySet()) {
			JpaParser parser = new JpaParser(new File(entry.getValue().toString()), packageEntity, packageRepository);
			parser.setUseEntity(true);
			parser.parse();
			parsers.add(parser);
		}
	}

	int index = 0;

	public void generate() throws IOException {

		String controllerName = packageService.substring(packageService.lastIndexOf('.') + 1, packageService.length());

		file = new File(apiDir, packageController.replace(".", "/").concat("/")
				.concat(Strings.capitalize(controllerName).concat("Controller")).concat(".java"));
		if (!file.getParentFile().isDirectory())
			file.getParentFile().mkdirs();
		JavaClassSource cls = null;
		if (file.exists()) {
			System.out.println("Modify Current Controller : " + controllerName);
			cls = Roaster.parse(JavaClassSource.class, file);
		} else {
			System.out.println("Start Creating Controller with name : " + controllerName);
			cls = Roaster.create(JavaClassSource.class);
			cls.setName(Strings.capitalize(controllerName.concat("Controller"))).setPublic();
			cls.setPackage(packageController);
			cls.addAnnotation("org.springframework.web.bind.annotation.RestController");
		}

		//

		for (JpaParser parser : this.getParsers()) {
			ConcurentDto service = parser.getService();
			String fieldName = StringUtils.uncapitalize(service.getString("className"));

			String mode = service.getString("mode");
			if (mode.equals("transaction")) {
				if (!cls.hasField(fieldName)) {
					System.out.println("Put new business transaction with name : " + fieldName);
					cls.addField().setType("org.slerp.core.business.BusinessTransaction").setName(fieldName)
							.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
				} else {
					System.out.println("Excluding existing business transaction with name : " + fieldName);
				}
			} else {
				if (!cls.hasField(fieldName)) {
					System.out.println("Put new business function with name : " + fieldName);
					cls.addField().setType("org.slerp.core.business.BusinessFunction").setName(fieldName)
							.addAnnotation("org.springframework.beans.factory.annotation.Autowired");

				} else {
					System.out.println("Excluding existing business function with name : " + fieldName);
				}
			}
			if (!isMethodExist(cls, fieldName)) {
				writeMethod(cls, fieldName, mode, service, controllerName, parser);
			}
		}
		FileWriter writer = new FileWriter(file);
		writer.write(cls.toString());
		writer.close();
		System.out.println("Generator successfully created " + cls.getCanonicalName().concat(".java"));

	}

	private boolean isMethodExist(JavaClassSource cls, String name) {
		for (MethodSource<JavaClassSource> method : cls.getMethods()) {
			if (method.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private void writeMethod(JavaClassSource cls, String fieldName, String mode, ConcurentDto service,
			String controllerName, JpaParser parser) {
		MethodSource<JavaClassSource> method = cls.addMethod(fieldName.concat("()")).setPublic();
		method.setReturnType(Domain.class);
		if (mode.equals("transaction")) {
			if (fieldName.startsWith("edit")) {
				method.addAnnotation("org.springframework.web.bind.annotation.PutMapping")
						.setStringValue("/".concat(fieldName));

			} else if (fieldName.startsWith("remove")) {
				method.addAnnotation("org.springframework.web.bind.annotation.DeleteMapping")
						.setStringValue("/".concat(fieldName));
			} else {
				method.addAnnotation("org.springframework.web.bind.annotation.PostMapping")
						.setStringValue("/".concat(fieldName));
			}
			StringBuilder builder = new StringBuilder();
			writeTransactionParam(method, service, parser.getFields());
			writeTransactionBody(method, service, builder);
		} else {
			method.addAnnotation("org.springframework.web.bind.annotation.GetMapping")
					.setStringValue("/".concat(fieldName));
			StringBuffer getBody = new StringBuffer();
			getBody.setLength(0);
			getBody.append("Domain " + controllerName.concat("Domain").concat(" = ").concat("new Domain();\n"));
			for (ConcurentDto field : parser.getFields()) {

				String simpleType = field.getString("fieldType");
				simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1, simpleType.length());
				ParameterSource<JavaClassSource> param = method.addParameter(simpleType, field.getString("fieldName"));
				param.addAnnotation("org.springframework.web.bind.annotation.RequestParam")
						.setStringValue(field.getString("fieldName"));

				getBody.append(controllerName.concat("Domain")).append(".put(\"").append(field.getString("fieldName"))
						.append("\", ").append(field.getString("fieldName")).append(");\n");
			}
			getBody.append("return ").append(fieldName).append(".handle(").append(controllerName.concat("Domain"))
					.append(");\n");
			method.setBody(getBody.toString());
		}
		method.addAnnotation("org.springframework.web.bind.annotation.ResponseBody");

	}

	public File getFile() {
		return file;
	}

	private void writeTransactionParam(MethodSource<JavaClassSource> method, ConcurentDto service,
			Set<ConcurentDto> fields) {
		String paramDto = getSimpleName(service);
		method.addParameter("Domain", paramDto).addAnnotation("org.springframework.web.bind.annotation.RequestBody");
	}

	private void writeTransactionBody(MethodSource<JavaClassSource> method, ConcurentDto service,
			StringBuilder builder) {
		String paramDto = getSimpleName(service);
		String className = Strings.uncapitalize(service.getString("className"));
		builder.append("Domain outputDto = ".concat(className).concat(".handle(" + paramDto + ");\n"));
		builder.append("return ".concat("outputDto").concat(";"));
		method.setBody(builder.toString());
		builder.setLength(0);
	}

	private String getSimpleName(ConcurentDto servieceDto) {
		String paramDto = servieceDto.getString("packageName");
		int index = paramDto.lastIndexOf('.');
		if (index == 0)
			return paramDto;
		return paramDto.substring(paramDto.lastIndexOf('.') + 1, paramDto.length()).concat("Domain");
	}

	public List<JpaParser> getParsers() {
		return parsers;
	}

	public void setParsers(List<JpaParser> parsers) {
		this.parsers = parsers;
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		ApiGenerator generator = new ApiGenerator("/home/kiditz/slerpio/slerp-io-api/src/main/java/",
				"/home/kiditz/slerpio/slerp-io-service/src/main/java/", "org.slerpio.entity", "org.slerpio.repository",
				"org.slerpio.service.profile", "org.slerp.core.api");
		generator.parse();
		// for (int i = 0; i < generator.getParsers().size(); i++) {
		// JpaParser parser = generator.getParsers().get(i);
		// //System.out.println(parser.getService().toString());
		// }
		generator.generate();
		System.out.println("Finish after " + Long.valueOf((System.currentTimeMillis() - startTime) / 1000) + "s");
	}

}
