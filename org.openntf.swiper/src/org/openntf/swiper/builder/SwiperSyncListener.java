/*******************************************************************************
 * Copyright 2017 Cameron Gregor (http://camerongregor.com) 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.builder;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.openntf.swiper.action.FilterMetadataAction;
import org.openntf.swiper.util.SwiperUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.jni.NotesDesignElement;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		super.postExport(src, dst, synccontext);
	}

	@SuppressWarnings("unused")
	private boolean equalsHash(IResource src, IResource dst)
			throws NoSuchAlgorithmException, NsfException, CoreException, IOException {

		// Hash Source
		MessageDigest md = MessageDigest.getInstance("MD5");

		NotesDesignElement nde = DominoResourcesPlugin.getNotesDesignElement(src);
		InputStream is = nde.fetchSyncContent(0, monitor);
		DigestInputStream dis = new DigestInputStream(is, md);
		byte[] digestsrc = md.digest();

		is.close();

		md.reset();

		// Hash Dst
		MessageDigest md2 = MessageDigest.getInstance("MD5");

		IFile dstFile = (IFile) dst;

		is = dstFile.getContents(true);
		dis = new DigestInputStream(is, md2);
		byte[] digestdst = md2.digest();

		is.close();

		String srchash = bytesToHex(digestsrc);
		String dsthash = bytesToHex(digestdst);

		return StringUtil.equals(srchash, dsthash);
	}

	@SuppressWarnings("unused")
	private boolean equalsHash(InputStream src, InputStream dst)
			throws NoSuchAlgorithmException, NsfException, CoreException, IOException {

		// Hash Source
		MessageDigest md = MessageDigest.getInstance("MD5");

		DigestInputStream dis = new DigestInputStream(src, md);
		byte[] digestsrc = md.digest();

		src.close();

		// Hash Dst
		MessageDigest md2 = MessageDigest.getInstance("MD5");

		dis = new DigestInputStream(dst, md2);
		byte[] digestdst = md2.digest();

		dst.close();

		String srchash = bytesToHex(digestsrc);
		String dsthash = bytesToHex(digestdst);

		return StringUtil.equals(srchash, dsthash);
	}

	public boolean preExport(IResource src, IResource dst, ExportContext context) {

		/*
		 * Hoping to figure out how to avoid filtering if contents are the same
		 * but haven't done so yet try {
		 * 
		 * if (context.getDesignerProject().getProject().hasNature(SwiperNature.
		 * NATURE_ID)) {
		 * 
		 * System.out.println("PRE EXPORT " + dst.getFullPath());
		 * 
		 * if (SwiperUtil.shouldFilter(src)) {
		 * 
		 * if (dst instanceof IFile) {
		 * 
		 * IFile dstFile = (IFile) dst;
		 * 
		 * try {
		 * 
		 * Transformer transformer = action.getTransformer(); InputStream is =
		 * action.getFilteredInputStream(dstFile, transformer, monitor);
		 * 
		 * InputStream destIs = dstFile.getContents(true);
		 * 
		 * if (SyncUtil.contentsEqual(is, destIs)) { System.out.println(
		 * "Filtered Contents Equal =="); } else { System.out.println(
		 * "Filtered Contents Different <>"); }
		 * 
		 * if (src instanceof IFile) {
		 * 
		 * if (equalsHash(((IFile) src).getContents(true), destIs)) {
		 * System.out.println("Filtered Contents Hash equal =="); } else {
		 * System.out.println("Filtered Contents Hash different <>"); } }
		 * 
		 * } catch (Exception e) {
		 * 
		 * }
		 * 
		 * }
		 * 
		 * } else { System.out.println(" No Need to filter " +
		 * src.getFullPath()); }
		 * 
		 * }
		 * 
		 * } catch (CoreException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		return true;
		// try {
		//
		// if
		// (context.getDesignerProject().getProject().hasNature(SwiperNature.NATURE_ID))
		// {
		//
		// System.out.println("PRE EXPORT " + dst.getFullPath());
		//
		// if (SwiperUtil.shouldFilter(src)) {
		//
		// if (dst instanceof IFile) {
		//
		// System.out.println("Checking Hashes for " + src.getFullPath());
		//
		// // Hash Source
		// MessageDigest md = MessageDigest.getInstance("MD5");
		//
		// NotesDesignElement nde =
		// DominoResourcesPlugin.getNotesDesignElement(src);
		// InputStream is = nde.fetchSyncContent(0, monitor);
		// DigestInputStream dis = new DigestInputStream(is, md);
		// byte[] digestsrc = md.digest();
		//
		// is.close();
		//
		// md.reset();
		//
		// // Hash Dst
		// MessageDigest md2 = MessageDigest.getInstance("MD5");
		//
		// IFile dstFile = (IFile) dst;
		//
		// is = dstFile.getContents(true);
		// dis = new DigestInputStream(is, md2);
		// byte[] digestdst = md2.digest();
		//
		// is.close();
		//
		// String srchash = bytesToHex(digestsrc);
		// String dsthash = bytesToHex(digestdst);
		//
		// System.out.println("Hash Src : " + srchash);
		// System.out.println("Hash Dst : " + dsthash);
		//
		// if (StringUtil.equals(srchash, dsthash)) {
		// System.out.println("DONT EXPORT IT");
		// //return false;
		// }
		//
		// }
		//
		// } else {
		// System.out.println(" No Need to filter " + src.getFullPath());
		// }
		//
		// }
		//
		// } catch (CoreException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (NoSuchAlgorithmException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (NsfException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// return true;
	}

	// From
	// http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java

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
