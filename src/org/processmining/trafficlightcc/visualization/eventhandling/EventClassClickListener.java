package org.processmining.trafficlightcc.visualization.eventhandling;

import java.util.EventListener;

public interface EventClassClickListener extends EventListener {
	
	public void eventClassClicked(EventClassSelectedEvent e);

}
