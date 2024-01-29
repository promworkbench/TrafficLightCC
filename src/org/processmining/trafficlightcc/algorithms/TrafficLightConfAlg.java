package org.processmining.trafficlightcc.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.connection.PAlignmentResultConnection;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.partialorder.models.replay.POSyncReplayResult;
import org.processmining.partialorder.plugins.AdvancedPartialNetReplayer;
import org.processmining.partialorder.plugins.PartialNetReplayer;
import org.processmining.partialorder.plugins.replay.PartialOrderILPLinearAlg;
import org.processmining.partialorder.plugins.replay.StandardPTraceConverter;
import org.processmining.partialorder.ptrace.model.PLog;
import org.processmining.partialorder.ptrace.param.PTraceParameter;
import org.processmining.partialorder.ptrace.plugins.builder.PLogPlugin;
import org.processmining.plugins.astar.petrinet.PartialOrderBuilder;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.manifestreplay.CostBasedCompleteManifestParam;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCDiagnosticsBaseAggregator;
import org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase.TLCElementDiagnostic;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.POABaseAnalyzer;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.TLVariantIndexDiagnostics;
import org.processmining.trafficlightcc.algorithms.preprocessing.FrequencyBasedCostManipulator;
import org.processmining.trafficlightcc.algorithms.replay.IndexedAResult;
import org.processmining.trafficlightcc.algorithms.replay.IndexedPOAResult;
import org.processmining.trafficlightcc.algorithms.replay.POReplayResultContainerR1;
import org.processmining.trafficlightcc.algorithms.replay.POReplayResultContainerR2;
import org.processmining.trafficlightcc.algorithms.replay.partialorder.ParitalRunInstanceSecondRun;
import org.processmining.trafficlightcc.algorithms.replay.partialorder.PartialRunInstanceFirstRun;
import org.processmining.trafficlightcc.dialogs.CostAdaptation;
import org.processmining.trafficlightcc.models.POReplayDataWrapper;
import org.processmining.trafficlightcc.models.TLConfDiagnosticsBundle;
import org.processmining.trafficlightcc.models.TLConfIndexDiagnosticsBundle;
import org.processmining.trafficlightcc.models.TLVariantReplayResult;
import org.processmining.trafficlightcc.parameters.TLConfParameters;
import org.processmining.trafficlightcc.util.PrintUtil;
import org.processmining.trafficlightcc.util.stopwatch.PerformanceLogger;
import org.processmining.trafficlightcc.util.stopwatch.StopwatchMilestone;

import nl.tue.astar.AStarException;

public class TrafficLightConfAlg {
	
	
	/**
	 * Performance logger instance.
	 */
	private final PerformanceLogger perfLogger;
	
	private boolean useNormalAlignments4R1 = true;
	
	public TrafficLightConfAlg() {
		this.perfLogger = new PerformanceLogger(); 
	}

