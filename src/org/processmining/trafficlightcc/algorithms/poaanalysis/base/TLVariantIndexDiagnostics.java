package org.processmining.trafficlightcc.algorithms.poaanalysis.base;

import java.util.SortedSet;

public class TLVariantIndexDiagnostics {
  private final int id;
  private final int nbrTraces;
  private final boolean alignR1Reliable;
  private final boolean alignR2Reliable;
  private final SortedSet<Integer> traceIndices;
  private final EventIndexBasedTLDiagnostics indexDiagnostics;
  
  public TLVariantIndexDiagnostics(int id, int nbrTraces, boolean alignR1Reliable, boolean alignR2Reliable,
      SortedSet<Integer> traceIndices, 
      EventIndexBasedTLDiagnostics indexDiagnostics) {
    this.id = id;
    this.nbrTraces = nbrTraces;
    this.alignR1Reliable = alignR1Reliable;
    this.alignR2Reliable = alignR2Reliable;
    this.traceIndices = traceIndices;
    this.indexDiagnostics = indexDiagnostics;
  }

  public int id() {
    return id;
  }

  public int nbrTraces() {
    return nbrTraces;
  }

  public boolean alignR1Reliable() {
    return alignR1Reliable;
  }

  public boolean alignR2Reliable() {
    return alignR2Reliable;
  }

  public SortedSet<Integer> traceIndices() {
    return traceIndices;
  }

  public EventIndexBasedTLDiagnostics indexDiagnostics() {
    return indexDiagnostics;
  }
}
