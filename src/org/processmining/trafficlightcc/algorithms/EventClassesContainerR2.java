package org.processmining.trafficlightcc.algorithms;

import java.util.Optional;

import org.deckfour.xes.classification.XEventClass;

public class EventClassesContainerR2 {
  private final Optional<XEventClass> evClassSync;
  private final Optional<XEventClass> evClassNonSync;

  public EventClassesContainerR2(Optional<XEventClass> evClassSync, Optional<XEventClass> evClassNonSync) {
    this.evClassSync = evClassSync;
    this.evClassNonSync = evClassNonSync;
  }

  public Optional<XEventClass> evClassSync() {
    return evClassSync;
  }

  public Optional<XEventClass> evClassNonSync() {
    return evClassNonSync;
  }
}
