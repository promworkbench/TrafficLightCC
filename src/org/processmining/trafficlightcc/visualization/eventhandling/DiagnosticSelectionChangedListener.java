package org.processmining.trafficlightcc.visualization.eventhandling;

import java.util.EventListener;

public interface DiagnosticSelectionChangedListener extends EventListener {
	
	public void selectedElementChanged(DiagnosticSelectionChangedEvent event);

}
