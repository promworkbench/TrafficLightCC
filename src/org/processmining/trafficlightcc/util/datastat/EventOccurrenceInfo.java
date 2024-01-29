package org.processmining.trafficlightcc.util.datastat;

import org.deckfour.xes.classification.XEventClass;

public class EventOccurrenceInfo {

  private final XEventClass eventClass;
  private final int occPosition;
  private final float occPositionRel;

  public EventOccurrenceInfo(XEventClass eventClass, int occPosition, float occPositionRel) {
    this.eventClass = eventClass;
    this.occPosition = occPosition;
    this.occPositionRel = occPositionRel;
  }

  public XEventClass eventClass() {
    return eventClass;
  }

  public int occPosition() {
    return occPosition;
  }

  public float occPositionRel() {
    return occPositionRel;
  }
}
