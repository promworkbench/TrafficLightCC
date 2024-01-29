package org.processmining.trafficlightcc.algorithms.poaanalysis.pnrun;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.trafficlightcc.util.PrintUtil;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class PNRunNetBuilder {
	
	/**
	 * The Petri net from which the run is extracted.
	 */
	private final Petrinet petriNet;
	
	/**
	 *  Causal net induced by the replay's sync and model moves
	 */
	private final PetrinetImpl causalPN;

	/**
	 * Next free place copy index.
	 */
	private final TObjectIntHashMap<Place> placeCounter;
	
	/**
	 * Next free transition copy index.
	 */
	private final TObjectIntHashMap<Transition> transitionCounter;
	
	/**
	 * Mapping between places in the causal net and their corresponding place in 
	 * the original net.
	 */
	private final Map<Place, Place> mapCausal2OrigPlace; 

	/**
	 * Mapping between transitions in the causal net and their corresponding 
	 * transition in the original net.
	 */
	private final Map<Transition, Transition> mapCausal2OrigTransition; 

	/**
	 * Mapping from a place in the causal net to an index in the alignment
	 * (i.e., index of the move that created this token)
	 */
	private final TObjectIntHashMap<Place> placeAlignIndex;

	/**
	 * Mapping from a transition in the causal net to an index in the alignment
	 * (i.e., index of the sync move/model move that fired the transition)
	 */
	private final TObjectIntHashMap<Transition> transitionAlignIndex;
	
	/**
	 * Mapping from indices in the alignment to the corresponding transition in the run
	 */
	private final TIntObjectHashMap<Transition> alignIndexRunTransition;
	
	/**
	 * Mapping place in the original Petri net
	 * -> FIFO Queue of tokens currently on this place 
	 * (causal net places without outgoing arcs)
	 */
	private Map<Place, Queue<Place>> pendingTokens;
	
	/**
	 * After getting the run, we freeze the causal net.
	 * Otherwise, it could be manipulated by side-effects.
	 */
	boolean freeze = false;
	
	/**
	 * Initial marking in the run.
	 */
	private Marking initialMarking;
	
	
	public PNRunNetBuilder(Petrinet pn, String labelCausalNet) {
		this.petriNet = pn;

		this.causalPN = (PetrinetImpl) PetrinetFactory.newPetrinet(labelCausalNet);
		
		// Counter maps
		this.placeCounter = new TObjectIntHashMap<>();
		this.transitionCounter = new TObjectIntHashMap<>();
		
		// Mappings between causal net and original Petri net
		this.mapCausal2OrigPlace = new HashMap<>();
		this.mapCausal2OrigTransition = new HashMap<>();
		
		// Causal net element -> alignment index
		this.placeAlignIndex = new TObjectIntHashMap<>();
		this.transitionAlignIndex = new TObjectIntHashMap<>();
		this.alignIndexRunTransition = new TIntObjectHashMap<>();
		
		// Token Queues
		this.pendingTokens = new HashMap<>();

	}
	
	public void processInitMarking(Collection<Place> initMarking) {
		this.checkState();
		this.initialMarking = new Marking();
		for (Place p : initMarking) {
			// Initial marking is before the align (0th move)
			Place cPlace = this.addTokenPlace(p, 0);
			this.initialMarking.add(cPlace);
		}
	}
	
	public void processTransition(Petrinet pn, Transition t, int alignIndex) {
		this.checkState();
		// Preset (places) in original net
		Collection<Place> pnPreSet = 
			pn.getInEdges(t).stream()
				.map(e -> (Place) e.getSource())
				.collect(Collectors.toSet());
		//		PrintUtil.lazy(() -> 
		//			pnPreSet.stream()
		//				.map(Place::getId)
		//				.map(NodeID::toString)
		//				.collect(Collectors.joining(", ", "[", "]"))
		//		)
		//);
		// Postset (places) in original net
		Collection<Place> pnPostSet = 
			pn.getOutEdges(t).stream()
				.map(e -> (Place) e.getTarget())
				.collect(Collectors.toSet());
		//		PrintUtil.lazy(() -> 
		//			pnPostSet.stream()
		//				.map(Place::getId)
		//				.map(NodeID::toString)
		//				.collect(Collectors.joining(", ", "[", "]"))
		//		)
		//);
		
		// Remove tokens and return token-containing 
		// places in Causal net for the preset
		// ASSUMING that the run is valid, there must be a token in the queue
		Collection<Place> cPNPrePlaces = 
				pnPreSet.stream()
					.map(this.pendingTokens::get) // Get queue
					.map(q -> Optional.of(q.poll()))  // FIFO dequeue token place
					.map(Optional::get)
					.collect(Collectors.toSet());

		////////////////////
		// Structure Update
		////////////////////
		// Transitions
		Transition cTrans = this.addTransition(t, alignIndex);
		
		// Places for postset
		Collection<Place> cPNPostPlaces = pnPostSet.stream()
				.map(p -> addTokenPlace(p, alignIndex))
				.collect(Collectors.toSet());
		
		// Edges 
		// Preset
		cPNPrePlaces.forEach(p -> this.causalPN.addArc(p, cTrans));
		// Postset
		cPNPostPlaces.forEach(p -> this.causalPN.addArc(cTrans, p));

	}
	
	private Place addTokenPlace(Place p, int alignMove) {
		// Update counter
		int repCounter = placeCounter.adjustOrPutValue(p, 1, 0);
		String cPlaceLabel = p.getLabel() + "_" + repCounter;
		
		// Add place to causal net
		Place cPlace = this.causalPN.addPlace(cPlaceLabel);
		
		////////////////////
		// Update Internal Datastructures
		////////////////////
		this.mapCausal2OrigPlace.put(cPlace, p);
		this.placeAlignIndex.put(cPlace, alignMove); 
		this.addPendingToken(p, cPlace);
		
		return cPlace;
	}
	
	private Transition addTransition(Transition t, int alignMove) {
		// Update counter
		int repCounter = transitionCounter.adjustOrPutValue(t, 1, 0);
		String cTransLabel = t.getLabel() + "_" + repCounter;

		// Add transition to causal net
		Transition cTrans = this.causalPN.addTransition(cTransLabel);

		////////////////////
		// Update Internal Datastructures
		////////////////////
		this.mapCausal2OrigTransition.put(cTrans, t);
		this.transitionAlignIndex.put(cTrans, alignMove); 
		this.alignIndexRunTransition.put(alignMove, cTrans);

		
		return cTrans;
		
	}
	
	
	/**
	 * Add a new pending token (place in causal net) for the provided place to 
	 * its token queue.
	 * @param place
	 * @param cPlace
	 */
	private void addPendingToken(Place place, Place cPlace) {
		Queue<Place> q = null;
		if (this.pendingTokens.containsKey(place)) {
			q = this.pendingTokens.get(place);
		}
		else {
			q = new LinkedList<>();
			this.pendingTokens.put(place, q);
		}
		q.add(cPlace);
	}
	
	public PNRun getRun() {
		Marking finalMarking = new Marking();
		this.pendingTokens.entrySet().stream()
			.flatMap(e -> e.getValue().stream())
			.forEach(finalMarking::add);
		
		// Freeze the current causal net
		this.freeze = true;

		return new PNRun(this.causalPN, this.initialMarking, finalMarking,
				mapCausal2OrigPlace, mapCausal2OrigTransition,
				placeAlignIndex, 
				transitionAlignIndex, alignIndexRunTransition);

	}
	
	private void checkState() {
		if (this.freeze) {
			throw new IllegalStateException("Not allowed to mutate a run after getting it.");
		}
	}

}
