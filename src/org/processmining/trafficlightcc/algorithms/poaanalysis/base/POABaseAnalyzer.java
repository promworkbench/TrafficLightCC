package org.processmining.trafficlightcc.algorithms.poaanalysis.base;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.partialorder.models.replay.POSyncReplayResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.trafficlightcc.algorithms.poaanalysis.pnrun.PNRun;
import org.processmining.trafficlightcc.algorithms.poaanalysis.pnrun.PNRunExtractor;
import org.processmining.trafficlightcc.models.TLVariantReplayResult;

import gnu.trove.list.TIntList;

public class POABaseAnalyzer {
	
	/**
	 * Compute per trace variant transition and event index-based diagnostics for the replay results
	 * obtained for an entire log. 
	 * @param pn Petri net on which alignments for computed (or the one for the image of the mapping)
	 * @param initialMarking Initial Marking
	 * @param replayBundles Bundle containing the replay results for the entire log.
	 * @param transTranslatorR2 Mapping between alignment transitions and provided Petri net
	 * @return Per variant transition and event index-based TL conformance information.
	 */
	public static Collection<TLVariantIndexDiagnostics> analyzeTLPOAlignments(
			Petrinet pn, Marking initialMarking,
			Collection<TLVariantReplayResult> replayBundles, Function<Transition, Transition> transTranslatorR2) {
		return replayBundles.stream()
			.map(b -> {
				boolean res2Reliable = b.res2().isPresent() ? b.res2().get().isReliable() : b.res1().isReliable();
				EventIndexBasedTLDiagnostics diag = initEventIndexBaseDiagnostics(
						pn, initialMarking, b.res1(), b.res2(), transTranslatorR2);
				return new TLVariantIndexDiagnostics(b.id(), b.nbrTraces(), 
						b.res1().isReliable(), res2Reliable, 
						b.res1().getTraceIndex(), diag);
			})
			.collect(Collectors.toSet());
	}
	
	/**
	 * Given the replay results from the first and second TL alignment rounds, compute the transitions and event indices
	 * for sync, missing, additional, and wrong order. 
	 * @param a1 First TL round alignment
	 * @param a2 Second TL round alignment
	 * @return Transitions and event categorized in TL conformance categories. 
	 */
	private static EventIndexBasedTLDiagnostics initEventIndexBaseDiagnostics(
			Petrinet pn, Marking initialMarking, SyncReplayResult a1, 
			Optional<POSyncReplayResult> a2, Function<Transition, Transition> transTranslator) {
		
		////////////////////
		// Replay diagnostics collections
		////////////////////
		// Sync Diagnostics
		List<EventIndexSyncDiagnostic> syncDiag = new LinkedList<>();
		// Order diagnostics
		List<EventIndexOrderDiagnostics> orderDiag = new LinkedList<>();
		// Additional behavior (log move events)
		List<Integer> addDiag = new LinkedList<>();
		// Missing behavior (model moves)
		List<Transition> missDiag = new LinkedList<>();

		////////////////////////////////////////
		// Process First Round
		////////////////////////////////////////
		// Sync Move Indices
		BitSet firstRoundSyncInd = new BitSet();
		List<Object> nodes = a1.getNodeInstance();
		Iterator<Object> it = nodes.iterator();

		int curLogInd = 0;
		for (StepTypes step : a1.getStepTypes()) {
			Object obj = it.next();
			switch (step) {
				case L :
					curLogInd++;
					break;
				case LMGOOD :
					Transition t = (Transition) obj;
					syncDiag.add(new EventIndexSyncDiagnostic(curLogInd, t));
					firstRoundSyncInd.set(curLogInd);
					curLogInd++;
					break;
				case MREAL :
					// If there is not second, partially ordered alignment, 
					// first round model moves become missing moves
					if (!a2.isPresent()) {
						Transition tModel = (Transition) obj;
						missDiag.add(tModel);
					}
					break;
				case MINVI :
					break;
				case LMNOGOOD :
					break;
			default:
				break;
			}	
		}
		
		if (a2.isPresent()) {

			////////////////////////////////////////
			// Process Second Round
			////////////////////////////////////////
			TIntList move2LogIndices = a2.get().getIndeces();
			
			final int traceLen = curLogInd;
			
			// Alignment Iteration
			nodes = a2.get().getNodeInstance();
			it = nodes.iterator();
			int stepIndex = 0;

			// Run corresponding to the second round alignment
			PNRun pnRun = null;
			
			for (StepTypes step : a2.get().getStepTypes()) {
				Object obj = it.next();
				int logIndex = move2LogIndices.get(stepIndex);
				switch (step) {
					case L :
						assert logIndex >= 0;
						addDiag.add(logIndex);
						break;
					case LMGOOD :
						assert logIndex >= 0;
						Transition t = transTranslator.apply((Transition) obj);
						
						// Previously not SYNC (otherwise already recorded above)
						if (!firstRoundSyncInd.get(logIndex)) {
							
							// Initialize the run if necessary
							if (pnRun == null) {
								pnRun = PNRunExtractor.extractRun(pn, initialMarking, a2.get(), 
										"Run", transTranslator);
							}
							
							// Extract next sync transition index from run (if there is any)
							Optional<Integer> runPrevFirstRoundSync = getPrevSyncCondition(pnRun, firstRoundSyncInd, 
									move2LogIndices, stepIndex);
							Optional<Integer> runNextFirstRoundSync = getNextSyncCondition(pnRun, firstRoundSyncInd, 
									move2LogIndices, stepIndex);
							// Index of event in trace (not alignment)
							int prevSyncTraceIndex;
							int nextSyncTraceIndex;
							
							//////////////////////////////
							// Handle Boundary Cases
							//////////////////////////////
							if (!runPrevFirstRoundSync.isPresent()) {
								// As if we had an artifical start symbol
								prevSyncTraceIndex = -1;
							}
							else {
								// 
								prevSyncTraceIndex = runPrevFirstRoundSync.get();
								prevSyncTraceIndex = move2LogIndices.get(prevSyncTraceIndex);
							}

							if (!runNextFirstRoundSync.isPresent()) {
								// Like as if we had an artificial end symbol
								nextSyncTraceIndex = traceLen + 1;
							}
							else {
								nextSyncTraceIndex = runNextFirstRoundSync.get();
								nextSyncTraceIndex = move2LogIndices.get(nextSyncTraceIndex);
							}

							//////////////////////////////
							// Check position of this index 
							// w.r.t. [last1SyncLogIndex, nextSyncLogIndex]
							//////////////////////////////
							EventIndexOrderDiagnostics diag;
							// Check Position
							if (logIndex < prevSyncTraceIndex) { 	// logCur, [last, shouldCur, next]
								diag = new EventIndexOrderDiagnostics(
										OrderDiagnosticType.EARLY, logIndex, prevSyncTraceIndex, t);
							}
							else if (logIndex < nextSyncTraceIndex) { // [last, shouldCur, isCur, next]
								// Happens for example, if, due to the chaotic behavior, a section
								// between last and next is skipped instead
								diag = new EventIndexOrderDiagnostics(
										OrderDiagnosticType.CHAOTICSECTION, logIndex, prevSyncTraceIndex, t);
							}
							else { 									// [last, shouldCur, next], logCur
								diag = new EventIndexOrderDiagnostics(
										OrderDiagnosticType.LATE, logIndex, nextSyncTraceIndex, t);
							}
							orderDiag.add(diag);
						}
						break;
					case MREAL :
						Transition tModel = transTranslator.apply((Transition) obj);
						missDiag.add(tModel);
						break;
					case MINVI :
						break;
					case LMNOGOOD :
						break;
				default:
					break;
				}	
				stepIndex++;
			}
			return new EventIndexBasedTLDiagnostics(syncDiag, addDiag, missDiag, orderDiag);
		}
		else {
			return new EventIndexBasedTLDiagnostics(syncDiag, addDiag, missDiag, orderDiag);
		}
		
	}

