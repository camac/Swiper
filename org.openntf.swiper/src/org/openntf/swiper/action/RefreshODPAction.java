package org.openntf.swiper.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.util.SyncUtil;

public class RefreshODPAction extends SyncAction {

	@Override
	public void doExecute() {

		IProject p = this.getSourceProject();

		if (p != null) {
			try {

				IDominoDesignerProject ddp = DominoResourcesPlugin.getDominoDesignerProject(p);
				IProject odp = SyncUtil.getAssociatedDiskProject(ddp, false);
				
				odp.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NsfException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	protected String getTaskName() {
		return "Refreshing On-Disk Project";
	}

}
