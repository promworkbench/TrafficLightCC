package org.processmining.trafficlightcc.algorithms.poaanalysis.pnrun;

import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class PNRun {

    private final Petrinet pnRun;
		private final Marking initialMarking;
		private final Marking finalMarking;
		private final Map<Place, Place> mapCausal2OrigPlace;
		private final Map<Transition, Transition> mapCausal2OrigTransition;
		private final TObjectIntHashMap<Place> placeAlignIndex;
		private final TObjectIntHashMap<Transition> transitionAlignIndex;
		private final TIntObjectHashMap<Transition> alignIndexRunTransition;

  public PNRun(Petrinet pnRun, 
      Marking initialMarking, Marking finalMarking,
      Map<Place, Place> mapCausal2OrigPlace, Map<Transition, Transition> mapCausal2OrigTransition,
      TObjectIntHashMap<Place> placeAlignIndex, 
      TObjectIntHashMap<Transition> transitionAlignIndex, TIntObjectHashMap<Transition> alignIndexRunTransition) {
    this.pnRun = pnRun;
		this.initialMarking = initialMarking;
		this.finalMarking = finalMarking;
		this.mapCausal2OrigPlace = mapCausal2OrigPlace;
		this.mapCausal2OrigTransition = mapCausal2OrigTransition;
		this.placeAlignIndex = placeAlignIndex;
		this.transitionAlignIndex = transitionAlignIndex;
		this.alignIndexRunTransition = alignIndexRunTransition;
  }

  public Petrinet pnRun() {
    return pnRun;
  }

  public Marking initialMarking() {
    return initialMarking;
  }

  public Marking finalMarking() {
    return finalMarking;
  }

  public Map<Place, Place> mapCausal2OrigPlace() {
    return mapCausal2OrigPlace;
  }

  public Map<Transition, Transition> mapCausal2OrigTransition() {
    return mapCausal2OrigTransition;
  }

  public TObjectIntHashMap<Place> placeAlignIndex() {
    return placeAlignIndex;
  }

  public TObjectIntHashMap<Transition> transitionAlignIndex() {
    return transitionAlignIndex;
  }

  public TIntObjectHashMap<Transition> alignIndexRunTransition() {
    return alignIndexRunTransition;
  }
}
