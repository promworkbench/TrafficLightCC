package org.processmining.trafficlightcc.util.promcon;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

public class ProMElementCloner {
	
	public static CostBasedCompleteParam cloneReplayParam(CostBasedCompleteParam param) {
		
		////////////////////////////////////////
		// Marking
		////////////////////////////////////////
		// "Copy" Initial Marking
		Marking initMarking = new Marking(param.getInitialMarking());

		// Final Marking
		Marking[] finalMarkingsOld = param.getFinalMarkings();
		Marking[] finalMarkings = new Marking[finalMarkingsOld.length];
		for (int i = 0; i < finalMarkingsOld.length; i++) {
			Marking fmOld = finalMarkingsOld[i];
			Marking fm = new Marking(fmOld);
			finalMarkings[i] = fm;
		}
		
		////////////////////////////////////////
		// Move Costs
		////////////////////////////////////////
		// Cost Maps later used for the replay parameters later used for the replay parameters later 
		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap<>(param.getMapEvClass2Cost());
		Map<Transition, Integer> mapTrans2Cost = new HashMap<>(param.getMapTrans2Cost());
		Map<Transition, Integer> mapSync2Cost = new HashMap<>(param.getMapSync2Cost());
		
		////////////////////////////////////////
		// Instantiate new Parameters
		////////////////////////////////////////
		CostBasedCompleteParam replayParameters = new CostBasedCompleteParam(
				mapEvClass2Cost, mapTrans2Cost, mapSync2Cost);
		replayParameters.setInitialMarking(initMarking);
		replayParameters.setFinalMarkings(finalMarkings);
		replayParameters.setCreateConn(false);
		replayParameters.setGUIMode(false);
		
		return replayParameters;
		
	}

}
