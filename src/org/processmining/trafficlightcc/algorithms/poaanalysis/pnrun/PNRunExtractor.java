package org.processmining.trafficlightcc.algorithms.poaanalysis.pnrun;

import java.util.Iterator;
import java.util.function.Function;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.partialorder.models.replay.POSyncReplayResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

public class PNRunExtractor {
	
	
	public static PNRun extractRun(Petrinet pn, Marking initialMarking,
			POSyncReplayResult pnPOReplay, String labelCausalNet, 
			Function<Transition, Transition> transTranslator) {
		
		// Run Builder (extends the causal net step by step)
		PNRunNetBuilder pnRunBuilder = new PNRunNetBuilder(pn, labelCausalNet);
		
		// Initialize with initial marking
		pnRunBuilder.processInitMarking(initialMarking);
		
		////////////////////////////////////////
		// Alignment Iteration
		////////////////////////////////////////
		// Alignment Accessors
		Iterator<Object> itAl = pnPOReplay.getNodeInstance().iterator();
		int stepIndex = 0;

		for (StepTypes step : pnPOReplay.getStepTypes()) {
			Object obj = itAl.next();

			switch (step) {
				case L :
					// Ignore log moves
					break;
				case LMGOOD : // Sync Move
				case MREAL :  // Model move (non-invisible)
				case MINVI :  // Model move (invisible)
					Transition t = transTranslator.apply((Transition) obj);
					pnRunBuilder.processTransition(pn, t, stepIndex);
					break;
				case LMNOGOOD :
					break;
			default:
				break;
			}	
			stepIndex++;
		}
		
		return pnRunBuilder.getRun();
		
	}


}
