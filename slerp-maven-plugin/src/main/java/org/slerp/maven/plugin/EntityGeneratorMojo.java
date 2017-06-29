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
import org.slerp.generator.EntityGenerator;
import org.slerp.model.JdbcTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Mojo(name = "entity", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class EntityGeneratorMojo extends AbstractMojo {
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
		System.out.println("Entity Generator");
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
		String cacheEnPackage = cacheDto.getString("packageEntity");
		String cacheRepPackage = cacheDto.getString("packageRepo");

		System.out.print("Entity Package " + (cacheEnPackage == null ? "" : "(" + cacheEnPackage + ")") + ": ");
		Scanner scanner = new Scanner(System.in);
		String packageName = scanner.nextLine();
		if (StringUtils.isEmpty(packageName)) {
			packageName = cacheEnPackage;
		}
		if (StringUtils.isEmpty(packageName)) {
			throw new CoreException("Package name is require to be filled");
		}

		validatePackage(packageName, "Entity Package is invalid");
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
		EntityGenerator entityGenerator = new EntityGenerator(properties.getAbsolutePath(), packageName,
				packageRepoName, srcDir.getAbsolutePath());
		List<JdbcTable> tables = entityGenerator.getConnection().getTables();
		String[] tablesName = new String[tables.size()];
		for (int i = 0; i < tables.size(); i++) {
			JdbcTable table = tables.get(i);
			tablesName[i] = table.getTableName();
		}

		try {
			String json = mapper.writeValueAsString(tablesName);
			System.out.println(json);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.print("Tables : ");
		String tablesInput = scanner.nextLine();
		scanner.close();
		List<String> tablesTemp = new ArrayList<String>();
		if (tablesInput.contains(",")) {
			String[] inputs = tablesInput.split(",");
			for (String table : inputs) {
				tablesTemp.add(table.trim());
			}
		} else {
			tablesTemp.add(tablesInput.trim());
		}
		System.out.println("Will Generate : " + tablesTemp.toString());
		for (String table : tablesTemp) {
			entityGenerator.generate(table);
		}
		cacheDto.put("packageEntity", packageName);
		cacheDto.put("packageRepo", packageRepoName);
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
