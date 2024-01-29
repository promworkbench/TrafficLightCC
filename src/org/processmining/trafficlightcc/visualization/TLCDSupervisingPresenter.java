package org.processmining.trafficlightcc.visualization;

import java.util.UUID;

import org.processmining.trafficlightcc.visualization.eventhandling.EventClassClickListener;
import org.processmining.trafficlightcc.visualization.eventhandling.EventClassSelectedEvent;
import org.processmining.trafficlightcc.visualization.eventhandling.TransitionClickListener;
import org.processmining.trafficlightcc.visualization.eventhandling.TransitionSelectedEvent;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;
import org.processmining.trafficlightcc.visualization.views.TLCDiagnosticsVisualization;

public class TLCDSupervisingPresenter {
	
	private final TLCDiagnosticsModel model;
	
	private final TLCDiagnosticsVisualization mainView;
	
	public TLCDSupervisingPresenter(TLCDiagnosticsModel model) {
		this.model = model;
		this.mainView = new TLCDiagnosticsVisualization(model);
		
		// Add listeners
		this.mainView.getPNView().addTransitionSelectedListener(new TransitionClickListener() {
			
			@Override
			public void transitionSelected(TransitionSelectedEvent e) {
				UUID idElement = model.getTrans2DiagId().get(e.getTransition());
				model.setSelectedUUID(idElement);
			}
		});
		
		this.mainView.getNonPNView().addEventClassSelectedListener(new EventClassClickListener() {
			
			@Override
			public void eventClassClicked(EventClassSelectedEvent e) {
				UUID idElement = model.getEvCl2DiagId().get(e.getEventClass());
				model.setSelectedUUID(idElement);
			}
		});
	}
	
	public TLCDiagnosticsVisualization getMainView() {
		return mainView;
	}
	

}
