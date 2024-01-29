package org.processmining.trafficlightcc.algorithms.replay.partialorder;

import java.util.Collection;
import java.util.HashMap;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

/**
 * Similar to the {@link TransEvClassMapping}; however, it reverses the map 
 * thereby offering the option to map multiple event classes to the same transition
 * 
 * @author brockhoff
 *
 */
public class EvClassTransMapping extends HashMap<XEventClass, Collection<Transition>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5463426591652946622L;
	
	/**
	 * Event classifier.
	 */
	private XEventClassifier eventClassifier;
	
	public EvClassTransMapping(XEventClassifier eventClassifier){
		this.eventClassifier = eventClassifier;
	}

	public XEventClassifier getEventClassifier() {
		return eventClassifier;
	}

	public void setEventClassifier(XEventClassifier eventClassifier) {
		this.eventClassifier = eventClassifier;
	}

}
