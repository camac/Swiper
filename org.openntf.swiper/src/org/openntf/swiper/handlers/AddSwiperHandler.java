package org.openntf.swiper.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openntf.swiper.util.SwiperUtil;

import com.ibm.designer.domino.team.action.AbstractTeamHandler;

public class AddSwiperHandler extends AbstractTeamHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		processSelectedProject(HandlerUtil.getCurrentSelection(event));

		if (this.desProject != null) {
			System.out.println("Would add swiper to "
					+ this.desProject.getProject().getName());
			
			SwiperUtil.addNature(this.desProject.getProject());
		}
		
		return null;

	}

}
