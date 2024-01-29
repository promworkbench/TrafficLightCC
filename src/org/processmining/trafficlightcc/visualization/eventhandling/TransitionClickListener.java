package org.processmining.trafficlightcc.visualization.eventhandling;

import java.util.EventListener;

public interface TransitionClickListener extends EventListener{
	
	public void transitionSelected(TransitionSelectedEvent e);

}
