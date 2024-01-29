package org.processmining.trafficlightcc.algorithms.replay;

import org.processmining.partialorder.models.replay.POSyncReplayResult;

public class IndexedPOAResult {

  private final int id;
  private final POSyncReplayResult  replayResult;

  public IndexedPOAResult(int id, POSyncReplayResult  replayResult) {
    this.id = id;
    this.replayResult = replayResult;
  }

  public int id() {
    return id;
  }

  public POSyncReplayResult replayResult() {
    return replayResult;
  }
}
