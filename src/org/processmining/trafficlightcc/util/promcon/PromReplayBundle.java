package org.processmining.trafficlightcc.util.promcon;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

public class PromReplayBundle {

  private final CostBasedCompleteParam replayParam;
  private final TransEvClassMapping mapping;
  private final Petrinet pn;

  public PromReplayBundle(CostBasedCompleteParam replayParam, TransEvClassMapping mapping, Petrinet pn) {
    this.replayParam = replayParam;
    this.mapping = mapping;
    this.pn = pn;
  }

  public CostBasedCompleteParam replayParam() {
    return replayParam;
  }

  public TransEvClassMapping mapping() {
    return mapping;
  }

  public Petrinet pn() {
    return pn;
  }
}
