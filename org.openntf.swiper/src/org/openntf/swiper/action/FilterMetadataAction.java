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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openntf.swiper.util.SwiperUtil;

import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.team.action.AbstractTeamHandler;
import com.ibm.designer.domino.team.util.SyncUtil;

public class FilterMetadataAction extends AbstractTeamHandler {

	private static final String DEFAULT_FILTER = "DXLClean.xsl";

	private static final byte[] XML_DECL = "<?xml version=\"1.0\"?>\n".getBytes();

	private Templates cachedXslt = null;

	public FilterMetadataAction() {

	}

	public void setSyncProjects(IDominoDesignerProject designerProject,
			IProject diskProject) {

		this.desProject = designerProject;

	}

	private Transformer getTransformer()
			throws TransformerConfigurationException, FileNotFoundException {

		if (this.cachedXslt == null) {

			TransformerFactory factory = TransformerFactory.newInstance();
			// Get Filter

			String filterFile = SwiperUtil.getDefaultFilterFilePath();

			InputStream is = null;
			Source xslt = null;

			if (filterFile != null && !"".equals(filterFile)) {

				File file = new File(filterFile);

				if (!file.exists()) {
					throw new FileNotFoundException(
							"Could not Find Swiper XSLT Filter");
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

	private void filter(IFile diskFile, Transformer transformer, IProgressMonitor monitor)
			throws TransformerException, CoreException, IOException {

		InputStream is = diskFile.getContents();
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
		if (SwiperUtil.isMimicXmlDeclaration()) {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			baos.write(XML_DECL);
		}

		StreamResult result = new StreamResult(baos);

		transformer.transform(source, result);

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		diskFile.setContents(bais, 0, monitor);

		if (!SwiperUtil.isDontAddNewLine()) {
			
			// Add a New line Because that is what normally happens
			String linesep = System.getProperty("line.separator");
			bais = new ByteArrayInputStream(linesep.getBytes("UTF-8"));
			diskFile.appendContents(bais, 0, monitor);

		}

		is.close();

		SyncUtil.setModifiedBySync(diskFile);

	}

	public void filterDiskFile(IResource designerResource, IFile diskFile,
			IProgressMonitor monitor) {

		SwiperUtil.logInfo("Filter" + diskFile.getName());

		if (!diskFile.exists())
			return;

		try {

			Transformer transformer = getTransformer();

			if (diskFile != null) {
				filter(diskFile, transformer, monitor);
			}

		} catch (TransformerConfigurationException e) {

			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message,
					IMarker.SEVERITY_INFO);

		} catch (TransformerException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message,
					IMarker.SEVERITY_INFO);

		} catch (CoreException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message,
					IMarker.SEVERITY_INFO);

		} catch (FileNotFoundException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message,
					IMarker.SEVERITY_WARNING);
		} catch (IOException e) {
			String message = e.getMessage();
			SwiperUtil.addMarker(designerResource, "Swiper Error " + message,
					IMarker.SEVERITY_WARNING);
		} finally {

		}

	}

}
