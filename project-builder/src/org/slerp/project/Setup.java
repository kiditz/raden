package org.slerp.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Setup {
	public static enum ProjectType {
		SERVICE, API, DISCOVERY,
	};

	public static void execute(Configuration configuration, ProjectType type) {
		configuration.outputDir = configuration.outputDir.concat("/").concat(configuration.artifactId);
		String packageDir = configuration.groupId.replace('.', '/');
		Project project = new Project();

		if (type == ProjectType.SERVICE) {
			project.files.add(
					new ProjectFile("service/Application", "src/main/java/" + packageDir + "/Application.java", true));
			project.files.add(new ProjectFile("service/pom.xml", "pom.xml", true));
			project.files.add(new ProjectFile("src/main/resources/readme.txt"));
			project.files.add(new ProjectFile("src/test/resources/application.properties", false));
			project.files.add(new ProjectFile("src/test/resources/applicationContext.xml"));
			project.files.add(new ProjectFile("src/test/resources/applicationContext-persistence.xml"));
			project.files.add(new ProjectFile("src/main/resources/generator.cache"));
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
			project.files.add(new ProjectFile("api/pom.xml", "pom.xml", true));
			project.files
					.add(new ProjectFile("api/Application", "src/main/java/" + packageDir + "/Application.java", true));
			project.files.add(new ProjectFile("api/RepositoryConfiguration",
					"src/main/java/" + packageDir + "/RepositoryConfiguration.java", true));
			project.files.add(
					new ProjectFile("api/application.properties", "src/main/resources/application.properties", false));
		}
		Map<String, String> values = new HashMap<>();
		values.put("${ARTIFACT_ID}", configuration.artifactId);
		values.put("${GROUP_ID}", configuration.groupId);
		values.put("${VERSION}", configuration.version);
		// HANDLE VERSIONING
		File outVersion = new File(System.getProperty("user.home").concat("/.slerp/version.txt"));
		if (!outVersion.getParentFile().isDirectory())
			outVersion.getParentFile().mkdirs();
		if (!outVersion.exists()) {
			FileSetup.writeFile(outVersion, "slerp.version=1.0.0\nspring.version=1.4.7.RELEASE".getBytes());
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outVersion)));
			Map<String, String> existVersion = convertToMap(reader);
			Map<String, String> netVersion = getProductVersionFromNet(10000);
			if (netVersion != null) {
				values.put("${SPRING-VERSION}", netVersion.get("spring.version"));
				values.put("${SLERP-VERSION}", netVersion.get("slerp.version"));
				FileSetup.writeFile(outVersion, "slerp.version=" + netVersion.get("slerp.version") + "\nspring.version="
						+ netVersion.get("spring.version").getBytes());
			} else {
				values.put("${SPRING-VERSION}", existVersion.get("spring.version"));
				values.put("${SLERP-VERSION}", existVersion.get("slerp.version"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileSetup.copyAndReplace(configuration.outputDir, project, values);
	}

	private static Map<String, String> convertToMap(BufferedReader reader) throws IOException {
		if (reader == null)
			return null;
		Map<String, String> map = new HashMap<>();
		map.put("slerp.version", reader.readLine().split("=")[1]);
		map.put("spring.version", reader.readLine().split("=")[1]);
		return map;
	}

	private static Map<String, String> getProductVersionFromNet(int timeout) {
		try {
			URL url = new URL("https://raw.githubusercontent.com/kiditz/slerp/framework/version.txt");
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(timeout);
			c.setReadTimeout(timeout);
			c.connect();
			int status = c.getResponseCode();
			if (status != 200)
				return null;
			BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
			return convertToMap(br);
		} catch (Exception e) {
			return null;
		}
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
		configuration.artifactId = "product-api";
		configuration.version = "1.0";
		execute(configuration, ProjectType.API);

	}
}
