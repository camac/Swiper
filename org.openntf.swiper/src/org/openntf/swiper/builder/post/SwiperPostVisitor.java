package org.openntf.swiper.builder.post;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.swiper.util.SwiperUtil;

import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.util.SyncUtil;

public class SwiperPostVisitor implements IResourceDeltaVisitor {

	private IProgressMonitor monitor = null;
	private SwiperPostSyncBuilder builder = null;
	private IDominoDesignerProject designerProject = null;

	public SwiperPostVisitor(IProgressMonitor monitor, SwiperPostSyncBuilder builder) {
		this.monitor = monitor;
		this.builder = builder;
		this.designerProject = builder.getDesignerProject();
	}

	private void processSharedAction(IFolder sharedActionFolder) throws CoreException {
		
		System.out.println("Going to process the shared action folder");
		
		processDesignerFile(sharedActionFolder);
		
		
	}
	
	private void processDesignerFile(IResource designerFile) throws CoreException {
		
		IFile diskFile = SwiperUtil.getRelevantDiskFile(designerProject, designerFile);

		if (diskFile != null && diskFile.exists()) {

			if (SwiperUtil.isModifiedBySync(diskFile)) {

				SwiperUtil.logInfo(diskFile.getName() + " was modified by sync - Filter It");

				if (SwiperUtil.shouldFilter(designerFile)) {
					builder.filterDiskFile(designerFile, diskFile, monitor);
				} else {
					SwiperUtil.logInfo("Not Configured to filter " + designerFile.getName());
				}

			} else {
				SwiperUtil.logInfo(diskFile.getName() + " untouched");
			}

		}
	}

	private boolean processAdded(IResourceDelta delta) {
		try {

			SwiperUtil.logInfo("Processing Added");

			if ((delta.getResource() instanceof IFolder)) {
				IFolder folder = (IFolder) delta.getResource();

				if (SyncUtil.isSharedAction(folder.getParent().getProjectRelativePath())) {
					processSharedAction(folder);
					return false;
				}
			} else if (delta.getResource() instanceof IFile) {

				IFile designerFile = (IFile) delta.getResource();
				processDesignerFile(designerFile);

			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean processChanged(IResourceDelta delta) {

		SwiperUtil.logInfo("Processing Changed");

		try {

			if ((delta.getResource() instanceof IFolder)) {
				IFolder folder = (IFolder) delta.getResource();

				if (SyncUtil.isSharedAction(folder.getParent().getProjectRelativePath())) {
					processSharedAction(folder);
					return false;
				}
			} else if (delta.getResource() instanceof IFile) {

				IFile designerFile = (IFile) delta.getResource();
				processDesignerFile(designerFile);

			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {

		SwiperUtil.logInfo("Visiting: " + delta.getResource().getName());

		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			if (!processAdded(delta)) {
				return false;
			}
			break;
		case IResourceDelta.CHANGED:
			if (!processChanged(delta)) {
				return false;
			}
			break;

		}

		return true;
	}

}
