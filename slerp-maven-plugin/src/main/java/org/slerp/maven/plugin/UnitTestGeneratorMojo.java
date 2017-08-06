package org.slerp.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.slerp.core.CoreException;
import org.slerp.core.ConcurentDto;
import org.slerp.generator.JUnitTestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Mojo(name = "test", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class UnitTestGeneratorMojo extends AbstractMojo {
	@Parameter(defaultValue = "${project.basedir}/src/main/resources/application.properties", property = "properties", required = true)
	private File properties;
	@Parameter(defaultValue = "${project.basedir}/src/main/java", property = "srcDir", required = true)
	private File srcDir;
	@Parameter(defaultValue = "${project.basedir}/src/main/resources", property = "srcDir", required = true)
	private File cacheDir;
	Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("resource")
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!properties.exists())
			throw new RuntimeException("Cannot found configuration file at " + properties.getPath());

		System.out.println("----------------------------------------------------------------------");
		System.out.println("Unit Test Generator");
		System.out.println("----------------------------------------------------------------------");
		if (!cacheDir.isDirectory())
			cacheDir.mkdirs();
		File cacheFile = new File(cacheDir, "generator.cache");
		ConcurentDto cacheDto = null;
		try {
			cacheDto = new ConcurentDto(readString(cacheFile));
		} catch (Exception e) {
			cacheDto = new ConcurentDto();
		}
		String cacheEnPackage = cacheDto.getString("packageEntity");
		String cacheTgtPackage = cacheDto.getString("packageService");
		String cacheRepPackage = cacheDto.getString("packageRepo");

		System.out.print("Package Entity" + (cacheEnPackage == null ? "" : "(" + cacheEnPackage + ")") + ": ");
		Scanner scanner = new Scanner(System.in);
		String packageEntity = scanner.nextLine();
		if (StringUtils.isEmpty(packageEntity)) {
			packageEntity = cacheEnPackage;
		}
		if (StringUtils.isEmpty(packageEntity)) {
			throw new CoreException("Package Entity is required to be filled");
		}

		validatePackage(packageEntity, "Package Entity is invalid");

		System.out.print("Package Target" + (cacheTgtPackage == null ? "" : "(" + cacheTgtPackage + ")") + ": ");
		String packageService = scanner.nextLine();
		if (StringUtils.isEmpty(packageService)) {
			packageService = cacheTgtPackage;
		}
		if (StringUtils.isEmpty(packageService)) {
			throw new CoreException("Package Target is required to be filled");
		}

		validatePackage(packageService, "Package Target is invalid");
		System.out.print("Package Repository " + (cacheRepPackage == null ? "" : "(" + cacheRepPackage + ")") + ": ");
		String packageRepoName = scanner.nextLine();
		if (StringUtils.isEmpty(packageRepoName)) {
			packageRepoName = cacheRepPackage;
		}
		if (StringUtils.isEmpty(packageRepoName)) {
			throw new CoreException("Package name is required to be filled");
		}

		validatePackage(packageRepoName, "Package Repository is invalid");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		JUnitTestGenerator generator = new JUnitTestGenerator(srcDir.getAbsolutePath(), packageEntity, packageRepoName,
				packageService);
		System.out.println("----------------------------------------------------------------------");
		System.out.println("Found Service in project");
		System.out.println("----------------------------------------------------------------------");
		ConcurentDto business = generator.getBusiness();
		int i = 1;
		for (Object businessName : business.keySet()) {
			System.out.println(i + ". " + businessName);
			i++;
		}
		System.out.print("Generate Class (a.k.a " + business.keySet().iterator().next() + ") : ");
		String className = scanner.nextLine();
		if (className == null)
			throw new CoreException("Class name should be filled");
		try {
			generator.parse(className);
		} catch (IOException e) {
			throw new CoreException(e);
		}
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

	public static void validatePackage(String input, String message) {
		String packageRegex = "^[a-z][a-z0-9_]*(.[a-z0-9_]+)+[0-9a-z_]$";
		if (!input.matches(packageRegex))
			throw new CoreException(message);
	}

	public static String readString(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		int read = 0;
		byte[] bytes = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((read = in.read(bytes)) != -1) {
			baos.write(bytes, 0, read);
		}
		in.close();
		return baos.toString("UTF-8");
	}
}
