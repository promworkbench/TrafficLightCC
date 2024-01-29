package org.processmining.trafficlightcc.algorithms.preprocessing;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.trafficlightcc.util.datastat.EventClassAggStat;
import org.processmining.trafficlightcc.util.datastat.LogStatUtil;

public class FrequencyBasedCostManipulator {
	
	public static final int FACTOR = 1000;

	/**
	 * Log to derive the event class info from.
	 */
	private XLog xlog;
	
	/**
	 * Event classes.
	 */
	private XEventClasses evClasses = null;
	
	/**
	 * Store information on event classes.
	 */
	protected Map<XEventClass, EventClassAggStat> eCInfo;

	public FrequencyBasedCostManipulator(XLog xlog, XEventClasses evClasses) {
		this.xlog = xlog;
		this.evClasses = evClasses;
	}
	
	public void initCostInfo() {
		// Not properly set
		if (xlog == null || this.evClasses == null) {
			return;
		}
		
		// Derive event class statistics
		eCInfo = LogStatUtil.getEventClassStats(xlog, evClasses) ;
	}

	public CostBasedCompleteParam adaptCostFunctions(TransEvClassMapping transEvMap, CostBasedCompleteParam param) {
		// Get cost functions
		Map<XEventClass, Integer> mLogCost = param.getMapEvClass2Cost();
		Map<Transition, Integer> mMoveCost = param.getMapTrans2Cost();
		Map<Transition, Integer> mSyncCost = param.getMapSync2Cost();
		
		Map<XEventClass, Integer> mLogCostCopy = new HashMap<>(mLogCost);
		Map<Transition, Integer> mMoveCostCopy = new HashMap<>(mMoveCost);
		Map<Transition, Integer> mSyncCostCopy = new HashMap<>(mSyncCost);
		
		CostBasedCompleteParam paramCopy = new CostBasedCompleteParam(mLogCostCopy, mMoveCostCopy, mSyncCostCopy);
		
    ////////////////////////////////////////
		// Copy Other Parameters
    ////////////////////////////////////////
		// Call all available getters and setters based on Eclipse' proposals
		paramCopy.setMaxNumOfStates(param.getMaxNumOfStates());
		paramCopy.setAsynchronousMoveSort(param.getAsynchronousMoveSort());
		paramCopy.setCanceller(param.getCanceller());
		paramCopy.setCreateConn(param.isCreatingConn());
		paramCopy.setEpsilon(param.getEpsilon());
		paramCopy.setExpectedAlignmentOverrun(param.getExpectedAlignmentOverrun());
		paramCopy.setFinalMarkings(param.getFinalMarkings());
		paramCopy.setGUIMode(param.isGUIMode());
		paramCopy.setInitialMarking(param.getInitialMarking());
    paramCopy.setNumThreads(param.getNumThreads());
    paramCopy.setQueueingModel(param.getQueueingModel());
    paramCopy.setType(param.getType());
    paramCopy.setUsePartialOrderedEvents(param.isPartiallyOrderedEvents());
    
    this.adaptCostFunctionsInplace(transEvMap, paramCopy);
    
    return paramCopy;
	}
	
	public void adaptCostFunctionsInplace(TransEvClassMapping transEvMap, CostBasedCompleteParam param) {
		// Get cost functions
		Map<XEventClass, Integer> mLogCost = param.getMapEvClass2Cost();
		Map<Transition, Integer> mMoveCost = param.getMapTrans2Cost();
		Map<Transition, Integer> mSyncCost = param.getMapSync2Cost();
		
		// Create costs groups (log moves)
		Map<Integer, List<XEventClass>> mCost2Ev = mLogCost.entrySet().stream()
				.collect(Collectors.groupingBy(Entry::getValue, 
						Collectors.mapping(Entry::getKey, Collectors.toList())));
		// Create costs groups (model moves)
		Map<Integer, List<Transition>> mCost2T = mMoveCost.entrySet().stream()
				.collect(Collectors.groupingBy(Entry::getValue, 
						Collectors.mapping(Entry::getKey, Collectors.toList())));
		
		// Compare event classes based on their occurrence and position
		Comparator<XEventClass> compareEvImportance = Comparator.comparing(eCInfo::get, 
				Comparator.nullsFirst(
						Comparator
							.comparing(EventClassAggStat::getCount)
							.thenComparing(Comparator.comparing(EventClassAggStat::getAvgPosRel).reversed())
							.thenComparing(Comparator.comparing(EventClassAggStat::getAvgPos).reversed())
						)
				);

		// Compare transitions based on their associated event classes
		Comparator<Transition> compareTransImportance = 
		Comparator.comparing(t -> transEvMap.get(t), compareEvImportance);
		
		////////////////////
		// Apply to cost groups
		////////////////////
		mCost2Ev.forEach((c, l) -> l.sort(compareEvImportance));
		mCost2T.forEach((c, l) -> l.sort(compareTransImportance));
		
		////////////////////
		// Adapt costs 
		// AFTER importance-based sorting
		////////////////////
		// Log moves
		mCost2Ev.forEach((c, l) -> {
			// Only adapt non-zero costs
			if(c != 0) {
				int i = 0;
				for (XEventClass eClass : l) {
					final int addCost = i;
					mLogCost.compute(eClass, (e2, cost) -> FACTOR * cost + addCost);
					i++;
				}
			}
		});

		// Model moves
		mCost2T.forEach((c, l) -> {
			if(c != 0) {
				int i = 0;
				for (Transition t : l) {
					final int addCost = i;
					mMoveCost.compute(t, (t2, cost) -> FACTOR * cost + addCost);
					i++;
				}
			}
		});

		// Also scale synchronous costs
		mSyncCost.entrySet().forEach(e -> e.setValue(e.getValue() * FACTOR));
	}

}
