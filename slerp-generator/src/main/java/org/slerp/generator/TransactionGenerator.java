package org.slerp.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.util.Assert;
import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.core.CoreException;
import org.slerp.core.Domain;
import org.slerp.core.ConcurentDto;
import org.slerp.core.business.DefaultBusinessTransaction;
import org.slerp.utils.EntityUtils;

public class TransactionGenerator implements Generator {
	private String packageTarget;
	private String packageEntity;
	private String packageRepo;
	private File baseDir;
	private boolean enableValidator;
	private ConcurentDto entity;

	private static enum TransactionMode {
		Add, Edit, Remove
	}

	TransactionMode mode = null;

	public TransactionGenerator(String mode, String packageEntity, String packageRepo, String packageTarget,
			File baseDir, boolean enableValidator) {
		this.enableValidator = enableValidator;
		this.packageTarget = packageTarget;
		Assert.notNull(packageTarget, "Package Name should be filled");
		this.baseDir = baseDir;
		Assert.notNull(baseDir, "Base Directory be should be filled");
		this.mode = TransactionMode.valueOf(mode);
		Assert.notNull(mode, "Transaction mode should be filled");
		this.packageRepo = packageRepo;
		Assert.notNull(packageEntity, "Package Entity should be filled");
		this.packageEntity = packageEntity;
		File entityDir = new File(baseDir, packageEntity.replace(".", "/").concat("/"));
		try {
			this.entity = EntityUtils.readEntities(entityDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPackageEntity() {
		return packageEntity;
	}

	public void setPackageEntity(String packageEntity) {
		this.packageEntity = packageEntity;
	}

	@Override
	public void generate(String fileName) {
		fileName = Strings.capitalize(fileName);
		try {

			if (entity.get(fileName) != null) {
				File entityFile = new File(entity.getString(fileName));
				ConcurentDto parseEntity = EntityUtils.readEntityAsDto(entityFile);
				createBusinessTransaction(parseEntity, entityFile);
				return;
			}
			throw new CoreException("Cannot find entity with name : " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createBusinessTransaction(ConcurentDto classDto, File entityFile) throws IOException {
		// System.out.println(classDto);
		JavaClassSource cls = Roaster.create(JavaClassSource.class);
		cls.setName(mode.name().concat(classDto.getString("className"))).setPackage(packageTarget).setPublic();
		cls.addAnnotation("org.springframework.stereotype.Service");
		cls.addAnnotation("org.springframework.transaction.annotation.Transactional");
		cls.addField().setName(Strings.uncapitalize(classDto.getString("className") + "Repository"))
				.setType(classDto.getString("className") + "Repository")
				.addAnnotation("org.springframework.beans.factory.annotation.Autowired");

		cls.addImport(packageRepo.concat(".").concat(classDto.getString("className")) + "Repository");
		cls.addImport(Domain.class);
		cls.addImport(classDto.getString("packageName").concat(".").concat(classDto.getString("className")));
		cls.addImport(CoreException.class);

		List<String> keyValues = new ArrayList<>();
		List<String> notNullValues = new ArrayList<>();
		List<String> numberValues = new ArrayList<>();
		List<String> emailValues = new ArrayList<>();
		List<ConcurentDto> fields = classDto.getList("fields");
		StringBuilder prepareBody = new StringBuilder();
		StringBuilder handleBody = new StringBuilder();
		String uncapClassName = Strings.uncapitalize(classDto.getString("className"));
		String className = classDto.getString("className");
		for (ConcurentDto field : fields) {
			String fieldName = field.getString("fieldName");
			String file = entityFile.getAbsolutePath();

			if (mode == TransactionMode.Add) {
				if (field.getBoolean("isForeignKey"))
					continue;
				if (field.getBoolean("isPrimaryKey")) {
					continue;
				}

				keyValues.add(fieldName);
				if (!field.getBoolean("isJoin") && !field.getBoolean("isNull")) {
					notNullValues.add(fieldName);
				}
				if (enableValidator)

					if (field.getBoolean("isJoin")) {
						String simpleField = field.getString("fieldType").substring(
								field.getString("fieldType").lastIndexOf('.') + 1,
								field.getString("fieldType").length());
						String repoName = Strings.uncapitalize(simpleField + "Repository");
						file = file.replace(className, simpleField);
						ConcurentDto referenceDto = EntityUtils.readEntityAsDto(new File(file));
						String primaryKeyRef = getPrimaryKeyRef(referenceDto.getList("fields")); //
						prepareBody.append("Domain " + Strings.uncapitalize(simpleField) + "Domain = " + uncapClassName
								+ "Domain.getDomain(\"" + fieldName + "\");\n");
						prepareBody.append(simpleField + " " + fieldName + " = " + Strings.uncapitalize(simpleField)
								+ "Domain" + ".convertTo(" + simpleField + ".class);\n");
						prepareBody.append(fieldName + " = " + repoName + ".findOne(" + fieldName + ".get"
								+ Strings.capitalize(primaryKeyRef) + "());");
						prepareBody.append("if (" + fieldName + " == null){");
						prepareBody.append("throw new CoreException(\""
								+ field.getString("fieldType").concat(".notFound") + "\");");
						prepareBody.append("}");
						prepareBody
								.append(uncapClassName + "Domain.put(\"" + fieldName + "\", new Domain(" + fieldName + "));");

						cls.addField().setName(repoName).setType(simpleField + "Repository")
								.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
						cls.addImport(packageRepo.concat(".").concat(simpleField) + "Repository");
						cls.addImport(classDto.getString("packageName").concat(".").concat(simpleField));
					}
				if (field.getBoolean("isNumber")) {
					numberValues.add(fieldName);
				}
				if (fieldName.contains("email")) {
					emailValues.add(fieldName);
				}
			} else if (mode == TransactionMode.Edit) {
				if (field.getBoolean("isForeignKey"))
					continue;
				// if (field.getBoolean("isPrimaryKey")) {
				// continue;
				// }

				keyValues.add(fieldName);
				if (!field.getBoolean("isJoin") && !field.getBoolean("isNull")) {
					notNullValues.add(fieldName);
				}
				if (enableValidator) {
					if (field.getBoolean("isJoin")) {
						String simpleField = field.getString("fieldType").substring(
								field.getString("fieldType").lastIndexOf('.') + 1,
								field.getString("fieldType").length());
						String repoName = Strings.uncapitalize(simpleField + "Repository");
						file = file.replace(className, simpleField);
						ConcurentDto referenceDto = EntityUtils.readEntityAsDto(new File(file));
						String primaryKeyRef = getPrimaryKeyRef(referenceDto.getList("fields")); //
						String primaryDtoVar = Strings.uncapitalize(simpleField) + "Domain";
						prepareBody.append("Domain " + primaryDtoVar + " = " + uncapClassName + "Domain.getDomain(\"" + fieldName
								+ "\");\n");
						prepareBody.append(simpleField.concat(" ").concat(fieldName).concat(" = ") + repoName
								+ ".findOne(" + primaryDtoVar.concat(".getLong(\"" + primaryKeyRef + "\")") + ");");
						prepareBody.append("if (" + fieldName + " == null){");
						prepareBody.append("throw new CoreException(\""
								+ field.getString("fieldType").concat(".notFound") + "\");");
						prepareBody.append("}");
						prepareBody
								.append(uncapClassName + "Domain.put(\"" + fieldName + "\", new Domain(" + fieldName + "));");

						prepareBody.append("\n");

						cls.addField().setName(repoName).setType(simpleField + "Repository")
								.addAnnotation("org.springframework.beans.factory.annotation.Autowired");
						cls.addImport(packageRepo.concat(".").concat(simpleField) + "Repository");
						cls.addImport(classDto.getString("packageName").concat(".").concat(simpleField));
					}
				}
				if (field.getBoolean("isNumber")) {
					numberValues.add(fieldName);
				}
				if (fieldName.contains("email")) {
					emailValues.add(fieldName);
				}
			} else if (mode == TransactionMode.Remove) {
				if (!field.getBoolean("isPrimaryKey")) {
					continue;
				}

				keyValues.add(fieldName);
				if (field.getBoolean("isNumber")) {
					numberValues.add(fieldName);
				}
				if (fieldName.contains("email")) {
					emailValues.add(fieldName);
				}

			}

		}
		if (enableValidator && !(mode == TransactionMode.Add)) {
			String pkField = getPrimaryKeyRef(fields);
			prepareBody.append(className.concat(" ").concat(uncapClassName).concat(" = ").concat(uncapClassName)
					.concat("Repository.findOne(").concat(uncapClassName).concat("Domain.getLong(\"").concat(pkField)
					.concat("\"));"));
			prepareBody.append("if (" + uncapClassName + " == null) {");
			prepareBody.append("throw new CoreException(\""
					+ classDto.getString("packageName").concat(".").concat(className).concat(".notFound") + "\");");
			prepareBody.append("}");
		}

		// Create handle body
		handleBody.append("super.handle(" + Strings.uncapitalize(classDto.getString("className")).concat("Domain") + ");");
		handleBody.append("try {");

		handleBody.append(className + " " + uncapClassName + " = " + uncapClassName + "Domain.convertTo("
				+ classDto.getString("className") + ".class);");
		if (mode != TransactionMode.Remove) {
			handleBody.append(uncapClassName + " = " + uncapClassName + "Repository.save(" + uncapClassName + ");");
			handleBody.append("return new Domain(" + uncapClassName + ");");
		} else {
			handleBody.append(uncapClassName + "Repository.delete(" + uncapClassName + ");");
			handleBody.append("return new Domain().put(\"success\", true);");
		}
		handleBody.append("} catch (Exception e) {");
		handleBody.append("throw new CoreException(e);");
		handleBody.append("}");

		// Generate Method
		MethodSource<JavaClassSource> prepareMethod = cls.addMethod("prepare()").setPublic();
		prepareMethod.addThrows(Exception.class);
		prepareMethod.setParameters("Domain " + Strings.uncapitalize(classDto.getString("className")).concat("Domain"))
				.addAnnotation(Override.class);
		prepareMethod.setBody(prepareBody.toString());
		MethodSource<JavaClassSource> methodHandle = cls.addMethod("handle()").setReturnType(Domain.class).setPublic();
		methodHandle.setParameters("Domain " + Strings.uncapitalize(classDto.getString("className")).concat("Domain"))
				.addAnnotation(Override.class);
		methodHandle.setBody(handleBody.toString());
		// Generate Validation
		if (!keyValues.isEmpty())
			cls.addAnnotation("org.slerp.core.validation.KeyValidation")
					.setStringArrayValue(keyValues.toArray(new String[] {}));
		if (!notNullValues.isEmpty())
			cls.addAnnotation("org.slerp.core.validation.NotBlankValidation")
					.setStringArrayValue(notNullValues.toArray(new String[] {}));
		if (!numberValues.isEmpty())
			cls.addAnnotation("org.slerp.core.validation.NumberValidation")
					.setStringArrayValue(numberValues.toArray(new String[] {}));
		if (!emailValues.isEmpty()) {
			cls.addAnnotation("org.slerp.core.validation.EmailValidation")
					.setStringArrayValue(emailValues.toArray(new String[] {}));
		}
		// Extend with DefaultBusinessTransaction
		cls.extendSuperType(DefaultBusinessTransaction.class);

		File outputDir = new File(baseDir, packageTarget.replace(".", "/").concat("/"));
		if (!outputDir.isDirectory())
			outputDir.mkdirs();
		File outputFile = new File(outputDir, cls.getName().concat(".java"));
		FileWriter writer = new FileWriter(outputFile);
		writer.write(cls.toString());
		writer.close();
		System.out.println("Generator successfully created : ".concat(cls.getCanonicalName().concat(".java")));
	}

	private String getPrimaryKeyRef(List<ConcurentDto> fields) {
		for (ConcurentDto field : fields) {
			if (field.getBoolean("isPrimaryKey")) {
				return field.getString("fieldName");
			}
		}
		return null;
	}

	public ConcurentDto getEntity() throws IOException {
		return entity;
	}

	public static void main(String[] args) throws IOException {
		TransactionGenerator generator = new TransactionGenerator("Add", "org.slerp.ecommerce.entity",
				"org.slerp.ecommerce.repo", "org.slerp.ecommerce.service.product",
				new File("/home/kiditz/apps/framework/slerp-ecommerce-service/src/main/java/"), true);
		System.err.println(generator.getEntity());
		// generator.generate("Category");
	}
}
