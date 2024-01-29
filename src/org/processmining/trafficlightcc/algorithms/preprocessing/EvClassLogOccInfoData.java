package org.processmining.trafficlightcc.algorithms.preprocessing;

public class EvClassLogOccInfoData {
  private final int occurrences;
  private final int aggPosition;

  public EvClassLogOccInfoData(int occurrences, int aggPosition) {
    this.occurrences = occurrences;
    this.aggPosition = aggPosition;
  }

  public int occurrences() {
    return occurrences;
  }

  public int aggPosition() {
    return aggPosition;
  }

}
