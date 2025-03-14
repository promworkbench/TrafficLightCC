package org.processmining.trafficlightcc.algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.partialorder.plugins.replay.StandardPTraceConverter;
import org.processmining.partialorder.ptrace.model.PLog;
import org.processmining.partialorder.ptrace.model.PTrace;
import org.processmining.partialorder.ptrace.model.imp.PLogImp;
import org.processmining.partialorder.ptrace.param.PAlignmentParameter;
import org.processmining.plugins.astar.petrinet.PartialOrderBuilder;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.DefTransClassifier;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.ITransClassifier;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.trafficlightcc.algorithms.preprocessing.FrequencyBasedCostManipulator;
import org.processmining.trafficlightcc.algorithms.replay.IndexedAResult;
import org.processmining.trafficlightcc.algorithms.replay.POReplayResultContainerR1;
import org.processmining.trafficlightcc.algorithms.replay.partialorder.ParitalRunInstanceSecondRun;
import org.processmining.trafficlightcc.algorithms.replay.partialorder.PartialRunInstanceFirstRun;
import org.processmining.trafficlightcc.models.POReplayDataWrapper;
import org.processmining.trafficlightcc.util.PrintUtil;

public class PartialRunInstanceCreator {

	
	/**
	 * Key of the attribute that will be added to events to indicate synchronous and non-synchronous 
	 * first round alignment moves.
	 */
	public static final String KEY_SYNC_EVENT = "R1Syc";
	
	/**
	 * High costs assigned to log moves on first round-synchronous activities.
	 * Be careful that these costs are high enough without causing overflows in the A* computations.
	 * In particular, be aware of {@link FrequencyBasedCostManipulator#FACTOR}.
	 */
	public static final int HIGHCOST = 100_000;
	
	/**
	 * Extension that is appended to the event's activity to generate 
	 * the synchronous move event class.
	 */
	public static final String SYNC_EXT = "-SYNC";

	
	/**
	 * Key for index reference to a trace's first round result.
	 */
	public static final String TRACE_R1_REF = "R1_Reference";
	
