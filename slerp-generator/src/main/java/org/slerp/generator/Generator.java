package org.slerp.generator;

import java.io.IOException;
import java.util.Properties;

import org.slerp.core.CoreException;
import org.slerp.model.JdbcTable;

public interface Generator {
	public String generate(JdbcTable table);

	static public class TypeConverter {
		static Properties properties = new Properties();
		static {
			try {
				properties.load(Generator.class.getResourceAsStream("/org/slerp/utils/datatype.properties"));
			} catch (IOException e) {
				throw new CoreException(e);
			}
		}

		public static Class<?> convert(String sqlType) {
			// System.err.println("Sql Type " + sqlType);

			try {
				if (properties.getProperty(sqlType) != null)
					return Class.forName(properties.getProperty(sqlType));
				else
					return Object.class;
			} catch (ClassNotFoundException e) {
				throw new CoreException(e);
			}
		}
	}
}
