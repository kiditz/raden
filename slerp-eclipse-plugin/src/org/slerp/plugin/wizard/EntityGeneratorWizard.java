package org.slerp.plugin.wizard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slerp.core.Dto;
import org.slerp.generator.EntityGenerator;
import org.slerp.utils.StringConverter;

public class EntityGeneratorWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private EntityGeneratorPage page;

	/**
	 * Constructor for {@linkplain EntityGeneratorWizard}.
	 */
	public EntityGeneratorWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new EntityGeneratorPage(selection);
		addPage(page);
	}

	public boolean performFinish() {

		final String items[] = page.getListReceiver().getItems();

		if (items == null || items.length == 0) {
			page.updateStatus("Please elect at least 1 data from database");
		}
		final String entityPackage = page.getTxtEntityPackage().getText();
		final String repositoryPackage = page.getTxtRepoPackage().getText();
		final IResource propertiesFile = page.getApplicationProperties();
		IProject project = page.getProject().getProject();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {

					doFinish(monitor, entityPackage, repositoryPackage, propertiesFile, project, items);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};

		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	private void doFinish(IProgressMonitor monitor, String entityPackage, String repositoryPackage,
			IResource propertiesFile, IProject project, String[] items) throws CoreException {
		// create a sample file

		EntityGenerator generator = new EntityGenerator(propertiesFile.getLocation().toOSString(), entityPackage,
				repositoryPackage, project.getLocation().toOSString().concat("/src/main/java"));
		for (String item : items) {
			monitor.beginTask("Generate Entity For Table : " + item, 2);
			try {
				generator.generate(item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Dto cacheDto = new Dto();
		cacheDto.put("packageEntity", entityPackage);
		cacheDto.put("packageRepo", repositoryPackage);

		try {
			File cacheDir = project.findMember("src/main/resources").getLocation().toFile();
			File cacheOut = new File(cacheDir, "generator.cache");
			FileWriter writer = new FileWriter(cacheOut);
			writer.write(cacheDto.toString());
			writer.close();
		} catch (IOException e1) {
			throwCoreException("Failed to write file \n" + e1.getMessage());
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				File cacheDir = project.findMember("src/main/java").getLocation().toFile();

				try {
					for (String item : items) {
						File outputEntity = new File(cacheDir, entityPackage.replace(".", "/").concat("/")
								.concat(StringConverter.convertCaseSensitive(item, true)).concat(".java"));
						File outputRepo = new File(cacheDir,
								repositoryPackage.replace(".", "/").concat("/")
										.concat(StringConverter.convertCaseSensitive(item, true)).concat("Repository")
										.concat(".java"));
						System.err.println(outputEntity.getAbsolutePath());
						System.err.println(outputRepo.getAbsolutePath());
						IFileStore fileStoreEntity = EFS.getLocalFileSystem().getStore(outputEntity.toURI());
						IFileStore fileStoreRepo = EFS.getLocalFileSystem().getStore(outputRepo.toURI());
						if (!fileStoreEntity.fetchInfo().isDirectory() && fileStoreEntity.fetchInfo().exists()) {
							IDE.openEditorOnFileStore(page, fileStoreEntity);
						}
						if (!fileStoreRepo.fetchInfo().isDirectory() && fileStoreRepo.fetchInfo().exists()) {
							IDE.openEditorOnFileStore(page, fileStoreRepo);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		monitor.worked(1);
	}

	protected void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "slerp-eclipse-plugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public static void main(String[] args) {
		WizardDialog dialog = new WizardDialog(new Display().getActiveShell(), new EntityGeneratorWizard());
		dialog.open();
	}
}