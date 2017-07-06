package org.slerp.plugin.wizard.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * @since 1.0
 */
public class SWTUtil {
	/**
	 * Stores a work queue for each display
	 */
	private static Map<Display, WorkQueue> mapDisplayOntoWorkQueue = new HashMap<Display, WorkQueue>();

	private SWTUtil() {
	}

	/**
	 * Runs the given runnable on the given display as soon as possible. If
	 * possible, the runnable will be executed before the next widget is
	 * repainted, but this behavior is not guaranteed. Use this method to
	 * schedule work will affect the way one or more widgets are drawn.
	 * 
	 * <p>
	 * This is threadsafe.
	 * </p>
	 * 
	 * @param d
	 *            display
	 * @param r
	 *            runnable to execute in the UI thread.
	 */
	public static void greedyExec(Display d, Runnable r) {
		if (d.isDisposed()) {
			return;
		}

		WorkQueue queue = getQueueFor(d);
		queue.asyncExec(r);
	}

	/**
	 * Runs the given runnable on the given display as soon as possible. Unlike
	 * greedyExec, this has no effect if the given runnable has already been
	 * scheduled for execution. Use this method to schedule work that will
	 * affect the way one or more wigdets are drawn, but that should only happen
	 * once.
	 * 
	 * <p>
	 * This is threadsafe.
	 * </p>
	 * 
	 * @param d
	 *            display
	 * @param r
	 *            runnable to execute in the UI thread. Has no effect if the
	 *            given runnable has already been scheduled but has not yet run.
	 */
	public static void runOnce(Display d, Runnable r) {
		if (d.isDisposed()) {
			return;
		}
		WorkQueue queue = getQueueFor(d);
		queue.runOnce(r);
	}

	/**
	 * Cancels a greedyExec or runOnce that was previously scheduled on the
	 * given display. Has no effect if the given runnable is not in the queue
	 * for the given display
	 * 
	 * @param d
	 *            target display
	 * @param r
	 *            runnable to execute
	 */
	public static void cancelExec(Display d, Runnable r) {
		if (d.isDisposed()) {
			return;
		}
		WorkQueue queue = getQueueFor(d);
		queue.cancelExec(r);
	}

	/**
	 * Returns the work queue for the given display. Creates a work queue if
	 * none exists yet.
	 * 
	 * @param d
	 *            display to return queue for
	 * @return a work queue (never null)
	 */
	private static WorkQueue getQueueFor(final Display d) {
		WorkQueue result;
		synchronized (mapDisplayOntoWorkQueue) {
			// Look for existing queue
			result = (WorkQueue) mapDisplayOntoWorkQueue.get(d);

			if (result == null) {
				// If none, create new queue
				result = new WorkQueue(d);
				final WorkQueue q = result;
				mapDisplayOntoWorkQueue.put(d, result);
				d.asyncExec(new Runnable() {
					public void run() {
						d.disposeExec(new Runnable() {
							public void run() {
								synchronized (mapDisplayOntoWorkQueue) {
									q.cancelAll();
									mapDisplayOntoWorkQueue.remove(d);
								}
							}
						});
					}
				});
			}
			return result;
		}
	}

	/**
	 * @param rgb1
	 * @param rgb2
	 * @param ratio
	 * @return the RGB object
	 */
	public static RGB mix(RGB rgb1, RGB rgb2, double ratio) {
		return new RGB(interp(rgb1.red, rgb2.red, ratio), interp(rgb1.green, rgb2.green, ratio),
				interp(rgb1.blue, rgb2.blue, ratio));
	}

	private static int interp(int i1, int i2, double ratio) {
		int result = (int) (i1 * ratio + i2 * (1.0d - ratio));
		if (result < 0)
			result = 0;
		if (result > 255)
			result = 255;
		return result;
	}

	private static String KEY_PRESS = "Ctrl+Space";

	public static void setAutoCompletion(Text text, String value, String[] proposals) {
		try {
			ContentProposalAdapter adapter = null;

			SimpleContentProposalProvider scp = new SimpleContentProposalProvider(proposals);
			scp.setProposals(proposals);
			KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
			adapter = new ContentProposalAdapter(text, new TextContentAdapter(), scp, ks, null);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<IJavaProject> getJavaProjects(IWorkspaceRoot root) {
		List<IJavaProject> projectList = new LinkedList<IJavaProject>();
		try {
			IProject[] projects = root.getProjects();
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
					projectList.add(JavaCore.create(project));
				}
			}
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
		return projectList;
	}
}