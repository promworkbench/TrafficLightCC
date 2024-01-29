package org.processmining.trafficlightcc.algorithms.preprocessing;

import org.deckfour.xes.classification.XEventClass;

public class EvClassOccPosTieBreakInfo {

  private final XEventClass eventClass;
  private final int nbrOcc;
  private final float avgFirstOccInTrace;

  public EvClassOccPosTieBreakInfo(XEventClass eventClass, int nbrOcc, float avgFirstOccInTrace) {
    this.eventClass = eventClass;
    this.nbrOcc = nbrOcc;
    this.avgFirstOccInTrace = avgFirstOccInTrace;
  }

  public XEventClass eventClass() {
    return eventClass;
  }

  public int nbrOcc() {
    return nbrOcc;
  }

  public float avgFirstOccInTrace() {
    return avgFirstOccInTrace;
  }

}
