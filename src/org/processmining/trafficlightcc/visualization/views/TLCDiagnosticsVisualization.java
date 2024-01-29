package org.processmining.trafficlightcc.visualization.views;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;

public class TLCDiagnosticsVisualization extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -584801313078512683L;


	
	
	/**
	 * Model providing the diagnostics data
	 */
	private final TLCDiagnosticsModel tlcDiagModel;

	
	/**
	 * Handle to the Petri net-based view
	 */
	private final TLCDiagnosticPNView pnView;
	
	/**
	 * Handle to the view containing the traffic lights that cannot be
	 * attached to the Petri net. 
	 * (Log moves on duplicate transitions or event classes that do not occur in the Petri net)
	 */
	private final TLCDiagnosticNonPNTLView nonPNView;
	
	/**
	 * Handle to the view that shows additional details for selected elements
	 */
	private final TLCDiagDetailsView detailsView;
	
	public TLCDiagnosticsVisualization(TLCDiagnosticsModel tlcDiagModel) {
		super();
		this.tlcDiagModel = tlcDiagModel;
		
		////////////////////
		// Init Important Sub components
		////////////////////
		this.pnView = new TLCDiagnosticPNView(tlcDiagModel);
		this.nonPNView = new TLCDiagnosticNonPNTLView(tlcDiagModel);
		this.detailsView = new TLCDiagDetailsView(tlcDiagModel);
		
		this.pnView.setBackground(Color.white);
		//this.nonPNView.setBackground(Color.green);
		//this.detailsView.setBackground(Color.red);
		
		//////////////////////////////
		// Layouting
		// - LayoutManager
		// - Arrangement in Split Panes
		//////////////////////////////
		this.setLayout(new BorderLayout());

		// Top - bottom: PN + remainder : Details
		ProMSplitPane splitPaneDiagDetails = new ProMSplitPane(ProMSplitPane.VERTICAL_SPLIT);
		// Left - right:  PN : Details
		ProMSplitPane splitPaneTL = new ProMSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
		
		splitPaneTL.setBackground(Color.blue);
		
		// Add Top components
		splitPaneTL.setLeftComponent(pnView);
		splitPaneTL.setRightComponent(nonPNView);
		
		// Add Traffic lights | details
		splitPaneDiagDetails.setTopComponent(splitPaneTL);
		splitPaneDiagDetails.setBottomComponent(detailsView);


		this.add(splitPaneDiagDetails, BorderLayout.CENTER);
	}
	
	public TLCDiagnosticPNView getPNView() {
		return pnView;
	}

	public TLCDiagnosticNonPNTLView getNonPNView() {
		return nonPNView;
	}

	public TLCDiagDetailsView getDetailsView() {
		return detailsView;
	}
	
}
