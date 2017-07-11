package org.slerp.plugin.wizard;

import java.io.IOException;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.generator.ApiGenerator;
import org.slerp.generator.JUnitTestGenerator;
import org.slerp.utils.JpaParser;

public class RestApiGeneratorPage extends BaseGenerator {
	private ApiGenerator generator = null;
	private ScrolledComposite sc;
	private Composite content;
	private List listService;

	public RestApiGeneratorPage(ISelection selection) {
		super(selection);
	}

	@Override
	public void createControl(Composite parent) {
		setEnableServicePackage(true);
		setEnableApiProject(true);
		setEnableControllerPackage(true);
		Composite container = bindParent(parent);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 2;
		Composite composite = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(gd);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalSpan = 2;
		gd.heightHint = 100;

		listService = new List(composite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		listService.setLayoutData(gd);

		sc = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayoutData(gd);

		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		content = new Composite(sc, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		content.setLayout(new GridLayout(3, false));
		sc.setContent(content);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		sc.setMinSize(point);
		Button btnScan = new Button(composite, SWT.PUSH);
		btnScan.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		btnScan.setText("&Scan Service");
		btnScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleGenerator();
			}
		});
		setControl(container);
	}

	private void handleGenerator() {		
		String apiProjectDir = getApiProject().getProject().findMember("src/main/java").getLocation().toOSString();
		String serviceProjectDir = getProject().getProject().findMember("src/main/java").getLocation().toOSString();
		String packageEntity = getTxtEntityPackage().getText();
		String packageRepository = getTxtRepoPackage().getText();
		String packageService = getTxtServicePackage().getText();
		String packageController = getTxtControllerPackage().getText();
		validate();
		generator = new ApiGenerator(apiProjectDir, serviceProjectDir, packageEntity, packageRepository, packageService,
				packageController);
		try {
			generator.parse();
			for (int i = 0; i < generator.getParsers().size(); i++) {
				JpaParser parser = generator.getParsers().get(i);
				listService.add(parser.getService().getString("className"));
				for (Dto field : parser.getFields()) {
					if (field.getString("fieldType").equals("java.lang.Object")) {
						new Label(content, SWT.NONE).setText("Data Type * ");
						Text txtDataType = new Text(content, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
						txtDataType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
						txtDataType.setText("");
						txtDataType.addModifyListener(new ModifyListener() {

							@Override
							public void modifyText(ModifyEvent arg0) {
								field.put("fieldType", JUnitTestGenerator.primitivType.get(txtDataType.getText()));
							}
						});
					}
				}
			}
		} catch (IOException e1) {
			throw new CoreException(e1);
		}

	}

	public ApiGenerator getGenerator() {
		return generator;
	}

}
