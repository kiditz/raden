package org.slerp.plugin.handler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
<<<<<<< HEAD
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
=======
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slerp.plugin.wizard.utils.ConnectionUtils;
import org.slerp.plugin.wizard.utils.ConnectionUtils.Setting;

public class DatabaseSettingDialog extends TitleAreaDialog {
	Properties properties;
	private Text txtPath;
	private Text txtPassword;
	private Text txtUsername;
	private Text txtUrl;
	private Text txtDriver;
	Setting setting = new Setting();
	IFile outputPropertiesFile;

	public DatabaseSettingDialog(Shell parentShell, Properties properties) {
		super(parentShell);
		this.properties = properties;

	}

	public void setOutputPropertiesFile(IFile outputPropertiesFile) {
		this.outputPropertiesFile = outputPropertiesFile;
	}

	public IFile getOutputPropertiesFile() {
		return outputPropertiesFile;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Setting the application properties and test the connection");

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(composite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Label lblJarFile = new Label(container, SWT.NONE);
		lblJarFile.setText("Jar File * ");
		txtPath = new Text(container, SWT.FILL);
		txtPath.setLayoutData(gd);
		Button btnBrowseJar = new Button(container, SWT.PUSH);
		btnBrowseJar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnBrowseJar.setText("&Browse");
		btnBrowseJar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseJar();
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		// DRIVER
		Label lblDriver = new Label(container, SWT.NONE);
		lblDriver.setText("Database Driver * ");
		txtDriver = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtDriver.setLayoutData(gd);
<<<<<<< HEAD

=======
		
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		// URL
		Label lblUrl = new Label(container, SWT.NONE);
		lblUrl.setText("Url * ");
		txtUrl = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtUrl.setLayoutData(gd);
<<<<<<< HEAD

=======
		
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		// Username
		Label lblUsername = new Label(container, SWT.NONE);
		lblUsername.setText("Username * ");
		txtUsername = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtUsername.setLayoutData(gd);
<<<<<<< HEAD

=======
		
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		// Password
		Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText("Password * ");
		txtPassword = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.PASSWORD);
		txtPassword.setLayoutData(gd);
<<<<<<< HEAD

=======
	
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		try {
			bindPropertiesToUI();
		} catch (Exception e) {
			e.printStackTrace();
			initConnection();
		}
		initConnection();
		modifyListener();
		return container;
	}
<<<<<<< HEAD

	private void initConnection() {
=======
	private void initConnection(){
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		txtDriver.setText("org.postgresql.Driver");
		txtUrl.setText("jdbc:postgresql://localhost:5432/database");
		txtUsername.setText("postgres");
		txtPassword.setText("postgres");
	}
<<<<<<< HEAD

=======
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
	private void modifyListener() {

	}

	private void bindPropertiesToUI() {
		if (properties == null)
			return;
		String path = properties.getProperty("slerp.database.path");
		if (path != null)
			txtPath.setText(path);
		String driver = properties.getProperty("spring.database.driverClassName");
		if (driver != null)
			txtDriver.setText(driver);
		String url = properties.getProperty("spring.datasource.url");
		if (url != null)
			txtUrl.setText(url);
		String username = properties.getProperty("spring.datasource.username");
		if (username != null)
			txtUsername.setText(username);
		String password = properties.getProperty("spring.datasource.password");
		if (password != null)
			txtPassword.setText(password);
	}

	private void putToProperties() {
		properties.put("slerp.database.path", setting.pathToJar);
		properties.put("spring.database.driverClassName", setting.driverClassName);
		properties.put("spring.datasource.url", setting.url);
		properties.put("spring.datasource.username", setting.username);
		properties.put("spring.datasource.password", setting.password);
	}

	private void bindTextToSetting() {
		setting.pathToJar = txtPath.getText();
		setting.driverClassName = txtDriver.getText();
		setting.url = txtUrl.getText();
		setting.username = txtUsername.getText();
		setting.password = txtPassword.getText();
		System.err.println(setting.toString());
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createButton(parent, IDialogConstants.NO_ID, "Test Connection", true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (txtPath.getText().isEmpty()) {
				updateError("The driver path cannot be empty");
				return;
			}
			createOutputFile();
			super.buttonPressed(buttonId);
		} else if (buttonId == IDialogConstants.NO_ID) {
			if (txtPath.getText().isEmpty()) {
				updateError("The driver path cannot be empty");
				return;
			}
			createConnection();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	private void createOutputFile() {
		try {
			putToProperties();
			IFile file = getOutputPropertiesFile();
			StringWriter writer = new StringWriter();
			properties.store(writer, "Update Properties file with slerp database setting");
			FileWriter fileWriter = new FileWriter(file.getLocation().toFile());
			fileWriter.write(writer.toString().replace("\\", ""));
			fileWriter.close();
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
			IDE.openEditor(page, file);
		} catch (IOException e) {
			updateError("Failed to write " + getOutputPropertiesFile().getName() + " Cause :" + e.getMessage());
		} catch (PartInitException e) {
			updateError("Failed to open file " + getOutputPropertiesFile().getName());
		} catch (CoreException e) {
			updateError("Failed to refresh properties file");
		}
	}

	private void createConnection() {
		try {
			bindTextToSetting();
			Connection connection = ConnectionUtils.getConnection(setting);
			if (connection != null)
				updateSuccess("Connection " + txtUrl.getText() + " has been success");

		} catch (Exception e1) {
			updateError("Connection Error for " + txtUrl.getText() + "\n" + e1.toString());
			e1.printStackTrace();
		}
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 350);
	}

	private void updateError(String message) {
		MessageDialog.openError(getShell(), "Error", message);
	}

	private void updateSuccess(String message) {
		MessageDialog.openInformation(getShell(), "Success", message);
	}

	private void browseJar() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText("Select the jdbc driver to create connection");
		dialog.setFilterExtensions(new String[] { "*.jar" });
		Path currPath = new Path(txtPath.getText());
		dialog.setFilterPath(currPath.toOSString());
		String res = dialog.open();
		if (res != null) {
			String file = Path.fromOSString(res).makeAbsolute().toOSString();
			txtPath.setText(file);

		}
	}

<<<<<<< HEAD
	public static void main(String[] args) {
		new DatabaseSettingDialog(new Display().getActiveShell(), null).open();
	}

	
=======
	public static void main(String[] args) {		
		new DatabaseSettingDialog(new Display().getActiveShell(), null).open();
	}

>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
}
