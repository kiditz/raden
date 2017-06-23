package org.slerp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAccessorType;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.PropertySource;
import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.connection.JdbcConnection;
import org.slerp.core.CoreException;
import org.slerp.model.JdbcColumn;
import org.slerp.model.JdbcTable;
import org.slerp.utils.StringConverter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EntityGenerator implements Generator {
	public String packageName;
	public String srcDir;
	JdbcConnection connection;

	public EntityGenerator(String settingPath, String packageName, String srcDir) {
		this.connection = new JdbcConnection(settingPath);
		this.packageName = packageName;
		this.srcDir = srcDir;
		if (packageName == null)
			throw new CoreException(packageName + " cannot be null null");
		if (srcDir == null)
			throw new CoreException(srcDir + " cannot be null null");
	}

	@Override
	public String generate(JdbcTable table) {
		table = connection.getTable(table.getTableName());
		String source = generateData(table);
		File src = new File(srcDir, packageName.replace(".", "/").concat("/"));
		if (!src.isDirectory())
			src.mkdirs();
		File fileToWrite = new File(src,
				StringConverter.convertCaseSensitive(table.getTableName(), true).concat(".java"));
		FileWriter writer;
		try {
			writer = new FileWriter(fileToWrite);
			writer.write(source);
			writer.close();
			source = generateJoin(table.getColumns(), source);
			writer = new FileWriter(fileToWrite);
			writer.write(source);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return source;
	}

	private String generateData(JdbcTable table) {
		JavaClassSource cls = Roaster.create(JavaClassSource.class);
		cls.setName(StringConverter.convertCaseSensitive(table.getTableName(), true)).setPublic();
		cls.setPackage(packageName);
		cls.setPublic();
		cls.addAnnotation("javax.persistence.Entity");
		cls.addAnnotation("javax.persistence.Table").setStringValue("name", table.getTableName());
		cls.addAnnotation(JsonAutoDetect.class).setLiteralValue("creatorVisibility", "JsonAutoDetect.Visibility.NONE")
				.setLiteralValue("fieldVisibility", "JsonAutoDetect.Visibility.NONE")
				.setLiteralValue("getterVisibility", "JsonAutoDetect.Visibility.NONE")
				.setLiteralValue("isGetterVisibility", "JsonAutoDetect.Visibility.NONE")
				.setLiteralValue("setterVisibility", "JsonAutoDetect.Visibility.NONE");
		List<JdbcColumn> columns = table.getColumns();
		JavaClassSource pkCls = null;
		if (table.getPrimaryKeyCount() > 1) {
			pkCls = Roaster.create(JavaClassSource.class);
			pkCls.setPackage(packageName);
			pkCls.setName(StringConverter.convertCaseSensitive(table.getTableName(), true).concat("Id")).setPublic();
			pkCls.addAnnotation("javax.persistence.Embeddable");
			pkCls.addAnnotation(XmlAccessorType.class).setLiteralValue("XmlAccessType.NONE");
			pkCls.addAnnotation(JsonAutoDetect.class)
					.setLiteralValue("creatorVisibility", "JsonAutoDetect.Visibility.NONE")
					.setLiteralValue("fieldVisibility", "JsonAutoDetect.Visibility.NONE")
					.setLiteralValue("getterVisibility", "JsonAutoDetect.Visibility.NONE")
					.setLiteralValue("isGetterVisibility", "JsonAutoDetect.Visibility.NONE")
					.setLiteralValue("setterVisibility", "JsonAutoDetect.Visibility.NONE");
		}
		for (int i = 0; i < columns.size(); i++) {
			JdbcColumn column = columns.get(i);
			// use embeded when pk is > 1

			if (table.getPrimaryKeyCount() > 1) {
				if (column.isPrimaryKey()) {
					PropertySource<JavaClassSource> propertyPk = pkCls.addProperty(
							TypeConverter.convert(column.getColumnType()),
							StringConverter.convertCaseSensitive(column.getColumnName(), false));
					// Add JsonPropertyAnnotation
					propertyPk.getAccessor().addAnnotation(JsonProperty.class);
					FieldSource<JavaClassSource> field = propertyPk.getField();
					field.addAnnotation("javax.persistence.Column").setStringValue("name", column.getColumnName());
					if (!column.isNullAble()) {
						field.addAnnotation("javax.persistence.Basic").setLiteralValue("optional", "false");
					}
					if (column.getColumnSize() < 100) {
						field.addAnnotation("javax.persistence.Size").setLiteralValue("min", "1").setLiteralValue("max",
								String.valueOf(column.getColumnSize()));
					}
				}
			} else {
				if (column.isPrimaryKey()) {
					PropertySource<JavaClassSource> property = cls.addProperty(
							TypeConverter.convert(column.getColumnType()),
							StringConverter.convertCaseSensitive(column.getColumnName(), false));
					property.getAccessor().addAnnotation(JsonProperty.class);
					FieldSource<JavaClassSource> field = property.getField();
					field.addAnnotation("javax.persistence.Id");
					field.addAnnotation("javax.persistence.Column").setStringValue("name", column.getColumnName());
					// SEQUENCE GENERATOR
					if (column.getSequenceName() != null) {
						field.addAnnotation("javax.persistence.GeneratedValue")
								.setLiteralValue("strategy", "GenerationType.SEQUENCE")
								.setStringValue("generator", column.getSequenceName().toUpperCase());
						field.addAnnotation("javax.persistence.SequenceGenerator")
								.setStringValue("name", column.getSequenceName().toUpperCase())
								.setStringValue("sequenceName", column.getSequenceName())
								.setLiteralValue("initialValue", "1");
						// System.err.println(column.getColumnName() + ":" +
						// column.getColumnSize());
						if (column.getColumnSize() < 100) {
							field.addAnnotation("javax.validation.constraints.Size").setLiteralValue("min", "1")
									.setLiteralValue("max", String.valueOf(column.getColumnSize()));
						}
						cls.addImport("javax.persistence.GenerationType");
					}
				}
			}

			if (table.getPrimaryKeyCount() > 1 && pkCls != null) {
				PropertySource<JavaClassSource> property = cls.addProperty(pkCls.getCanonicalName(),
						Strings.uncapitalize(pkCls.getName()));
				property.getField().addAnnotation("javax.persistence.EmbeddedId");
				property.getAccessor().addAnnotation(JsonProperty.class);
			}

		}
		// Remove all columns if is primary key
		columns.removeIf(new Predicate<JdbcColumn>() {
			@Override
			public boolean test(JdbcColumn t) {
				return t.isPrimaryKey();
			}
		});
		// Re create after remove primary key
		for (JdbcColumn column : columns) {
			PropertySource<JavaClassSource> property = cls.addProperty(TypeConverter.convert(column.getColumnType()),
					StringConverter.convertCaseSensitive(column.getColumnName(), false));
			property.getAccessor().addAnnotation(JsonProperty.class);
			FieldSource<JavaClassSource> field = property.getField();
			field.addAnnotation("javax.persistence.Column").setStringValue("name", column.getColumnName());
			if (!column.isNullAble()) {
				field.addAnnotation("javax.persistence.Basic").setLiteralValue("optional", "false");
				field.addAnnotation("javax.validation.constraints.NotNull").setStringValue("message",
						packageName.concat(".").concat(StringConverter.convertCaseSensitive(table.getTableName(), true))
								.concat(".")
								.concat(StringConverter.convertCaseSensitive(column.getColumnName(), false)));

			}
			if (column.getColumnSize() < 100) {
				field.addAnnotation("javax.validation.constraints.Size").setLiteralValue("min", "1")
						.setLiteralValue("max", String.valueOf(column.getColumnSize()));
			}
		}
		return cls.toString();
	}

	private String generateJoin(List<JdbcColumn> columns, String src) {
		JavaClassSource cls = Roaster.parse(JavaClassSource.class, src);
		for (JdbcColumn column : columns) {
			if (column.getForeignKeys() != null) {
				String fkColumnName = column.getForeignKeys().getString("fkColumnName");
				String fkTableName = column.getForeignKeys().getString("fkTableName");
				String pkColumnName = column.getForeignKeys().getString("pkColumnName");
				String pkTableName = column.getForeignKeys().getString("pkTableName");
				File referenceFile = new File(srcDir, packageName.replace(".", "/").concat("/")
						.concat(StringConverter.convertCaseSensitive(pkTableName, true)).concat(".java"));

				// Make sure remove ne !
				if (referenceFile.exists()) {
					PropertySource<JavaClassSource> property = cls
							.getProperty(StringConverter.convertCaseSensitive(fkColumnName, false));
					cls.removeProperty(property);
					String fkField = StringConverter.convertCaseSensitive(fkColumnName, false);
					property = cls.addProperty(
							packageName.concat(".").concat(StringConverter.convertCaseSensitive(pkTableName, true)),
							fkField);
					property.getAccessor().addAnnotation(JsonProperty.class);
					FieldSource<JavaClassSource> field = property.getField();
					// @JoinColumn(name = "category_id", referencedColumnName =
					// "id")
					field.addAnnotation("javax.persistence.ManyToOne").setLiteralValue("optional", "false");
					field.addAnnotation("javax.persistence.JoinColumn").setStringValue("name", fkColumnName)
							.setStringValue("referencedColumnName",
									StringConverter.convertCaseSensitive(pkColumnName, false));
					try {
						JavaClassSource refCls = Roaster.parse(JavaClassSource.class, referenceFile);
						String className = packageName.concat(".")
								.concat(StringConverter.convertCaseSensitive(fkTableName, true));
						PropertySource<JavaClassSource> fkProperty = refCls.addProperty(
								"java.util.List<" + className + ">",
								StringConverter.convertCaseSensitive(fkTableName, false) + "List");
						fkProperty.getField().addAnnotation("javax.persistence.OneToMany")
								.setLiteralValue("cascade", "CascadeType.ALL").setStringValue("mappedBy", fkField);
						refCls.addImport("javax.persistence.CascadeType");
						FileWriter writer = new FileWriter(referenceFile);
						writer.write(refCls.toString());
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		return cls.toString();
	}

	public static void main(String[] args) {
		EntityGenerator generator = new EntityGenerator("src/main/resources/slerp.properties", "com.slerp.entity",
				"../slerp-base/src/main/java");
		generator.generate(new JdbcTable("category"));
		generator.generate(new JdbcTable("product"));
	}
}
