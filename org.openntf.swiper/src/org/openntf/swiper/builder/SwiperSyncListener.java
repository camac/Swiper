/*******************************************************************************
 * Copyright 2017 Cameron Gregor (http://camerongregor.com) 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.builder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.openntf.swiper.action.FilterMetadataAction;
import org.openntf.swiper.util.SwiperUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;
import com.ibm.commons.xml.XMLException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.builder.ExportContext;
import com.ibm.designer.domino.team.builder.ISyncContext;
import com.ibm.designer.domino.team.builder.RenameSyncContext;
import com.ibm.designer.domino.team.builder.SyncListener;
import com.ibm.designer.domino.team.util.SyncUtil;

public class SwiperSyncListener extends SyncListener {

	private IProgressMonitor monitor = new NullProgressMonitor();

	public SwiperSyncListener() {
		super();
	}

	@Override
	public void preSync(IDominoDesignerProject desProject, IProject diskProject, int direction) {

		SwiperUtil.logTrace("About to Sync " + desProject.getProject().getName());

	}

	@Override
	public void postSync(IDominoDesignerProject desProject, IProject diskProject, int direction) {

		SwiperUtil.logTrace("Finished Sync : " + desProject.getProject().getName());

	}

	private void filterIfNeeded(IDominoDesignerProject designerProject, IResource src, IResource dst,
			ISyncContext context) {

		try {

			FilterMetadataAction action = new FilterMetadataAction();

			IProject diskProject = SyncUtil.getAssociatedDiskProject(designerProject, false);

			if (diskProject == null) {
				throw new NullPointerException("Could not find related Disk Project");
			}

			action.setSyncProjects(designerProject, diskProject);

			boolean enabledforall = SwiperUtil.isEnableForAll();

			if (enabledforall) {
				SwiperUtil.logTrace("Swiping because swiper is enabled for all projects");
			}

			if (enabledforall || designerProject.getProject().hasNature(SwiperNature.NATURE_ID)) {

				if (SwiperUtil.shouldFilterDestinationFile(dst)) {

					if (dst instanceof IFile) {
						action.filterDiskFile(src, (IFile) dst, monitor);
					}

				} else {
					SwiperUtil.logTrace("No Need to filter " + src.getFullPath());
				}

			}

		} catch (Exception e) {
			SwiperUtil.logTrace(e.getMessage());
		}

	}

	@Override
	public void postRename(IResource src, IResource dst, ISyncContext context) {

		if (context instanceof RenameSyncContext) {
			RenameSyncContext renameContext = (RenameSyncContext) context;
			IDominoDesignerProject designerProject = renameContext.getDesignerProject();
			SwiperUtil.logTrace("POST RENAME: " + dst.getFullPath() + "'" + dst.getFileExtension() + "'");

			filterIfNeeded(designerProject, renameContext.getNewNsfFile(), dst, context);

		}

		super.postRename(src, dst, context);
	}

	@Override
	public void postExport(IResource src, IResource dst, ISyncContext synccontext) {

		if (synccontext instanceof ExportContext) {
			try {

				SwiperUtil.logTrace("POST EXPORT: " + dst.getFullPath() + "'" + dst.getFileExtension() + "'");
				IDominoDesignerProject designerProject = ((ExportContext) synccontext).getDesignerProject();
				filterIfNeeded(designerProject, src, dst, synccontext);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		super.postExport(src, dst, synccontext);
	}

	@Override
	public boolean preExport(IResource src, IResource dst, ISyncContext synccontext) {

		if (synccontext instanceof ExportContext) {

			try {

				ExportContext context = (ExportContext) synccontext;

				if (context.getDesignerProject().getProject().hasNature(SwiperNature.NATURE_ID)) {

					SwiperUtil.logTrace("PRE EXPORT Src " + src.getFullPath());
					SwiperUtil.logTrace("PRE EXPORT Dst " + dst.getFullPath());

					if (SwiperUtil.shouldFilter(src)) {

						if (dst instanceof IFile) {

							IFile dstFile = (IFile) dst;

							try {

								if (dstFile.exists()) {

									if (SwiperUtil.isMetadata(dstFile) && SwiperUtil.isDontOverwriteMetadata()) {
										SwiperUtil.logTrace("No Export - set to Don't Overwrite existing metadata: "
												+ dst.getFullPath());
										return false;
									}

									if (SwiperUtil.isOnlyExportIfDifferent()
											&& SwiperUtil.contentsEqual(src, dst)) {
										return false;
									}

								}

							} catch (Exception e) {
								SwiperUtil.logException(e, "Error in PRE EXPORT");
							}

						}

					} else {
						SwiperUtil.logTrace(" No Need to filter " + src.getFullPath());
					}

				}

			} catch (CoreException e) {
				SwiperUtil.logException(e, "Error in PRE EXPORT");
			}
		}

		return true;

	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
