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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jboss.forge.roaster.model.util.Strings;
import org.slerp.core.Dto;
import org.slerp.generator.FunctionGenerator;
import org.slerp.generator.FunctionGenerator.FunctionType;

public class FunctionGeneratorWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private FunctionGeneratorPage page;

	/**
	 * Constructor for {@linkplain FunctionGeneratorWizard}.
	 */
	public FunctionGeneratorWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new FunctionGeneratorPage(selection);
		addPage(page);
	}

	public boolean performFinish() {
		page.validate();
		final String entityPackage = page.getTxtEntityPackage().getText();
		final String repositoryPackage = page.getTxtRepoPackage().getText();
		// final IResource propertiesFile = page.getApplicationProperties();
		final String methodName = page.getTxtMethodName().getText();
		final Dto paramDto = page.getParamDto();
		IProject project = page.getProject().getProject();
		final String packageService = page.getTxtServicePackage().getText();
		final String query = page.getTxtQuery().getText();
		FunctionType type = page.functionType;
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(monitor, entityPackage, repositoryPackage, methodName, paramDto, query, packageService,
							project, type);
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

	private void doFinish(IProgressMonitor monitor, String entityPackage, String repositoryPackage, String methodName,
			Dto paramDto, String query, String packageService, IProject project, FunctionType type)
			throws CoreException {
		System.err.println(query);
		System.err.println(packageService);
		System.err.println(entityPackage);
		System.err.println(repositoryPackage);
		String baseDir = project.getLocation().toOSString().concat("/src/main/java");
		System.err.println(baseDir);
		monitor.beginTask("Generate Business Function For Method : " + methodName, 2);
		Dto cacheDto = new Dto();
		cacheDto.put("packageEntity", entityPackage);
		cacheDto.put("packageRepo", repositoryPackage);
		cacheDto.put("packageService", packageService);
		FunctionGenerator generator = new FunctionGenerator(entityPackage, repositoryPackage, baseDir, methodName);
		generator.packageTarget = packageService;
		generator.params = paramDto;
		generator.type = type;
		generator.generate(query);
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

		try {
			File cacheDir = project.findMember("src/main/resources").getLocation().toFile();

			File cacheOut = new File(cacheDir, "generator.cache");
			FileWriter writer = new FileWriter(cacheOut);
			writer.write(cacheDto.toString());
			writer.close();
		} catch (IOException e1) {
			throwCoreException("Failed to write file \n" + e1.getMessage());
		}
		monitor.worked(1);
		getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				File cacheDir = project.findMember("src/main/java").getLocation().toFile();
				File outputService = new File(cacheDir, packageService.replace(".", "/").concat("/")
						.concat(Strings.capitalize(methodName).concat(".java")));
				monitor.setTaskName("Opening file " + outputService.getName());
				IFileStore fileStoreService = EFS.getLocalFileSystem().getStore(outputService.toURI());
				if (!fileStoreService.fetchInfo().isDirectory() && fileStoreService.fetchInfo().exists()) {
					try {
						IDE.openEditorOnFileStore(page, fileStoreService);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}

		});

	}

	protected void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "slerp-eclipse-plugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public static void main(String[] args) {
		WizardDialog dialog = new WizardDialog(new Display().getActiveShell(), new FunctionGeneratorWizard());
		dialog.open();
	}
}