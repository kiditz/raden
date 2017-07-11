package org.slerp.plugin.wizard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Text;
import org.slerp.core.Dto;
import org.slerp.generator.FunctionGenerator;
import org.slerp.generator.FunctionGenerator.FunctionType;
import org.slerp.generator.JUnitTestGenerator;
import org.slerp.plugin.wizard.utils.SWTUtil;

public class FunctionGeneratorPage extends BaseGenerator {

	private Text txtMethodName;
	private StyledText txtQuery;
	List<StyleRange> list = new ArrayList<>();
	FunctionType functionType = null;
	private Set<String> params = new LinkedHashSet<>();
	private Dto paramDto = new Dto();
	// get this array from
	// http://docs.oracle.com/html/E13946_04/ejb3_langref.html
	public static final String[] queryCompletion = new String[] { "SELECT", "FROM", "WHERE", "UPDATE", "DELETE",
			"JOIN", "OUTER", "INNER", "LEFT", "GROUP", "BY", "HAVING", "FETCH", "DISTINCT", "OBJECT", "NULL", "TRUE",
			"FALSE", "NOT", "AND", "OR", "LIKE", "IN", "AS", "UNKNOWN", "EMPTY", "MEMBER", "OF", "IS", "AVG", "MAX",
			"MIN", "SUM", "COUNT", "ORDER", "BY", "ASC", "DESC", "MOD", "UPPER", "LOWER", "TRIM", "POSITION",
			"CHARACTER_LENGTH", "CHAR_LENGTH", "BIT_LENGTH", "CURRENT_DATE", "CURRENT_TIMESTAMP", "NEW", "EXISTS",
			"ALL", "ANY", "SOME" };

	public FunctionGeneratorPage(ISelection selection) {
		super(selection);
		setTitle("Function Generator Wizard");
		setDescription("This wizard creates a new jpa business proccess class from jpa query.");
	}

	@Override
	public void createControl(Composite parent) {
		setEnableServicePackage(true);
		Composite container = bindParent(parent);
		new Label(container, SWT.NONE).setText("Method Name* ");
		txtMethodName = new Text(container, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		txtMethodName.setLayoutData(gd);
		txtMethodName.setText("");
		txtMethodName.setMessage("Fill with method name a.k.a findUserByUsername");
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
		gd.verticalAlignment = 9;
		gd.heightHint = 160;

		Composite queryCompo = new Composite(container, SWT.NONE);
		queryCompo.setLayoutData(gd);
		queryCompo.setLayout(new GridLayout(1, false));
		new Label(queryCompo, SWT.NONE).setText("Query* ");
		txtQuery = new StyledText(queryCompo, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		txtQuery.setLayoutData(gd);
		txtQuery.setText("");
		txtQuery.setFocus();
		txtQuery.addLineStyleListener(new LineStyleListener() {

			@Override
			public void lineGetStyle(LineStyleEvent event) {
				if (txtQuery.getText().isEmpty()) {
					event.styles = new StyleRange[0];
					return;
				}

				String line = event.lineText;
				int cursor = -1;
				for (int i = 0; i < queryCompletion.length; i++) {
					while ((cursor = line.indexOf(queryCompletion[i], cursor + 1)) >= 0) {
						list.add(getHighlightStyle(event.lineOffset + cursor, queryCompletion[i].length()));
					}
				}

				event.styles = (StyleRange[]) list.toArray(new StyleRange[list.size()]);
			}
		});

		gd = new GridData(SWT.CENTER, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
		gd.verticalSpan = 2;
		Composite compoRadio = new Composite(container, SWT.NONE);
		compoRadio.setLayoutData(gd);
		compoRadio.setLayout(new GridLayout(3, false));

		Button radSingle = new Button(compoRadio, SWT.RADIO);
		radSingle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		radSingle.setText("Single");
		radSingle.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				functionType = FunctionType.SINGLE;
			}
		});
		Button radPage = new Button(compoRadio, SWT.RADIO);
		radPage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		radPage.setText("Page");
		radPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				functionType = FunctionType.PAGE;
			}
		});
		Button radList = new Button(compoRadio, SWT.RADIO);
		radList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		radList.setText("List");
		radList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				functionType = FunctionType.LIST;
			}
		});

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 3;
		gd.heightHint = 60;
		ScrolledComposite sc = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayoutData(gd);

		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		Group content = new Group(sc, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		content.setLayout(new GridLayout(1, false));
		content.setText("Parameter Type");
		sc.setContent(content);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		content.setSize(point);
		sc.setMinSize(point);

		//
		Button btnRefresh = new Button(container, SWT.PUSH);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 3;
		btnRefresh.setLayoutData(gd);
		btnRefresh.setText("Refresh");
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (int i = 0; i < content.getChildren().length; i++) {
					content.getChildren()[i].dispose();
				}
				params.addAll(FunctionGenerator.getParamsByQuery(txtQuery.getText()));
				if (params != null && !params.isEmpty()) {
					for (String param : params) {
						Text txtParam = new Text(content, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
						txtParam.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
						txtParam.setMessage("Data type for parameter :" + param);
						SWTUtil.setAutoCompletion(txtParam,
								JUnitTestGenerator.primitivType.keySet().toArray(new String[] {}));
						txtParam.addModifyListener(new ModifyListener() {

							@Override
							public void modifyText(ModifyEvent arg0) {
								String type = JUnitTestGenerator.primitivType.get(txtParam.getText());
								if (type != null) {
									try {
										String simpleType = Class.forName(type).getSimpleName();
										paramDto.put(param, simpleType);
									} catch (ClassNotFoundException e) {
									}
								}
							}
						});
						Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						content.setSize(point);
						sc.setMinSize(point);
						sc.layout();
					}

				}
			}
		});
		setControl(container);
	}

	private StyleRange getHighlightStyle(int startOffset, int length) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = startOffset;
		styleRange.length = length;
		styleRange.foreground = getControl().getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE);
		return styleRange;
	}

	public IResource getApplicationProperties() {
		IResource applicationProperties = getProject().getProject()
				.findMember("src/test/resources/application.properties");
		return applicationProperties;
	}

	public Text getTxtMethodName() {
		return txtMethodName;
	}

	public void setTxtMethodName(Text txtMethodName) {
		this.txtMethodName = txtMethodName;
	}

	public StyledText getTxtQuery() {
		return txtQuery;
	}

	public void setTxtQuery(StyledText txtQuery) {
		this.txtQuery = txtQuery;
	}

	public Dto getParamDto() {
		return paramDto;
	}

}
