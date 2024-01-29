package org.processmining.trafficlightcc.parameters;

import java.util.Arrays;

import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.trafficlightcc.dialogs.CostAdaptation;

public class TLConfParameters {

  /**
   * Costs used in Petri net replay
   */
  private final CostBasedCompleteParam confAlgParameters;

  /**
   * Mapping betweeen transitions (model) and event classes (i.e., activitiy names
   * derived from events)
   */
  private final TransEvClassMapping mapping;

  /**
   * Nudge the costs a bit (based on frequency) to make the algorithm "more"
   * deterministic
   */
  private final CostAdaptation costAdaptation;

  public TLConfParameters(CostBasedCompleteParam confAlgParameters, TransEvClassMapping mapping,
      CostAdaptation costAdaptation) {
    super();
    this.confAlgParameters = confAlgParameters;
    this.mapping = mapping;
    this.costAdaptation = costAdaptation;
  }

  @Override
  public boolean equals(Object obj) {
    // Precisely the same object
    if (super.equals(obj)) {
      return true;
    }
    else {
      if (!(obj instanceof TLConfParameters)) {
        return false;
      }
      TLConfParameters other = (TLConfParameters)obj;
      // Compare logs briefly
      // We assume both logs to be the same iff
      // same name and same number of traces
      if (checkReplayCostEquality(this.confAlgParameters, other.confAlgParameters)&& // replay parameters correspond
          this.mapping.equals(other.mapping) &&  // Same event class mapping
          this.costAdaptation == other.costAdaptation) { // Same cost adaptation strategy
        return true;
      }
      else {
        return false;
      }
    }
  }
  
  private boolean checkReplayCostEquality(CostBasedCompleteParam param1, CostBasedCompleteParam param2) {
    return Arrays.equals(param1.getFinalMarkings(), param2.getFinalMarkings()) 
        && param1.getInitialMarking().equals(param2.getInitialMarking())
        && param1.getMapEvClass2Cost().equals(param2.getMapEvClass2Cost())
        && param1.getMapSync2Cost().equals(param2.getMapSync2Cost())
        && param1.getMapTrans2Cost().equals(param2.getMapTrans2Cost())
        && param1.getAsynchronousMoveSort().equals(param2.getAsynchronousMoveSort())
        && param1.isGUIMode() == param2.isGUIMode()
        && param1.isCreatingConn() == param2.isCreatingConn()
        && param1.isPartiallyOrderedEvents() == param2.isPartiallyOrderedEvents()
        && Double.compare(param1.getEpsilon(), param2.getEpsilon()) == 0
        && Double.compare(param1.getExpectedAlignmentOverrun(), param2.getExpectedAlignmentOverrun()) == 0
        && param1.getNumThreads() == param2.getNumThreads()
        && Integer.compare(param1.getMaxNumOfStates(), param2.getMaxNumOfStates()) == 0
        && param1.getType().equals(param2.getType());
  }

  public CostBasedCompleteParam getConfAlgParameters() {
    return confAlgParameters;
  }

  public TransEvClassMapping getMapping() {
    return mapping;
  }

  public CostAdaptation getCostAdaptation() {
    return costAdaptation;
  }

}
