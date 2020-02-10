package org.openntf.swiper;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.ui.IFileEditorInput;

import com.ibm.commons.swt.util.UIUtils;
import com.ibm.designer.domino.ide.resources.DominoResourcesPlugin;
import com.ibm.designer.domino.ide.resources.NsfException;
import com.ibm.designer.domino.ide.resources.project.IDominoDesignerProject;
import com.ibm.designer.domino.preferences.DominoPreferenceManager;
import com.ibm.designer.domino.xsp.editor.XSPStructuredTextEditor;
import com.ibm.designer.domino.xsp.ide.imagePalette.DesignerDefaultEditDomain;
import com.ibm.designer.domino.xsp.internal.XFacesComponentsRegistry;
import com.ibm.designer.domino.xsp.palette.PalettePopulator;
import com.ibm.designer.prj.resources.commons.CommonProjectSynchronizer;
import com.ibm.designer.prj.resources.commons.DesignerProjectAdapter;
import com.ibm.designer.prj.resources.commons.ICommonDesignerProject;
import com.ibm.etools.xve.palette.PaletteAccess;
import com.ibm.etools.xve.palette.PaletteChangeListener;

public class SwiperSourceOnlyEditor extends XSPStructuredTextEditor {

	private boolean _paletteInitialized = false;

	private PalettePopulator _palettePopulator;

	private PaletteChangeListener _paletteChangeListener = null;

	private Runnable _initCustomControlsRunnable;

	private DesignerProjectAdapter _projectListener;

	public SwiperSourceOnlyEditor() {
		super();
		System.out.println("HEY HEY");
	}

	public boolean isPaletteInitialized() {
		return this._paletteInitialized;
	}

	protected void savePaletteState() {
		DominoPreferenceManager localDominoPreferenceManager = DominoPreferenceManager.getInstance();
		boolean bool = localDominoPreferenceManager.getBooleanValue("domino.prefs.palette.state.saving", false);
		if (!bool) {
			DesignerDefaultEditDomain localDesignerDefaultEditDomain = null;// getDesignerEditDomain();
			if (localDesignerDefaultEditDomain != null) {
				localDesignerDefaultEditDomain.savePaletteState();
			}
		}
	}

	public IDominoDesignerProject getDesignerProject() {
		try {
			return DominoResourcesPlugin
					.getDominoDesignerProject(((IFileEditorInput) getEditorInput()).getFile().getProject());
		} catch (NsfException localNsfException) {
			localNsfException.printStackTrace();
		}
		return null;
	}

