package org.processmining.trafficlightcc.algorithms.preprocessing;

import org.deckfour.xes.classification.XEventClass;

public class EvClOccInfo {

  private final XEventClass eventClass;
  private final int pos;

  public EvClOccInfo(XEventClass eventClass, int pos) {
    this.eventClass = eventClass;
    this.pos = pos;
  }

  public XEventClass eventClass() {
    return eventClass;
  }

  public int pos() {
    return pos;
  }
}