	/**
	 * Run the 2-phase traffic light conformance score computation.
	 * 
	 * @param context PluginContext used by the replayer to create connection and display progress. 
	 * Can be empty if {@link IPNReplayParameter#setCreateConn(boolean)} and {@link IPNReplayParameter#setGUIMode(boolean)}
	 * were set to false.
	 * @param pn Petri net
	 * @param log Event log
	 * @param param Replay parameter (including move costs)
	 * @param transEvMapping Transition to event class mapping
	 * @param createRoundConnections Create connectionss for the Petri net replay results in each round (requires a Plugin context)
	 */
	public Optional<TLConfDiagnosticsBundle> calculateTrafficLightConformance(Optional<PluginContext> context, 
	    Petrinet pn, XLog log, TLConfParameters tlConfParam, boolean createRoundConnections) {

    ////////////////////////////////////////
    // Cost Nudging to Reduce Non-determinism
    ////////////////////////////////////////
    CostBasedCompleteParam param = tlConfParam.getConfAlgParameters();
	  if (tlConfParam.getCostAdaptation() == CostAdaptation.YES) {
      context.ifPresent(c -> c.log("Adapting Costs...", MessageLevel.NORMAL));

      XEventClasses evClasses = XEventClasses.deriveEventClasses(
          tlConfParam.getMapping().getEventClassifier(), log);
	    // Copy parameters
      FrequencyBasedCostManipulator freqManip = new FrequencyBasedCostManipulator(log, evClasses);
      freqManip.initCostInfo();
      param = freqManip.adaptCostFunctions(tlConfParam.getMapping(), param);
	  }
	  else {
        context.ifPresent(c -> c.log("No cost adaption desired.", MessageLevel.NORMAL));
	  }
		
		// Start stopwatch
		perfLogger.startMeasurement(StopwatchMilestone.ALGORITHM);
		Optional<TLConfIndexDiagnosticsBundle> tlIndexDiagnostics = 
				calculateTrafficLightConformanceIndex(context, pn, log, param, 
						tlConfParam.getMapping(), createRoundConnections);
		
		if (!tlIndexDiagnostics.isPresent()) {
			this.perfLogger.stopMeasurement(StopwatchMilestone.ALGORITHM);
			return Optional.empty();
		}

		this.perfLogger.resumeMeasurement(StopwatchMilestone.ANALYSISROUNDTWO);
		Collection<TLCElementDiagnostic> tlcDiagnostics = 
				TLCDiagnosticsBaseAggregator.aggregateIndexBasedDiagnostics(
					pn, log, tlConfParam.getMapping(), tlIndexDiagnostics.get().tlcDiagnostics());
		this.perfLogger.stopMeasurement(StopwatchMilestone.ANALYSISROUNDTWO);
		
		int nbrUnreliableAlignmentsR1 = tlIndexDiagnostics.get().tlcDiagnostics().stream()
				.mapToInt(v -> (v.alignR1Reliable() ? 0 : v.nbrTraces()))
				.sum();

		int nbrUnreliableAlignmentsR2 = tlIndexDiagnostics.get().tlcDiagnostics().stream()
				.mapToInt(v -> (v.alignR2Reliable() ? 0 : v.nbrTraces()))
				.sum();

		// Stop stopwatch
		this.perfLogger.stopMeasurement(StopwatchMilestone.ALGORITHM);

    context.ifPresent(c -> c.log("Performance Stats: " + this.perfLogger.toString(), MessageLevel.NORMAL));

		return Optional.of(new TLConfDiagnosticsBundle(pn, tlIndexDiagnostics.get().resRound1(), 
				tlIndexDiagnostics.get().resRound2(), tlcDiagnostics, 
				nbrUnreliableAlignmentsR1, nbrUnreliableAlignmentsR2));
				
	}
	
