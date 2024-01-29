package org.processmining.trafficlightcc.visualization.eventhandling;

import org.deckfour.xes.classification.XEventClass;

public class EventClassSelectedEvent {

	private final XEventClass evClass;
	
	public EventClassSelectedEvent(XEventClass evClass) {
		this.evClass = evClass;
	}

	public XEventClass getEventClass() {
		return evClass;
	}
}
