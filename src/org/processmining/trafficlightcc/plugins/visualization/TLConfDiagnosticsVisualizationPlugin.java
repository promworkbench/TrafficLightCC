package org.processmining.trafficlightcc.plugins.visualization;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.trafficlightcc.models.TLConfDiagnosticsBundle;
import org.processmining.trafficlightcc.visualization.TLCDSupervisingPresenter;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;

@Visualizer
public class TLConfDiagnosticsVisualizationPlugin {

	@Visualizer
	@PluginVariant(requiredParameterLabels = { 0 })
	@Plugin(name = "Visualize Traff Light Conformance Diagnostics", 
		returnLabels = { "TL Visualization component" }, 
		returnTypes = { JComponent.class }, 
		parameterLabels = { "Traffic Light Diagnostics Result Bundle" }, userAccessible = true) 
	public JComponent visualize(PluginContext context, TLConfDiagnosticsBundle resBundle) {
		
		TLCDiagnosticsModel diagModel = new TLCDiagnosticsModel(resBundle.pn(), resBundle.tlcDiagnostics());
		TLCDSupervisingPresenter presenter = new TLCDSupervisingPresenter(diagModel);
		
		return presenter.getMainView();
	}
}
