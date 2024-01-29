package org.processmining.trafficlightcc.visualization.eventhandling;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class TransitionSelectedEventImpl implements TransitionSelectedEvent {
	
	private final Transition t;
	
	public TransitionSelectedEventImpl(Transition t) {
		this.t = t;
	}

	@Override
	public Transition getTransition() {
		return t;
	}

}
