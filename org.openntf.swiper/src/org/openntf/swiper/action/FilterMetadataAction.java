/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openntf.swiper.util.SwiperUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.action.AbstractTeamHandler;
import com.ibm.designer.domino.team.util.SyncUtil;

public class FilterMetadataAction extends AbstractTeamHandler {

	private static final String DEFAULT_FILTER = "DXLClean.xsl";

	private static final byte[] XML_DECL = "<?xml version=\"1.0\"?>\n".getBytes();

	private List<IFile> filesTofilter = new ArrayList<IFile>();

	private String customFilterPath = null;
	private boolean mimicDoraXmlDeclaration = false;
	private boolean mimicDxlExportEOF = false;

	private Templates cachedXslt = null;

	public FilterMetadataAction() {

	}

	private void initSwiperSettings() {

		IProject prj = this.desProject.getProject();

		if (prj != null) {
			this.customFilterPath = SwiperUtil.getCustomFilterFilePath(prj);
			this.mimicDoraXmlDeclaration = SwiperUtil.isMimicXmlDeclaration(prj);
			this.mimicDxlExportEOF = SwiperUtil.isMimicDxlExportEOF(prj);
		} else {
			SwiperUtil.logError("DesignerProject.getProject() is null, couldn't retrieve Swiper settings");
		}

	}

	public void setSyncProjects(IDominoDesignerProject designerProject, IProject diskProject) {

		this.desProject = designerProject;

		initSwiperSettings();

	}

	public Transformer getTransformer() throws TransformerConfigurationException, FileNotFoundException {

		if (this.cachedXslt == null) {

			TransformerFactory factory = TransformerFactory.newInstance();
			// Get Filter

			InputStream is = null;
			Source xslt = null;

			if (StringUtil.isNotEmpty(customFilterPath)) {

				File file = new File(customFilterPath);

				if (!file.exists()) {
					throw new FileNotFoundException("Could not Find Swiper XSLT Filter");
				}
				xslt = new StreamSource(file);

			} else {
				is = getClass().getResourceAsStream(DEFAULT_FILTER);
				xslt = new StreamSource(is);
			}

			this.cachedXslt = factory.newTemplates(xslt);

			try {

				if (is != null) {
					is.close();
				}

			} catch (IOException e) {

			}

		}

		return cachedXslt.newTransformer();

	}

	public InputStream getFilteredInputStream(IFile diskFile, Transformer transformer, IProgressMonitor monitor)
			throws TransformerException, CoreException, IOException {
		
		InputStream is = diskFile.getContents(true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Source source = new StreamSource(is);

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");

		/*
		 * If we want to match the existing DXL as close as possible we omit the
		 * transformer's declaration as it includes the encoding='UTF-8'
		 * attribute that is not included in normal DXL Export
		 */
		if (mimicDoraXmlDeclaration) {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			baos.write(XML_DECL);
		}

		StreamResult result = new StreamResult(baos);

		transformer.transform(source, result);

		is.close();
		
		return new ByteArrayInputStream(baos.toByteArray());		
		
	}
	
	private void filter(IFile diskFile, Transformer transformer, IProgressMonitor monitor)
			throws TransformerException, CoreException, IOException {

		InputStream is = getFilteredInputStream(diskFile, transformer, monitor);
		
		diskFile.setContents(is, 0, monitor);

		/*
		 * When you export the DXL normally, there is an Newline at the end of
		 * the file. However, this transformer does not include the Newline at
		 * the end. By Default, we will just go with what the transformer does.
		 *
		 * However, if we choose to mimic what the DXL Export EOF does then we
		 * will add the extra new lines to be the same as the normmal DXL
		 * Export.
		 */
		if (mimicDxlExportEOF) {

			// Add a New line Because that is what normally happens
			String linesep = System.getProperty("line.separator");

			linesep = linesep + linesep;
			ByteArrayInputStream bais = new ByteArrayInputStream(linesep.getBytes("UTF-8"));
			diskFile.appendContents(bais, 0, monitor);

		}

		is.close();

	}

	public void filterDiskFile(IResource designerResource, IFile diskFile, IProgressMonitor monitor) {

		SwiperUtil.logTrace("About To Filter" + diskFile.getName());

		if (!diskFile.exists())
			return;

		try {

			Transformer transformer = getTransformer();

			if (diskFile != null) {
				filter(diskFile, transformer, monitor);
				SwiperUtil.logInfo("Filtered " + diskFile.getName());
			}

		} catch (TransformerConfigurationException e) {

			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message, IMarker.SEVERITY_INFO);

		} catch (TransformerException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message, IMarker.SEVERITY_INFO);

		} catch (CoreException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message, IMarker.SEVERITY_INFO);

		} catch (FileNotFoundException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message, IMarker.SEVERITY_WARNING);
		} catch (IOException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message, IMarker.SEVERITY_WARNING);
		} finally {

		}

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		SwiperUtil.logInfo("**** Performing Explicit Filtering");

		processSelection(event);

		if (this.desProject == null) {

			SwiperUtil.logError("Could not determine the Designer Project");

			return null;
		}

		initSwiperSettings();

		for (IFile designerFile : filesTofilter) {

			try {

				IFile diskFile = SwiperUtil.getRelevantDiskFile(this.desProject, designerFile);

				if (diskFile != null && diskFile.exists()) {

					SwiperUtil.logTrace(diskFile.getName() + " has been explicitly told to filter - Filter It");

					if (SwiperUtil.shouldFilter(designerFile)) {
						filterDiskFile(designerFile, diskFile, new NullProgressMonitor());
					} else {
						SwiperUtil.logTrace("Not Configured to filter " + designerFile.getName());
					}

				}

			} catch (CoreException e) {
				SwiperUtil.logError(e.getMessage());
			}

		}

		return super.execute(event);
	}

	private void processSelection(ExecutionEvent event) {

		this.desProject = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof StructuredSelection) {

			StructuredSelection ss = (StructuredSelection) selection;

			IProject project = null;

			List<?> list = ss.toList();

			// Check to determine first project
			for (Object o : list) {

				if (o instanceof IFile) {

					IFile file = (IFile) o;

					if (project == null) {
						project = file.getProject();
					}

				}

			}

			// if we found the project, assign the this.desProject
			if (project != null) {

				if (DominoResourcesPlugin.isDominoDesignerProject(project)) {
					try {
						this.desProject = DominoResourcesPlugin.getDominoDesignerProject(project);
					} catch (NsfException e) {
						SwiperUtil.logError(e.getMessage());
					}
				}

			} else {
				// If we can't figure out the project then it is no good
				return;
			}

			// Add files that belong to that project
			for (Object o : list) {

				if (o instanceof IFile) {

					IFile file = (IFile) o;

					if (file.getProject() == project) {
						filesTofilter.add(file);
					}

				}

			}

		}

	}

}