	/**
	 * Counts number of deviating traces.
	 * For each deviating trace, a trace variant will be extracted and added to the
	 * XLog that supports the partially ordered log in the second alignment iteration.
	 */
	private int deviatingTraceCounter = 0;
	
	
	public ParitalRunInstanceSecondRun buildSecondRoundRunInstance(XEventClasses eventClasses, 
			PartialRunInstanceFirstRun priFirst,
			POReplayResultContainerR1 resFirst) {
		
		////////////////////////////////////////
		// Event Classes and Classifiers
		////////////////////////////////////////
		// Classifier used in the first round
		// Event classifier for the second round
		String[] keysR1 = eventClasses.getClassifier().getDefiningAttributeKeys();
		String[] keysR2 = Arrays.copyOf(keysR1, keysR1.length + 1);
		keysR2[keysR1.length] = KEY_SYNC_EVENT;
		final XEventClassifier classifierR2 = new XEventAttributeClassifier("TL R2 Classifier", keysR2);
		
		////////////////////////////////////////
		// Process Replays
		// - Initialize the bridge based on OCCURRING event classes
		// - Create partially ordered traces
		////////////////////////////////////////
		EventClassBridgeR1R2 evClassBridge = new EventClassBridgeR1R2(eventClasses, classifierR2);
		List<Pair<XTrace, PTrace>> secRoundTraces = resFirst.replays().stream()
				.map(res -> processReplay(resFirst.log(), evClassBridge,res))
				.filter(p -> p.getRight().isPresent())
				.map(p -> Pair.of(p.getLeft(), p.getRight().get()))
				.collect(Collectors.toList());
		
		////////////////////////////////////////
		// Setup Transition CLASS -> Event CLASSES
		////////////////////////////////////////
		// Transition Classes 
		ITransClassifier transClassifier = new DefTransClassifier();
		TransClasses transClasses = new TransClasses(priFirst.pn(), transClassifier);
		
		final Map<TransClass, Set<EvClassPattern>> tClass2eClassPattern = new HashMap<>();
		priFirst.t2evMapping().entrySet().forEach(e -> {
			// t -> event Class (R1)
			// Get transition class 
			TransClass transClass = transClasses.getClassOf(e.getKey());
			// R1 event class
			XEventClass evClassR1 = e.getValue();
			
			Optional<EventClassesContainerR2> evClassesR2 = evClassBridge.getEventClassesR2(evClassR1);
			
			// It is ok to not assign any event class to the transition
			if (evClassesR2.isPresent()) {
				Set<EvClassPattern> patterns = new HashSet<>();
				
				// Synchronous event
				if (evClassesR2.get().evClassSync().isPresent()) {
					EvClassPattern pat = new EvClassPattern();
					pat.add(evClassesR2.get().evClassSync().get());
					patterns.add(pat);
				}

				// Non-synchronous event
				if (evClassesR2.get().evClassNonSync().isPresent()) {
					EvClassPattern pat = new EvClassPattern();
					pat.add(evClassesR2.get().evClassNonSync().get());
					patterns.add(pat);
				}
				
				tClass2eClassPattern.put(transClass, patterns);
			}
		});

		
		
		////////////////////////////////////////
		// Cost Maps
		////////////////////////////////////////
		////////////////////
		// Transition costs
		////////////////////
		Map<TransClass, Integer> mapTrans2Cost = new HashMap<TransClass, Integer>();
//		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap<XEventClass, Integer>(
//				priFirst.parameters().getMapEvClass2Cost());
		// Initialize transition CLASS costs from first round transition costs
		for (Entry<Transition, Integer> e : priFirst.parameters().getMapTrans2Cost().entrySet()) {
			TransClass tClass = transClasses.getClassOf(e.getKey());
			if (mapTrans2Cost.containsKey(tClass) 
					&& Integer.compare(mapTrans2Cost.get(tClass), e.getValue()) != 0) {
			}
			else {
				mapTrans2Cost.put(tClass, e.getValue());
			}
		}
		
		////////////////////
		// Event Class Costs
		////////////////////
		Map<XEventClass, Integer> mapEvClass2CostR1 = priFirst.parameters().getMapEvClass2Cost();
		Map<XEventClass, Integer> mapEvClass2CostR2 = new HashMap<XEventClass, Integer>();
		evClassBridge.getMapping().forEach((evClassR1, assocClasses) -> {
			if (assocClasses.evClassSync().isPresent()) {
				mapEvClass2CostR2.put(assocClasses.evClassSync().get(), HIGHCOST);
			}
			if (assocClasses.evClassNonSync().isPresent()) {
				mapEvClass2CostR2.put(assocClasses.evClassNonSync().get(), 
						mapEvClass2CostR1.get(evClassR1));
			}
		});
		
		
		
		////////////////////////////////////////
		// Prepare Data
		////////////////////////////////////////
		// Build Logs and Trace Data
		secRoundTraces.sort((p1, p2) -> 
			Integer.compare(p1.getRight().getTraceIndex(), p2.getRight().getTraceIndex()));

		// XLog
		XLog secRoundVariantLog = XLogBuilder.newInstance().startLog("VariantLogR2").build();
		secRoundTraces.forEach(p -> secRoundVariantLog.add(p.getLeft()));
		
		// PLog + Data
		PLog secRoundPlog = new PLogImp(secRoundVariantLog);
		secRoundTraces.forEach(p -> secRoundPlog.add(p.getRight().getTraceIndex(), p.getRight()));
		PartialOrderBuilder poBuilder = new StandardPTraceConverter(secRoundPlog, new PAlignmentParameter());
		POReplayDataWrapper secRoundReplayData = new POReplayDataWrapper(poBuilder, secRoundPlog, secRoundVariantLog);

		////////////////////////////////////////
		// Build Replay Parameters
		////////////////////////////////////////
		// Clean pattern mapping from unused event patterns
		// If an event is always executed synchronously, the normal "activity name pattern" is not needed.
		// Since the TransClass2PatternMap gets its encoding from the event log, this is also leads to errors
		// in the pattern encoding lookup.
		// --------------------
		// I could derive this from the first round; however, the following instantiation of the "TransClass2PatternMap"
		// calls "XLogInfoFactory.createLogInfo(...)" anyhow and the typical/default implementation caches this
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(secRoundVariantLog, evClassBridge.getEventClassesR2().getClassifier());
		XEventClasses usedEventClasses = logInfo.getEventClasses();
		// TODO Can be computed during the first round result processing
		// Used Event Classes
		final Map<TransClass, Set<EvClassPattern>> tClass2eClassPatternHot = tClass2eClassPattern.entrySet().stream()
				.flatMap(e -> e.getValue().stream().map(p -> Pair.of(e.getKey(), p))) // Pairs (t, eventPattern)
				.filter(p -> usedEventClasses.getByIdentity(p.getRight().get(0).getId()) != null) // Keep if eventPattern is relevant
				.collect(Collectors.groupingBy(Pair::getLeft,
						Collectors.mapping(Pair::getRight, Collectors.toSet())));	// Map: p -> (p.left = t, set(pair.right = pat)

		TransClass2PatternMap tClass2eClassPatternMapping = new TransClass2PatternMap(secRoundVariantLog,
				priFirst.pn(), evClassBridge.getEventClassesR2().getClassifier(), transClasses, tClass2eClassPatternHot);

		//		PrintUtil.lazy(() -> PrintUtil.formattedTClass2EClassesMap(tClass2eClassPatternMapping, 
		//				priFirst.pn().getTransitions())));
		
		PNManifestReplayerParameter parameter = new PNManifestReplayerParameter(mapTrans2Cost, mapEvClass2CostR2,
				tClass2eClassPatternMapping, priFirst.parameters().getMaxNumOfStates(), 
				priFirst.parameters().getInitialMarking(), priFirst.parameters().getFinalMarkings());
		
		return new ParitalRunInstanceSecondRun(priFirst.pn(), secRoundReplayData, parameter);

	}
	
	
	public Pair<XTrace, Optional<PTrace>> processReplay(final XLog xlog, EventClassBridgeR1R2 eventClassBridge,
			IndexedAResult poAlginResult) {

		// XTrace Variant
		XTrace traceVariant = (XTrace) xlog.get(poAlginResult.replayResult().getTraceIndex().first()).clone();
		// Assign Relation key to first Round
		traceVariant.getAttributes().put(TRACE_R1_REF, new XAttributeDiscreteImpl(TRACE_R1_REF, poAlginResult.id())); 
		
		int curIndexInTrace = 0;
		boolean containsLogMove = false;
		XEvent event;
		for (StepTypes step : poAlginResult.replayResult().getStepTypes()) {
			switch (step) {
			case LMGOOD:
				event = traceVariant.get(curIndexInTrace);
				// Sync event info
				eventClassBridge.addInfoSyncEvent(event);
				eventClassBridge.updateWithSnycExecution(event);
				curIndexInTrace++;
				break;
			case L:
				containsLogMove = true;
				event = traceVariant.get(curIndexInTrace);
				// Non-sync event info
				eventClassBridge.addInfoNonSyncEvent(event);
				eventClassBridge.updateWithNonSnycExecution(event);
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
		// a few events in the trace might remain unprocessed
		// In fact, the current implementation of alignments, will, when cancelled, 
		// return the alignment of the current prefix. Therefore, a postfix of the trace
		// might be unprocessed. If these events are not extended for the classifier, this will cause problems.
		// !!! In the current implementation, we will treat them as log moves!!!
		for (int misPos = curIndexInTrace; misPos < traceVariant.size(); misPos++) {
			event = traceVariant.get(misPos);
			eventClassBridge.addInfoNonSyncEvent(event);
			eventClassBridge.updateWithNonSnycExecution(event);
			// We treat them as if they were log moves
			containsLogMove = true;
		}

		// No log moves -> no event order relaxation required
		if (!containsLogMove) {
			return Pair.of(traceVariant, Optional.empty());
		}
		else {
			PTrace pTrace = PTraceBuilderRelaxedReplay.computePTraceFromReplay(traceVariant, 
					deviatingTraceCounter, poAlginResult.replayResult());
			deviatingTraceCounter++;
			return Pair.of(traceVariant, Optional.of(pTrace));
		}

	}
}
