package org.processmining.trafficlightcc.plugins;

import java.util.Collection;
import java.util.Optional;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.trafficlightcc.algorithms.TrafficLightConfAlg;
import org.processmining.trafficlightcc.connections.TLCCConnection;
import org.processmining.trafficlightcc.dialogs.CostAdaptation;
import org.processmining.trafficlightcc.dialogs.TrafficLightConfParamUI;
import org.processmining.trafficlightcc.models.TLConfDiagnosticsBundle;
import org.processmining.trafficlightcc.parameters.TLConfParameters;

@Plugin(name = "Traffic Light Conformance", 
	returnLabels = {"Petri net traffic light replay result"},
	returnTypes = {TLConfDiagnosticsBundle.class}, 
	parameterLabels = { "Log", "Petri net" },
	help = "None",
	categories = {PluginCategory.ConformanceChecking, PluginCategory.Analytics }, 
	keywords = { "Partial Order", "Conformance" }, userAccessible = true)
public class TrafficLightConformancePlugin {
	

	/**
	 * GUI variants
	 */
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Tobias", email = "brockhoff@pads.rwth-aachen", 
			pack = "TrafficLightConformance")
	@PluginVariant(variantLabel = "Using net, log, and standard parameter", requiredParameterLabels = { 0, 1 })
	public TLConfDiagnosticsBundle replayLogGUIStandard(final UIPluginContext context, XLog log, Petrinet pn) {

		//Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
		//		PNReplayAlgorithm.class);
		
		//for (Class<?> c : coverageEstimatorClasses) {
		//	System.out.println(c.getCanonicalName());
		//}
		
		// XXC: Copied from replayLogGUI and set self the partial log builder of the algorithm 
		TrafficLightConfParamUI partialnetReplayerUI = new TrafficLightConfParamUI();
		Object[] resultConfiguration = partialnetReplayerUI.getConfiguration(context, pn,
				log);
		if (resultConfiguration == null) {
			context.getFutureResult(0).cancel(true);
			return null;
		}

		// if all parameters are set, run the algorithm
		if (resultConfiguration[TrafficLightConfParamUI.MAPPING] != null) {
			CostBasedCompleteParam algParameters = (CostBasedCompleteParam) resultConfiguration[TrafficLightConfParamUI.PARAMETERS];
			TransEvClassMapping mapping = (TransEvClassMapping) resultConfiguration[TrafficLightConfParamUI.MAPPING];
			
			TLConfParameters tlConfParam = new TLConfParameters(algParameters, mapping, 
			    (CostAdaptation) resultConfiguration[TrafficLightConfParamUI.COSTADAPTATION]);
			
			//////////////////////////////
			// Check for existing connection
			//////////////////////////////
      try {
        Collection<TLCCConnection> connections = 
            context.getConnectionManager().getConnections(TLCCConnection.class, context, pn, log);
        for (TLCCConnection connection : connections) {
          if (connection.getObjectWithRole(TLCCConnection.MODEL).equals(pn) 
              && connection.getObjectWithRole(TLCCConnection.LOG).equals(log)
              && connection.equalParam(tlConfParam)) {
            return connection.getObjectWithRole(TLCCConnection.DIAGNOSTICS);
          }
        }
      } catch (ConnectionCannotBeObtained e) {
      }
      
			//////////////////////////////
			// Compute Diagnostics
			//////////////////////////////
			TrafficLightConfAlg tlConfAlg = new TrafficLightConfAlg();
			Optional<TLConfDiagnosticsBundle> tlConfRes = tlConfAlg.calculateTrafficLightConformance(
					Optional.of(context), pn, log, tlConfParam, true);
			
			// Add connection
			context.addConnection(new TLCCConnection(pn, log, tlConfParam, tlConfRes.orElse(null)));
			
			if (!tlConfRes.isPresent()) {
				return null;
			}
			else {
				return tlConfRes.get();
			}
			
		}
		return null;
	}

	
}
