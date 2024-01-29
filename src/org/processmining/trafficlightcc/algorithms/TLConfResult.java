package org.processmining.trafficlightcc.algorithms;

public class TLConfResult {

  private final MoveConformanceInfo moveInfo;
  private final int count;

  public TLConfResult(MoveConformanceInfo moveInfo, int count) {
    this.moveInfo = moveInfo;
    this.count = count;
  }

  public MoveConformanceInfo moveInfo() {
    return moveInfo;
  }

  public int count() {
    return count;
  }

}
