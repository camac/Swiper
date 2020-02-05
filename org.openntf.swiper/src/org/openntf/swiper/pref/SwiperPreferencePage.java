/*******************************************************************************
 * Copyright 2017 Cameron Gregor (http://camerongregor.com) 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License
 *******************************************************************************/
package org.openntf.swiper.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openntf.swiper.SwiperActivator;

import com.bdaum.overlayPages.FieldEditorOverlayPage;

public class SwiperPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {

	public static final String PAGE_ID = "org.openntf.swiper.swiperPage";
	public static final String PREF_CUST_FILTER = "customFilter";
	public static final String PREF_MIMICXMLDECL = "mimicXmlDeclaration";
	public static final String PREF_MIMIC_DXLEXPORT_EOF = "mimicDxlExportEof";
	public static final String PREF_ENABLE_ALL = "enableForAll";

	public SwiperPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		IPreferenceStore store = SwiperActivator.getDefault().getPreferenceStore();
		return store;
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Swiper Preferences");
	}

	@Override
	protected void createFieldEditors() {

		
		FileFieldEditor defaultFilter = new FileFieldEditor(PREF_CUST_FILTER, "Custom XSLT Filter",
				getFieldEditorParent());	
		addField(defaultFilter);
		
		BooleanFieldEditor mimcXmlDeclaration = new BooleanFieldEditor(PREF_MIMICXMLDECL,
				"Mimic Dora XML Declaration (no 'encoding=UTF-8')", getFieldEditorParent());
		addField(mimcXmlDeclaration);

		BooleanFieldEditor dontAddNewLine = new BooleanFieldEditor(PREF_MIMIC_DXLEXPORT_EOF,
				"Mimic EOF of DXL Export (Add extra Line Endings to end of file)", getFieldEditorParent());
		addField(dontAddNewLine);
		
		if (!isPropertyPage()) {
			BooleanFieldEditor enableAll = new BooleanFieldEditor(PREF_ENABLE_ALL, "Enable Swiper for ALL Projects", getFieldEditorParent());
			addField(enableAll);
		}
		
	}

	@Override
	protected String getPageId() {
		return PAGE_ID;
	}
}
