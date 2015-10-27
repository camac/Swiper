package org.openntf.swiper.handlers;

import java.io.InputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openntf.swiper.action.FilterMetadataAction;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.jni.NotesDesignElement;
import com.ibm.designer.domino.team.action.AbstractTeamHandler;

import com.ibm.designer.domino.team.util.SyncUtil;

import static org.openntf.swiper.util.SwiperUtil.*;

public class SaveDatabasePropertiesHandler extends AbstractTeamHandler {

	private boolean odponly = false;

	private IProject diskProject = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		processSelectedProject(HandlerUtil.getCurrentSelection(event));

		if (this.desProject != null) {

			try {
				this.diskProject = SyncUtil.getAssociatedDiskProject(
						this.desProject, false);
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}

			InputDialog dialog = new InputDialog(
					getShell(),
					"Prefix",
					"Enter a prefix for saving the database property "
							+ " and icon files. Letters and Numbers no spaces or "
							+ "special Characters", "example",
					new PropertiesPrefixInputValidator());

			if (dialog.open() != Window.OK) {
				return null;
			}

			String prefix = dialog.getValue();

			if (StringUtil.isEmpty(prefix)) {
				return null;
			}

			IProgressMonitor monitor = new NullProgressMonitor();

			saveDatabaseProperties(prefix, monitor);

			saveIconNote(prefix, monitor);

			saveDBIcon(prefix, monitor);

			saveResImgDBIcon(prefix, monitor);

			/**
			 * also check out DominoDesignerProject resetDBIcon() and
			 * resetOldDBIcon()
			 */

		}

		return null;

	}

	private IProject getProject() {
		if (odponly) {
			return this.diskProject;
		} else {
			return this.desProject.getProject();
		}
	}

	private boolean saveDatabaseProperties(String prefix,
			IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_DBPROPS, prefix);

		IFile localIfile = getProject().getFile(NSF_FILE_DBPROPS);
		IFile destfile = getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor, true);

	}

	private boolean saveIconNote(String prefix, IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_ICONNOTE, prefix);

		IFile localIfile = getProject().getFile(NSF_FILE_ICONNOTE);
		IFile destfile = getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor, true);

	}

	private boolean saveDBIcon(String prefix, IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_DBICON, prefix);

		IFile localIfile = getProject().getFile(NSF_FILE_DBICON);
		IFile destfile = getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor, false);

	}

	private boolean saveResImgDBIcon(String prefix, IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_RESDBICON, prefix);

		IFile localIfile = getProject().getFile(NSF_FILE_RESDBICON);
		IFile destfile = getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor, false);

	}

	private boolean simpleSave(IFile localIfile, IFile destfile,
			IProgressMonitor monitor, boolean runFilter) {

		if (!localIfile.exists()) return false;
		
		try {

			InputStream is = localIfile.getContents();

			if (destfile.exists()) {
				destfile.setContents(is, 0, monitor);
			} else {

				if (!destfile.getParent().exists()
						&& destfile.getParent() instanceof IFolder) {

					IFolder folder = (IFolder) destfile.getParent();
					folder.create(false, false, monitor);

				}

				destfile.create(is, 0, monitor);
				
				SyncUtil.setModifiedBySync(destfile);
			}

			if (runFilter) {
				FilterMetadataAction.filterDiskFile(destfile, monitor);
			}

		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	private boolean saveElement(IFile localIfile, IFile destfile,
			IProgressMonitor monitor, boolean runFilter) {

		if (odponly)
			return simpleSave(localIfile, destfile, monitor, runFilter);

		NotesDesignElement element = DominoResourcesPlugin
				.getNotesDesignElement(localIfile);

		try {
			InputStream is = element.fetchSyncContent(0, monitor);

			if (destfile.exists()) {
				destfile.setContents(is, 0, monitor);
			} else {

				if (!destfile.getParent().exists()
						&& destfile.getParent() instanceof IFolder) {

					IFolder folder = (IFolder) destfile.getParent();
					folder.create(false, false, monitor);

				}

				destfile.create(is, 0, monitor);
			}

			if (runFilter) {
				FilterMetadataAction.filterDiskFile(destfile, monitor);
			}

		} catch (NsfException e) {
			e.printStackTrace();
			return false;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

}
