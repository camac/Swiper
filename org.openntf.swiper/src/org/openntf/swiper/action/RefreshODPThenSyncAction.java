/*******************************************************************************
 * Copyright 2017 Cameron Gregor (http://camerongregor.com) 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.util.SyncUtil;

public class RefreshODPThenSyncAction extends SyncAction {

	public RefreshODPThenSyncAction() {
		super();
	}

	public RefreshODPThenSyncAction(int arg0) {
		super(arg0);
	}

	@Override
	public void doExecute(IProgressMonitor monitor) {

		IProject p = this.getSourceProject();

		if (p != null) {
			try {

				IDominoDesignerProject ddp = DominoResourcesPlugin.getDominoDesignerProject(p);
				IProject odp = SyncUtil.getAssociatedDiskProject(ddp, false);

				odp.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				
				super.doExecute(monitor);

			} catch (CoreException e) {
				e.printStackTrace();
			} catch (NsfException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected String getTaskName() {
		return "Refreshing On-Disk Project";
	}

}
