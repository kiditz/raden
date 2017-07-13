package org.slerp.connection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtils {
	public static class Setting {
		public String pathToJar;
		public String driverClassName;
		public String url;
		public String username;
		public String password;

		@Override
		public String toString() {
			return "Setting [pathToJar=" + pathToJar + ", driverClassName=" + driverClassName + ", url=" + url
					+ ", username=" + username + ", password=" + password + "]";
		}

	}

	public static Connection getConnection(Setting setting) {
		URL u;
		try {
			u = new File(setting.pathToJar).toURI().toURL();

			String classname = setting.driverClassName;
			System.out.println(classname);
			URLClassLoader ucl = new URLClassLoader(new URL[] { u });
			Driver d = (Driver) Class.forName(classname, true, ucl).newInstance();
			DriverManager.registerDriver(new DriverShim(d));
			return DriverManager.getConnection(setting.url, setting.username, setting.password);
		} catch (MalformedURLException e) {
			throwRuntimeException(e.getMessage());
		} catch (InstantiationException e) {
			throwRuntimeException(e.getMessage());
		} catch (IllegalAccessException e) {
			throwRuntimeException(e.getMessage());
		} catch (ClassNotFoundException e) {
		} catch (SQLException e) {
			throwRuntimeException(e.getMessage());
		}
		return null;
	}

	private static void throwRuntimeException(String message) {
		throw new RuntimeException(message);
	}

	public static void main(String[] args) throws Exception {
		String file = "/home/kiditz/.m2/repository/org/postgresql/postgresql/9.4.1212.jre7/postgresql-9.4.1212.jre7.jar";
		Setting setting = new Setting();
		setting.pathToJar = file;
		setting.url = "jdbc:postgresql://localhost:5432/product-service";
		setting.driverClassName = "org.postgresql.Driver";
		setting.username = "postgres";
		setting.password = "rioters7";
		try {
			Connection connection = getConnection(setting);
			System.err.println(connection == null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
