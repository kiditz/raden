package org.slerp.plugin.handler;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slerp.connection.JdbcConnection;
import org.slerp.core.CoreException;
import org.slerp.core.Dto;
import org.slerp.plugin.wizard.FunctionGeneratorPage;

public class InsertQueryFromSelectRealDatabaseDialog extends TitleAreaDialog {
	JdbcConnection connection;
	private List<StyleRange> list = new ArrayList<>();
	private Group tableContainer;
	private TableViewer viewer;
	private StyledText txtQuery;

	public InsertQueryFromSelectRealDatabaseDialog(Shell shell, File settingFile) {
		super(shell);
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

			for (int i = 1; i <= columnCount; i++) {

				TableViewerColumn column = createTableViewerColumn(meta.getColumnName(i), 100, i - 1);
				final String tableName = meta.getColumnName(i);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Dto dto = ((Dto) element);
						return dto.getString(tableName);
					}
				});

			}

			List<Dto> list = new ArrayList<Dto>();
			while (rs.next()) {
				Dto dto = new Dto();
				for (int i = 1; i <= columnCount; i++) {
					dto.put(meta.getColumnName(i), rs.getString(i));
				}
				list.add(dto);
			}

			viewer.setInput(list);

			final TableEditor editor = new TableEditor(table);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			table.addListener(SWT.MouseDown, new Listener() {
				public void handleEvent(Event event) {
					Rectangle clientArea = table.getClientArea();
					Point pt = new Point(event.x, event.y);
					int index = table.getTopIndex();
					while (index < table.getItemCount()) {
						boolean visible = false;

						final TableItem item = table.getItem(index);

						for (int i = 0; i < table.getColumnCount(); i++) {
							Rectangle rect = item.getBounds(i);
							if (rect.contains(pt)) {
								final int column = i;

								Text text = new Text(table, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
								text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
								Listener textListener = new Listener() {
									public void handleEvent(final Event e) {
										switch (e.type) {
										case SWT.FocusOut:
											item.setText(column, text.getText());
											text.dispose();
											break;
										case SWT.Traverse:
											switch (e.detail) {
											case SWT.TRAVERSE_RETURN:
												item.setText(column, text.getText());
												// FALL THROUGH
											case SWT.TRAVERSE_ESCAPE:
												text.dispose();
												e.doit = false;
											}
											break;
										}
									}
								};
								text.addListener(SWT.FocusOut, textListener);
								text.addListener(SWT.Traverse, textListener);
								editor.setEditor(text, item, i);
								text.setText(item.getText(i));
								text.selectAll();
								text.setFocus();
								text.redraw();
								text.update();
								if (text.isFocusControl()) {

								}

								return;
							}
							if (!visible && rect.intersects(clientArea)) {
								visible = true;
							}
						}
						if (!visible)
							return;
						index++;
					}
				}
			});
			tableContainer.layout();
		} catch (SQLException e) {
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

	public static void main(String[] args) {
		File settingFile = new File(
				"/home/kiditz/slerp-git/runtime-EclipseApplication/oauth/src/test/resources/application.properties");
		InsertQueryFromSelectRealDatabaseDialog dialog = new InsertQueryFromSelectRealDatabaseDialog(
				new Display().getActiveShell(), settingFile);
		dialog.open();
	}

	private StyleRange getHighlightStyle(int startOffset, int length) {
		StyleRange styleRange = new StyleRange();
		styleRange.start = startOffset;
		styleRange.length = length;
		styleRange.foreground = getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE);
		return styleRange;
	}

}
