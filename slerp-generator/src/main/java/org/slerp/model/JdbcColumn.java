package org.slerp.model;

import java.io.Serializable;

import org.slerp.core.Dto;

public class JdbcColumn implements Serializable {
	private static final long serialVersionUID = 1L;
	private String columnName;
	private String sequenceName;
	private boolean primaryKey;
	private String columnType;
	private int columnSize;
	private boolean nullAble;
	private Dto foreignKeys;

	public JdbcColumn(String columnName, String columnType, int columnSize) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.columnSize = columnSize;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public int getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}
	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Dto getForeignKeys() {
		return foreignKeys;
	}

	public void setForeignKeys(Dto foreignKeys) {
		this.foreignKeys = foreignKeys;
	}

	public boolean isNullAble() {
		return nullAble;
	}

	public void setNullAble(boolean nullAble) {
		this.nullAble = nullAble;
	}

	@Override
	public String toString() {
		return "[columnName=" + columnName + ", primaryKey=" + primaryKey + ", columnType=" + columnType
				+ ", columnSize=" + columnSize + "]";
	}
}
