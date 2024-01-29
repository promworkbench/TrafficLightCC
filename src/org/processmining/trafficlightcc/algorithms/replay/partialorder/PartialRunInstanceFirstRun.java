package org.processmining.trafficlightcc.algorithms.replay.partialorder;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.trafficlightcc.models.POReplayDataWrapper;

public class PartialRunInstanceFirstRun {

  private final Petrinet pn;
  private final POReplayDataWrapper poData;
  private final TransEvClassMapping t2evMapping;
  private final CostBasedCompleteParam parameters;

  public PartialRunInstanceFirstRun(Petrinet pn, POReplayDataWrapper poData, 
      TransEvClassMapping t2evMapping, CostBasedCompleteParam parameters) {
    this.pn = pn;
    this.poData = poData;
    this.t2evMapping = t2evMapping;
    this.parameters = parameters;
  }

  public Petrinet pn() {
    return pn;
  }

  public POReplayDataWrapper poData() {
    return poData;
  }

  public TransEvClassMapping t2evMapping() {
    return t2evMapping;
  }

  public CostBasedCompleteParam parameters() {
    return parameters;
  }
}
