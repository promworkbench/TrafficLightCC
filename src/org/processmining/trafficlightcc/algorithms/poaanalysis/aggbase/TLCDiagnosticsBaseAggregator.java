package org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.EventIndexOrderDiagnostics;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.EventIndexSyncDiagnostic;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.OrderDiagnosticType;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.TLVariantIndexDiagnostics;

public class TLCDiagnosticsBaseAggregator {

	
	public static Collection<TLCElementDiagnostic> aggregateIndexBasedDiagnostics(final Petrinet pn, final XLog xlog, 
			final TransEvClassMapping transEvMapping,
			final Collection<TLVariantIndexDiagnostics> indexedDiagnostics) {
		
		//TODO the passed PN is not the same a the one in the diagnostics
		// transitions are not the same
		
		// Transition duplicates in terms of mapped event classes
		// Count assigned transitions for each event class
		Map<XEventClass, Long> eventClassTransCount = transEvMapping.entrySet().stream()
			.collect(Collectors.toMap(Entry::getValue, e -> 1L, Long::sum));
		
		// Event classes that can be uniquely identified with a transition
		Set<XEventClass> eventClassesUniqTrans = eventClassTransCount.entrySet().stream()
				.filter(e -> e.getValue() == 1L)
				.map(Entry::getKey)
				.collect(Collectors.toSet());
		
		// Event Classes (it would be ok if we don't count the time required to determine this again)
		// In principle, we could simply pass it from the setup of the cost function where this
		// is computed anyway. However to decouple it, we did not do that yet
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(transEvMapping.getEventClassifier(), xlog);
		
		// All diagnostics
		Set<TLCElementDiagnostic> elementDiagnostics = new HashSet<>();
		// Mapping from transitions and event classes to their corresponding diagnostics element
		// If an event class can be uniquely identified with a transition, they point to the same entry
		Map<XEventClass, TLCElementDiagnostic> mapEC2DiagRecorder = new HashMap<>();
		Map<Transition, TLCElementDiagnostic> mapT2DiagRecorder = new HashMap<>();
		
		//////////////////////////////
		// Setup Counters
		//////////////////////////////
		// Init counter for the transitions (and uniquely attributable event classes)
		for (Transition t: pn.getTransitions()) {
			// Ignore tau
			if (t.isInvisible()) {
				continue;
			}

			XEventClass evClass = transEvMapping.get(t);
			TLCElementDiagnostic diag;
			// Transition and its event class are uniquely identifiable
			if (eventClassesUniqTrans.contains(evClass)) {
				// Add to both mappings
				diag = new TLCElementDiagnostic(TLCElementType.ATTRIBUTABLE_TRANSITION, 
						Optional.of(t), Optional.of(evClass), xlog.size());
				mapEC2DiagRecorder.put(evClass, diag);
			}
			// Event class can be attributed to multiple transitions
			else if (eventClasses.getClasses().contains(evClass)){
				diag = new TLCElementDiagnostic(TLCElementType.TRANSITION_NONUNIQUE, 
						Optional.of(t), Optional.empty(), xlog.size());
			}
			// Transition only exists in Petri net
			else {
				diag = new TLCElementDiagnostic(TLCElementType.TRANSITION_ADDITIONAL, 
						Optional.of(t), Optional.empty(), xlog.size());
			}
			mapT2DiagRecorder.put(t, diag);
			elementDiagnostics.add(diag);
		}
		
		// Init counter for event classes that cannot be attributed uniquely to a transition 
		for (XEventClass evClass : eventClasses.getClasses()) {
			if (eventClassesUniqTrans.contains(evClass)) {
				// Already handled that before
				continue;
			}
			TLCElementDiagnostic diag = null;
			// There is at least one transition that maps to this event class
			if (eventClassTransCount.containsKey(evClass)) {
				diag = new TLCElementDiagnostic(TLCElementType.EVENTCLASS_NONUNIQUE, 
						Optional.empty(), Optional.of(evClass), xlog.size());
			}
			// Event class does not occur in Petri net at all
			else {
				diag = new TLCElementDiagnostic(TLCElementType.EVENTCLASS_ADDITIONAL, 
						Optional.empty(), Optional.of(evClass), xlog.size());
			}
			mapEC2DiagRecorder.put(evClass, diag);
			elementDiagnostics.add(diag);
		}

		//////////////////////////////
		//  Populate Counters
		//////////////////////////////
		
		for (TLVariantIndexDiagnostics tlVarIndices : indexedDiagnostics) {
			digestVariantIndexDiagnostics(transEvMapping, eventClasses, 
					mapEC2DiagRecorder, mapT2DiagRecorder, tlVarIndices, xlog);
		}
		
		return elementDiagnostics;
		
	}
	
