package org.processmining.trafficlightcc.algorithms.replay;

import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class IndexedAResult {

  private final int id;
  private final SyncReplayResult  replayResult;

  public IndexedAResult(int id, SyncReplayResult  replayResult) {
    this.id = id;
    this.replayResult = replayResult;
  }

  public int id() {
    return id;
  }

  public SyncReplayResult replayResult() {
    return replayResult;
  }
}