	private static Optional<Integer> getPrevSyncCondition(PNRun pnRun, BitSet firstRoundSyncInd, 
			TIntList move2LogIndices, int alignIndex) {
		Transition transCur = pnRun.alignIndexRunTransition().get(alignIndex);
		return getPrevSyncConditionRecursive(pnRun, firstRoundSyncInd, move2LogIndices, transCur);
	}
	
	private static Optional<Integer> getNextSyncCondition(PNRun pnRun, BitSet firstRoundSyncInd, 
			TIntList move2LogIndices, int alignIndex) {
		Transition transCur = pnRun.alignIndexRunTransition().get(alignIndex);
		return getNextSyncConditionRecursive(pnRun, firstRoundSyncInd, move2LogIndices, transCur);
	}
	
	private static Optional<Integer> getPrevSyncConditionRecursive(PNRun pnRun, BitSet firstRoundSyncInd, 
			TIntList move2LogIndices, Transition transCur) {
		// Alignment index that created the transition
		int transCurAlignIndex = pnRun.transitionAlignIndex().get(transCur);
		// Trace index corresponding to this transition (-1 if does not exist)
		int transCurTraceIndex = move2LogIndices.get(transCurAlignIndex);
		// Synchronous in first iteration?
		if (transCurTraceIndex != -1 && firstRoundSyncInd.get(transCurTraceIndex)) {
			return Optional.of(transCurAlignIndex);
		}
		
		// Otherwise find conditional predecessor transitions
		final AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> g = 
		    transCur.getGraph();
		Optional<Integer> max = g.getInEdges(transCur).stream()
				.map(PetrinetEdge::getSource)
				.flatMap(p -> g.getInEdges(p).stream())
				.map(PetrinetEdge::getSource)
				.map(t -> getPrevSyncConditionRecursive(pnRun, firstRoundSyncInd, move2LogIndices, (Transition) t))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.max(Integer::compare);
		return max;
	}

	private static Optional<Integer> getNextSyncConditionRecursive(PNRun pnRun, BitSet firstRoundSyncInd, 
			TIntList move2LogIndices, Transition transCur) {
		// Alignment index that created the transition
		int transCurAlignIndex = pnRun.transitionAlignIndex().get(transCur);
		// Trace index corresponding to this transition (-1 if does not exist)
		int transCurTraceIndex = move2LogIndices.get(transCurAlignIndex);
		// Synchronous in first iteration?
		if (transCurTraceIndex != -1 && firstRoundSyncInd.get(transCurTraceIndex)) {
			return Optional.of(transCurAlignIndex);
		}

		// Otherwise find conditional successor transitions
		final AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> g = 
		    transCur.getGraph();
		Optional<Integer> max = g.getOutEdges(transCur).stream()
				.map(PetrinetEdge::getTarget)
				.flatMap(p -> g.getOutEdges(p).stream())
				.map(PetrinetEdge::getTarget)
				.map(t -> getNextSyncConditionRecursive(pnRun, firstRoundSyncInd, move2LogIndices, (Transition) t))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.min(Integer::compare);
		return max;
	}
	
}
