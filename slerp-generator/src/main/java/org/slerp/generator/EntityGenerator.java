package org.slerp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
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
	public String packageRepoName;
	public String srcDir;
	private JdbcConnection connection;

	public EntityGenerator(String settingPath, String packageName, String packageRepoName, String srcDir) {
		this.connection = new JdbcConnection(settingPath);
		this.packageName = packageName;
		this.srcDir = srcDir;
		if (packageName == null)
			throw new CoreException(packageName + " cannot be null null");
		if (srcDir == null)
			throw new CoreException(srcDir + " cannot be null null");
		if (packageRepoName == null)
			this.packageRepoName = packageName.substring(0, packageName.lastIndexOf('.')).concat(".repo");
		else
			this.packageRepoName = packageRepoName;
	}

	@Override
	public void generate(String tableName) {
		JdbcTable table = connection.getTable(tableName);

		String source = generateEntity(table);

		File src = new File(srcDir, packageName.replace(".", "/").concat("/"));

		if (!src.isDirectory())
			src.mkdirs();

		File fileToWrite = new File(src,
				StringConverter.convertCaseSensitive(table.getTableName(), true).concat(".java"));
		FileWriter writer;
		try {

			writer = new FileWriter(fileToWrite);
			System.out.println("Generator successfully create " + StringConverter.getFilename(fileToWrite));
			writer.write(source);
			writer.close();
			source = generateJoin(table.getColumns(), source);
			writer = new FileWriter(fileToWrite);
			writer.write(source);
			writer.close();
			String repository = generateRepository(table);
			File srcRepo = new File(srcDir, packageRepoName.replace(".", "/").concat("/"));
			if (!srcRepo.isDirectory())
				srcRepo.mkdirs();

			fileToWrite = new File(srcRepo,
					StringConverter.convertCaseSensitive(table.getTableName(), true) + "Repository".concat(".java"));
			writer = new FileWriter(fileToWrite);
			writer.write(repository);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Generator successfully create " + StringConverter.getFilename(fileToWrite));
		// System.out.println("At " + fileToWrite.getAbsolutePath());

	}

	private String generateEntity(JdbcTable table) {

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
		cls.addAnnotation(XmlAccessorType.class).setLiteralValue("XmlAccessType.NONE");
		cls.addImport(XmlAccessType.class);

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

					if ((column.getColumnType().equalsIgnoreCase("varchar")
							|| column.getColumnType().equalsIgnoreCase("bpchar")
							|| column.getColumnType().equalsIgnoreCase("text")) && column.getColumnSize() < 100) {
						field.addAnnotation("javax.validation.constraints.Size").setLiteralValue("min", "1")
								.setLiteralValue("max", String.valueOf(column.getColumnSize()));
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
								.setLiteralValue("initialValue", "1").setLiteralValue("allocationSize", "1");
						// System.err.println(column.getColumnName() + ":" +
						// column.getColumnSize());
						if ((column.getColumnType().equalsIgnoreCase("varchar")
								|| column.getColumnType().equalsIgnoreCase("bpchar")
								||column.getColumnType().equalsIgnoreCase("text")) && column.getColumnSize() < 100) {
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
			if ((column.getColumnType().equalsIgnoreCase("varchar") || column.getColumnType().equalsIgnoreCase("bpchar")
					|| column.getColumnType().equalsIgnoreCase("text")) && column.getColumnSize() < 100) {
				field.addAnnotation("javax.validation.constraints.Size").setLiteralValue("min", "1")
						.setLiteralValue("max", String.valueOf(column.getColumnSize()));
			}
			if (column.getColumnType().equalsIgnoreCase("timestamp")
					|| column.getColumnType().equalsIgnoreCase("timestamptz")) {
				field.addAnnotation("javax.persistence.Temporal").setLiteralValue("TemporalType.TIMESTAMP");
				cls.addImport("javax.persistence.TemporalType");
			} else if (column.getColumnType().equalsIgnoreCase("date")) {
				field.addAnnotation("javax.persistence.Temporal").setLiteralValue("TemporalType.DATE");
				pkCls.addImport("javax.persistence.TemporalType");
			} else if (column.getColumnType().equalsIgnoreCase("time")) {
				field.addAnnotation("javax.persistence.Temporal").setLiteralValue("TemporalType.TIME");
				cls.addImport("javax.persistence.TemporalType");
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
					field.addAnnotation("javax.persistence.ManyToOne").setLiteralValue("optional", "false");
					field.addAnnotation("javax.persistence.JoinColumn").setStringValue("name", fkColumnName)
							.setStringValue("referencedColumnName", pkColumnName);
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

	private String generateRepository(JdbcTable table) {
		table = connection.getTable(table.getTableName());
		String className = StringConverter.convertCaseSensitive(table.getTableName(), true);
		JavaInterfaceSource iCls = Roaster.create(JavaInterfaceSource.class);
		iCls.addImport(packageName.concat(".").concat(className));
		Class<?> primaryKeyDataType = TypeConverter.convert(getColumnByPK(table.getColumns()).getColumnType());
		iCls.setPackage(packageRepoName).setName(className.concat("Repository")).setPublic();
		iCls.addInterface("org.springframework.data.jpa.repository.JpaRepository<"
				+ packageName.concat(".").concat(className) + ", " + primaryKeyDataType.getName() + ">");

		return iCls.toString();
	}

	public JdbcColumn getColumnByPK(List<JdbcColumn> columns) {
		for (JdbcColumn column : columns) {
			if (column.isPrimaryKey())
				return column;
		}
		return null;
	}

	public JdbcConnection getConnection() {
		return connection;
	}

	public static void main(String[] args) {
		EntityGenerator generator = new EntityGenerator("src/main/resources/slerp.properties",
				"org.slerp.ecomerce.entity", null, "/home/kiditz/apps/framework/slerp-ecomerce/src/main/java");

		generator.generate("product");
		generator.generate("category");
	}
}