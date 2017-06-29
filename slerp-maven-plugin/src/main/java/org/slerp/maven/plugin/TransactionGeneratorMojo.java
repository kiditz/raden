package org.slerp.maven.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.slerp.generator.TransactionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Mojo(name = "transaction", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class TransactionGeneratorMojo extends AbstractMojo {
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
		System.out.println("Transaction Generator");
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
		String cacheEnPackage = cacheDto.getString("packageTarget");
		String cacheRepPackage = cacheDto.getString("packageRepo");
		
		System.out.print("Package " + (cacheEnPackage == null ? "" : "(" + cacheEnPackage + ")") + ": ");
		Scanner scanner = new Scanner(System.in);
		String packageTarget = scanner.nextLine();
		if (StringUtils.isEmpty(packageTarget)) {
			packageTarget = cacheEnPackage;
		}
		if (StringUtils.isEmpty(packageTarget)) {
			throw new CoreException("Package name is require to be filled");
		}

		validatePackage(packageTarget, "Package is invalid");
		System.out.print("Repository Package " + (cacheRepPackage == null ? "" : "(" + cacheRepPackage + ")") + ": ");
		String packageRepoName = scanner.nextLine();
		if (StringUtils.isEmpty(packageRepoName)) {
			packageRepoName = cacheRepPackage;
		}
		if (StringUtils.isEmpty(packageRepoName)) {
			throw new CoreException("Package name is require to be filled");
		}

		validatePackage(packageRepoName, "Repository Package is invalid");
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		System.out.println("----------------------------------------------------------------------");
		System.out.println("Transaction Type");
		System.out.println("----------------------------------------------------------------------");
		System.out.println("1. Add\n2. Edit\n3. Remove\n");
		System.out.print("Choose : ");
		int choosedType = scanner.nextInt();
		String transactionMode = null;
		switch (choosedType) {
		case 1:
			transactionMode = "Add";
			break;
		case 2:
			transactionMode = "Edit";
			break;
		case 3:
			transactionMode = "Remove";
			break;
		}

		System.out.print("Enable Prepare (Y/N) : ");
		boolean enablePrepare = true;
		if (cacheDto.get("enablePrepare") != null)
			enablePrepare = cacheDto.getBoolean("enablePrepare");

		if (scanner.nextLine().equalsIgnoreCase("Y"))
			enablePrepare = true;
		else if (scanner.nextLine().equalsIgnoreCase("N"))
			enablePrepare = false;
		TransactionGenerator generator = new TransactionGenerator(transactionMode, packageTarget, packageRepoName,
				srcDir, enablePrepare);
		System.out.println("----------------------------------------------------------------------");
		System.out.println("Found entity in project");
		System.out.println("----------------------------------------------------------------------");
		Dto entity;
		try {
			entity = generator.getEntity();
			int index = 0;
			for (Object element : entity.keySet()) {
				System.out.println((index + 1) + ". " + element);
				++index;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print("Entity Name : ");
		String inputEntity = scanner.nextLine();
		if (StringUtils.isEmpty(inputEntity))
			throw new CoreException("Entity name should be filled");
		List<String> inputTemp = new ArrayList<>();
		if (inputEntity.contains(",")) {
			String[] temps = inputEntity.split(",");
			for (String temp : temps) {
				inputTemp.add(temp.trim());
			}
		} else {
			inputTemp.add(inputEntity);
		}
		System.out.println("Will be generated " + inputTemp);
		for (String entityName : inputTemp) {
			generator.generate(entityName);
		}

		cacheDto.put("packageTarget", packageTarget);
		cacheDto.put("packageRepo", packageRepoName);
		cacheDto.put("enablePrepare", enablePrepare);
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
