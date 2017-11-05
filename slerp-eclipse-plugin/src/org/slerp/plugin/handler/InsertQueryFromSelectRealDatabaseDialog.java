package org.slerp.plugin.handler;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.slerp.connection.JdbcConnection;
import org.slerp.core.CoreException;
import org.slerp.plugin.wizard.FunctionGeneratorPage;
import org.springframework.util.StringUtils;

public class InsertQueryFromSelectRealDatabaseDialog extends TitleAreaDialog {
	JdbcConnection connection;
	private final StringBuilder builder = new StringBuilder();
	private final List<StyleRange> list = new ArrayList<>();
	private Group tableContainer;
	private TableViewer viewer;
	private StyledText txtQuery;
	private String resultQuery;
	IFile outputFile;
	IWorkbenchWindow window;

	public InsertQueryFromSelectRealDatabaseDialog(IWorkbenchWindow window, File settingFile) {
		super(window.getShell());
		this.window = window;
		if (settingFile == null)
			throw new CoreException("Setting file is required");
		this.connection = new JdbcConnection(settingFile.getAbsolutePath());
	}

	@Override
	public void create() {
		super.create();
		setTitle("Insert Query From Select");
		setMessage("This menu can be create insert query from your database recursively by using select query");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite((Composite) super.createDialogArea(parent), SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		container.setLayout(layout);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		new Label(container, SWT.NONE).setText("Query");

		txtQuery = new StyledText(container, SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		txtQuery.setLayoutData(gd);
		txtQuery.addLineStyleListener(new LineStyleListener() {
			@Override
			public void lineGetStyle(LineStyleEvent event) {
				if (txtQuery.getText().isEmpty()) {
					event.styles = new StyleRange[0];
					return;
				}

				String line = event.lineText;
				int cursor = -1;
				for (int i = 0; i < FunctionGeneratorPage.queryCompletion.length; i++) {
					while ((cursor = line.indexOf(FunctionGeneratorPage.queryCompletion[i], cursor + 1)) >= 0) {
						list.add(getHighlightStyle(event.lineOffset + cursor,
								FunctionGeneratorPage.queryCompletion[i].length()));
					}
				}

				event.styles = (StyleRange[]) list.toArray(new StyleRange[list.size()]);
			}
		});

		tableContainer = new Group(container, SWT.NONE);
		tableContainer.setText("Table Result");
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 9;
		tableContainer.setLayout(layout);
		tableContainer.setLayoutData(gd);
		viewer = new TableViewer(tableContainer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.heightHint = 100;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		txtQuery.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.stateMask == SWT.CTRL && e.keyCode == SWT.SPACE) {
					handleTable();
				}
			}
		});

