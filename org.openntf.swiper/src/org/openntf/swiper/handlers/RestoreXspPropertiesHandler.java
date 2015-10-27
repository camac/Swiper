package org.openntf.swiper.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.commons.ResourceHandler;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.jni.NotesDesignElement;
import com.ibm.designer.domino.ide.resources.util.NsfUtil;
import com.ibm.designer.domino.team.action.AbstractTeamHandler;
import com.ibm.designer.domino.team.util.SyncUtil;

import static org.openntf.swiper.util.SwiperUtil.*;

public class RestoreXspPropertiesHandler extends AbstractTeamHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		processSelectedProject(HandlerUtil.getCurrentSelection(event));

		IProgressMonitor monitor = new NullProgressMonitor();

		try {
			InputDialog dialog = new InputDialog(
					getShell(),
					"Prefix",
					"Enter a prefix of the xsp.properties file to restore."
							+ ". Letters and Numbers no spaces or "
							+ "special Characters", "example",
					new PropertiesPrefixInputValidator());

			if (dialog.open() != Window.OK) {
				return null;
			}

			String prefix = dialog.getValue();

			if (StringUtil.isEmpty(prefix)) {
				return null;
			}

			replaceDatabaseProperties(prefix, monitor);

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	private boolean replaceDatabaseProperties(String prefix,
			IProgressMonitor monitor) throws CoreException {

		String sourceFileName = String.format(SAVE_FILE_XSPPROPS, prefix);

		IFile nsfFile = this.desProject.getProject().getFile(NSF_FILE_XSPPROPS);
		IFile diskFile = this.desProject.getProject().getFile(sourceFileName);

		return replaceNsfContents(diskFile, nsfFile, monitor);

	}

	private boolean replaceNsfContents(IFile diskFile, IFile nsfFile,
			IProgressMonitor monitor) throws CoreException {

		if (!diskFile.exists() || !nsfFile.exists()) {
			System.out
					.println("For this to work, both NSF and DISK File need to exist");
			return false;
		}

		int saveOptions = 0;

		IProgressMonitor paramIProgressMonitor = new NullProgressMonitor();

		boolean needsLock = false;
		boolean lockResult = false;

		NotesDesignElement localNotesDesignElement = DominoResourcesPlugin
				.getNotesDesignElement(nsfFile);

		ByteStreamCache localByteStreamCache = new ByteStreamCache();

		try {

			needsLock = NsfUtil.needsImplicitLock(localNotesDesignElement);

			if (needsLock) {
				lockResult = NsfUtil
						.performLockOperation(localNotesDesignElement);
			}

			if ((!needsLock) || (lockResult)) {

				InputStream localInputStream = diskFile.getContents();
				StreamUtil.copyStream(localInputStream,
						localByteStreamCache.getOutputStream());
				localInputStream.close();

				// this.saveOptions is something to do with postProcessing /
				// maybe update timestamp?
				localNotesDesignElement.saveSyncContent(saveOptions,
						localByteStreamCache.toByteArray(),
						paramIProgressMonitor);

				localNotesDesignElement.refreshMetadata();

				SyncUtil.setModifiedBySync(nsfFile);
				SyncUtil.setSyncTimestamp(diskFile);
			}
			SyncUtil.logToConsole("locked " + lockResult + ":" + diskFile);
			if (lockResult) {
				NsfUtil.performUnLockOperation(localNotesDesignElement);
			}
			return true;
		} catch (IOException localIOException) {
			SyncUtil.logErrorToConsole(StringUtil.format(ResourceHandler
					.getString("ImportSyncOperation.Errorimporting0.4"),
					new Object[] { diskFile }), localIOException);
		} catch (NsfException localNsfException) {
			SyncUtil.logErrorToConsole(StringUtil.format(ResourceHandler
					.getString("ImportSyncOperation.Errorimporting0.5"),
					new Object[] { diskFile }), localNsfException);
		}
		return false;

	}

}
