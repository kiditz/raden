package org.slerp.plugin.wizard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
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
import org.slerp.core.Dto;
import org.slerp.generator.ApiGenerator;

public class RestApiGeneratorWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private RestApiGeneratorPage page;

	/**
	 * Constructor for {@linkplain RestApiGeneratorWizard}.
	 */
	public RestApiGeneratorWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new RestApiGeneratorPage(selection);
		addPage(page);
	}

	@Override
	public boolean canFinish() {
		return true;
	}

	@Override
	public boolean performFinish() {
		IJavaProject apiProject = page.getProject();
		IJavaProject serviceProject = page.getProject();
		ApiGenerator generator = page.getGenerator();

		Dto cacheDto = new Dto();
		cacheDto.put("packageEntity", page.getTxtEntityPackage().getText());
		cacheDto.put("packageRepo", page.getTxtRepoPackage().getText());
		cacheDto.put("packageService", page.getTxtServicePackage().getText());
		cacheDto.put("packageController", page.getTxtControllerPackage().getText());
		cacheDto.put("enablePrepare", true);
		try {
			File cacheDir = page.getProject().getProject().findMember("src/main/resources").getLocation().toFile();
			File cacheOut = new File(cacheDir, "generator.cache");
			FileWriter writer = new FileWriter(cacheOut);
			writer.write(cacheDto.toString());
			writer.close();
		} catch (IOException e1) {

		}
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(monitor, generator, serviceProject, apiProject);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (IOException e) {
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

	private void doFinish(IProgressMonitor monitor, ApiGenerator generator, IJavaProject serviceProject,
			IJavaProject apiProject) throws CoreException, IOException {
		generator.generate();
		monitor.beginTask("Generate " + generator.getFile().getName(), 2);
		apiProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		serviceProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		monitor.worked(1);
		getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IFileStore fileStoreService = EFS.getLocalFileSystem().getStore(generator.getFile().toURI());
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
		WizardDialog dialog = new WizardDialog(new Display().getActiveShell(), new RestApiGeneratorWizard());
		dialog.open();
	}
}