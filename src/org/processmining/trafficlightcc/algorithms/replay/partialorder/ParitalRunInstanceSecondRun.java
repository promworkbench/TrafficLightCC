package org.processmining.trafficlightcc.algorithms.replay.partialorder;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.trafficlightcc.models.POReplayDataWrapper;

public class ParitalRunInstanceSecondRun {

  private final Petrinet pn;
  private final POReplayDataWrapper poData; 
	private final PNManifestReplayerParameter parameters;

  public ParitalRunInstanceSecondRun(Petrinet pn, POReplayDataWrapper poData, 
      PNManifestReplayerParameter parameters) {
    this.pn = pn;
    this.poData = poData; 
    this.parameters = parameters;

  }

  public Petrinet pn() {
    return pn;
  }

  public POReplayDataWrapper poData() {
    return poData;
  }

  public PNManifestReplayerParameter parameters() {
    return parameters;
  }
}
