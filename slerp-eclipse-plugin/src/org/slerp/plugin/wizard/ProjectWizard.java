package org.slerp.plugin.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.eclipse.ui.IWorkbenchWizard;
import org.slerp.project.Setup;
import org.slerp.project.Setup.Configuration;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class ProjectWizard extends Wizard implements INewWizard {
	private ProjectWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for MainWizard.
	 */
	public ProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new ProjectWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(monitor);
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
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();

			e.printStackTrace();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	private void doFinish(IProgressMonitor monitor) throws CoreException {
		// create a sample file
		Configuration configuration = page.getConfiguration();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IProject project = root.getProject(configuration.artifactId);

		try {
			project.create(monitor);

			System.err.println(project.getName());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		monitor.beginTask("Creating Service Project : " + configuration.artifactId, 2);

		try {
			System.err.println(configuration.outputDir);
			validateInput(configuration);
			Setup.execute(configuration);
			root.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			project.open(monitor);
					

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "slerp-eclipse-plugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	private void validateInput(Configuration configuration) throws CoreException {
		if (isNullOrEmpty(configuration.artifactId)) {
			throwCoreException("org.slerp.config.artifactId");
		}
		if (isNullOrEmpty(configuration.groupId)) {
			throwCoreException("org.slerp.config.groupId");
		}
		if (isNullOrEmpty(configuration.outputDir)) {
			throwCoreException("org.slerp.config.outputDir");
		}
		if (isNullOrEmpty(configuration.version)) {
			throwCoreException("org.slerp.config.version");
		}
	}

	public boolean isNullOrEmpty(String input) {
		return input == null || input.isEmpty();
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public static void main(String[] args) {
		WizardDialog dialog = new WizardDialog(new Display().getActiveShell(), new ProjectWizard());
		dialog.open();
	}
}