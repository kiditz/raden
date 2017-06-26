package org.slerp.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.slerp.core.Dto;

public class EntityUtils {
	static Dto input = new Dto();

	public static Dto readEntities(File baseDir) throws IOException {

		File[] listFile = baseDir.listFiles();
		for (File file : listFile) {
			if (file.isDirectory()) {
				readEntities(file);
			} else {
				if (StringConverter.getExtension(file).equals("java")) {
					if (isEntity(file)) {
						// Dto entityDto = new Dto();
						input.put(StringConverter.getFilename(file), file.getAbsolutePath());

					}
				}
			}
		}
		return input;
	}

	private static boolean isEntity(File file) throws IOException {
		try {
			JavaClassSource cls = Roaster.parse(JavaClassSource.class, file);
			if (cls.hasAnnotation("javax.persistence.Entity")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static Dto readEntityAsDto(File entityFile) throws IOException {
		JavaClassSource cls = Roaster.parse(JavaClassSource.class, entityFile);
		List<FieldSource<JavaClassSource>> fields = cls.getFields();
		Dto classDto = new Dto();
		classDto.put("className", cls.getName());
		classDto.put("packageName", cls.getPackage());
		List<Dto> fieldsDto = new ArrayList<>();
		for (FieldSource<JavaClassSource> field : fields) {
			Dto fieldDto = new Dto();
			fieldDto.put("fieldName", field.getName());
			fieldDto.put("fieldType", field.getType().getQualifiedName());

			if (field.hasAnnotation("javax.validation.constraints.NotNull")) {
				fieldDto.put("isNull", false);
			} else {
				fieldDto.put("isNull", true);
			}
			if (field.hasAnnotation("javax.persistence.Id")) {
				fieldDto.put("isPrimaryKey", true);
			} else {
				fieldDto.put("isPrimaryKey", false);
			}
			String fieldType = field.getType().getQualifiedName();
			if (fieldType.equals("java.lang.String")) {
				fieldDto.put("isString", true);
			} else
				fieldDto.put("isString", false);
			if (fieldType.equals("java.lang.Long") || fieldType.equals("java.lang.Integer")
					|| fieldType.equals("java.lang.Short") || fieldType.equals("java.lang.Float")
					|| fieldType.equals("java.lang.Double")) {
				fieldDto.put("isNumber", true);
			} else {
				fieldDto.put("isNumber", false);
			}
			if (field.hasAnnotation("javax.persistence.JoinColumn")) {
				fieldDto.put("isJoin", true);
			} else {
				fieldDto.put("isJoin", false);
			}
			if (field.hasAnnotation("javax.persistence.OneToMany")) {
				fieldDto.put("isForeignKey", true);
			} else {
				fieldDto.put("isForeignKey", false);
			}
			fieldsDto.add(fieldDto);
		}
		return classDto.put("fields", fieldsDto);
	}

	public static void main(String[] args) throws IOException {
		Dto input = EntityUtils.readEntities(new File("/home/kiditz/apps/framework/slerp-ecomerce/src/main/java/"));
		System.err.println(input);
	}
}
