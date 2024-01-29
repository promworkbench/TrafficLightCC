package org.processmining.trafficlightcc.algorithms.poaanalysis.base;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

/**
 * 
 * @author brockhoff
 *
 *	For early events, eventSync refers to the next sync event
 *	For late events, eventSync refers to the last sync event
 *	For chaotic events, eventSync refers to the last sync event
 */
public class EventIndexOrderDiagnostics {

  private final OrderDiagnosticType type;

  private final int eventOrder;

  private final int eventSync;

  private final Transition transition;

  public EventIndexOrderDiagnostics(OrderDiagnosticType type, int eventOrder, int eventSync, Transition transition) {
    this.type = type;
    this.eventOrder = eventOrder;
    this.eventSync = eventSync;
    this.transition = transition;
  }

  public OrderDiagnosticType type() {
    return type;
  }

  public int eventOrder() {
    return eventOrder;
  }

  public int eventSync() {
    return eventSync;
  }

  public Transition transition() {
    return transition;
  }
}
