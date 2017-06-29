package org.slerp.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.generator.FunctionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Mojo(name = "function", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class FunctionGeneratorMojo extends AbstractMojo {
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
		System.out.println("Function Generator");
		System.out.println("----------------------------------------------------------------------");
		if (!cacheDir.isDirectory())
			cacheDir.mkdirs();
		File cacheFile = new File(cacheDir, "generator.cache");
		Dto cacheDto = null;
		try {
			cacheDto = new Dto(readString(cacheFile));
		} catch (Exception e) {
			cacheDto = new Dto();
		}
		String cacheTargetPackage = cacheDto.getString("packageTarget");
		String cacheEnPackage = cacheDto.getString("packageEntity");
		String cacheRepPackage = cacheDto.getString("packageRepo");

		System.out.print("Package Entity" + (cacheEnPackage == null ? "" : "(" + cacheEnPackage + ")") + ": ");
		Scanner scanner = new Scanner(System.in);
		String packageEntity = scanner.nextLine();
		if (StringUtils.isEmpty(packageEntity)) {
			packageEntity = cacheEnPackage;
		}
		if (StringUtils.isEmpty(packageEntity)) {
			throw new CoreException("Package Entity name is required to be filled");
		}

		validatePackage(packageEntity, "Package Entity is invalid");

		System.out.print("Package Repository  " + (cacheRepPackage == null ? "" : "(" + cacheRepPackage + ")") + ": ");
		String packageRepoName = scanner.nextLine();
		if (StringUtils.isEmpty(packageRepoName)) {
			packageRepoName = cacheRepPackage;
		}
		if (StringUtils.isEmpty(packageRepoName)) {
			throw new CoreException("Package Repository is required to be filled");
		}
		validatePackage(packageRepoName, "Package Repository is invalid");

		System.out.print("Package Target " + (cacheTargetPackage == null ? "" : "(" + cacheTargetPackage + ")") + ": ");
		String packageTarget = scanner.nextLine();
		if (StringUtils.isEmpty(packageTarget)) {
			packageTarget = cacheTargetPackage;
		}
		if (StringUtils.isEmpty(packageTarget)) {
			throw new CoreException("Package Target is required to be filled");
		}
		validatePackage(packageTarget, "Package Target is invalid");
		System.out.print("Query : ");
		String query = scanner.nextLine();
		if (StringUtils.isEmpty(query)) {
			throw new CoreException("Query is required to be filled");
		}

		System.out.print("Method Name : ");
		String methodName = scanner.nextLine();
		if (StringUtils.isEmpty(methodName)) {
			throw new CoreException("Method name is required to be filled");
		}

		FunctionGenerator generator = new FunctionGenerator(packageEntity, packageRepoName, srcDir.getAbsolutePath(),
				methodName);
		List<String> params = FunctionGenerator.getParamsByQuery(query);
		Dto paramDto = new Dto();
		for (String param : params) {
			System.out.print("Data Type for " + param + " : ");
			String type = scanner.nextLine();
			if (StringUtils.isEmpty(type)) {
				throw new CoreException("Data type is required to be filled");
			}
			paramDto.put(param, type);
		}
		generator.params = paramDto;
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		System.out.print("Use List (Y/N) : ");
		boolean useList = true;
		if (cacheDto.get("useList") != null)
			useList = cacheDto.getBoolean("useList");
		if (scanner.nextLine().equalsIgnoreCase("Y"))
			useList = true;
		else if (scanner.nextLine().equalsIgnoreCase("N"))
			useList = false;
		generator.packageTarget = packageTarget;
		generator.setList(useList);
		generator.generate(query);
		cacheDto.put("packageTarget", packageTarget);
		cacheDto.put("packageEntity", packageEntity);
		cacheDto.put("packageRepo", packageRepoName);
		cacheDto.put("useList", useList);
		try {
			mapper.writeValue(cacheFile, cacheDto);
		} catch (IOException e1) {
			e1.printStackTrace();
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
