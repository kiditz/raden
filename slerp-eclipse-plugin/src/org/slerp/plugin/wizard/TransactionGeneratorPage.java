package org.slerp.plugin.wizard;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
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
import org.slerp.core.Dto;
import org.slerp.utils.EntityUtils;

public class TransactionGeneratorPage extends BaseGenerator {

	private String transactionMode;
	private Group group;
	private List listSender;
	private List listReceiver;
	private Dto entityDto;
	boolean enablePrepare = false;

	public TransactionGeneratorPage(ISelection selection) {
		super(selection);
		setTitle("Entity Generator Wizard");
		setDescription("This wizard creates a new jpa entity class from postgres table.");
	}

	@Override
	public void createControl(Composite parent) {
		setEnableServicePackage(true);
		Composite container = bindParent(parent);
		gd = new GridData(SWT.CENTER, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 2;
		Composite compoRadio = new Composite(container, SWT.NONE);
		compoRadio.setLayoutData(gd);
		compoRadio.setLayout(new GridLayout(4, false));

		Button radSingle = new Button(compoRadio, SWT.RADIO);
		radSingle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		radSingle.setText("Add");
		radSingle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transactionMode = "Add";
			}
		});
		Button radPage = new Button(compoRadio, SWT.RADIO);
		radPage.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		radPage.setText("Edit");
		radPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transactionMode = "Edit";
			}
		});
		Button radRemove = new Button(compoRadio, SWT.RADIO);
		radRemove.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		radRemove.setText("Remove");
		radRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				transactionMode = "Remove";
			}
		});
		Button btnPrepare = new Button(compoRadio, SWT.CHECK);
		btnPrepare.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		btnPrepare.setText("Enable Prepare");
		btnPrepare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enablePrepare = true;
			}
		});

		group = new Group(container, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		group.setLayout(layout);
		group.setText("Choose Entity :");
		GridData data = GridDataFactory.fillDefaults().create();
		data.horizontalSpan = 3;
		data.heightHint = 200;
		group.setLayoutData(data);
		listSender = new List(group, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		listSender.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
		Button btnScanEntity = new Button(container, SWT.PUSH);
		btnScanEntity.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnScanEntity.setText("&Scan Entity");
		btnScanEntity.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				readEntity();
			}
		});

		setControl(container);
	}

	private void readEntity() {
		validate();
		updateStatus(null);
		File baseDir = getProject().getProject().findMember("src/main/java").getLocation().toFile();
		File entityDir = new File(baseDir, getTxtEntityPackage().getText().replace(".", "/"));
		try {
			entityDto = EntityUtils.readEntities(entityDir);
			for (Object element : entityDto.keySet()) {
				listSender.add(element.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	public IResource getApplicationProperties() {
		IResource applicationProperties = getProject().getProject()
				.findMember("src/test/resources/config.properties");
		return applicationProperties;
	}

	public String getTransactionMode() {
		return transactionMode;
	}

	public List getListReceiver() {
		return listReceiver;
	}

	public void setListReceiver(List listReceiver) {
		this.listReceiver = listReceiver;
	}

	public void setTransactionMode(String transactionMode) {
		this.transactionMode = transactionMode;
	}

	public Dto getEntityDto() {
		return entityDto;
	}

	public boolean isEnablePrepare() {
		return enablePrepare;
	}
}
