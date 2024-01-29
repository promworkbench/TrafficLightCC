package org.processmining.trafficlightcc.algorithms;

import org.processmining.plugins.petrinet.replayresult.StepTypes;

public class MoveConformanceInfo {

	private final String activity;
	private final StepTypes types;

  public MoveConformanceInfo(String activity, StepTypes types) {
    this.activity = activity;
    this.types = types;
  }

  public String getActivity() {
    return activity;
  }

  public StepTypes getTypes() {
    return types;
  }
}
