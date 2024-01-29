package org.processmining.trafficlightcc.models;

import org.deckfour.xes.model.XLog;
import org.processmining.partialorder.ptrace.model.PLog;
import org.processmining.plugins.astar.petrinet.PartialOrderBuilder;

public class POReplayDataWrapper {
  // mockLog can contain anything as long as it has the same number as pLog,
  // It would be good if the indices in mockLog can be related to indices in pLog
  // (that's the connection that is used internally)
  private final PartialOrderBuilder poBuilder;
  private final PLog pLog;
  private final XLog xlog;

  public POReplayDataWrapper(PartialOrderBuilder poBuilder, PLog pLog, XLog xlog) {
    this.poBuilder = poBuilder;
    this.pLog = pLog;
    this.xlog = xlog;
  }

  public PartialOrderBuilder poBuilder() {
    return poBuilder;
  }

  public PLog pLog() {
    return pLog;
  }

  public XLog xlog() {
    return xlog;
  }

}
