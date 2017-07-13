package org.slerp.plugin.wizard;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.generator.JUnitTestGenerator;
import org.slerp.plugin.wizard.utils.SWTUtil;

public class UnitTestGeneratorPage extends BaseGenerator {

	private String transactionMode;
	private Group group;
	private List listSender;
	private List listReceiver;
	private Dto serviceDto;
	private JUnitTestGenerator generator;
<<<<<<< HEAD
	private Button btnScanService;	
=======
	private Button btnScanService;
	private Button btnRefresh;
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
	private Group content;
	private ScrolledComposite sc;
	private Set<Dto> fields = new HashSet<>();

	public UnitTestGeneratorPage(ISelection selection) {
		super(selection);
		setTitle("JUnit Testing Generator Wizard");
		setDescription("This wizard creates a new unit test from business object.");
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

		group = new Group(container, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		group.setLayout(layout);
		group.setText("Choose Business Object ");
		GridData data = GridDataFactory.fillDefaults().create();
		data.horizontalSpan = 3;
		// data.heightHint = 70;
		group.setLayoutData(data);
		listSender = new List(group, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		listSender.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite center = new Composite(group, SWT.NONE);
		center.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
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
		listReceiver.setToolTipText("Select one class in here");
		btnScanService = new Button(container, SWT.PUSH);
		btnScanService.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnScanService.setText("&Scan Business");
		btnScanService.addSelectionListener(new SelectionAdapter() {
<<<<<<< HEAD
=======

>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				readBusiness();
			}
		});
<<<<<<< HEAD
		listReceiver.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					handleParser();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

=======
		btnRefresh = new Button(container, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 3;
		btnRefresh.setLayoutData(gd);
		btnRefresh.setText("Refresh");
		btnRefresh.setEnabled(false);
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					handleParser();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		});
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
		gd.heightHint = 50;
		sc = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayoutData(gd);

		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		content = new Group(sc, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		content.setLayout(new GridLayout(3, false));
		content.setText("Value & Data Type");
		sc.setContent(content);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		sc.setMinSize(point);
		setControl(container);
	}

	protected void handleParser() throws ClassNotFoundException {
		validate();
		if (generator == null) {
			updateStatus("Please select one in the receiver class");
<<<<<<< HEAD
=======
			listReceiver.setToolTipText("Select one class in here");
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
			btnScanService.setFocus();
			return;
		}
		int index = listReceiver.getSelectionIndex();
		if (index < 0) {
			updateStatus("Please select one in the receiver class");
			btnScanService.setFocus();
			return;
		}
		for (int i = 0; i < content.getChildren().length; i++) {
			content.getChildren()[i].dispose();
		}
		updateStatus(null);
		String className = listReceiver.getItem(index);
		try {
			generator.parse(className);
		} catch (IOException e) {
			throw new CoreException(e);
		}

		fields.clear();
		fields.addAll(generator.getFields());

		for (Dto field : fields) {
			// if (field.getString("fieldType").equals("java.lang.Object")) {
			String fieldName = field.getString("fieldName");
			String fieldType = field.getString("fieldType");
<<<<<<< HEAD
			System.out.println(fieldType);
			new Label(content, SWT.NONE).setText(fieldName);
			Text txtDataType = new Text(content, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtDataType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			SWTUtil.setAutoCompletion(txtDataType, JUnitTestGenerator.primitivType.values().toArray(new String[] {}));
			try {
				Class<?> clz = Class.forName(fieldType);
				txtDataType.setText(clz.getSimpleName());
			} catch (Exception e) {
				txtDataType.setText(fieldType);
			}

			txtDataType.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					String dataType = txtDataType.getText();
					if (JUnitTestGenerator.primitivType.get(txtDataType.getText()) != null) {
						dataType = JUnitTestGenerator.primitivType.get(txtDataType.getText());
					}
					field.put("fieldType", dataType);
				}
			});
=======
			new Label(content, SWT.NONE).setText(fieldName);
			Text txtDataType = new Text(content, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtDataType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			Class<?> clz = Class.forName(fieldType);
			SWTUtil.setAutoCompletion(txtDataType, JUnitTestGenerator.primitivType.values().toArray(new String[] {}));
			txtDataType.setText(clz.getSimpleName());
			String dataType = JUnitTestGenerator.primitivType.get(txtDataType.getText());
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
			Text txtValue = new Text(content, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
			txtValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			txtValue.setMessage("value for " + fieldName);
			txtValue.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent arg0) {
					field.put("value", txtValue.getText());
				}
			});
<<<<<<< HEAD

=======
			txtDataType.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent arg0) {
					field.put("fieldType", dataType);
				}
			});
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
		}

		fields.forEach(f -> {
			System.out.println(f.toString());
		});
		group.layout();
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		sc.setMinSize(point);
		sc.layout();
	}

	private void readBusiness() {
		validate();
<<<<<<< HEAD

		String baseDir = getProject().getProject().findMember("src/main/java").getLocation().toOSString();
		final String entityPackage = getTxtEntityPackage().getText();
		final String repositoryPackage = getTxtRepoPackage().getText();
		final String servicePackage = getTxtServicePackage().getText();
		generator = new JUnitTestGenerator(baseDir, entityPackage, repositoryPackage, servicePackage);
		if(serviceDto != null)
			serviceDto.clear();
		serviceDto = generator.getBusiness();
		
		for (Object element : serviceDto.keySet()) {
			System.out.println("{" + element.toString() + "}");
			listSender.add(element.toString().trim());
=======
		this.btnRefresh.setEnabled(true);
		String baseDir = getProject().getProject().findMember("src/main/java").getLocation().toOSString();
		generator = new JUnitTestGenerator(baseDir, getTxtEntityPackage().getText(), getTxtRepoPackage().getText(),
				getTxtServicePackage().getText());

		serviceDto = generator.getBusiness();
		for (Object element : serviceDto.keySet()) {
			listSender.add(element.toString());
>>>>>>> 46384b33f249163934744a89a9ee61aade39c9c5
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
				.findMember("src/test/resources/application.properties");
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

	public JUnitTestGenerator getGenerator() {
		return generator;
	}
}
