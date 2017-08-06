package org.slerp.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.slerp.core.CoreException;
import org.slerp.core.ConcurentDto;
import org.slerp.generator.ApiGenerator;
import org.slerp.generator.JUnitTestGenerator;
import org.slerp.utils.JpaParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Mojo(name = "rest", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ApiGeneratorMojo extends AbstractMojo {
	@Parameter(defaultValue = "${project.basedir}/src/main/resources/application.properties", property = "properties", required = true)
	private File properties;
	@Parameter(defaultValue = "${project.basedir}/src/main/java", property = "srcDir", required = true)
	private File srcDir;
	@Parameter(defaultValue = "${project.basedir}/src/main/java", property = "apiDir", required = true)
	private File apiDir;
	@Parameter(defaultValue = "${project.basedir}/src/main/resources", property = "cacheDir", required = true)
	private File cacheDir;
	Logger log = LoggerFactory.getLogger(getClass());

	@SuppressWarnings("resource")
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!properties.exists())
			throw new RuntimeException("Cannot found configuration file at " + properties.getPath());

		System.out.println("----------------------------------------------------------------------");
		System.out.println("Rest A.P.I Generator");
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
		String cacheConPackage = cacheDto.getString("packageController");
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

		System.out.print("Package Controller " + (cacheConPackage == null ? "" : "(" + cacheConPackage + ")") + ": ");
		String packageController = scanner.nextLine();
		if (StringUtils.isEmpty(packageController)) {
			packageController = cacheConPackage;
		}
		if (StringUtils.isEmpty(packageController)) {
			throw new CoreException("Package Controller is required to be filled");
		}

		validatePackage(packageController, "Package Entity is invalid");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		ApiGenerator generator = new ApiGenerator(apiDir.getAbsolutePath(), srcDir.getAbsolutePath(), packageEntity,
				packageRepoName, packageService, packageController);
		try {
			generator.parse();
			System.out.println("----------------------------------------------------------------------");
			System.out.println("Found Service in project");
			System.out.println("----------------------------------------------------------------------");
			for (int i = 0; i < generator.getParsers().size(); i++) {
				JpaParser parser = generator.getParsers().get(i);
				System.out.println((i + 1) + ". " + parser.getService().getString("className"));
				System.out.println("----------------------------------------------------------------------");
				for (ConcurentDto field : parser.getFields()) {
					if (field.getString("fieldType").equals("java.lang.Object")) {
						System.out.print("Data type for (" + field.getString("fieldName") + ") : ");
						String type = scanner.nextLine();
						field.put("fieldType", JUnitTestGenerator.primitivType.get(type));
					}
				}
			}
			scanner.close();
			generator.generate();
			System.out.println("At " + apiDir.getAbsolutePath());

			cacheDto.put("packageEntity", packageEntity);
			cacheDto.put("packageService", packageService);
			cacheDto.put("packageRepo", packageRepoName);
			cacheDto.put("packageController", packageController);
			try {
				mapper.writeValue(cacheFile, cacheDto);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
