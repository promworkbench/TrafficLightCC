package org.processmining.trafficlightcc.algorithms.replay;

import java.util.ArrayList;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class POReplayResultContainerR1 {

  private final XLog log;
  private final Petrinet pn;
  private final PNRepResult pnRepRes;
  private final ArrayList<IndexedAResult> replays;

  public POReplayResultContainerR1(XLog log, Petrinet pn, PNRepResult pnRepRes, ArrayList<IndexedAResult> replays) {
    this.log = log;
    this.pn = pn;
    this.pnRepRes = pnRepRes;
    this.replays = replays;
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

  public ArrayList<IndexedAResult> replays() {
    return replays;
  }
}
