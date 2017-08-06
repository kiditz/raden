package org.slerp.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.slerp.core.CoreException;
import org.slerp.core.ConcurentDto;

public class JpaParser {
	private final File businessFile;
	private String packageRepo = null;
	private String packageEntity = null;
	private Set<ConcurentDto> fields = ConcurrentHashMap.newKeySet();
	private boolean useEntity = false;
	private ConcurentDto businessDto;

	public JpaParser(File businessFile, String packageEntity, String packageRepo) {
		this.businessFile = businessFile;
		this.packageRepo = packageRepo;
		this.packageEntity = packageEntity;
	}

	public ConcurentDto parse() throws IOException {
		List<ConcurentDto> tempFields = new ArrayList<>();
		JavaClassSource cls = Roaster.parse(JavaClassSource.class, businessFile);
		String repositoryPath = businessFile.getAbsolutePath();
		StringBuffer buffer = new StringBuffer();
		repositoryPath = repositoryPath.substring(0, repositoryPath.indexOf("java") + 4).concat("/")
				.concat(packageRepo.replace(".", "/")).concat("/");

		buffer.append(repositoryPath);
		String repoName = cls.getFields().get(0).getType().getName();
		buffer.append(repoName.concat(".java"));
		if (!buffer.toString().endsWith(".java")) {
			throw new CoreException("The Method name should be contins with entity");
		}

		System.out.println("Repository Path " + buffer.toString());
		File repositoryFile = new File(buffer.toString());
		if (!repositoryFile.exists())
			throw new CoreException("Cannot found repository for business " + cls.getName());

		JavaInterfaceSource repoInf = Roaster.parse(JavaInterfaceSource.class, repositoryFile);
		String jpaInfo = repoInf.getInterfaces().get(0);

		businessDto = new ConcurentDto();

		if (isUseEntity()) {
			String entityInfo = jpaInfo.substring(jpaInfo.indexOf("<") + 1, jpaInfo.indexOf(","));
			String entityType = jpaInfo.substring(jpaInfo.indexOf(',') + 1, jpaInfo.indexOf('>'));
			String entityPath = businessFile.getAbsolutePath().concat("/")
					.concat(packageEntity.replace(".", "/").concat("/"));
			entityPath = entityPath.substring(0, entityPath.indexOf("java") + 4);
			ConcurentDto files = EntityUtils.readEntities(new File(entityPath));
			File entityFile = new File(files.getString(entityInfo));
			ConcurentDto entity = EntityUtils.readEntityAsDto(entityFile);
			List<ConcurentDto> fieldsDto = Collections.synchronizedList(entity.getList("fields"));
			// Get Temp FROM JOIN
			for (ConcurentDto field : fieldsDto) {
				if (field.getBoolean("isJoin")) {
					String type = field.getString("fieldType");
					String simpleType = type.substring(type.lastIndexOf('.') + 1, type.length());
					File file = new File(entityFile.getParent(), simpleType.concat(".java"));
					ConcurentDto entityDto = EntityUtils.readEntityAsDto(file);
					tempFields = entityDto.getList("fields");
				}
			}
			if (!tempFields.isEmpty()) {
				for (int j = 0; j < fieldsDto.size(); j++) {
					ConcurentDto temp = getFieldByName(tempFields, fieldsDto.get(j).getString("fieldName"));
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
				ConcurentDto field = getFieldByName(entity.getList("fields"), keyValidations[i]);
				if (field == null) {
					field = new ConcurentDto();
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

		ConcurentDto repositoryDto = new ConcurentDto();
		repositoryDto.put("packageName", repoInf.getPackage());
		repositoryDto.put("className", repoInf.getName());
		businessDto.put("repository", repositoryDto);

		return businessDto;
	}

	public Set<ConcurentDto> getFields() {
		return fields;
	}

	public void setFields(Set<ConcurentDto> fields) {
		this.fields = fields;
	}

	public static ConcurentDto getFieldByName(List<ConcurentDto> fields, String name) {
		int i = 0;
		for (ConcurentDto field : fields) {
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

	public ConcurentDto getService() {
		return businessDto;
	}

	public static void main(String[] args) throws IOException {
		JpaParser parser = new JpaParser(
				new File(
						"/home/kiditz/slerp-git/runtime-EclipseApplication/oauth/src/main/java/org/slerp/oauth/service/principal/AddUserPrincipal.java"),
				"org.slerp.oauth.entity", "org.slerp.oauth.repository");
		parser.setUseEntity(true);
		System.err.println(parser.parse());

		Set<ConcurentDto> fields = parser.getFields();

		Iterator<ConcurentDto> it = fields.iterator();
		while (it.hasNext()) {
			ConcurentDto field = (ConcurentDto) it.next();
			// System.err.print((i + 1) + ". "
			// + field.getString("fieldName").concat(field.getBoolean("isNull")
			// ? " " : " (*) ").concat(" : "));
			//
			// i++;
			System.err.println(field.toString());
		}

	}
}
