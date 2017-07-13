package org.slerp.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.connection.ConnectionUtils.Setting;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.core.utils.StreamUtils;
import org.slerp.model.JdbcColumn;
import org.slerp.model.JdbcTable;

public class JdbcConnection {
	static private final Properties properties = new Properties();
	static final String TABLE_NAME = "TABLE_NAME";
	static final String COLUMN_NAME = "COLUMN_NAME";
	static final String TYPE_NAME = "TYPE_NAME";
	static final String COLUMN_SIZE = "COLUMN_SIZE";
	static final String FK_COLUMN_NAME = "FK" + COLUMN_NAME;
	static final String FK_TABLE_NAME = "FK" + TABLE_NAME;
	static final String IS_NULLABLE = "IS_NULLABLE";
	private DatabaseMetaData metaData = null;
	Connection connection;
	private Setting setting = new Setting();

	public JdbcConnection(String settingPath) {
		File file = new File(settingPath);
		InputStream stream = null;

		try {
			stream = new FileInputStream(file);
			properties.load(stream);
			this.setting.pathToJar = properties.getProperty("slerp.database.path");
			this.setting.driverClassName = properties.getProperty("spring.database.driverClassName").trim();
			this.setting.url = properties.getProperty("spring.datasource.url").trim();
			this.setting.username = properties.getProperty("spring.datasource.username").trim();
			this.setting.password = properties.getProperty("spring.datasource.password").trim();
			this.connection = getConnection();
		} catch (IOException e) {
			throw new CoreException(e.getMessage());
		} finally {
			StreamUtils.close(stream);
		}
		try {
			printMetaData();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

<<<<<<< HEAD
	public Connection getConnection() {
=======
	private Connection getConnection() {
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5

		Connection connection = null;
		try {
			if (Strings.isNullOrEmpty(setting.pathToJar)) {
				Class.forName(setting.driverClassName);
				connection = DriverManager.getConnection(setting.url, setting.username, setting.password);
				this.metaData = connection.getMetaData();
			} else {
				connection = ConnectionUtils.getConnection(setting);
				this.metaData = connection.getMetaData();
			}
			System.out.println(connection == null ? "connection fail" : "connection success");
		} catch (Exception e) {
			throw new CoreException(e.getMessage(), e);
		}
		return connection;
	}

	public List<JdbcTable> getTables() {
		String types[] = { "TABLE" };
		try {
			ResultSet rs = metaData.getTables(null, null, null, types);

			List<JdbcTable> tables = new ArrayList<>();
			while (rs.next()) {
				String tableName = rs.getString(TABLE_NAME);
				tables.add(new JdbcTable(tableName));
			}
			for (JdbcTable table : tables) {
				List<JdbcColumn> columns = getColumns(table);
				table.setColumns(columns);
				table.setPrimaryKeyCount(countPrimaryKey(columns));

			}
			return tables;
		} catch (Exception e) {
			throw new CoreException(e);
		}
	}

	public JdbcTable getTable(String name) {
		List<JdbcTable> tables = getTables();
		for (JdbcTable table : tables) {
			if (table.getTableName().equalsIgnoreCase(name))
				return table;
		}
		throw new CoreException("Failed to find table with name " + name);
	}

	private List<JdbcColumn> getColumns(JdbcTable table) throws SQLException {

		ResultSet rs = metaData.getColumns(null, null, table.getTableName(), null);
		ResultSet rsPrimary = metaData.getPrimaryKeys(null, null, table.getTableName());
		ResultSet rsForeignKey = metaData.getImportedKeys(connection.getCatalog(), null, table.getTableName());
		// System.err.println(table.getTableName() + ":" +
		// getSequenceByTableName(table.getTableName()));

		List<JdbcColumn> columns = new ArrayList<>();
		while (rs.next()) {
			String isNullAble = rs.getString(IS_NULLABLE);
			JdbcColumn column = new JdbcColumn(rs.getString(COLUMN_NAME), rs.getString(TYPE_NAME),
					rs.getInt(COLUMN_SIZE));
			column.setNullAble(isNullAble.equals("YES") ? true : false);

			columns.add(column);
		}
		String sequenceName = getSequenceByTableName(table.getTableName());
		while (rsPrimary.next()) {
			String columnName = rsPrimary.getString(COLUMN_NAME);
			for (JdbcColumn jdbcColumn : columns) {
				if (jdbcColumn.getColumnName().equals(columnName)) {
					jdbcColumn.setPrimaryKey(true);
					if (sequenceName != null) {
						jdbcColumn.setSequenceName(sequenceName);
					}
				}

			}
			// System.err.println(columnName + keySeq);
		}

		while (rsForeignKey.next()) {
			String fkName = rsForeignKey.getString("FKCOLUMN_NAME");
			String pkName = rsForeignKey.getString("PKCOLUMN_NAME");
			String pkTableName = rsForeignKey.getString("PKTABLE_NAME");
			String fkTableNAme = rsForeignKey.getString("FKTABLE_NAME");

			for (JdbcColumn jdbcColumn : columns) {
				if (jdbcColumn.getColumnName().equals(fkName)) {
					Dto foreignKey = new Dto();
					foreignKey.put("fkColumnName", fkName);
					foreignKey.put("pkColumnName", pkName);
					foreignKey.put("pkTableName", pkTableName);
					foreignKey.put("fkTableName", fkTableNAme);
					jdbcColumn.setForeignKeys(foreignKey);
				}
			}
		}
		return columns;
	}

	private int countPrimaryKey(List<JdbcColumn> columns) {
		int countCol = 0;
		for (JdbcColumn jdbcColumn : columns) {
			if (jdbcColumn.isPrimaryKey())
				countCol++;
		}
		return countCol;
	}

	private void printMetaData() throws SQLException, ClassNotFoundException {
		System.out.println("Product Name\t: " + metaData.getDatabaseProductName());
		System.out.println("Product Version\t: " + metaData.getDatabaseProductVersion());
		System.out.println("Driver Name\t: " + metaData.getDriverName());
		System.out.println("Login User\t: " + metaData.getUserName());
		System.out.println();
	}

	public static void main(String[] args) {
		JdbcConnection connection = new JdbcConnection("src/main/resources/slerp.properties");
		Dto dto = new Dto();
		dto.put("tables", connection.getTable("category"));
		System.out.println(dto);
	}

	/**
	 * The database table name should to be contains with sequence name if is
	 * not the result will be null
	 * 
	 * @return sequenceName
	 */
	private String getSequenceByTableName(String tableName) throws SQLException {
		ResultSet rsSequence = metaData.getTables(null, null, null, new String[] { "SEQUENCE" });
		while (rsSequence.next()) {
			String seq = rsSequence.getString(TABLE_NAME);
			if (seq.contains(tableName)) {
				return seq;
			}
		}
		return null;
	}
}
