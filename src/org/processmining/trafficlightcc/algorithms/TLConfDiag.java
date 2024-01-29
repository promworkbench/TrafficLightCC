package org.processmining.trafficlightcc.algorithms;

public class TLConfDiag {

  private final String activity;
  private final TLConformanceType type;

  public TLConfDiag(String activity, TLConformanceType type) {
    this.activity = activity;
    this.type = type;
  }

  public String activity() {
    return activity;
  }

  public TLConformanceType type() {
    return type;
  }
}
