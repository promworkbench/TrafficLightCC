package org.processmining.trafficlightcc.algorithms.poaanalysis.base;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class EventIndexSyncDiagnostic {
  private final int index;

  private final Transition transition;

  public EventIndexSyncDiagnostic(int index, Transition transition) {
    this.index = index;
    this.transition = transition;
  }

  public int index() {
    return index;
  }

  public Transition transition() {
    return transition;
  }
}
