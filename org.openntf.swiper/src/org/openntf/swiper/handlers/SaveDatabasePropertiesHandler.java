package org.openntf.swiper.handlers;

import java.io.InputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

public class SaveDatabasePropertiesHandler extends AbstractTeamHandler {

	private final String NSF_FILE_DBPROPS = "AppProperties/database.properties";
	private final String NSF_FILE_ICONNOTE = "Resources/IconNote";
	private final String NSF_FILE_DBICON = "AppProperties/$DBIcon";

	private final String SAVE_FILE_DBPROPS = "Swiper/%s.database.properties";
	private final String SAVE_FILE_ICONNOTE = "Swiper/%s.IconNote";
	private final String SAVE_FILE_DBICON = "Swiper/%s.$DBIcon";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		processSelectedProject(HandlerUtil.getCurrentSelection(event));

		if (this.desProject != null) {

			InputDialog dialog = new InputDialog(getShell(), "Codename", "Type the codename", "testing", null);

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

			// WHat about Resources/Images/$DBIcon
			/**
			 * also check out DominoDesignerProject resetDBIcon() and resetOldDBIcon()
			 */
			
		}

		return null;

	}

	private boolean saveDatabaseProperties(String prefix, IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_DBPROPS, prefix);

		IFile localIfile = this.desProject.getProject().getFile(NSF_FILE_DBPROPS);
		IFile destfile = this.desProject.getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor);

	}

	private boolean saveIconNote(String prefix, IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_ICONNOTE, prefix);

		IFile localIfile = this.desProject.getProject().getFile(NSF_FILE_ICONNOTE);
		IFile destfile = this.desProject.getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor);

	}

	private boolean saveDBIcon(String prefix, IProgressMonitor monitor) {

		String destFileName = String.format(SAVE_FILE_DBICON, prefix);

		IFile localIfile = this.desProject.getProject().getFile(NSF_FILE_DBICON);
		IFile destfile = this.desProject.getProject().getFile(destFileName);

		return saveElement(localIfile, destfile, monitor);

	}

	private boolean saveElement(IFile localIfile, IFile destfile, IProgressMonitor monitor) {

		NotesDesignElement element = DominoResourcesPlugin.getNotesDesignElement(localIfile);

		try {
			InputStream is = element.fetchSyncContent(0, monitor);

			if (destfile.exists()) {
				destfile.setContents(is, 0, monitor);
			} else {

				if (!destfile.getParent().exists() && destfile.getParent() instanceof IFolder) {

					IFolder folder = (IFolder) destfile.getParent();
					folder.create(false, false, monitor);

				}

				destfile.create(is, 0, monitor);
			}

			FilterMetadataAction.filterDiskFile(destfile, monitor);

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
