package org.slerp.project;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Setup {
	static enum ProjectType {
		SERVICE, API, DISCOVERY,
	}
	private String springBootVersion;
	private String slerpVersion;
	public static void execute(Configuration configuration, ProjectType type) {
		configuration.outputDir = configuration.outputDir.concat("/").concat(configuration.artifactId);
		String packageDir = configuration.groupId.replace('.', '/');
		Project project = new Project();

		project.files.add(new ProjectFile("src/main/java/Application",
				"src/main/java/" + packageDir + "/Application.java", true));
		if (type == ProjectType.SERVICE) {
			project.files.add(new ProjectFile("service/pom.xml"));
			project.files.add(new ProjectFile("src/main/resources/readme.txt"));
			project.files.add(new ProjectFile("src/test/resources/application.properties", false));
			project.files.add(new ProjectFile("src/test/resources/applicationContext.xml"));
			project.files.add(new ProjectFile("src/test/resources/applicationContext-persistence.xml"));
			// Entity Dir
			File entityDir = new File(configuration.outputDir,
					"src/main/java/" + packageDir.concat("/").concat("entity"));
			if (!entityDir.isDirectory() && !entityDir.mkdirs())
				throw new RuntimeException("Cannot entity generate package");
			// Repository Dir
			File repositoryDir = new File(configuration.outputDir,
					"src/main/java/" + packageDir.concat("/").concat("repository"));
			if (!repositoryDir.isDirectory() && !repositoryDir.mkdirs())
				throw new RuntimeException("Cannot repository generate package");
			// Service Dir
			File serviceDir = new File(configuration.outputDir,
					"src/main/java/" + packageDir.concat("/").concat("service"));
			if (!serviceDir.isDirectory() && !serviceDir.mkdirs())
				throw new RuntimeException("Cannot service generate package");
		} else if (type == ProjectType.API) {
			project.files.add(new ProjectFile("api/pom.xml"));
		}
		Map<String, String> values = new HashMap<>();
		values.put("${ARTIFACT_ID}", configuration.artifactId);
		values.put("${GROUP_ID}", configuration.groupId);
		values.put("${VERSION}", configuration.version);
		
		FileSetup.copyAndReplace(configuration.outputDir, project, values);
	}
	private Map<String, String> getProductVersion(){
		
		return null;
	}
	public static class Configuration {
		public String outputDir;
		public String groupId;
		public String artifactId;
		public String version;
	}

	public static void main(String[] args) {		
		Configuration configuration = new Configuration();
		configuration.outputDir = "/home/kiditz/apps/framework/test";
		configuration.groupId = "org.slerp.product";
		configuration.artifactId = "product-service";
		configuration.version = "1.0";		
		execute(configuration, ProjectType.SERVICE);
	}
}
