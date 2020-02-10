package org.openntf.swiper;

import com.ibm.designer.domino.xsp.editor.XFacesEditor;
import com.ibm.designer.domino.xsp.editor.XFacesEditorActionGroup;
import com.ibm.designer.domino.xsp.editor.XSPActionBarContributor;
import com.ibm.designer.domino.xsp.editor.XSPStructuredTextEditor;
import com.ibm.designer.domino.xsp.editor.menu.DataTableMenuManager;
import com.ibm.designer.domino.xsp.editor.menu.PaletteEntriesMenuManager;
import com.ibm.designer.domino.xsp.editor.menu.TabPanelMenuManager;
import com.ibm.designer.domino.xsp.editor.menu.TabletMenuManager;
import com.ibm.designer.domino.xsp.editor.menu.ViewPanelMenuManager;
import com.ibm.etools.xve.editor.XVEMultiPageEditorActionBarContributor;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.texteditor.ITextEditor;

public class SwiperSourceOnlyEditorContributor extends XVEMultiPageEditorActionBarContributor implements IMenuListener {
	private SwiperSourceOnlyEditor _activeXSPEditor = null;
	private IMenuManager _workbenchMenuManager = null;
	private IToolBarManager _toolbarManager = null;
	private XFacesEditorActionGroup _actionGroup = null;
	private PaletteEntriesMenuManager _paletteMenuManager = null;
	private TabletMenuManager _tableMenuManager = null;
	private ViewPanelMenuManager _viewPanelMenuManager = null;
	private DataTableMenuManager _dataTableMenuManager = null;
	private TabPanelMenuManager _tabPanelMenuManager = null;
	XSPActionBarContributor actionbarContributor;

	protected IAction getAction(ITextEditor paramITextEditor, String paramString) {
		return paramITextEditor == null ? null : paramITextEditor.getAction(paramString);
	}

	public void contributeToMenu(IMenuManager paramIMenuManager) {
		this._workbenchMenuManager = paramIMenuManager;
		this._paletteMenuManager = new PaletteEntriesMenuManager(this._activeXSPEditor, paramIMenuManager);

		this._paletteMenuManager.getCreateMenu().addMenuListener(this);
		this._paletteMenuManager.getCreateMenu().setVisible(true);

		this._tableMenuManager = new TabletMenuManager(this._activeXSPEditor, paramIMenuManager);
		this._viewPanelMenuManager = new ViewPanelMenuManager(this._activeXSPEditor, paramIMenuManager);
		this._tabPanelMenuManager = new TabPanelMenuManager(this._activeXSPEditor, paramIMenuManager);
		this._dataTableMenuManager = new DataTableMenuManager(this._activeXSPEditor, paramIMenuManager);

		this._workbenchMenuManager.insertAfter("com.ibm.rcp.ui.createmenu", this._tableMenuManager);
		this._workbenchMenuManager.insertAfter("com.ibm.rcp.ui.createmenu", this._viewPanelMenuManager);
		this._workbenchMenuManager.insertAfter("com.ibm.rcp.ui.createmenu", this._tabPanelMenuManager);
		this._workbenchMenuManager.insertAfter("com.ibm.rcp.ui.createmenu", this._dataTableMenuManager);

		this._workbenchMenuManager.updateAll(true);
	}

	public void contributeToToolBar(IToolBarManager paramIToolBarManager) {
		this._toolbarManager = paramIToolBarManager;
	}

