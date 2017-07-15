package org.slerp.plugin.wizard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;

@SuppressWarnings("restriction")
public abstract class BaseGenerator extends WizardPage {
	private String settingPath;
	private String packageName;
	private String packageRepoName;
	private String srcDir;

	private ISelection selection;
	private Text txtProject;
	private IJavaProject project;
	private Text txtEntityPackage;
	private Text txtRepoPackage;
	private Text txtServicePackage;
	public GridData gd;
	private Text txtApiProject;
	private IJavaProject apiProject;
	private boolean enableServicePackage;
	private boolean enableApiProject;
	private boolean enableControllerPackage;
	private Text txtControllerPackage;

	/**
	 * Constructor for {@link BaseGenerator}.
	 * 
	 * @param selection
	 */
	public BaseGenerator(ISelection selection) {
		super("entityPage");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		setControl(bindParent(parent));
	}

	public Composite bindParent(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		new Label(container, SWT.NONE).setText(isEnableApiProject() ? "Service Project *" : "Project* ");
		txtProject = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtProject.setMessage("Press Browse To Select Project");
		Button btnBrowseProject = new Button(container, SWT.PUSH);
		btnBrowseProject.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnBrowseProject.setText("&Browse");
		btnBrowseProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openProject();
				validate();
			}
		});
		if (isEnableApiProject()) {
			new Label(container, SWT.NONE).setText("Api Project* ");
			txtApiProject = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtApiProject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtApiProject.setMessage("Press Browse To Select Project");
			Button btnApiBrowseProject = new Button(container, SWT.PUSH);
			btnApiBrowseProject.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			btnApiBrowseProject.setText("&Browse");
			btnApiBrowseProject.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openApiProject();
					validate();
				}
			});
		}
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;

		new Label(container, SWT.NONE).setText("Entity Package* ");
		txtEntityPackage = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtEntityPackage.setLayoutData(gd);
		txtEntityPackage.setMessage("Press CTRL+SPACE to select package");

		new Label(container, SWT.NONE).setText("Repository Package* ");
		txtRepoPackage = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtRepoPackage.setLayoutData(gd);
		txtRepoPackage.setText("");
		txtRepoPackage.setMessage("Press CTRL+SPACE to select package");
		if (isEnableServicePackage()) {
			new Label(container, SWT.NONE).setText("Service Package* ");
			txtServicePackage = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtServicePackage.setLayoutData(gd);
			txtServicePackage.setMessage("Press CTRL+SPACE to select package");
		}
		if (isEnableControllerPackage()) {
			new Label(container, SWT.NONE).setText("Api Package* ");
			txtControllerPackage = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtControllerPackage.setLayoutData(gd);
			txtControllerPackage.setMessage("Press CTRL+SPACE to select package");
		}

		txtEntityPackage.addModifyListener(e -> {
			validate();
		});
		txtRepoPackage.addModifyListener(e -> {
			validate();
		});
		if (isEnableServicePackage()) {
			txtServicePackage.addModifyListener(e -> {
				validate();
			});
		}
		txtEntityPackage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.stateMask == SWT.CTRL && evt.keyCode == SWT.SPACE) {
					openPackage(txtEntityPackage, false);
				}
			}
		});
		txtRepoPackage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if (evt.stateMask == SWT.CTRL && evt.keyCode == SWT.SPACE) {
					openPackage(txtRepoPackage, false);
				}
			}
		});
		if (isEnableServicePackage()) {
			txtServicePackage.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent evt) {
					if (evt.stateMask == SWT.CTRL && evt.keyCode == SWT.SPACE) {
						openPackage(txtServicePackage, false);
					}
				}
			});
		}
		if (isEnableControllerPackage()) {
			txtControllerPackage.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent evt) {
					if (evt.stateMask == SWT.CTRL && evt.keyCode == SWT.SPACE) {
						openPackage(txtControllerPackage, true);
					}
				}
			});
		}
		validate();
		initializeSelection();
		return container;
	}

	private void openPackage(Text text, boolean isApi) {
		int style = IJavaElementSearchConstants.CONSIDER_REQUIRED_PROJECTS;
		try {
			SelectionDialog dialog = JavaUI.createPackageDialog(getShell(), isApi ? apiProject : project, style,
					text.getText());
			dialog.setTitle("Package Selection Dialog");
			dialog.setMessage("Please select a package");
			if (dialog.open() == Window.OK) {
				IPackageFragment fragment = (IPackageFragment) dialog.getResult()[0];
				text.setText(fragment.getElementName());
			}
		} catch (JavaModelException e) {

			e.printStackTrace();
		}
	}

	private void openProject() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			ListSelectionDialog dialog = new ListSelectionDialog(getShell(), root, new BaseWorkbenchContentProvider(),
					new WorkbenchLabelProvider(), "Select The Project");
			if (dialog.open() == IDialogConstants.OK_ID) {
				Object[] result = dialog.getResult();
				project = JavaCore.create((Project) result[0]);
				txtProject.setText(project.getElementName());
				updateCache();
			}
		} catch (Exception e) {
			// ignored
		}
	}

	private void openApiProject() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			ListSelectionDialog dialog = new ListSelectionDialog(getShell(), root, new BaseWorkbenchContentProvider(),
					new WorkbenchLabelProvider(), "Select The Project");
			dialog.setHelpAvailable(false);
			if (dialog.open() == IDialogConstants.OK_ID) {
				Object[] result = dialog.getResult();
				apiProject = JavaCore.create((Project) result[0]);
				txtApiProject.setText(apiProject.getElementName());

			}
		} catch (Exception e) {
		}
	}

	private void initializeSelection() {

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
				IProject prjTemp = container.getProject();
				this.project = JavaCore.create(prjTemp);
				txtProject.setText(prjTemp.getName());
				updateCache();
			}
		}
	}

	private Dto updateCache() {
		validate();
		if (project != null) {
			IResource cacheResource = project.getResource().getProject()
					.findMember("src/main/resources/generator.cache");
			if (cacheResource != null && cacheResource.exists()) {
				try {
					Dto cache = new Dto(readString(cacheResource.getLocation().toFile()));
					String cacheEnPackage = cache.getString("packageEntity");
					String cacheRepPackage = cache.getString("packageRepo");
					txtEntityPackage.setText(cacheEnPackage);
					txtRepoPackage.setText(cacheRepPackage);
					if (isEnableServicePackage()) {
						try {
							txtServicePackage.setText(cache.getString("packageService"));
						} catch (NullPointerException e) {
						}

					}
					if (isEnableControllerPackage()) {
						try {
							txtControllerPackage.setText(cache.getString("packageController"));
						} catch (NullPointerException e) {
						}
					}
					return cache;
				} catch (Exception e) {
					throw new CoreException(
							"Failed to get cache, please delete generator.cache and let slerp update this one after generating",
							e);
				}
			}
		}
		return null;

	}

	public void setEnableServicePackage(boolean enableServicePackage) {
		this.enableServicePackage = enableServicePackage;
	}

	public boolean isEnableServicePackage() {
		return enableServicePackage;
	}

	public void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public void validate() {
		if (txtProject.getText().isEmpty() || project == null) {
			if (!isEnableApiProject())
				updateStatus("Project should be filled");
			else
				updateStatus("Service Project should be filled");
			return;
		}

		if (txtEntityPackage.getText().isEmpty()) {
			updateStatus("Entity package should be filled");
			return;
		}
		String packageRegex = "^[a-z][a-z0-9_]*(.[a-z0-9_]+)+[0-9a-z_]$";
		if (!txtEntityPackage.getText().matches(packageRegex)) {
			updateStatus("Entity Package is invalidated!");
			return;
		}

		if (txtRepoPackage.getText().isEmpty()) {
			updateStatus("Repository Package should be filled");
			return;
		}

		if (!txtRepoPackage.getText().matches(packageRegex)) {
			updateStatus("Repository Package is invalidated!");
			return;
		}
		if (isEnableServicePackage()) {
			if (txtServicePackage.getText().isEmpty()) {
				updateStatus("Service package should be filled");
				return;
			}
			if (!txtServicePackage.getText().matches(packageRegex)) {
				updateStatus("Service Package is invalidated!");
				return;
			}
		}
		if (isEnableApiProject()) {
			if (txtApiProject.getText().isEmpty() || apiProject == null) {
				updateStatus("Api Project should be filled");
				return;
			}
		}
		if (isEnableControllerPackage()) {
			if (txtControllerPackage.getText().isEmpty()) {
				updateStatus("Controller package should be filled");
				return;
			}
			if (!txtControllerPackage.getText().matches(packageRegex)) {
				updateStatus("Controller Package is invalidated!");
				return;
			}
		}
		updateStatus(null);

	}

	public String readString(File file) throws IOException {
		if (!file.exists())
			throw new CoreException("Cannot read file who's not exist");
		InputStream in = new FileInputStream(file);
		int read = 0;
		byte[] bytes = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((read = in.read(bytes)) != -1) {
			baos.write(bytes, 0, read);
		}
		in.close();

		return baos.toString("UTF-8");
	}

	public ISelection getSelection() {
		return selection;
	}

	public String getSettingPath() {
		return settingPath;
	}

	public void setSettingPath(String settingPath) {
		this.settingPath = settingPath;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageRepoName() {
		return packageRepoName;
	}

	public void setPackageRepoName(String packageRepoName) {
		this.packageRepoName = packageRepoName;
	}

	public String getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}

	public Text getTxtProject() {
		return txtProject;
	}

	public void setTxtProject(Text txtProject) {
		this.txtProject = txtProject;
	}

	public IJavaProject getProject() {
		return project;
	}

	public void setProject(IJavaProject project) {
		this.project = project;
	}

	public Text getTxtEntityPackage() {
		return txtEntityPackage;
	}

	public void setTxtEntityPackage(Text txtEntityPackage) {
		this.txtEntityPackage = txtEntityPackage;
	}

	public Text getTxtRepoPackage() {
		return txtRepoPackage;
	}

	public void setTxtRepoPackage(Text txtRepoPackage) {
		this.txtRepoPackage = txtRepoPackage;
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
	}

	public void validatePackage(String input, String message) {

		updateStatus(null);
	}

	public Text getTxtServicePackage() {
		return txtServicePackage;
	}

	public Text gettxtApiProject() {
		return txtApiProject;
	}

	public boolean isEnableApiProject() {
		return enableApiProject;
	}

	public void setEnableApiProject(boolean enableApiProject) {
		this.enableApiProject = enableApiProject;
	}

	public void setTxtServicePackage(Text txtServicePackage) {
		this.txtServicePackage = txtServicePackage;
	}

	public void settxtApiProject(Text txtApiProject) {
		this.txtApiProject = txtApiProject;
	}

	public IJavaProject getApiProject() {
		return apiProject;
	}

	public void setApiProject(IJavaProject apiProject) {
		this.apiProject = apiProject;
	}

	public boolean isEnableControllerPackage() {
		return enableControllerPackage;
	}

	public void setEnableControllerPackage(boolean enableControllerPackage) {
		this.enableControllerPackage = enableControllerPackage;
	}

	public Text getTxtControllerPackage() {
		return txtControllerPackage;
	}

	public void setTxtControllerPackage(Text txtControllerPackage) {
		this.txtControllerPackage = txtControllerPackage;
	}
}