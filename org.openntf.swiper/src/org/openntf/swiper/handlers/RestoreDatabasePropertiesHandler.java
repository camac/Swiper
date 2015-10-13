package org.openntf.swiper.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.designer.domino.team.action.AbstractTeamHandler;

public class RestoreDatabasePropertiesHandler extends AbstractTeamHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		processSelectedProject(HandlerUtil.getCurrentSelection(event));

		if (this.desProject != null) {
			System.out.println("Would Restore App Properties for" + this.desProject.getProject().getName());

		}

		return null;
		
	}

}