	protected void initializeGraphicalViewer() {

		PaletteAccess localPaletteAccess = (PaletteAccess) getAdapter(PaletteAccess.class);
		if (localPaletteAccess != null) {
			this._paletteChangeListener = new PaletteChangeListener() {
				public void contentsInitialized() {
					SwiperSourceOnlyEditor.this.getPalettePopulator().initializePalette();
					SwiperSourceOnlyEditor.this.setCallbackControlPaletteVisibility();
					if ((!SwiperSourceOnlyEditor.this.isPaletteInitialized())
							&& ((PaletteViewer) SwiperSourceOnlyEditor.this.getAdapter(PaletteViewer.class) != null)
							&& (((PaletteViewer) SwiperSourceOnlyEditor.this.getAdapter(PaletteViewer.class))
									.getControl() != null)) {
						UIUtils.setInfoHelp(
								((PaletteViewer) SwiperSourceOnlyEditor.this.getAdapter(PaletteViewer.class))
										.getControl(),
								"com.ibm.designer.domino.ui.doc.designerControls");
					}
					SwiperSourceOnlyEditor.this._paletteInitialized = true;
				}
			};
			localPaletteAccess.addPaletteChangeListener(this._paletteChangeListener);
		}

		// PaletteViewer localPaletteViewer = (PaletteViewer)
		// getAdapter(PaletteViewer.class);
		// CommandStack localCommandStack = getEditDomain().getCommandStack();
		// DesignerDefaultEditDomain localDesignerDefaultEditDomain = new
		// DesignerDefaultEditDomain(this,
		// localPaletteViewer);
		// localDesignerDefaultEditDomain.setCommandStack(localCommandStack);
		// localDesignerDefaultEditDomain.setActiveTool(this._xspSelectionTool);
		// setEditDomain(localDesignerDefaultEditDomain);
		//
		// EditorUtils.setDesignerProject(getGraphicalViewer(),
		// getDesignerProject());
		//
		// super.initializeGraphicalViewer();
		// Document localDocument = ((ModelWrapper)
		// getGraphicalViewer().getContents().getModel()).getDocument();
		// FormModelUtil.ensureRequiredNodesExist(localDocument);
		//
		// new XspConfigModel(this);
		//
		// moveBOD();
		// if (EditorUtils.isEditingCustomControl(this)) {
		// UIUtils.setInfoHelp(getViewerControl().getParent().getParent(),
		// "com.ibm.designer.domino.ui.doc.designerCustomControls");
		// } else {
		// UIUtils.setInfoHelp(getViewerControl().getParent().getParent(),
		// "com.ibm.designer.domino.ui.doc.designerWebPages");
		// }
		// PaletteAccess localPaletteAccess = (PaletteAccess)
		// getAdapter(PaletteAccess.class);
		// if (localPaletteAccess != null) {
		// this._paletteChangeListener = new PaletteChangeListener() {
		// public void contentsInitialized() {
		// XFacesEditor.this.getPalettePopulator().initializePalette();
		// XFacesEditor.this.setCallbackControlPaletteVisibility();
		// if ((!XFacesEditor.this.isPaletteInitialized())
		// && ((PaletteViewer) XFacesEditor.this.getAdapter(PaletteViewer.class)
		// != null)
		// && (((PaletteViewer)
		// XFacesEditor.this.getAdapter(PaletteViewer.class))
		// .getControl() != null)) {
		// UIUtils.setInfoHelp(
		// ((PaletteViewer)
		// XFacesEditor.this.getAdapter(PaletteViewer.class)).getControl(),
		// "com.ibm.designer.domino.ui.doc.designerControls");
		// }
		// XFacesEditor.this._paletteInitialized = true;
		// }
		// };
		// localPaletteAccess.addPaletteChangeListener(this._paletteChangeListener);
		// }
		// hookRegistryChanges();
		// if (this._palettePrefListener == null) {
		// this._palettePrefListener = new IDominoPreferenceListener() {
		// public void preferenceChanged(DominoPreferenceChangeEvent
		// paramAnonymousDominoPreferenceChangeEvent) {
		// if (paramAnonymousDominoPreferenceChangeEvent.getType() == 30) {
		// XFacesEditor.this.getPalettePopulator().refresh();
		// XFacesEditor.this.setCallbackControlPaletteVisibility();
		// }
		// }
		// };
		// }
		// DominoPreferenceManager.getInstance().addPreferenceListener(this._palettePrefListener);
		//
		// this._selectionChangedListener = new ISelectionChangedListener() {
		// public void selectionChanged(SelectionChangedEvent
		// paramAnonymousSelectionChangedEvent) {
		// IAction localIAction =
		// XFacesEditor.this.getEditorSite().getActionBars()
		// .getGlobalActionHandler(ActionFactory.UP.getId());
		// localIAction.setEnabled(localIAction.isEnabled());
		// }
		// };
		// getEditorSite().getSelectionProvider().addSelectionChangedListener(this._selectionChangedListener);
		// getGraphicalViewer().setEditDomain(localDesignerDefaultEditDomain);
		// this._isActivating = false;
	}

	private void updateRegistryDependencies(final ICommonDesignerProject paramICommonDesignerProject) {
		this._initCustomControlsRunnable = new Runnable() {
			public void run() {
				SwiperSourceOnlyEditor.this.refreshPalette();
				if ((paramICommonDesignerProject instanceof IDominoDesignerProject)) {
					XFacesComponentsRegistry.initCustomControls((IDominoDesignerProject) paramICommonDesignerProject);
				}
			}
		};
		getEditorSite().getShell().getDisplay().asyncExec(this._initCustomControlsRunnable);
	}

	private void hookRegistryChanges() {
		this._projectListener = new DesignerProjectAdapter() {
			public void designerProjectRegistryChanged(ICommonDesignerProject paramAnonymousICommonDesignerProject) {
				SwiperSourceOnlyEditor.this.updateRegistryDependencies(paramAnonymousICommonDesignerProject);
			}

			public void designerDesignDataUpdated(ICommonDesignerProject paramAnonymousICommonDesignerProject) {
				SwiperSourceOnlyEditor.this.updateRegistryDependencies(paramAnonymousICommonDesignerProject);
			}

			public void designerProjectPropertiesChanged(ICommonDesignerProject paramAnonymousICommonDesignerProject) {

			}
		};

		CommonProjectSynchronizer.addCommonDesignerProjectChangeListener(this._projectListener);
	}

	public void refreshPalette() {
		if (this.isClosed) {
			return;
		}
		getPalettePopulator().refresh();
		setCallbackControlPaletteVisibility();
	}

}