		return container;
	}

	protected void handleTable() {
		Table table = viewer.getTable();
		while (table.getColumnCount() > 0) {
			table.getColumns()[0].dispose();
		}
		Connection conn = connection.getConnection();
		try {
			ResultSet rs = conn.createStatement().executeQuery(txtQuery.getText());
			ResultSetMetaData meta = rs.getMetaData();
			int columnCount = meta.getColumnCount();
			builder.setLength(0);
			builder.append("INSERT INTO ").append(meta.getTableName(1));
			builder.append(" (");
			builder.append(meta.getColumnName(1));			
			int index = 2;
			for (int i = 1; i <= columnCount; i++) {
				String columnName = meta.getColumnName(i);
				builder.append(", ").append(meta.getColumnName(index++));
				if(index > columnCount)
					index = 0;
				index++;
				TableViewerColumn column = createTableViewerColumn(columnName, 100, i - 1);
				final String tableName = meta.getColumnName(i);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						@SuppressWarnings("unchecked")
						HashMap<String, Object> dto = ((HashMap<String, Object>) element);
						return dto.get(tableName) != null ? dto.get(tableName).toString() :"null" ;
					}
				});
			}
			builder.append(") VALUES ");

			List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

			while (rs.next()) {
				
				builder.append("\n(");
				if (meta.getColumnTypeName(1).equalsIgnoreCase("varchar")
						|| meta.getColumnTypeName(1).equalsIgnoreCase("text")
						|| meta.getColumnTypeName(1).equalsIgnoreCase("bpchar")
						|| meta.getColumnTypeName(1).equalsIgnoreCase("timestamp")
						|| meta.getColumnTypeName(1).equalsIgnoreCase("timestamptz")
						|| meta.getColumnTypeName(1).equalsIgnoreCase("bytea")
						|| meta.getColumnTypeName(1).equalsIgnoreCase("date")) {
					builder.append("'").append(rs.getString(1)).append("'");
				} else {
					builder.append(rs.getString(1));
				}
				HashMap<String, Object> map = new HashMap<>();
				for (int i = 1; i <= columnCount; i++) {
					//System.err.println(rs.getString(i));
					//System.err.println(meta.getColumnName(i));
					map.put(meta.getColumnName(i), rs.getString(i));
				}
				for (int i = 2; i <= columnCount; i++) {
					builder.append(", ");
					if (meta.getColumnTypeName(i).equalsIgnoreCase("varchar")
							|| meta.getColumnTypeName(i).equalsIgnoreCase("text")
							|| meta.getColumnTypeName(i).equalsIgnoreCase("bpchar")
							|| meta.getColumnTypeName(i).equalsIgnoreCase("timestamp")
							|| meta.getColumnTypeName(i).equalsIgnoreCase("timestamptz")
							|| meta.getColumnTypeName(i).equalsIgnoreCase("bytea")
							|| meta.getColumnTypeName(i).equalsIgnoreCase("date")) {
						if (rs.getString(i) != null) {
							builder.append("'").append(rs.getString(i)).append("'");
						} else {
							builder.append(rs.getString(i));
						}
					} else {
						builder.append(rs.getString(i));
					}
				}
				builder.append("),");
				list.add(map);
			}

			resultQuery = builder.toString().substring(0, builder.toString().length() - 1).concat(";");
			System.out.println(resultQuery);
			viewer.setInput(list);

		} catch (

		SQLException e) {
			MessageDialog.openError(getShell(), "Error!!", e.toString());
			return;
		}
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final org.eclipse.swt.widgets.TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(200);
		column.setResizable(true);
		column.setMoveable(true);

		return viewerColumn;
	}

	private StyleRange getHighlightStyle(int startOffset, int length) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = startOffset;
		styleRange.length = length;
		styleRange.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE);
		return styleRange;
	}

	public String getInsertQuery() {
		return resultQuery;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.NO_ID, "Preview", false);
		super.createButtonsForButtonBar(parent);
		getButton(Window.OK).setText("Save");
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		if (buttonId == IDialogConstants.OK_ID) {
			if (getInsertQuery().toString() == null || StringUtils.isEmpty(getInsertQuery().toString())) {
				MessageDialog.openError(getShell(), "We cannot recognize your request",
						"Just try to use select query and get insert query in your sql file!!");
				return;
			}
			try {
				FileWriter writer = new FileWriter(outputFile.getLocation().toFile(), true);
				writer.append("\n");
				writer.append(getInsertQuery());
				writer.close();
				if (outputFile != null)
					outputFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				IDE.openEditor(window.getActivePage(), outputFile);
			} catch (Exception e) {
				System.err.println(getInsertQuery());
				throw new CoreException("Failed to open file ", e);
			}
		}
	}

	public void setOutputFile(IFile outputFile) {
		this.outputFile = outputFile;
	}

//	public static void main(String[] args) {
//		TitleAreaDialog dialog = new InsertQueryFromSelectRealDatabaseDialog(new Shell(), new File("/home/kiditz/slerpio/slerp-io-service/src/test/resources/config.properties"));
//		dialog.open();
//	}	
}
