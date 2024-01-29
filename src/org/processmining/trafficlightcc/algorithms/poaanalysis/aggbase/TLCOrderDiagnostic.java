package org.processmining.trafficlightcc.algorithms.poaanalysis.aggbase;

/**
 * 
 * Value saved for early occurrences
 * Value saved for late occurrences
 * Value saved for chaotic occurrences (neither late nor early; 
 * however, we might have skipped a segment due to misordering)
 * @author brockhoff
 *
 */
public class TLCOrderDiagnostic {
  
  private int orderEarly;
  
  private int orderChaos;
  
  private int orderLate;
  
  public TLCOrderDiagnostic (int orderEarly, int orderChaos, int orderLate) {
    this.orderEarly = orderEarly;
    this.orderChaos = orderChaos;
    this.orderLate = orderLate;
  }
  
  public int orderEarly() {
    return this.orderEarly;
  }

  public int orderChaos() {
    return orderChaos;
  }

  public int orderLate() {
    return orderLate;
  }

}
