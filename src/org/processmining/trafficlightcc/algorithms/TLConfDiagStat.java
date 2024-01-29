package org.processmining.trafficlightcc.algorithms;

public class TLConfDiagStat {

  private final TLConfDiag tlConfDiag;
  private final int count;

  public TLConfDiagStat(TLConfDiag tlConfDiag, int count) {
    this.tlConfDiag = tlConfDiag;
    this.count = count;
  }

  public TLConfDiag tlConfDiag() {
    return tlConfDiag;
  }

  public int count() {
    return count;
  }
}
