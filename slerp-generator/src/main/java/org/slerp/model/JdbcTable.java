package org.slerp.model;

import java.io.Serializable;
import java.util.List;

public class JdbcTable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String tableName;
	private List<JdbcColumn> columns;
	private int primaryKeyCount;

	public JdbcTable(String tableName) {
		super();
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getPrimaryKeyCount() {
		return primaryKeyCount;
	}

	public void setPrimaryKeyCount(int primaryKeyCount) {
		this.primaryKeyCount = primaryKeyCount;
	}

	public List<JdbcColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<JdbcColumn> columns) {
		this.columns = columns;
	}

	@Override
	public String toString() {
		return "[tableName=" + tableName + ", columns=" + columns + ", primaryKeyCount=" + primaryKeyCount + "]";
	}

}
