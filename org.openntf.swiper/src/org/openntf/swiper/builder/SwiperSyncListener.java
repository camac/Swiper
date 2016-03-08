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
import com.ibm.designer.domino.team.builder.SyncListener;
import com.ibm.misc.IOUtils;

public class SwiperSyncListener extends SyncListener {

	private FilterMetadataAction action;
	private IProgressMonitor monitor = new NullProgressMonitor();

	@Override
	public void preSync(IDominoDesignerProject desProject, IProject diskProject, int direction) {

		System.out.println("-------------------");
		System.out.println("About to Sync " + desProject.getProject().getName());

		action = new FilterMetadataAction();

		action.setSyncProjects(desProject, diskProject);

	}

	@Override
	public void postSync(IDominoDesignerProject desProject, IProject diskProject, int direction) {

		System.out.println("---------------------------");
		System.out.println("Finished Sync : " + desProject.getProject().getName());
		System.out.println("===========================");

	}

	@Override
	public void postExport(IResource src, IResource dst, ExportContext context) {

		try {

			if (context.getDesignerProject().getProject().hasNature(SwiperNature.NATURE_ID)) {

				System.out.println("POST EXPORT: " + dst.getFullPath());
				
				if (SwiperUtil.shouldFilterDestinationFile(dst)) {

					if (dst instanceof IFile) {
						action.filterDiskFile(src, (IFile) dst, monitor);
					}

				} else {
					System.out.println(" No Need to filter " + src.getFullPath());
				}

			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.postExport(src, dst, context);
	}

	@Override
	public boolean preExport(IResource src, IResource dst, ExportContext context) {
		return true;
//		try {
//
//			if (context.getDesignerProject().getProject().hasNature(SwiperNature.NATURE_ID)) {
//
//				System.out.println("PRE EXPORT " + dst.getFullPath());
//				
//				if (SwiperUtil.shouldFilter(src)) {
//
//					if (dst instanceof IFile) {
//
//						System.out.println("Checking Hashes for " + src.getFullPath());
//						
//						// Hash Source
//						MessageDigest md = MessageDigest.getInstance("MD5");
//
//						NotesDesignElement nde = DominoResourcesPlugin.getNotesDesignElement(src);
//						InputStream is = nde.fetchSyncContent(0, monitor);
//						DigestInputStream dis = new DigestInputStream(is, md);
//						byte[] digestsrc = md.digest();
//						
//						is.close();
//						
//						md.reset();
//
//						// Hash Dst
//						MessageDigest md2 = MessageDigest.getInstance("MD5");
//
//						IFile dstFile = (IFile) dst;
//
//						is = dstFile.getContents(true);
//						dis = new DigestInputStream(is, md2);
//						byte[] digestdst = md2.digest();
//
//						is.close();
//						
//						String srchash = bytesToHex(digestsrc);
//						String dsthash = bytesToHex(digestdst);
//						
//						System.out.println("Hash Src : " + srchash);
//						System.out.println("Hash Dst : " + dsthash);
//
//						if (StringUtil.equals(srchash, dsthash)) {
//							System.out.println("DONT EXPORT IT");
//							//return false;
//						}
//						
//					}
//
//				} else {
//					System.out.println(" No Need to filter " + src.getFullPath());
//				}
//
//			}
//
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NsfException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return true;
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
