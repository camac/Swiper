package org.openntf.swiper.handlers;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openntf.swiper.action.FilterMetadataAction;

import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.jni.NotesDesignElement;
import com.ibm.designer.domino.ide.resources.metamodel.MetaModelCategory;
import com.ibm.designer.domino.ide.resources.metamodel.MetaModelDescriptor;
import com.ibm.designer.domino.ide.resources.metamodel.MetaModelRegistry;
import com.ibm.designer.domino.ide.resources.project.DominoDesignerProject;
import com.ibm.designer.domino.team.action.AbstractTeamHandler;
import com.ibm.misc.IOUtils;

public class SaveDatabasePropertiesHandler extends AbstractTeamHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		processSelectedProject(HandlerUtil.getCurrentSelection(event));

		IProgressMonitor monitor = new NullProgressMonitor();
		
		if (this.desProject != null) {

			IFile localIfile = this.desProject.getProject().getFile("AppProperties/database.properties");
			
			IFile destfile = this.desProject.getProject().getFile("Swiper/test.properties");
			
			if (localIfile.exists()) {

			}
						
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
//		IProgressMonitor monitor = new NullProgressMonitor();
//		NotesDesignElement dbprops = null;
//		
//		IFile fileSaveProps = new File("C:\\Users\\cgregor\\Desktop\\TestSwiper");
//
//		try {
//			
//			InputStream is = dbprops.fetchSyncContent(0, monitor);
//
//			if (fileSaveProps.exists()) { // Update
//				try {
//					fileSaveProps.setContents(is, 0, monitor);
//				} catch (CoreException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			} else { // create
//				try {
//					fileSaveProps.create(is, 0, monitor);
//				} catch (Exception e) {
//					// Couldn't save it
//				}
//			}
//
//		} catch (NsfException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return null;

	}

}
