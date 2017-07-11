package org.slerp.plugin.handler;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class DatabaseSettingHandler extends AbstractHandler {
	IFile file;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelectionService service = window.getSelectionService();
		// set structured selection
		IStructuredSelection structured = (IStructuredSelection) service.getSelection();
		if (structured.getFirstElement() instanceof IFile) {
			// get the selected file
			IFile file = (IFile) structured.getFirstElement();
			// get the path
			IPath path = file.getLocation();
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(path.toFile()));
				DatabaseSettingDialog dialog = new DatabaseSettingDialog(window.getShell(), properties);
				dialog.setOutputPropertiesFile(file);
				dialog.open();				
			} catch (IOException e) {
				MessageDialog.openError(window.getShell(), "Error", "Failed to read file properties");
			}
		}

		return null;
	}
}
