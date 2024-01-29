package org.processmining.trafficlightcc.algorithms.replay.partialorder;

import java.util.Collection;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.partialorder.models.replay.PartialAwarePILPDelegate;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class MultEvTransMapAwarePILPDelegate extends PartialAwarePILPDelegate {
	
	private final EvClassTransMapping addEventToTransitions;

	/**
	 * Constructor similar to all other delegates.
	 * However, it explicitly considers the event class to transition map allowing to map multiple event classes
	 * to the same transition. For easier compatibility, is also considers the standard {@link TransEvClassMapping}.
	 * The {@link EvClassTransMapping} <b>must only</b> specify additional (to the {@link TransEvClassMapping}) mapping. 
	 * 
	 * @param net Petri net
	 * @param log Log
	 * @param classes Event Classes
	 * @param map Transition to event class map
	 * @param evtMap Additional event classes to transition map (in addition to map)
	 * @param mapTrans2Cost Costs for model moves
	 * @param mapEvClass2Cost Costs for log moves
	 * @param mapSync2Cost Costs for synchronous moves
	 * @param delta 
	 * @param threads
	 * @param set
	 */
	public MultEvTransMapAwarePILPDelegate(Petrinet net, XLog log, XEventClasses classes, 
			TransEvClassMapping map, EvClassTransMapping addEvtMap,
			Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, Marking[] set) {
		super(net, log, classes, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads, set);
		
		this.addEventToTransitions = addEvtMap;
	}

	@Override
	protected void initialize(Collection<XEventClass> eventClasses, TransEvClassMapping map,
			Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, Marking... set) {
		
		// Important: Run the standard initialization
		super.initialize(eventClasses, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, set);
		
		////////////////////////////////////////
		// Adapt the internal Mapping to cater for additional 
		// --- Multi event classes to activity mapping: ---
		// activity Index  -> TransitionS
		// Transition  -> Activity IndiceS
		//////////////////////////////
		// As suggested above, the delegate internally allows for
		// a m:n mapping between transitions and event classes based on a categorical indexing.
		//
		// The internal categorical event class embedding
		// is calculated based on the event classes that we provided
		// in the constructor. Therefore, our "additional" event classes
		// must be in the set of event classes beforehand.
		//
		// We can offer the m:n mapping to the outside. 
		////////////////////////////////////////
		
		final XEventClass dummy = map.getDummyEventClass();
		this.addEventToTransitions.forEach((e, transCollection) -> {
			// An explicit additional mapping of the dummy class to a transition???
			if (dummy.compareTo(e) == 0) {
				throw new IllegalArgumentException("Mapping the dummy event class explicitly to a transition "
						+ "(not vice versa) does not make sense!");
			}
			
			// Iterate over mapped transitions
			transCollection.forEach(t -> {
				// Skip //if not additional to transition -> event class mapping
				if (map.getOrDefault(t, dummy).compareTo(e) == 0) {
					return;
				}
				
				short catE = this.act2int.get(e);
				if (catE == NEV) {
					// somehow, the map contains a event class that is not part of the eventclasses
					// provided (for example a dummy event class).
					// We do not cater for that here.
					return;
				}
				
				// Index of the transition
				short catT = this.trans2int.get(t);
				
				// Activity -> Transitions
				actIndex2trans.get(catE).add(catT);
				// Transition -> Activities
				transIndex2act.get(catT).add(catE);
			});
		});
		
	}
	
	

}
