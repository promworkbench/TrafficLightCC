package org.processmining.trafficlightcc.algorithms.poaanalysis.base;

import java.util.Collection;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class EventIndexBasedTLDiagnostics {

  private final Collection<EventIndexSyncDiagnostic> snyc;

  private final Collection<Integer> additional;

  private final Collection<Transition> missing;

  private final Collection<EventIndexOrderDiagnostics> order;

  public EventIndexBasedTLDiagnostics(Collection<EventIndexSyncDiagnostic> snyc, 
      Collection<Integer> additional, Collection<Transition> missing, 
      Collection<EventIndexOrderDiagnostics> order) {

		this.snyc = snyc;
		this.additional = additional;
		this.missing = missing;
		this.order = order;
	}

  public Collection<EventIndexSyncDiagnostic> snyc() {
    return snyc;
  }

  public Collection<Integer> additional() {
    return additional;
  }

  public Collection<Transition> missing() {
    return missing;
  }

  public Collection<EventIndexOrderDiagnostics> order() {
    return order;
  }
}
