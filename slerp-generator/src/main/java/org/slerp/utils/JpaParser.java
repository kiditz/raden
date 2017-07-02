package org.slerp.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;

public class JpaParser {
	private final File businessFile;
	private String packageRepo = null;
	private String packageEntity = null;
	private Set<Dto> fields = ConcurrentHashMap.newKeySet();
	private boolean useEntity = false;
	private Dto businessDto;

	public JpaParser(File businessFile, String packageEntity, String packageRepo) {
		this.businessFile = businessFile;
		this.packageRepo = packageRepo;
		this.packageEntity = packageEntity;
	}

	public Dto parse() throws IOException {
		List<Dto> tempFields = new ArrayList<>();
		JavaClassSource cls = Roaster.parse(JavaClassSource.class, businessFile);
		String repositoryPath = businessFile.getAbsolutePath();
		StringBuffer buffer = new StringBuffer();
		repositoryPath = repositoryPath.substring(0, repositoryPath.indexOf("java") + 4).concat("/")
				.concat(packageRepo.replace(".", "/")).concat("/");
		buffer.append(repositoryPath);
		for (FieldSource<JavaClassSource> field : cls.getFields()) {
			if (cls.getName().contains(field.getType().getName().replace("Repository", ""))) {
				String repoName = field.getType().getName();
				buffer.append(repoName.concat(".java"));
			}
		}
		File repositoryFile = new File(buffer.toString());
		if (!repositoryFile.exists())
			throw new CoreException("Cannot found repository for business " + cls.getName());
		JavaInterfaceSource repoInf = Roaster.parse(JavaInterfaceSource.class, repositoryFile);
		String jpaInfo = repoInf.getInterfaces().get(0);

		businessDto = new Dto();

		if (isUseEntity()) {
			String entityInfo = jpaInfo.substring(jpaInfo.indexOf("<") + 1, jpaInfo.indexOf(","));
			String entityType = jpaInfo.substring(jpaInfo.indexOf(',') + 1, jpaInfo.indexOf('>'));
			String entityPath = businessFile.getAbsolutePath().concat("/")
					.concat(packageEntity.replace(".", "/").concat("/"));
			entityPath = entityPath.substring(0, entityPath.indexOf("java") + 4);
			Dto files = EntityUtils.readEntities(new File(entityPath));
			File entityFile = new File(files.getString(entityInfo));
			Dto entity = EntityUtils.readEntityAsDto(entityFile);
			List<Dto> fieldsDto = Collections.synchronizedList(entity.getList("fields"));
			// Get Temp FROM JOIN
			for (Dto field : fieldsDto) {
				if (field.getBoolean("isJoin")) {
					String type = field.getString("fieldType");
					String simpleType = type.substring(type.lastIndexOf('.') + 1, type.length());
					File file = new File(entityFile.getParent(), simpleType.concat(".java"));
					Dto entityDto = EntityUtils.readEntityAsDto(file);
					tempFields = entityDto.getList("fields");
				}
			}
			if (!tempFields.isEmpty()) {
				for (int j = 0; j < fieldsDto.size(); j++) {
					Dto temp = getFieldByName(tempFields, fieldsDto.get(j).getString("fieldName"));
					if (temp != null) {
						fieldsDto.remove(j);
						temp.put("isJoin", true);
					}
				}
				fieldsDto.addAll(tempFields);
			}
			entity.put("fields", fieldsDto);
			businessDto.put("entity", entity);
			String[] keyValidations = cls.getAnnotation("org.slerp.core.validation.KeyValidation")
					.getStringArrayValue();
			for (int i = 0; i < keyValidations.length; i++) {
				Dto field = getFieldByName(entity.getList("fields"), keyValidations[i]);
				if (field == null) {
					field = new Dto();
					field.put("fieldName", keyValidations[i]);
					field.put("fieldType", "java.lang.Object");
					field.put("isNull", true);
					field.put("isPrimaryKey", false);
					field.put("isString", false);
					field.put("isNumber", false);
					field.put("isJoin", false);
					field.put("isForeignKey", false);
				}
				getFields().add(field);
			}
			businessDto.put("entityType", entityType.trim());
		}

		if (cls.getSuperType().equals("org.slerp.core.business.DefaultBusinessTransaction")) {
			businessDto.put("mode", "transaction");
		} else if (cls.getSuperType().equals("org.slerp.core.business.DefaultBusinessFunction")) {
			businessDto.put("mode", "function");
		}

		businessDto.put("packageName", cls.getPackage());
		businessDto.put("className", cls.getName());

		Dto repositoryDto = new Dto();
		repositoryDto.put("packageName", repoInf.getPackage());
		repositoryDto.put("className", repoInf.getName());
		businessDto.put("repository", repositoryDto);

		return businessDto;
	}

	public Set<Dto> getFields() {
		return fields;
	}

	public void setFields(Set<Dto> fields) {
		this.fields = fields;
	}

	public static Dto getFieldByName(List<Dto> fields, String name) {
		int i = 0;
		for (Dto field : fields) {
			if (field.getString("fieldName").equals(name)) {
				field.put("index", i);
				return field;
			}
			++i;
		}
		return null;
	}

	public boolean isUseEntity() {
		return useEntity;
	}

	public void setUseEntity(boolean useEntity) {
		this.useEntity = useEntity;
	}

	public Dto getService() {
		return businessDto;
	}

	public static void main(String[] args) throws IOException {
		JpaParser parser = new JpaParser(
				new File(
						"/home/kiditz/apps/framework/slerp-ecommerce-service/src/main/java/org/slerp/ecommerce/service/product/EditProduct.java"),
				"org.slerp.ecommerce.entity", "org.slerp.ecommerce.repository");
		System.err.println(parser.parse());
		Scanner scanner = new Scanner(System.in);
		Set<Dto> fields = parser.getFields();
		Iterator<Dto> it = fields.iterator();
		int i = 0;
		while (it.hasNext()) {
			Dto field = (Dto) it.next();
			System.err.print((i + 1) + ". "
					+ field.getString("fieldName").concat(field.getBoolean("isNull") ? " " : " (*) ").concat(" : "));
			String value = scanner.nextLine();
			field.put("value", value);
			i++;
		}
		scanner.close();
	}
}
