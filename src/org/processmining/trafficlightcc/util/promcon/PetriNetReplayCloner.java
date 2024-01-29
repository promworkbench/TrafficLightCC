package org.processmining.trafficlightcc.util.promcon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.AbstractResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

public class PetriNetReplayCloner {
	
	public static Pair<PromReplayBundle, HashMap<DirectedGraphElement, DirectedGraphElement>> clonePNReplay(
			CostBasedCompleteParam param, TransEvClassMapping mapping, Petrinet pn) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		
		////////////////////////////////////////////////////////////
		// Copy Petri Net
		////////////////////////////////////////////////////////////
		PetrinetImpl newNet = new PetrinetImpl(pn.getLabel());
		Method cloneProtected = AbstractResetInhibitorNet.class.getDeclaredMethod("cloneFrom", 
			DirectedGraph.class);

		cloneProtected.setAccessible(true);

		// Clone and Init Element Correspondence Map
		HashMap<DirectedGraphElement, DirectedGraphElement> mappingPn = 
				(HashMap<DirectedGraphElement, DirectedGraphElement>) cloneProtected.invoke(newNet, pn); 

		////////////////////////////////////////////////////////////
		// Transition - Event Class mapping 
		////////////////////////////////////////////////////////////
		TransEvClassMapping mappingCopy = new TransEvClassMapping(mapping.getEventClassifier(), 
				mapping.getDummyEventClass());
		for (Transition t : pn.getTransitions()) {
			mappingCopy.put((Transition) mappingPn.get(t), mapping.get(t));
		}
		
		
		////////////////////////////////////////////////////////////
		// Replay Parameter
		////////////////////////////////////////////////////////////
		////////////////////////////////////////
		// Marking
		////////////////////////////////////////
		// "Copy" Initial Marking
		Marking initMarking = new Marking();
		param.getInitialMarking().stream()
			.map(p -> (Place) mappingPn.get(p))
			.forEach(initMarking::add);
		
		// Final Marking
		Marking[] finalMarkingsOld = param.getFinalMarkings();
		Marking[] finalMarkings = new Marking[finalMarkingsOld.length];
		for (int i = 0; i < finalMarkingsOld.length; i++) {
			Marking fm = new Marking();
			finalMarkingsOld[i].stream()
				.map(p -> (Place) mappingPn.get(p))
				.forEach(fm::add);
			finalMarkings[i] = fm;
		}
		
		////////////////////////////////////////
		// Move Costs
		////////////////////////////////////////
		// Cost Maps later used for the replay parameters later used for the replay parameters later 
		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap<>(param.getMapEvClass2Cost());
		Map<Transition, Integer> mapTrans2Cost = new HashMap<>();
		Map<Transition, Integer> mapSync2Cost = new HashMap<>();
		
		// Copy into new maps
		param.getMapSync2Cost().entrySet().stream().forEach(e -> {
			mapSync2Cost.put((Transition) mappingPn.get(e.getKey()), e.getValue());
		});
		param.getMapTrans2Cost().entrySet().stream().forEach(e -> {
			mapTrans2Cost.put((Transition) mappingPn.get(e.getKey()), e.getValue());
		});

		////////////////////////////////////////
		// Instantiate new Parameters
		////////////////////////////////////////
		CostBasedCompleteParam replayParameters = new CostBasedCompleteParam(mapEvClass2Cost.keySet(), 
				mappingCopy.getDummyEventClass(),
				pn.getTransitions());
		replayParameters.setInitialMarking(initMarking);
		replayParameters.setMaxNumOfStates(100000);
		replayParameters.setFinalMarkings(finalMarkings);
		replayParameters.setCreateConn(false);
		replayParameters.setGUIMode(false);
		
		replayParameters.setMapEvClass2Cost(mapEvClass2Cost);
		replayParameters.setMapSync2Cost(mapSync2Cost);
		replayParameters.setMapTrans2Cost(mapTrans2Cost);
		
		return Pair.of(new PromReplayBundle(replayParameters, mappingCopy, newNet), mappingPn);
		
	}

}