	/**
	 * Run the 2-phase traffic light conformance score computation and 
	 * return the index based classification per trace variant.
	 * 
	 * @param context PluginContext used by the replayer to create connection and display progress. 
	 * Can be empty if {@link IPNReplayParameter#setCreateConn(boolean)} and {@link IPNReplayParameter#setGUIMode(boolean)}
	 * were set to false.
	 * @param pn Petri net
	 * @param log Event log
	 * @param param Replay parameter (including move costs)
	 * @param transEvMapping Transition to event class mapping
	 * @param createRoundConnections Create connectionss for the Petri net replay results in each round (requires a Plugin context)
	 */
	public Optional<TLConfIndexDiagnosticsBundle> calculateTrafficLightConformanceIndex(Optional<PluginContext> context, 
			Petrinet pn, XLog log, CostBasedCompleteParam param,
			TransEvClassMapping transEvMapping, boolean createRoundConnections) {
		if (!context.isPresent() && createRoundConnections) {
		  String errorMessage = "Cannot create connection without begin provided a PluginContext. "
					+ "No connections will be created!";
      context.ifPresent(c -> c.log(errorMessage, MessageLevel.NORMAL));
			createRoundConnections = false;
		}

		// Event Classes
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(transEvMapping.getEventClassifier(), log);

        
		////////////////////////////////////////
        // PO Data
		////////////////////////////////////////
		POReplayDataWrapper poDataFirstRound = null;
		if (useNormalAlignments4R1) {
			// Partial order log not used later
			// TODO not clean
			poDataFirstRound = new POReplayDataWrapper(null, null, log);
		}
		else {
			// Partially ordered log (from log) -> sequence by default
			PLog pLog = PLogPlugin.computeSeqPTraces(log);
			// Parameter unused anyway
			PartialOrderBuilder poBuilder = new StandardPTraceConverter(pLog, null);
			poDataFirstRound = new POReplayDataWrapper(poBuilder, pLog, log);
		}
		
		// Run Instance first iteration
		PartialRunInstanceFirstRun priFirst = new PartialRunInstanceFirstRun(pn, 
				poDataFirstRound, transEvMapping, param);

		////////////////////////////////////////////////////////////
		// First Round
		////////////////////////////////////////////////////////////
    context.ifPresent(c -> c.log("Starting first alignment round", MessageLevel.NORMAL));
		Optional<POReplayResultContainerR1> resFirstRound;
		try {
			resFirstRound = runFirstRound(context, priFirst, createRoundConnections);
		} catch (AStarException e) {
			e.printStackTrace();
			return null;
		}
    context.ifPresent(c -> c.log("Done first alignment round", MessageLevel.NORMAL));
		
		if (!resFirstRound.isPresent()) {
      context.ifPresent(c -> c.log("Could not finish phase 1. Terminating...", MessageLevel.NORMAL));
			return Optional.empty();
		}
		
		////////////////////////////////////////////////////////////
		// Second Round
		////////////////////////////////////////////////////////////

		this.perfLogger.resumeMeasurement(StopwatchMilestone.ANALYSISROUNDONE);
		PartialRunInstanceCreator poRun2Creator = new PartialRunInstanceCreator();
		ParitalRunInstanceSecondRun priSecond = 
				poRun2Creator.buildSecondRoundRunInstance(eventClasses, priFirst, resFirstRound.get());
		this.perfLogger.stopMeasurement(StopwatchMilestone.ANALYSISROUNDONE);
		Optional<POReplayResultContainerR2> resSecRound = null;
		
    context.ifPresent(c -> c.log("Starting second alignment round", MessageLevel.NORMAL));
		try {
			resSecRound = runSecondRound(context, priSecond, createRoundConnections);
		} catch (AStarException e) {
			e.printStackTrace();
			return null;
		}
    context.ifPresent(c -> c.log("Done second alignment round", MessageLevel.NORMAL));
		if (!resSecRound.isPresent()) {
      context.ifPresent(c -> c.log("Could not finish phase 2. Terminating...", MessageLevel.NORMAL));
			return Optional.empty();
		}
		
		////////////////////////////////////////////////////////////
		// Consolidate Results
		////////////////////////////////////////////////////////////
		// Resume Analysis Stopwatch
		this.perfLogger.resumeMeasurement(StopwatchMilestone.ANALYSISROUNDTWO);
		ArrayList<TLVariantReplayResult> resBundle = bundleVariantReplayResults(resFirstRound.get(), 
				resSecRound.get());

		////////////////////////////////////////////////////////////
		// Analyze Results
		////////////////////////////////////////////////////////////
		Collection<TLVariantIndexDiagnostics> tlIndexDiagnostics = POABaseAnalyzer.analyzeTLPOAlignments(
				priFirst.pn(), priFirst.parameters().getInitialMarking(),
				resBundle,
				resSecRound.get().manifestFlattener()::getOrigTransFor);
		
		// Stop Stopwatch
		this.perfLogger.pauseMeasurement(StopwatchMilestone.ANALYSISROUNDTWO);
		
		return Optional.of(new TLConfIndexDiagnosticsBundle(resFirstRound.get().pnRepRes(), 
				resSecRound.get().pnRepRes(), tlIndexDiagnostics));
	}

	
	private Optional<POReplayResultContainerR1> runFirstRound(Optional<PluginContext> context, 
			PartialRunInstanceFirstRun priFirst, boolean createRoundConnections) throws AStarException {
		
		////////////////////
		// Run Alignments
		////////////////////
		this.perfLogger.startMeasurement(StopwatchMilestone.ALIGNROUNDONE);
		PNRepResult resRep1 = null;
		IPNReplayAlgorithm alg = null;
		if (useNormalAlignments4R1) {
			PNLogReplayer replayer = new PNLogReplayer();
			alg = new PetrinetReplayerWithILP();

			resRep1 = replayer.replayLog(context.isPresent() ? context.get() : null, priFirst.pn(), 
					priFirst.poData().xlog(), priFirst.t2evMapping(),
					alg, priFirst.parameters());
		}
		else {
			PartialNetReplayer partialNetReplayer = new PartialNetReplayer();
			// The algorithm that is used internally
			PartialOrderILPLinearAlg algPO = new PartialOrderILPLinearAlg();

			resRep1 = partialNetReplayer.replayLog(context.isPresent() ? context.get() : null, 
					priFirst.pn(), priFirst.poData().xlog(), 
						priFirst.t2evMapping(), algPO, priFirst.poData().poBuilder(), priFirst.parameters());
			alg = algPO;
			
		}

		this.perfLogger.stopMeasurement(StopwatchMilestone.ALIGNROUNDONE);
		
		if (resRep1 == null) {
      context.ifPresent(c -> c.log("Failed to compute the t-alignment", MessageLevel.NORMAL));
			return Optional.empty();
		}
		
		// Create Connection
		if (createRoundConnections) {
			context.get().addConnection(new PNRepResultAllRequiredParamConnection("Connection Result 1st Round", 
					priFirst.pn(), priFirst.poData().xlog(), priFirst.t2evMapping(), 
					alg, priFirst.parameters(), resRep1));
		}

		////////////////////
		// Analyze Results
		////////////////////
		this.perfLogger.startMeasurement(StopwatchMilestone. ANALYSISROUNDONE);
		// Create index result
		Iterator<SyncReplayResult> syncRepIt = resRep1.iterator();
		ArrayList<IndexedAResult> indexedResults = new ArrayList<>(resRep1.size());
		int i = 0;
		while (syncRepIt.hasNext()) {
			SyncReplayResult res = syncRepIt.next();
			indexedResults.add(new IndexedAResult(i, res));
			i++;
		}
		POReplayResultContainerR1 resFirstRound = new POReplayResultContainerR1(priFirst.poData().xlog(), 
				priFirst.pn(), resRep1, indexedResults);
		
		this.perfLogger.pauseMeasurement(StopwatchMilestone. ANALYSISROUNDONE);
		return Optional.of(resFirstRound);
	}
	
