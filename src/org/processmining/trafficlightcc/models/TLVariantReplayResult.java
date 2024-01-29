package org.processmining.trafficlightcc.models;

import java.util.Optional;

import org.processmining.partialorder.models.replay.POSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class TLVariantReplayResult {

  private final int id;
  private final int nbrTraces;
  private final SyncReplayResult res1;
  private final Optional<POSyncReplayResult> res2;

  public TLVariantReplayResult(int id, int nbrTraces, SyncReplayResult res1, Optional<POSyncReplayResult> res2) {
    this.id = id;
    this.nbrTraces = nbrTraces;
    this.res1 = res1;
    this.res2 = res2;
  }

  public int id() {
    return id;
  }

  public int nbrTraces() {
    return nbrTraces;
  }

  public SyncReplayResult res1() {
    return res1;
  }

  public Optional<POSyncReplayResult> res2() {
    return res2;
  }
}
