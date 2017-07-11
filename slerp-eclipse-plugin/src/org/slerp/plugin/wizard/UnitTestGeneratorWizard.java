package org.slerp.plugin.wizard;

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
import org.slerp.generator.JUnitTestGenerator;

public class UnitTestGeneratorWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private UnitTestGeneratorPage page;

	/**
	 * Constructor for {@linkplain UnitTestGeneratorWizard}.
	 */
	public UnitTestGeneratorWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new UnitTestGeneratorPage(selection);
		addPage(page);
	}

	public boolean performFinish() {
		JUnitTestGenerator generator = page.getGenerator();
		IJavaProject project = page.getProject();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(monitor, generator, project);
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

	private void doFinish(IProgressMonitor monitor, JUnitTestGenerator generator, IJavaProject project)
			throws CoreException {
		
		generator.generate();
		monitor.beginTask("Generate Test For : " + generator.getOutputFile().getName(), 2);
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		monitor.worked(1);
		getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IFileStore fileStoreService = EFS.getLocalFileSystem().getStore(generator.getOutputFile().toURI());
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
		WizardDialog dialog = new WizardDialog(new Display().getActiveShell(), new UnitTestGeneratorWizard());
		dialog.open();
	}
}