	private Optional<POReplayResultContainerR2> runSecondRound(Optional<PluginContext> context, 
			ParitalRunInstanceSecondRun priSecond, boolean createRoundConnections) throws AStarException {

		// Start Stopwatch
		this.perfLogger.startMeasurement(StopwatchMilestone.ALIGNROUNDTWO);
		
		////////////////////
		// Config and parameters
		////////////////////
		PNManifestFlattener flattener = new PNManifestFlattener(priSecond.pn(), priSecond.parameters()); 

		PartialOrderILPLinearAlg selectedAlg = new PartialOrderILPLinearAlg();

		CostBasedCompleteManifestParam algParameters = new CostBasedCompleteManifestParam(
				flattener.getMapEvClass2Cost(), flattener.getMapTrans2Cost(), flattener.getMapSync2Cost(),
				flattener.getInitMarking(), flattener.getFinalMarkings(), priSecond.parameters().getMaxNumOfStates(),
				flattener.getFragmentTrans());
		

		if (createRoundConnections) {
			algParameters.setCreateConn(true);
		}
		else {
			algParameters.setCreateConn(false);
		}
		algParameters.setGUIMode(false);
		AdvancedPartialNetReplayer advancedPPNReplayers = new AdvancedPartialNetReplayer();

		////////////////////
		// Alignments
		////////////////////
    context.ifPresent(c -> c.log("R2: Start Alignments", MessageLevel.NORMAL));
		PNRepResult resRep2 = advancedPPNReplayers.replayLogPrivate(context.isPresent() ? context.get() : null, 
				flattener.getNet(), priSecond.poData().xlog(), flattener.getMap(), 
				selectedAlg, priSecond.poData().poBuilder(), algParameters);
    context.ifPresent(c -> c.log("R2: Done Alignments", MessageLevel.NORMAL));
		
		// Stop Stopwatch
		this.perfLogger.stopMeasurement(StopwatchMilestone.ALIGNROUNDTWO);
		
		if (resRep2 == null) {
      context.ifPresent(c -> c.log("Failed to compute the p-alignment", MessageLevel.NORMAL));
			return Optional.empty();
		}

		// Create Connection
		if (createRoundConnections) {
			context.get().addConnection(new PAlignmentResultConnection(priSecond.poData().xlog(), 
					priSecond.poData().pLog(), resRep2, flattener.getNet(), flattener
					.getMap(), algParameters, new PTraceParameter()));
		}
		
		//////////////////////////////
		// Create indexed results
		//////////////////////////////
		// Start Stopwatch
		this.perfLogger.startMeasurement(StopwatchMilestone.ANALYSISROUNDTWO);
		Iterator<SyncReplayResult> syncRepIt = resRep2.iterator();
		Map<Integer, IndexedPOAResult> indexedResults = new HashMap<>((int) 1.5 * resRep2.size());
		while (syncRepIt.hasNext()) {
			POSyncReplayResult res = (POSyncReplayResult) syncRepIt.next();
			Optional<Integer> traceIndex = res.getTraceIndex().stream().findFirst();
			
			// Must be present
			assert traceIndex.isPresent();
			
			XTrace t = priSecond.poData().xlog().get(traceIndex.get());
			if (!t.getAttributes().containsKey(PartialRunInstanceCreator.TRACE_R1_REF)) {
				throw new IllegalStateException("Variant trace for second round of traffic light conformance "
						+ "does not have a reference to a first round result!");
			}
			else {
				int ref = (int) ((XAttributeDiscrete) t.getAttributes().get(
						PartialRunInstanceCreator.TRACE_R1_REF)).getValue();
				indexedResults.put(ref, new IndexedPOAResult(ref, res));
			}
		}
		
		this.perfLogger.pauseMeasurement(StopwatchMilestone.ANALYSISROUNDTWO);

		POReplayResultContainerR2 resSecRound = new POReplayResultContainerR2(priSecond.poData().xlog(), 
				priSecond.pn(), resRep2, indexedResults, flattener);
		
		return Optional.of(resSecRound);

	}
	
	public ArrayList<TLVariantReplayResult> bundleVariantReplayResults(POReplayResultContainerR1 resContainer1, 
			POReplayResultContainerR2 resContainer2) {
		
		ArrayList<TLVariantReplayResult> res = new ArrayList<>(resContainer1.replays().size());

		for (int i = 0; i < resContainer1.replays().size(); i++) {
			IndexedAResult r1 = resContainer1.replays().get(i);
			IndexedPOAResult r2 = resContainer2.indexedReplays().get(i);
			
			res.add(new TLVariantReplayResult(i, r1.replayResult().getTraceIndex().size(), 
					r1.replayResult(), (r2 == null) ? Optional.empty() : Optional.of(r2.replayResult())));
			
		}
		
		return res;
	}
	
	public PerformanceLogger getPerformanceLogger() {
		return this.perfLogger;
	}

}