	private static void digestVariantIndexDiagnostics(TransEvClassMapping transEvMapping,
			XEventClasses eventClasses,
			Map<XEventClass, TLCElementDiagnostic> mapEC2DiagRecorder,
			Map<Transition, TLCElementDiagnostic> mapT2DiagRecorder, 
			TLVariantIndexDiagnostics tlVariantDiag,
			XLog xlog) {
		
		// Set of already encountered transitions in this trace variant
		Set<Transition> alreadyActivatedInTrace = new HashSet<>();
		BitSet evClassActivatedInTrace = new BitSet();

		//////////////////////////////
		// Sync Moves
		//////////////////////////////
		Collection<EventIndexSyncDiagnostic> snycDiag = tlVariantDiag.indexDiagnostics().snyc();

		for (EventIndexSyncDiagnostic indDiag : snycDiag) {
			if (indDiag.transition().isInvisible()) {
				continue;
			}
			// Get corresponding element diagnostics counter
			TLCElementDiagnostic tlceDiag = mapT2DiagRecorder.get(indDiag.transition());
			XEventClass evSync = transEvMapping.get(indDiag.transition());
			
			// Count snyc activation for transition
			tlceDiag.incSync(tlVariantDiag.nbrTraces());

			updateGeneralCaseActivationsStatistics(mapEC2DiagRecorder, mapT2DiagRecorder, 
					alreadyActivatedInTrace, evClassActivatedInTrace, 
					evSync, Optional.of(indDiag.transition()), tlVariantDiag.nbrTraces());
		}

		//////////////////////////////
		// Order Moves
		//////////////////////////////
		Collection<EventIndexOrderDiagnostics> orderDiag = tlVariantDiag.indexDiagnostics().order();
		
		for (EventIndexOrderDiagnostics indDiag : orderDiag) {
			if (indDiag.transition().isInvisible()) {
				continue;
			}
			// Get corresponding element diagnostics counter
			TLCElementDiagnostic tlceDiag = mapT2DiagRecorder.get(indDiag.transition());
			XEventClass evSync = transEvMapping.get(indDiag.transition());
			
			// Count mis-ordered activation for transition/event class
			tlceDiag.incOrder(indDiag.type(), tlVariantDiag.nbrTraces());
			
			////////////////////
			// Compute Timedeltas
			////////////////////
			if ((indDiag.type() == OrderDiagnosticType.EARLY) || (indDiag.type() == OrderDiagnosticType.LATE)) {
				final int indexSync = indDiag.eventSync();
				final int indexOrder = indDiag.eventOrder();
				
				List<Long> timeDeltas = tlVariantDiag.traceIndices().parallelStream()
					.map(xlog::get)
					.map(t -> Pair.of(XTimeExtension.instance().extractTimestamp(t.get(indexSync)), 
							XTimeExtension.instance().extractTimestamp(t.get(indexOrder))))
					.map(p -> (p.getRight().getTime() - p.getLeft().getTime()))
					.collect(Collectors.toList());

				tlceDiag.addTimedeltas(indDiag.type(), timeDeltas);
			}
						

			updateGeneralCaseActivationsStatistics(mapEC2DiagRecorder, mapT2DiagRecorder, 
					alreadyActivatedInTrace, evClassActivatedInTrace, 
					evSync, Optional.of(indDiag.transition()), tlVariantDiag.nbrTraces());
		}

		//////////////////////////////
		// Missing (Model Move)
		//////////////////////////////
		Collection<Transition> missingDiag = tlVariantDiag.indexDiagnostics().missing();
		
		for (Transition t : missingDiag) {
			if (t.isInvisible()) {
				continue;
			}
			// Get corresponding element diagnostics counter
			TLCElementDiagnostic tlceDiag = mapT2DiagRecorder.get(t);
			XEventClass evSync = transEvMapping.get(t);
			
			// Count missing activation for transition
			tlceDiag.incMissing(tlVariantDiag.nbrTraces());

			updateGeneralCaseActivationsStatistics(mapEC2DiagRecorder, mapT2DiagRecorder, 
					alreadyActivatedInTrace, evClassActivatedInTrace, 
					evSync, Optional.of(t), tlVariantDiag.nbrTraces());
		}

		//////////////////////////////
		// Additional (Log Move)
		//////////////////////////////
		Collection<Integer> addDiag = tlVariantDiag.indexDiagnostics().additional();
		
		XTrace trace = xlog.get(tlVariantDiag.traceIndices().first());
		for (Integer evIndex : addDiag) {
			// Get corresponding element diagnostics counter
			XEventClass evSync = eventClasses.getClassOf(trace.get(evIndex));
			TLCElementDiagnostic tlceDiag = mapEC2DiagRecorder.get(evSync);
			
			// Count additional activations for event class
			tlceDiag.incAdditional(tlVariantDiag.nbrTraces());

			updateGeneralCaseActivationsStatistics(mapEC2DiagRecorder, mapT2DiagRecorder, 
					alreadyActivatedInTrace, evClassActivatedInTrace, 
					evSync, Optional.empty(), tlVariantDiag.nbrTraces());
		}
		
		
	}
	
	private static void updateGeneralCaseActivationsStatistics(
			Map<XEventClass, TLCElementDiagnostic> mapEC2DiagRecorder, Map<Transition, TLCElementDiagnostic> mapT2DiagRecorder, 
			Set<Transition> alreadyActivatedInTrace, BitSet evClassActivatedInTrace,
			XEventClass evClass, Optional<Transition> transition, int nbrTraces) {

		TLCElementDiagnostic tlcElDiag = null;
		if (transition.isPresent()) {
			Transition t = transition.get();
			tlcElDiag = mapT2DiagRecorder.get(t);

			// Always count general activations
			tlcElDiag.incActivations(nbrTraces);
			// If first occurrence of this transition, count it as activated in the trace
			if (!alreadyActivatedInTrace.contains(t)) {
				tlcElDiag.incNbrCases(nbrTraces);
				alreadyActivatedInTrace.add(t);
			}
		}

		// Done: sync/model move that can be uniquely attributed to a transition 
		if (tlcElDiag != null && (tlcElDiag.getDiagnosticType() == TLCElementType.ATTRIBUTABLE_TRANSITION)) {
			return;
		}
		else {
			// Log move or non-attributable transition/sync move
			// Get Associated event class
			tlcElDiag = mapEC2DiagRecorder.get(evClass);
		}

		// Trace variant not yet counted as activating variant
		if (!evClassActivatedInTrace.get(evClass.getIndex())) {
			tlcElDiag.incNbrCases(nbrTraces);
		}
		// Activation counting
		tlcElDiag.incActivations(nbrTraces);
		evClassActivatedInTrace.set(evClass.getIndex());
	}

}
