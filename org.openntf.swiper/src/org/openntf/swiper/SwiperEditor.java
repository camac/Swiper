package org.openntf.swiper;

import org.eclipse.ui.IEditorPart;

import com.ibm.designer.domino.xsp.editor.XFacesMultiPageEditor;

public class SwiperEditor extends XFacesMultiPageEditor {

	public SwiperEditor() {
		super();
		System.out.println("HEY HEY HO HO");
	}

	@Override
	protected IEditorPart createDesignPage() {

		System.out.println("Create Design Page");
		
		return super.createDesignPage();
	}

	

}
