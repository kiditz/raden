package org.slerp.plugin.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slerp.plugin.wizard.utils.EditMask;
import org.slerp.project.Setup.Configuration;

/**
 * The "New Service" wizard page allows you to generate project
 */

public class ProjectWizardPage extends WizardPage {

	private ISelection selection;

	private Text txtGroupId;

	private Text txtArtifactId;

	private Text txtVersion;

	private Configuration configuration = new Configuration();

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ProjectWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("New Service Project");
		setDescription("This wizard will create service project with maven");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);		
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		new Label(container, SWT.NONE).setText("Group Id : ");
		txtGroupId = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtGroupId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		new Label(container, SWT.NONE).setText("Artifact Id : ");
		txtArtifactId = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtArtifactId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new Label(container, SWT.NONE).setText("Version : ");
		txtVersion = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		EditMask mask = new EditMask(txtVersion);
		mask.setMask("#.#.#-nnnnnnnn");
		initialize();
		setControl(container);

	}

	private void validatePackage(String input) {
		String packageRegex = "^[a-z][a-z0-9_]*(.[a-z0-9_]+)+[0-9a-z_]$";
		if (!input.matches(packageRegex)) {
			updateStatus("Package name is invalie");
			return;
		}
		updateStatus(null);

	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		configuration.artifactId = txtArtifactId.getText();
		configuration.groupId = txtGroupId.getText();
		configuration.outputDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		configuration.version = txtVersion.getText();
		txtGroupId.addModifyListener(evt -> {
			validatePackage(txtGroupId.getText());
			configuration.groupId = txtGroupId.getText();
		});
		txtArtifactId.addModifyListener(evt -> {
			configuration.artifactId = txtArtifactId.getText();
		});
		txtVersion.addModifyListener(evt -> {
			configuration.version = txtVersion.getText();
		});

		if (selection == null)
			return;

		if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				// containerText.setText(container.getFullPath().toString());
				if (container != null) {
					updateStatus(container.getFullPath().toOSString());
				}
			}
		}
		// fileText.setText("new_file.mpe");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public Configuration getConfiguration() {
		return configuration;
	}
}