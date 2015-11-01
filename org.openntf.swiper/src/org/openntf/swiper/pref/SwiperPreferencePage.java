/*******************************************************************************
 * Copyright 2015 Cameron Gregor
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
import org.openntf.swiper.Activator;

import com.bdaum.overlayPages.FieldEditorOverlayPage;

public class SwiperPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {

	public static final String PAGE_ID = "org.openntf.swiper.swiperPage";
	public static final String PREF_DEFFILTER = "defaultFilter";
	public static final String PREF_MIMICXMLDECL = "mimicXmlDeclaration";

	public SwiperPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store;
	}

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Swiper Preferences");
	}

	@Override
	protected void createFieldEditors() {

		FileFieldEditor defaultFilter = new FileFieldEditor(PREF_DEFFILTER,
				"Default XSLT Filter", getFieldEditorParent());
		addField(defaultFilter);

		BooleanFieldEditor mimcXmlDeclaration = new BooleanFieldEditor(
				PREF_MIMICXMLDECL, "Mimic the XML Declaration From Dora",
				getFieldEditorParent());
		addField(mimcXmlDeclaration);


	}

	@Override
	protected String getPageId() {
		return PAGE_ID;
	}
}
