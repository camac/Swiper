/*******************************************************************************
 * Copyright 2015 Cameron Gregor
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.util;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openntf.swiper.SwiperActivator;
import org.openntf.swiper.builder.SwiperNature;
import org.openntf.swiper.pref.SwiperPreferenceManager;
import org.openntf.swiper.pref.SwiperPreferencePage;

import com.bdaum.overlayPages.FieldEditorOverlayPage;
import com.ibm.commons.log.Log;
import com.ibm.commons.log.LogMgr;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.jni.NotesDesignElement;
import com.ibm.designer.domino.ide.resources.metamodel.IMetaModelConstants;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.preferences.DominoPreferenceManager;
import com.ibm.designer.domino.team.util.SyncUtil;
import com.ibm.designer.prj.resources.commons.IMetaModelDescriptor;

public class SwiperUtil {

	public static LogMgr SWIPER_LOG = Log.load("org.openntf.swiper", "Logger used for Swiper");
	private static final String MARKER_TYPE = "org.openntf.swiper.xmlProblem";

	private static Set<String> extensionsToFilter;
	private static Set<String> filenamesToFilter;
	
	private static boolean loggingToFile = false;
	
	private static boolean filterEverything = true;

	public static Set<String> getCanFilterIds() {

		HashSet<String> things = new HashSet<String>();

		things.add(IMetaModelConstants.AGENTS);
		things.add(IMetaModelConstants.WEBSERVICES);
		things.add(IMetaModelConstants.SHARED_ELEMENTS);
		things.add(IMetaModelConstants.DATACONNS);
		things.add(IMetaModelConstants.DAVS);
		things.add(IMetaModelConstants.FOLDERS);
		things.add(IMetaModelConstants.FIELDS);
		things.add(IMetaModelConstants.COLUMNS);
		things.add(IMetaModelConstants.FRAMESET);
		things.add(IMetaModelConstants.JAVAFILE);
		things.add(IMetaModelConstants.JAVAJARS);
		things.add(IMetaModelConstants.DBSCRIPT);
		// Metadata
		things.add(IMetaModelConstants.XSPPAGES);
		things.add(IMetaModelConstants.XSPCCS);
		things.add(IMetaModelConstants.NAVIGATORS);
		things.add(IMetaModelConstants.OUTLINES);
		things.add(IMetaModelConstants.PAGES);
		things.add(IMetaModelConstants.SUBFORMS);
		things.add(IMetaModelConstants.VIEWS);
		things.add(IMetaModelConstants.FORMS);

		things.add(IMetaModelConstants.IMAGES);
		things.add(IMetaModelConstants.FILES);

		things.add(IMetaModelConstants.APPLETS);
		things.add(IMetaModelConstants.STYLESHEETS);
		
		things.add(IMetaModelConstants.ABOUTDOC);
		things.add(IMetaModelConstants.DBPROPS);
		things.add(IMetaModelConstants.ICONNOTE);
		things.add(IMetaModelConstants.ACTIONS);
		things.add(IMetaModelConstants.USINGDOC);

		things.add(IMetaModelConstants.SCRIPTLIB);
		
		things.add(IMetaModelConstants.STYLEKITS);

		things.add(IMetaModelConstants.WIRINGPROPS);
		things.add(IMetaModelConstants.APPS);
		things.add(IMetaModelConstants.COMPONENTS);
		
		return things;
	}

	public static String getPreferenceKey(IMetaModelDescriptor mmd) {
		return "swiper.filter." + mmd.getID();
	}

	public static String getPreferenceKey(String id) {
		return "swiper.filter." + id;
	}

	public static boolean isSetToFilter(IMetaModelDescriptor mmd) {

		if (filterEverything)
			return true;

		String prefKey = getPreferenceKey(mmd);

		boolean isset = SwiperPreferenceManager.getInstance().getBooleanValue(prefKey, false);

		if (isset) {
			logTrace(prefKey + " is currently set to True");
		} else {
			logTrace(prefKey + " is currently set to False");
		}

		return isset;
	}

	public static boolean shouldFilter(IResource resource) {

		NotesDesignElement element = DominoResourcesPlugin.getNotesDesignElement(resource);

		if (element == null) {
			return false;
		}

		boolean hasMetadata = SyncUtil.hasMetadataFile(element);

		if (hasMetadata) {
			logTrace("Design Element Name: " + element.getName() + "Has Metadata");
		} else {
			logTrace("Design Element Name: " + element.getName() + "Does not have metadata");
		}

		IMetaModelDescriptor mmd = element.getMetaModel();

		if (mmd == null) {
			return false;
		}

		String id = mmd.getID();

		if (id.equals(IMetaModelConstants.XSPCCS)) {

			if (StringUtil.equals(resource.getFileExtension(), "xsp-config")) {
				return false;
			}

		}
		
		if (id.equals(IMetaModelConstants.SCRIPTLIB)) {
			
			if (StringUtil.equals(resource.getFileExtension(), "js")) {
				return false;
			}

			if (StringUtil.equals(resource.getFileExtension(), "jss")) {
				return false;
			}
			
			if (StringUtil.equals(resource.getFileExtension(), "lss")) {
				return false;
			}

			
		}
		
		if (getCanFilterIds().contains(id)) {

			logTrace("Yes we can filter" + mmd.getName());

			return isSetToFilter(mmd);

		} else {

			logTrace("No we don't filter" + mmd.getName() + " (" + id + ")");
			return false;

		}

	}

	public static String getCustomFilterFilePath() {
		return SwiperPreferenceManager.getInstance().getValue(SwiperPreferencePage.PREF_CUST_FILTER, false);
	}

	public static String getCustomFilterFilePath(IResource resource) {

		IPreferenceStore store = SwiperPreferenceManager.getInstance().getPreferenceStore();
		String pageId = SwiperPreferencePage.PAGE_ID;

		return getOverlayedPreferenceValue(store, resource, pageId, SwiperPreferencePage.PREF_CUST_FILTER);

	}

	public static Boolean isMimicXmlDeclaration() {
		return SwiperPreferenceManager.getInstance().getBooleanValue(SwiperPreferencePage.PREF_MIMICXMLDECL, false);
	}

	public static Boolean isMimicXmlDeclaration(IResource resource) {

		IPreferenceStore store = SwiperPreferenceManager.getInstance().getPreferenceStore();
		String pageId = SwiperPreferencePage.PAGE_ID;

		String stringValue = getOverlayedPreferenceValue(store, resource, pageId,
				SwiperPreferencePage.PREF_MIMICXMLDECL);

		return StringUtil.equalsIgnoreCase(Boolean.TRUE.toString(), stringValue);

	}

	public static Boolean isMimicDxlExportEOF() {
		return SwiperPreferenceManager.getInstance().getBooleanValue(SwiperPreferencePage.PREF_MIMIC_DXLEXPORT_EOF,
				false);
	}

	public static Boolean isMimicDxlExportEOF(IResource resource) {
		IPreferenceStore store = SwiperPreferenceManager.getInstance().getPreferenceStore();
		String pageId = SwiperPreferencePage.PAGE_ID;

		String stringValue = getOverlayedPreferenceValue(store, resource, pageId,
				SwiperPreferencePage.PREF_MIMIC_DXLEXPORT_EOF);

		return StringUtil.equalsIgnoreCase(Boolean.TRUE.toString(), stringValue);

	}

	public static IFile getRelevantDiskFile(IDominoDesignerProject designerProject, IResource designerFile)
			throws CoreException {

		NotesDesignElement designElement = DominoResourcesPlugin.getNotesDesignElement(designerFile);

		IProject diskProject = SyncUtil.getAssociatedDiskProject(designerProject, false);

		IFile diskFile = null;

		if (SyncUtil.hasMetadataFile(designElement)) {

			SwiperUtil.logTrace("Metadata file needed " + designerFile.getName());

			IPath localPath = designerFile.getProjectRelativePath().addFileExtension("metadata");
			diskFile = diskProject.getFile(localPath);

		} else {

			SwiperUtil.logTrace("No Metadata file needed for " + designerFile.getName());
			diskFile = SyncUtil.getPhysicalFile(designerProject, designerFile);

		}

		return diskFile;

	}

	public static QualifiedName getSyncModifiedQualifiedName(IResource paramIResource) {
		QualifiedName localQualifiedName = new QualifiedName("org.openntf.swiper",
				paramIResource.getProjectRelativePath().toString());
		return localQualifiedName;

	}

	public static boolean isModifiedBySync(IResource resource) {

		if (resource.exists()) {
			try {
				resource.refreshLocal(1, new NullProgressMonitor());

				long l1 = resource.getLocalTimeStamp();
				String str = getPersistentSyncTimestamp(resource);
				if (StringUtil.equals(str, String.valueOf(l1))) {
					return false;
				}
				// if (StringUtil.isNotEmpty(str)) {
				// try {
				// long l2 = Long.parseLong(str);
				// long l3 = l2 - l1;
				// if ((l3 > 500L) && (l3 < 2000L)) {
				// return true;
				// }
				// } catch (NumberFormatException localNumberFormatException) {
				// }
				// }
				return true;
			} catch (CoreException localCoreException) {
				localCoreException.printStackTrace();
			}
		}
		return false;
	}

	public static String getPersistentSyncTimestamp(IResource paramIResource) throws CoreException {
		QualifiedName localQualifiedName = getSyncModifiedQualifiedName(paramIResource);
		return paramIResource.getProject().getPersistentProperty(localQualifiedName);
	}

	public static void setSyncTimestamp(IResource paramIResource) {

		if ((paramIResource == null) || (!paramIResource.exists())) {
			return;
		}
		try {
			if (paramIResource.getType() == 1) {
				paramIResource.refreshLocal(1, new NullProgressMonitor());
			}
			if (paramIResource.exists()) {
				long l = paramIResource.getLocalTimeStamp();
				QualifiedName localQualifiedName = getSyncModifiedQualifiedName(paramIResource);
				paramIResource.getProject().setPersistentProperty(localQualifiedName, String.valueOf(l));
			}
		} catch (CoreException localCoreException) {
			localCoreException.printStackTrace();
		}

	}

	public static boolean isLoggingToFile() {
		return loggingToFile;
	}
	
	public static void startLoggingToFile() {

		logInfo("Starting Logging to File");
		
		Handler handler = SwiperActivator.getDefault().getFileHandler();		
		SWIPER_LOG.getLogger().addHandler(handler);
		SWIPER_LOG.getLogger().setLevel(Level.ALL);
		loggingToFile = true;
			
	}
	
	public static void stopLoggingToFile() {

		logInfo("Stopping Logging to File");
		
		Handler handler = SwiperActivator.getDefault().getFileHandler();		
		SWIPER_LOG.getLogger().removeHandler(handler);
		SWIPER_LOG.getLogger().setLevel(Level.INFO);
		
		loggingToFile = false;

		SwiperActivator.getDefault().closeFileHandler();

		
	}
	
	public static void logInfo(String message) {
		if (SWIPER_LOG.isInfoEnabled()) {
			SWIPER_LOG.infop("SwiperUtil", "", "Swiper: " + message, new Object[0]);
		}
	}

	public static void logInfo(String message, Object... args) {
		if (SWIPER_LOG.isInfoEnabled()) {
			SWIPER_LOG.infop("SwiperUtil", "", "Swiper: " + message, args);
		}
	}

	public static void logTrace(String message) {
		SWIPER_LOG.traceDebug("Swiper: " + message);
	}

	public static void logError(String message) {
		SWIPER_LOG.error(message);
	}

	public static void addNature(IProject project) {

		SwiperUtil.logTrace("Attempt to Add Nature");

		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {

				if (SwiperNature.NATURE_ID.equals(natures[i])) {
					SwiperUtil.logInfo("Swiper Nature already exists");
					return;
				}
			}

			// Add the nature
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = SwiperNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

			logInfo("Added Swiper Nature to " + project.getName());

		} catch (CoreException e) {

			SwiperUtil.logError(e.getMessage());
			e.printStackTrace();

		} catch (Exception e) {

			SwiperUtil.logError(e.getMessage());
			e.printStackTrace();

		}

	}

	public static void removeNature(IProject project) {

		SwiperUtil.logTrace("Attempt to Remove Nature");

		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (SwiperNature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);

					logInfo("Removed Swiper Nature from " + project.getName());

					return;
				}
			}

		} catch (CoreException e) {

			SwiperUtil.logInfo(e.getMessage());
			e.printStackTrace();

		} catch (Exception e) {

			SwiperUtil.logInfo(e.getMessage());
			e.printStackTrace();

		}
	}

	public static void addMarker(IProject project, String message, int severity) {

		try {
			IMarker marker = project.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
		} catch (CoreException e) {

		}

	}

	public static void addMarker(IResource resource, String message, int severity) {

		try {
			IMarker marker = resource.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
		} catch (CoreException e) {

		}

	}

	public static void addMarker(IFolder folder, String message, int severity) {

		try {
			IMarker marker = folder.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
		} catch (CoreException e) {

		}

	}

	public static void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
	}

	public static void cleanMarkers(IProject project) throws CoreException {

		project.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);

	}

	public static void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
		}
	}

	public static boolean isAutoExportEnabled() {
		return DominoPreferenceManager.getInstance().getBooleanValue("domino.prefs.keys.team.export.auto", false);
	}

	public static String getOverlayedPreferenceValue(IPreferenceStore store, IResource resource, String pageId,
			String name) {

		IProject project = resource.getProject();
		String value = null;
		if (useProjectSettings(project, pageId)) {
			value = getProperty(resource, pageId, name);
		}
		if (value != null)
			return value;
		return store.getString(name);

	}

	private static boolean useProjectSettings(IResource resource, String pageId) {
		String use = getProperty(resource, pageId, FieldEditorOverlayPage.USEPROJECTSETTINGS);
		return "true".equals(use);
	}

	private static String getProperty(IResource resource, String pageId, String key) {

		try {
			return resource.getPersistentProperty(new QualifiedName(pageId, key));
		} catch (CoreException e) {

		}
		return null;

	}
	
	public static Set<String> getExtensionsToFilter() {
		if (SwiperUtil.extensionsToFilter == null) {
			SwiperUtil.extensionsToFilter = new HashSet<String>();

			SwiperUtil.extensionsToFilter.add(".metadata");
			SwiperUtil.extensionsToFilter.add(".aa");
			SwiperUtil.extensionsToFilter.add(".column");
			SwiperUtil.extensionsToFilter.add(".dcr");
			SwiperUtil.extensionsToFilter.add(".fa");
			SwiperUtil.extensionsToFilter.add(".field");
			SwiperUtil.extensionsToFilter.add(".folder");
			SwiperUtil.extensionsToFilter.add(".form");
			SwiperUtil.extensionsToFilter.add(".frameset");
			SwiperUtil.extensionsToFilter.add(".ija");
			SwiperUtil.extensionsToFilter.add(".ja");
			SwiperUtil.extensionsToFilter.add(".javalib");
			SwiperUtil.extensionsToFilter.add(".lsa");
			SwiperUtil.extensionsToFilter.add(".lsdb");
			SwiperUtil.extensionsToFilter.add(".navigator");
			SwiperUtil.extensionsToFilter.add(".outline");
			SwiperUtil.extensionsToFilter.add(".page");
			SwiperUtil.extensionsToFilter.add(".subform");
			SwiperUtil.extensionsToFilter.add(".view");

			SwiperUtil.extensionsToFilter.add("AboutDocument");
			SwiperUtil.extensionsToFilter.add("database.properties");
			SwiperUtil.extensionsToFilter.add("IconNote");
			SwiperUtil.extensionsToFilter.add("Shared?Actions");
			SwiperUtil.extensionsToFilter.add("UsingDocument");
			
		}
		
		return SwiperUtil.extensionsToFilter;
	}
	
	public static boolean shouldFilterDestinationFile(IResource res) {
		
		String extension = res.getFileExtension();
		
		
		
		if (StringUtil.isNotEmpty(extension)) {
			
			System.out.println("." + extension);
			
			if (getExtensionsToFilter().contains("." + extension)) {
				return true;
			}
		}
		
		String name = res.getName();
		
		if (StringUtil.isNotEmpty(name)) {
			if (getExtensionsToFilter().contains(name)) {
				return true;
			}
		}
		
		return false;
		
		
	}

}
