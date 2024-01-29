package org.processmining.trafficlightcc.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.trafficlightcc.models.TLConfDiagnosticsBundle;
import org.processmining.trafficlightcc.parameters.TLConfParameters;

public class TLCCConnection extends AbstractConnection {
  
  /**
   * Label for log input of the connection
   */
  public final static String LOG = "Log";

  /**
   * Label for model input of the connection
   */
  public final static String MODEL = "Model";

  /**
   * Label for end of the connection (i.e., the diagnostics)
   */
  public final static String DIAGNOSTICS = "Diagnostics";
  
  /**
   * Algorithm parameter used to derive the diagnostics.
   */
  private final TLConfParameters tlConfParam;
  
  public TLCCConnection(Petrinet pn, XLog log, TLConfParameters tlConfParam, 
      TLConfDiagnosticsBundle tlcDiagBundle ) {
    super("Traffic light conformance checking diagnstostics for log and model");
    
    this.tlConfParam = tlConfParam;
    put(LOG, log);
    put(MODEL, pn);
    put(DIAGNOSTICS, tlcDiagBundle);
  }
  
  public boolean equalParam(TLConfParameters paramOther) {
    // Boundary case
    if (tlConfParam == null) {
      if (paramOther == null) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return this.tlConfParam.equals(paramOther);
    }
  }
}
