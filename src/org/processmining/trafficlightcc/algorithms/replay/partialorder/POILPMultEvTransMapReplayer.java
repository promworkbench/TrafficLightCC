package org.processmining.trafficlightcc.algorithms.replay.partialorder;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.partialorder.models.replay.PartialAwarePILPDelegate;
import org.processmining.partialorder.plugins.replay.PartialOrderILPLinearAlg;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public class POILPMultEvTransMapReplayer extends PartialOrderILPLinearAlg {
	
	/**
	 * Additional event classes to transition mapping.
	 * 
	 * Additional w.r.t. the trans -> Event class mapping provided in 
	 * {@link this#replayLog(org.processmining.framework.plugin.PluginContext, org.processmining.models.graphbased.directed.petrinet.PetrinetGraph, org.deckfour.xes.model.XLog, org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping, org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter)}.
	 * 
	 */
	private final EvClassTransMapping addEvTransMap;

	public POILPMultEvTransMapReplayer(EvClassTransMapping addEvTransMap) {
		 this.addEvTransMap = addEvTransMap;
	}

	@Override
	protected PartialAwarePILPDelegate getDelegate(PetrinetGraph net, XLog log, XEventClasses classes,
			TransEvClassMapping mapping, int delta, int threads) {
		if (net instanceof Petrinet) {
			return new MultEvTransMapAwarePILPDelegate((Petrinet) net, log, classes, 
					mapping, addEvTransMap, 
					mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, 
					delta, threads, finalMarkings);
		}
		else {
			//		+ "not support multi event classes to transition mappings"  );
			return super.getDelegate(net, log, classes, mapping, delta, threads);
		}
	}
}
