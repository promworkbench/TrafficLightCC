package org.processmining.trafficlightcc.algorithms.replay;

import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

/**
 * Class that contains the replay results of the second traffic light conformance round
 * @author brockhoff
 *
 */
public class POReplayResultContainerR2 {
	// The replay algorithm by Xixi returns PNRepResult which extends SyncReplayResult; however,
	// it actually is (in terms of inheritance) a POSyncReplayResult. I still added the PNRepResult because with
	// the appropriate connection, ProM can visualize it (it is a relatively generic object in ProM).

  private final XLog log;
  private final Petrinet pn;
  private final PNRepResult pnRepRes;
  private final Map<Integer, IndexedPOAResult> indexedReplays;
  private final PNManifestFlattener manifestFlattener;

  public POReplayResultContainerR2(XLog log, Petrinet pn, PNRepResult pnRepRes, 
      Map<Integer, IndexedPOAResult> indexedReplays, PNManifestFlattener manifestFlattener) {
    this.log = log;
    this.pn = pn;
    this.pnRepRes = pnRepRes;
    this.indexedReplays = indexedReplays;
    this.manifestFlattener = manifestFlattener;
  }

  public XLog log() {
    return log;
  }

  public Petrinet pn() {
    return pn;
  }

  public PNRepResult pnRepRes() {
    return pnRepRes;
  }

  public Map<Integer, IndexedPOAResult> indexedReplays() {
    return indexedReplays;
  }

  public PNManifestFlattener manifestFlattener() {
    return manifestFlattener;
  }
}
