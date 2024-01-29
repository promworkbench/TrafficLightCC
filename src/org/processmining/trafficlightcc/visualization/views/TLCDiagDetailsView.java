package org.processmining.trafficlightcc.visualization.views;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.trafficlightcc.visualization.model.TLCDiagnosticsModel;

public class TLCDiagDetailsView extends JPanel  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -394555990086308522L;

	/**
	 * Model providing the diagnostics data
	 */
	private final TLCDiagnosticsModel tlcDiagModel;
	
	private final ProMTable tableDetail;
	
	private final TLCDOrderTimeView orderTimeView;
	
	public TLCDiagDetailsView(TLCDiagnosticsModel tlcDiagModel) {
		super();
		this.tlcDiagModel = tlcDiagModel;
		this.tableDetail = createDetailTable();
		this.orderTimeView = new TLCDOrderTimeView(tlcDiagModel);

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(tableDetail);
		this.add(this.orderTimeView);
	}

	private ProMTable createDetailTable() {
		ProMTable table = new ProMTable(tlcDiagModel.getDiagDetailTableModel());
		table.setPreferredSize(new Dimension(400, 70));
		return table;
	}
}
