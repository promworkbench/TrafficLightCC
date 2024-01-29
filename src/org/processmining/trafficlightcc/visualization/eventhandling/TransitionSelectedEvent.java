package org.processmining.trafficlightcc.visualization.eventhandling;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public interface TransitionSelectedEvent {
	
	public Transition getTransition();

}
