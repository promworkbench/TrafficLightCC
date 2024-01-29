package org.processmining.trafficlightcc.models;

import java.util.Collection;

import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.trafficlightcc.algorithms.poaanalysis.base.TLVariantIndexDiagnostics;

public class TLConfIndexDiagnosticsBundle {

  private final PNRepResult resRound1;
  private final PNRepResult resRound2;
  private final Collection<TLVariantIndexDiagnostics> tlcDiagnostics;

  public TLConfIndexDiagnosticsBundle(PNRepResult resRound1, PNRepResult resRound2, 
      Collection<TLVariantIndexDiagnostics> tlcDiagnostics) {
    this.resRound1 = resRound1;
    this.resRound2 = resRound2;
    this.tlcDiagnostics = tlcDiagnostics;
  }

  public PNRepResult resRound1() {
    return resRound1;
  }

  public PNRepResult resRound2() {
    return resRound2;
  }

  public Collection<TLVariantIndexDiagnostics> tlcDiagnostics() {
    return tlcDiagnostics;
  }

}
