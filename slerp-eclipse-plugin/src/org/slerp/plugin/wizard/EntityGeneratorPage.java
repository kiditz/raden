package org.slerp.plugin.wizard;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.slerp.connection.JdbcConnection;
import org.slerp.core.CoreException;
import org.slerp.model.JdbcTable;
import org.slerp.plugin.handler.DatabaseSettingDialog;

public class EntityGeneratorPage extends BaseGenerator {
	private List listSender;
	private List listReceiver;
	private Group group;

	public EntityGeneratorPage(ISelection selection) {
		super(selection);
		setTitle("Entity Generator Wizard");
		setDescription("This wizard creates a new jpa entity class from postgres table.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = bindParent(parent);
		group = new Group(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		group.setLayout(layout);
		GridData data = GridDataFactory.fillDefaults().create();
		data.horizontalSpan = 3;
		data.heightHint = 200;
		group.setLayoutData(data);
		listSender = new List(group, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		listSender.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// listSender.setItems(new String[] { "Test 1", "Test 2", "Test 3" });
		Composite center = new Composite(group, SWT.NONE);
		center.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		center.setLayout(new GridLayout(1, false));

		Button btnAdd = new Button(center, SWT.PUSH);
		btnAdd.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		btnAdd.setText(">");
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateList(false);
			}
		});
		Button btnRemove = new Button(center, SWT.PUSH);
		btnRemove.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		btnRemove.setText("<");
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateList(true);
			}
		});

		listReceiver = new List(group, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		listReceiver.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button btnConnect = new Button(container, SWT.PUSH);
		btnConnect.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnConnect.setText("&Connect");
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					handleConnection(e);
				} catch (CoreException e2) {
					updateStatus("Failed to connect, Please set click database setting to hanle connection");
					return;
				}
				updateStatus(null);
			}
		});
		btnConnect.setFocus();

		setControl(container);

	}

	private void updateList(boolean isRemove) {
		if (!isRemove) {
			String selected[] = listSender.getSelection();
			for (int i = 0; i < selected.length; i++) {
				listReceiver.add(selected[i]);
			}
			listSender.remove(listSender.getSelectionIndices());
		} else {
			String selected[] = listReceiver.getSelection();
			for (int i = 0; i < selected.length; i++) {
				listSender.add(selected[i]);
			}
			listReceiver.remove(listReceiver.getSelectionIndices());
		}
	}

	protected void handleConnection(SelectionEvent e) {
		IJavaProject project = getProject();
		if (project != null) {
			IResource applicationProperties = getApplicationProperties();
			try {
				// System.err.println(applicationProperties.getLocation());
				if (applicationProperties != null && applicationProperties.exists()) {
					JdbcConnection connection = new JdbcConnection(applicationProperties.getLocation().toOSString());
					java.util.List<JdbcTable> tables = connection.getTables();
					for (JdbcTable table : tables) {
						listSender.add(table.getTableName());
					}
					group.layout();
				} else {
					updateStatus("Cannot found config.properties file");
					return;
				}
				updateStatus(null);
			} catch (Exception e2) {
				Properties properties = new Properties();
				if (applicationProperties != null && applicationProperties.exists()) {
					try {
						properties.load(new FileInputStream(applicationProperties.getLocation().toFile()));
						DatabaseSettingDialog dialog = new DatabaseSettingDialog(getShell(), properties);
						dialog.setOutputPropertiesFile((IFile) applicationProperties);
						dialog.open();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					updateStatus("Close this and Right click in the config.properties -> Database Setting");
					return;
				}
				updateStatus(null);
			}

		}
	}

	public List getListReceiver() {
		return listReceiver;
	}

	public IResource getApplicationProperties() {
		IResource applicationProperties = getProject().getProject()
				.findMember("src/test/resources/config.properties");
		return applicationProperties;
	}
}
