package org.slerp.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.slerp.core.ConcurentDto;

public class EntityUtils {
	static private ConcurentDto inputEntity = new ConcurentDto();
	static private ConcurentDto inputBusiness = new ConcurentDto();

	public static ConcurentDto readEntities(File baseDir) throws IOException {
		File[] listFile = baseDir.listFiles();
		if (listFile == null || listFile.length == 0)
			return null;

		for (File file : listFile) {
			if (file.isDirectory()) {
				readEntities(file);
			} else {
				if (StringConverter.getExtension(file).equals("java")) {
					if (isEntity(file)) {
						// Dto entityDto = new Dto();
						inputEntity.put(StringConverter.getFilename(file), file.getAbsolutePath());

					}
				}
			}
		}
		return inputEntity;
	}

	public static ConcurentDto readBusiness(File baseDir) throws IOException {
		File[] listFile = baseDir.listFiles();
		if (listFile == null || listFile.length == 0)
			return null;
		for (File file : listFile) {
			if (file.isDirectory()) {
				readBusiness(file);
			} else {
				if (StringConverter.getExtension(file).equals("java")) {
					if (isBo(file)) {
						// System.err.println("Path Entity Utils " +
						// file.getAbsolutePath());

						inputBusiness.put(StringConverter.getFilename(file), file.getAbsolutePath());
					}
				}
			}
		}
		return inputBusiness;
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

	private static boolean isBo(File file) throws IOException {
		try {
			JavaClassSource cls = Roaster.parse(JavaClassSource.class, file);
			if (cls.getSuperType().equals("org.slerp.core.business.DefaultBusinessTransaction")
					|| cls.getSuperType().equals("org.slerp.core.business.DefaultBusinessFunction")) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;

	}

	public static ConcurentDto readEntityAsDto(File entityFile) throws IOException {
		JavaClassSource cls = Roaster.parse(JavaClassSource.class, entityFile);
		List<FieldSource<JavaClassSource>> fields = cls.getFields();
		ConcurentDto classDto = new ConcurentDto();
		classDto.put("className", cls.getName());
		classDto.put("packageName", cls.getPackage());
		List<ConcurentDto> fieldsDto = new ArrayList<>();
		for (FieldSource<JavaClassSource> field : fields) {
			ConcurentDto fieldDto = new ConcurentDto();
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
		ConcurentDto input = EntityUtils
				.readBusiness(new File("/home/kiditz/apps/framework/slerp-ecommerce-service/src/main/java/"));
		System.err.println(input.toString());
		for (Object obj : input.values()) {
			System.err.println(obj);
		}
	}
}
