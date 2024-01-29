package org.processmining.trafficlightcc.algorithms;

import java.util.Iterator;

import org.deckfour.xes.model.XTrace;
import org.processmining.partialorder.models.dependency.DependencyFactory;
import org.processmining.partialorder.ptrace.model.PTrace;
import org.processmining.partialorder.ptrace.model.imp.PTraceImp;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class PTraceBuilderRelaxedReplay {
	
	public static PTrace computePTraceFromReplay(XTrace t, int traceIndex, SyncReplayResult aTrace) {
		int traceSize = t.size();
		PTrace ptrace = new PTraceImp(t, traceIndex);
		if(traceSize == 0){
			return ptrace;
		}
		
		int prevSnycTrace = -1;
		int curIndexInTrace = 0;
		Iterator<StepTypes> itType = aTrace.getStepTypes().iterator();
		//Iterator<Object> itNode = aTrace.getNodeInstance().iterator();
		// Iterate over all alignment operations
		while (itType.hasNext()) {
			StepTypes type = itType.next();
			switch (type) {
			case LMGOOD:
				ptrace.addEvent(curIndexInTrace);
				if (prevSnycTrace >= 0) {
					ptrace.addDependency(DependencyFactory.createSimpleDirectDependency(
							prevSnycTrace, curIndexInTrace), prevSnycTrace, curIndexInTrace);
				}
				prevSnycTrace = curIndexInTrace;
				curIndexInTrace++;
				break;
			case L:
				ptrace.addEvent(curIndexInTrace);
				curIndexInTrace++;
				break;
			case LMNOGOOD:
			case LMREPLACED:
			case LMSWAPPED:
			case MINVI:
			case MREAL:
			default:
				break;
			
			}
		}
		
		// If we could only approximate the alignment,
		// a few events in the postfix of the trace might remain unprocessed
		// !!! In the current implementation, we will treat them as log moves!!!
		for (int misPos = curIndexInTrace; misPos < t.size(); misPos++) {
			ptrace.addEvent(misPos);
		}
		
		return ptrace;
		
	}

}