	public void setActiveEditor(IEditorPart paramIEditorPart) {
		super.setActiveEditor(paramIEditorPart);
		if ((paramIEditorPart != null) && (!paramIEditorPart.equals(this._activeXSPEditor))
				&& ((paramIEditorPart instanceof SwiperSourceOnlyEditor))) {
			this._activeXSPEditor = ((SwiperSourceOnlyEditor) paramIEditorPart);
			SwiperSourceOnlyEditor localXFacesEditor = this._activeXSPEditor;
			if (localXFacesEditor != null) {
				if (localXFacesEditor.isPaletteInitialized()) {
					this._paletteMenuManager.updateEditorAndMenu(localXFacesEditor, this._workbenchMenuManager);
					this._workbenchMenuManager.updateAll(true);
					localXFacesEditor.savePaletteState();
				}
				this._tableMenuManager.updateEditorAndMenu(localXFacesEditor, this._workbenchMenuManager);
				this._viewPanelMenuManager.updateEditorAndMenu(localXFacesEditor, this._workbenchMenuManager);
				this._dataTableMenuManager.updateEditorAndMenu(localXFacesEditor, this._workbenchMenuManager);
				this._tabPanelMenuManager.updateEditorAndMenu(localXFacesEditor, this._workbenchMenuManager);
			}
		}
		SwiperSourceOnlyEditor localXFacesEditor = this._activeXSPEditor;
		if (localXFacesEditor != null) {
			GraphicalViewer localGraphicalViewer = (GraphicalViewer) localXFacesEditor
					.getAdapter(GraphicalViewer.class);
			if (localGraphicalViewer != null) {
				if (this._actionGroup == null) {
					this._actionGroup = new XFacesEditorActionGroup();
					this._actionGroup.addToToolBars(this._toolbarManager);
				}
				if (paramIEditorPart.getEditorSite() != null) {
					this._actionGroup.init(localXFacesEditor.getDesignerProject(), localGraphicalViewer,
							paramIEditorPart.getEditorSite().getPart());
				} else {
					this._actionGroup.init(localXFacesEditor.getDesignerProject(), localGraphicalViewer, null);
				}
				localGraphicalViewer.addSelectionChangedListener(this._actionGroup);
				localGraphicalViewer.addSelectionChangedListener(this._tableMenuManager);
				localGraphicalViewer.addSelectionChangedListener(this._viewPanelMenuManager);
				localGraphicalViewer.addSelectionChangedListener(this._tabPanelMenuManager);
				localGraphicalViewer.addSelectionChangedListener(this._dataTableMenuManager);
			}
		}
	}

	public void menuAboutToShow(IMenuManager paramIMenuManager) {
		SwiperSourceOnlyEditor localXFacesEditor = this._activeXSPEditor;
		if (localXFacesEditor.isPaletteInitialized()) {
			this._paletteMenuManager.updateEditorAndMenu(localXFacesEditor, this._workbenchMenuManager);
		}
	}

	public void dispose() {
		super.dispose();
		if (this._activeXSPEditor != null) {
			SwiperSourceOnlyEditor localXFacesEditor = this._activeXSPEditor;
			if (localXFacesEditor != null) {
				GraphicalViewer localGraphicalViewer = (GraphicalViewer) localXFacesEditor
						.getAdapter(GraphicalViewer.class);
				localGraphicalViewer.removeSelectionChangedListener(this._actionGroup);
				localGraphicalViewer.removeSelectionChangedListener(this._tableMenuManager);
				localGraphicalViewer.removeSelectionChangedListener(this._viewPanelMenuManager);
				localGraphicalViewer.removeSelectionChangedListener(this._tabPanelMenuManager);
				localGraphicalViewer.removeSelectionChangedListener(this._dataTableMenuManager);
			}
		}
		if ((this._paletteMenuManager != null) && (!this._paletteMenuManager.isDisposed())) {
			this._paletteMenuManager.dispose();
		}
		if (this.actionbarContributor != null) {
			if ((this.actionbarContributor.getActionBars() instanceof EditorActionBars)) {
				((EditorActionBars) this.actionbarContributor.getActionBars()).setEditorContributor(null);
			}
			this.actionbarContributor = null;
		}
		this._paletteMenuManager = null;
		this._activeXSPEditor = null;

		this._workbenchMenuManager.remove(this._tableMenuManager);
		this._workbenchMenuManager.remove(this._viewPanelMenuManager);
		this._workbenchMenuManager.remove(this._dataTableMenuManager);
		this._workbenchMenuManager.remove(this._tabPanelMenuManager);
		if (this._actionGroup != null) {
			this._actionGroup.dispose();
		}
		this._tableMenuManager.dispose();
		this._viewPanelMenuManager.dispose();
		this._tabPanelMenuManager.dispose();
		this._dataTableMenuManager.dispose();
		this._toolbarManager.removeAll();

		this._workbenchMenuManager = null;
		this._toolbarManager = null;
		this._actionGroup = null;
		this._tableMenuManager = null;
		this._viewPanelMenuManager = null;
		this._dataTableMenuManager = null;
		this._tabPanelMenuManager = null;
	}

	protected IEditorActionBarContributor createDesignViewerActionBarContributor() {
		this.actionbarContributor = new XSPActionBarContributor();
		return this.actionbarContributor;
	}
}