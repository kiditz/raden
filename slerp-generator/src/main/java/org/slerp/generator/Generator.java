package org.slerp.generator;

import java.util.Properties;

import org.slerp.core.CoreException;

public interface Generator {
	
	
	public void generate(String tableName);

	static public class TypeConverter {
		static Properties properties = new Properties();

		static {
			try {
				properties.put("varchar", "java.lang.String");
				properties.put("bpchar", "java.lang.String");
				properties.put("text", "java.lang.String");
				properties.put("bytea", "[B");

				properties.put("int8", "java.lang.Long");
				properties.put("bigserial", "java.lang.Long");

				properties.put("int4", "java.lang.Integer");
				properties.put("int2", "java.lang.Short");
				properties.put("numeric", "java.math.BigDecimal");
				properties.put("float4", "java.lang.Float");
				properties.put("float8", "java.lang.Double");
				properties.put("timestamp", "java.util.Date");
				properties.put("timestamptz", "java.util.Date");
				properties.put("date", "java.util.Date");
				properties.put("time", "java.util.Date");
				properties.put("boolean", "java.lang.Boolean");
				properties.put("bool", "java.lang.Boolean");
			} catch (Exception e) {
				throw new CoreException(e);
			}
		}

		public static Class<?> convert(String sqlType) {
			//System.err.println("Sql Type " + sqlType);

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
